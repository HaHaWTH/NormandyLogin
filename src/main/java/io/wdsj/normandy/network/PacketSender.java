package io.wdsj.normandy.network;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.wdsj.normandy.NormandyLogin;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public class PacketSender {
    private final JavaPlugin plugin;

    public PacketSender(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void sendLoginChallenge(Player player, String challenge) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {
            Messaging.writeMinecraftString(dos, challenge);
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeBoolean(NormandyLogin.config().use_uuid_as_identifier);
            UUID uuid = NormandyLogin.config().server_identifier;
            out.writeLong(uuid.getLeastSignificantBits());
            out.writeLong(uuid.getMostSignificantBits());
            out.write(baos.toByteArray());
            player.sendPluginMessage(plugin, PacketChannels.S2C.LOGIN_CHALLENGE, out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendKeyGenerationRequest(Player player) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeBoolean(NormandyLogin.config().use_uuid_as_identifier);
        UUID uuid = NormandyLogin.config().server_identifier;
        out.writeLong(uuid.getLeastSignificantBits());
        out.writeLong(uuid.getMostSignificantBits());
        player.sendPluginMessage(plugin, PacketChannels.S2C.KEY_GEN_REQUEST, out.toByteArray());
    }

    public void sendHandshakeAck(Player player) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeInt(PacketChannels.PROTOCOL_VERSION);
        player.sendPluginMessage(plugin, PacketChannels.S2C.HANDSHAKE_ACK, out.toByteArray());
    }
}