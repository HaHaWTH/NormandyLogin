package io.wdsj.normandy;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.wdsj.normandy.command.GenerateTokenCommand;
import io.wdsj.normandy.config.Config;
import io.wdsj.normandy.hook.AbstractHook;
import io.wdsj.normandy.hook.NoOpHook;
import io.wdsj.normandy.hook.PluginEnum;
import io.wdsj.normandy.hook.authme.AuthMeHook;
import io.wdsj.normandy.listener.QuitListener;
import io.wdsj.normandy.network.Messaging;
import io.wdsj.normandy.network.PacketChannels;
import io.wdsj.normandy.network.PacketSender;
import io.wdsj.normandy.util.ComponentUtils;
import io.wdsj.normandy.util.KeyStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class NormandyLogin extends JavaPlugin {
    private PacketSender packetSender;
    private KeyStorage keyStorage;
    private final Map<UUID, String> pendingChallenges = new ConcurrentHashMap<>();
    private static Logger logger;
    private static NormandyLogin instance;
    public static final ExecutorService EXECUTOR_POOL = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder()
                    .setNameFormat("Normandy Login - %d")
                    .setDaemon(true)
                    .build()
    );
    private static AbstractHook hook;
    private static Config config;
    @Override
    public void onLoad() {
        logger = getSLF4JLogger();
        instance = this;
        reloadConfiguration();
    }
    @Override
    public void onEnable() {
        packetSender = new PacketSender(this);
        keyStorage = new KeyStorage(this);
        Messenger messenger = getServer().getMessenger();
        messenger.registerIncomingPluginChannel(this, PacketChannels.C2S.HANDSHAKE, new Messaging(this));
        messenger.registerIncomingPluginChannel(this, PacketChannels.C2S.CHALLENGE_RESPONSE, new Messaging(this));
        messenger.registerIncomingPluginChannel(this, PacketChannels.C2S.PUBLIC_KEY_SHARE, new Messaging(this));

        messenger.registerOutgoingPluginChannel(this, PacketChannels.S2C.LOGIN_CHALLENGE);
        messenger.registerOutgoingPluginChannel(this, PacketChannels.S2C.KEY_GEN_REQUEST);
        messenger.registerOutgoingPluginChannel(this, PacketChannels.S2C.HANDSHAKE_ACK);
        getServer().getPluginManager().registerEvents(new QuitListener(), this);
        Objects.requireNonNull(getCommand("generatetoken")).setExecutor(new GenerateTokenCommand(this));
        hookPlugins();
    }

    @Override
    public void onDisable() {
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        HandlerList.unregisterAll(this);
        EXECUTOR_POOL.shutdown();
        for (var pendingChallenge : pendingChallenges.entrySet()) {
            Player player = Bukkit.getPlayer(pendingChallenge.getKey());
            if (player != null) {
                player.getScheduler().execute(this, () -> {
                    ComponentUtils.kick(player, config().message_plugin_disabled);
                }, null, 1L);
            }
        }
    }

    public PacketSender getPacketSender() {
        return packetSender;
    }

    public KeyStorage getKeyStorage() {
        return keyStorage;
    }
    public Map<UUID, String> getPendingChallenges() {
        return pendingChallenges;
    }
    public static Logger logger() {
        return logger;
    }
    public static NormandyLogin getInstance() {
        return instance;
    }
    private void reloadConfiguration() {
        try {
            File dataFolder = getDataFolder();
            createDirectory(dataFolder);
            config = new Config(this, dataFolder);
            config.saveConfig();
        } catch (Throwable t) {
            logger.error("Error occurred while loading config!", t);
        }
    }

    public void createDirectory(File dir) throws IOException {
        try {
            Files.createDirectories(dir.toPath());
        } catch (FileAlreadyExistsException e) { // Thrown if dir exists but is not a directory
            if (dir.delete()) createDirectory(dir);
        }
    }

    public static Config config() {
        return config;
    }

    private void hookPlugins() {
        boolean isHooked = false;
        switch (config().hooked_plugin) {
            case AUTHME -> {
                if (PluginEnum.AUTHME.isEnabled()) {
                    hook = new AuthMeHook();
                    isHooked = true;
                } else {
                    hook = new NoOpHook();
                }
            }
            case AUTO -> {
                for (PluginEnum pluginEnum : PluginEnum.values()) {
                    if (pluginEnum.isEnabled()) {
                        // noinspection SwitchStatementWithTooFewBranches
                        switch (pluginEnum) {
                            case AUTHME -> {
                                hook = new AuthMeHook();
                                isHooked = true;
                            }
                        }
                        break;
                    }
                }
            }
        }
        if (!isHooked) logger.warn("Cannot find plugin {}, disabling hook.", config().hooked_plugin.toString());
        else logger.info("Hooked into {}", config().hooked_plugin.toString());
    }

    public static AbstractHook getHook() {
        return hook;
    }
}
