package io.wdsj.normandy.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.wdsj.normandy.NormandyLogin;
import io.wdsj.normandy.core.ServerKeyData;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class KeyStorage {
    private final JavaPlugin plugin;
    private final File storageDir;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Cache<UUID, ServerKeyData> cache = CacheBuilder.newBuilder()
            .maximumSize(1024L)
            .expireAfterAccess(5, TimeUnit.SECONDS)
            .build();

    public KeyStorage(JavaPlugin plugin) {
        this.plugin = plugin;
        this.storageDir = new File(plugin.getDataFolder(), "keys");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
    }

    public synchronized boolean saveKey(UUID playerUuid, String publicKey, String playerName) {
        cache.invalidate(playerUuid);
        ServerKeyData data = new ServerKeyData(publicKey, System.currentTimeMillis(), playerName);
        File playerFile = new File(storageDir, playerUuid + ".json");

        try (FileWriter writer = new FileWriter(playerFile)) {
            gson.toJson(data, writer);
            return true;
        } catch (IOException e) {
            NormandyLogin.logger().error("Error saving key for {}.", playerUuid, e);
        }
        return false;
    }

    public CompletableFuture<Boolean> saveKeyAsync(UUID playerUuid, String publicKey, String playerName) {
        return CompletableFuture.supplyAsync(() -> saveKey(playerUuid, publicKey, playerName), NormandyLogin.EXECUTOR_POOL);
    }

    public synchronized ServerKeyData getKeyData(UUID playerUuid) {
        var cachedData = cache.getIfPresent(playerUuid);
        if (cachedData != null) {
            return cachedData;
        }
        var data = getKeyDataFromFile(playerUuid);
        if (data == null) return null;
        cache.put(playerUuid, data);
        return data;
    }

    public CompletableFuture<ServerKeyData> getKeyDataAsync(UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> getKeyData(playerUuid), NormandyLogin.EXECUTOR_POOL);
    }

    public synchronized boolean deleteKey(UUID playerUuid) {
        cache.invalidate(playerUuid);
        File playerFile = new File(storageDir, playerUuid + ".json");
        if (!playerFile.exists()) {
            return false;
        }
        return playerFile.delete();
    }

    public CompletableFuture<Boolean> deleteKeyAsync(UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> deleteKey(playerUuid), NormandyLogin.EXECUTOR_POOL);
    }

    private ServerKeyData getKeyDataFromFile(UUID playerUuid) {
        File playerFile = new File(storageDir, playerUuid.toString() + ".json");
        if (!playerFile.exists()) {
            return null;
        }

        try (FileReader reader = new FileReader(playerFile)) {
            return gson.fromJson(reader, ServerKeyData.class);
        } catch (Exception e) {
            NormandyLogin.logger().error("Error loading key data for {}.", playerUuid, e);
            return null;
        }
    }
}