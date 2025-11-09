package io.wdsj.normandy.network;

import com.google.common.base.Charsets;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import io.wdsj.normandy.NormandyLogin;
import io.wdsj.normandy.core.ServerKeyData;
import io.wdsj.normandy.util.ComponentUtils;
import io.wdsj.normandy.util.CryptoUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Messaging implements PluginMessageListener {

    private final NormandyLogin plugin;

    public Messaging(NormandyLogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] message) {
        if (!channel.startsWith("normandylogin:")) {
            return;
        }

        try (ByteArrayInputStream bis = new ByteArrayInputStream(message);
             DataInputStream dis = new DataInputStream(bis)) {

            switch (channel) {
                case PacketChannels.C2S.HANDSHAKE:
                    ByteArrayDataInput in = ByteStreams.newDataInput(message);
                    int protocol = in.readInt();
                    if (protocol != PacketChannels.PROTOCOL_VERSION) {
                        NormandyLogin.logger().info("Player {} tried to connect with a different protocol version ({}).", player.getName(), protocol);
                    }
                    handleHandshake(player);
                    break;

                case PacketChannels.C2S.CHALLENGE_RESPONSE:
                    if (message.length == 0) return;
                    String signature = readMinecraftString(dis);
                    handleChallengeResponse(player, signature);
                    break;

                case PacketChannels.C2S.PUBLIC_KEY_SHARE:
                    if (message.length == 0) return;
                    String publicKey = readMinecraftString(dis);
                    handlePublicKeyShare(player, publicKey);
                    break;
            }
        } catch (IOException e) {
            NormandyLogin.logger().error("Error while parsing plugin message", e);
        }
    }

    /**
     * Reads a String from a DataInputStream, formatted in the Minecraft VarInt-prefixed way.
     *
     * @param in The DataInputStream to read from.
     * @return The read String.
     * @throws IOException if an I/O error occurs.
     */
    public static String readMinecraftString(DataInputStream in) throws IOException {
        int length = readVarInt(in);
        if (length < 0) {
            throw new IOException("The received encoded string buffer length is less than zero! Weird string!");
        }
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        return new String(bytes, Charsets.UTF_8);
    }

    /**
     * Reads a VarInt from a DataInputStream.
     *
     * @param in The DataInputStream to read from.
     * @return The read VarInt.
     * @throws IOException if an I/O error occurs.
     */
    public static int readVarInt(DataInputStream in) throws IOException {
        int i = 0;
        int j = 0;
        while (true) {
            int k = in.readByte();
            i |= (k & 0x7F) << j++ * 7;
            if (j > 5) {
                throw new RuntimeException("VarInt too big");
            }
            if ((k & 0x80) != 128) {
                break;
            }
        }
        return i;
    }

    public static void writeMinecraftString(DataOutputStream out, String s) throws IOException {
        byte[] bytes = s.getBytes(Charsets.UTF_8);
        writeVarInt(out, bytes.length);
        out.write(bytes);
    }

    public static void writeVarInt(DataOutputStream out, int value) throws IOException {
        while (true) {
            if ((value & ~0x7F) == 0) {
                out.writeByte(value);
                return;
            }
            out.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
    }

    private void handleHandshake(Player player) {
        if (plugin.getKeyStorage().getKeyData(player.getUniqueId()) != null) {
            CompletableFuture.supplyAsync(() -> CryptoUtils.generateChallenge(64), NormandyLogin.EXECUTOR_POOL).thenAccept(challenge -> {
                plugin.getPendingChallenges().put(player.getUniqueId(), challenge);
                player.getScheduler().runDelayed(plugin, task -> {
                    plugin.getPacketSender().sendHandshakeAck(player);
                    plugin.getPacketSender().sendLoginChallenge(player, challenge);
                    NormandyLogin.logger().info("Sent login challenge to {}", player.getName());
                }, null, 10L);

                plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, task -> {
                    if (plugin.getPendingChallenges().containsKey(player.getUniqueId())) {
                        player.getScheduler().execute(plugin, () -> {
                            ComponentUtils.sendMessage(player, NormandyLogin.config().message_challenge_timed_out);
                            plugin.getPendingChallenges().remove(player.getUniqueId());
                        }, null, 1L);
                    }
                }, 20 * 20L);
            });
        } else {
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, task -> plugin.getPacketSender().sendHandshakeAck(player), 5L);
        }
    }

    private void handleChallengeResponse(Player player, String signature) {
        UUID playerUuid = player.getUniqueId();
        String challenge = plugin.getPendingChallenges().remove(playerUuid);
        if (challenge == null) {
            ComponentUtils.kick(player, NormandyLogin.config().message_invalid_session);
            return;
        }

        ServerKeyData publicKeyStr = plugin.getKeyStorage().getKeyData(playerUuid);
        try {
            if (CryptoUtils.verify(challenge, signature, CryptoUtils.stringToPublicKey(publicKeyStr.publicKey()))) {
                ComponentUtils.sendMessage(player, NormandyLogin.config().message_authentication_success);
                NormandyLogin.logger().info("Player {} authenticated successfully via NormandyLogin.", player.getName());
                player.getScheduler().execute(plugin, () -> {
                    if (!NormandyLogin.getHook().isPlayerLogin(playerUuid)) {
                        NormandyLogin.getHook().loginPlayer(playerUuid);
                    }
                }, null, 5L);
            } else {
                player.getScheduler().execute(plugin, () -> {
                    ComponentUtils.kick(player, NormandyLogin.config().message_invalid_signature);
                }, null, 1L);
            }
        } catch (Exception e) {
            NormandyLogin.logger().error("Error during signature verification for {}.", player.getName(), e);
            player.getScheduler().execute(plugin, () -> {
                ComponentUtils.kick(player, NormandyLogin.config().message_error_occurred);
            }, null, 1L);
        }
    }

    private void handlePublicKeyShare(Player player, String publicKey) {
        CompletableFuture.runAsync(() -> {
            plugin.getKeyStorage().saveKey(player.getUniqueId(), publicKey, player.getName());
            ComponentUtils.sendMessage(player, NormandyLogin.config().message_key_saving_success);
            NormandyLogin.logger().info("Saved new public key for {}", player.getName());
        }, NormandyLogin.EXECUTOR_POOL);
    }
}
