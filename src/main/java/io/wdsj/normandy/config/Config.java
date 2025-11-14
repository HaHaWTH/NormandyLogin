package io.wdsj.normandy.config;

import io.github.thatsmusic99.configurationmaster.api.ConfigFile;
import io.github.thatsmusic99.configurationmaster.api.ConfigSection;
import io.wdsj.normandy.NormandyLogin;
import io.wdsj.normandy.hook.PluginEnum;
import io.wdsj.normandy.network.PacketChannels;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class Config {
    private final ConfigFile config;
    private final JavaPlugin plugin;
    /* Updater config */
    public final boolean check_for_update;

    /* Plugin config */
    public final boolean use_uuid_as_identifier;
    public final UUID server_identifier;
    public final int generation_rate_limit;
    public final int token_expire_hours;
    public final PluginEnum hooked_plugin;

    /* Message config */
    public final String message_prefix;
    public final String message_invalid_session, message_invalid_signature, message_authentication_success,
    message_error_occurred, message_key_saving_success, message_challenge_timed_out, message_plugin_disabled,
    message_no_permission, message_must_be_player, message_key_generation_sent, message_invalid_arguments,
    message_generation_rate_limit, message_not_login_yet, message_player_not_found, message_revoke_success, message_token_expired;

    public Config(JavaPlugin plugin, File dataFolder) throws Exception {
        this.plugin = plugin;
        // Load config.yml with ConfigMaster
        this.config = ConfigFile.loadConfig(new File(dataFolder, "config.yml"));
        config.set("plugin-version", PacketChannels.PROTOCOL_VERSION);

        // Pre-structure to force order
        structureConfig();

        this.check_for_update = getBoolean("plugin.check-update", true, """
                If set to true, will check for update on plugin startup.""");
        this.use_uuid_as_identifier = getBoolean("plugin.use-uuid-as-identifier", true, """
                If set to true, will use the generated UUID as the server identifier.
                If set to false, Normandy Login will use the server address as the server identifier.
                Using UUID can sync tokens when players connect with another hostname, which is recommended.
                """);
        this.server_identifier = UUID.fromString(getString("plugin.server-identifier", UUID.randomUUID().toString(), """
                The UUID generated used to identify the server.
                """));
        this.generation_rate_limit = getInt("plugin.generation-rate-limit", 30, "How frequent a player can attempt to generate a token in seconds.");
        this.hooked_plugin = PluginEnum.valueOf(getString("plugin.hooked-plugins", PluginEnum.AUTHME.getName(), """
                A list of plugins to hook into.
                Available options:
                AuthMe
                Auto
                """).toUpperCase(Locale.ROOT));
        this.token_expire_hours = getInt("plugin.token-expire-hours", 168, "When will the client's token expire? Leave as -1 to disable expiration (not recommended)");


        this.message_prefix = getString("messages.prefix", "<gold>[Normandy Login] ");
        this.message_invalid_session = this.message_prefix + getString("messages.invalid-session", "<red>Invalid session. Please try again.");
        this.message_invalid_signature = this.message_prefix + getString("messages.invalid-signature", "<red>Authentication failed, invalid signature.");
        this.message_authentication_success = this.message_prefix + getString("messages.authentication-success", "<green>Authentication successful. Welcome back.");
        this.message_error_occurred = this.message_prefix + getString("messages.error-occurred", "<red>An error occurred. Please contact the server administrator.");
        this.message_key_saving_success = this.message_prefix + getString("messages.key-saving-success", "<green>Your public key has been saved. Welcome back.");
        this.message_challenge_timed_out = this.message_prefix + getString("messages.challenge-timed-out", "<red>Challenge request timed out.");
        this.message_plugin_disabled = this.message_prefix + getString("messages.plugin-disabled", "<red>Normandy Login is disabled, please rejoin.");
        this.message_no_permission = this.message_prefix + getString("messages.no-permission", "<red>You do not have permission to use this command.");
        this.message_must_be_player = this.message_prefix + getString("messages.must-be-player", "<red>You must be a player to use this command.");
        this.message_key_generation_sent = this.message_prefix + getString("messages.key-generation-sent", "<aqua>Sending a key generation request to your client.");
        this.message_invalid_arguments = this.message_prefix + getString("messages.invalid-arguments", "<red>Invalid arguments.");
        this.message_generation_rate_limit = this.message_prefix + getString("messages.generation-rate-limit", "<red>You are generating tokens too quickly. Please wait.");
        this.message_not_login_yet = this.message_prefix + getString("messages.not-login-yet", "<red>You must login to perform this command.");
        this.message_player_not_found = this.message_prefix + getString("messages.player-not-found", "<red>Player not found.");
        this.message_revoke_success = this.message_prefix + getString("messages.revoke-success", "<green>Successfully revoked token.");
        this.message_token_expired = this.message_prefix + getString("messages.token-expired", "<red>Your token has expired. Please login again.");
    }

    public void saveConfig() {
        try {
            config.save();
        } catch (Exception e) {
            NormandyLogin.logger().error("Failed to save config file", e);
        }
    }

    private void structureConfig() {
        createTitledSection("Plugin general setting", "plugin");
        createTitledSection("Message setting (MiniMessage supported)", "messages");
    }

    public void createTitledSection(String title, String path) {
        config.addSection(title);
        config.addDefault(path, null);
    }

    public boolean getBoolean(String path, boolean def, String comment) {
        config.addDefault(path, def, comment);
        return config.getBoolean(path, def);
    }

    public boolean getBoolean(String path, boolean def) {
        config.addDefault(path, def);
        return config.getBoolean(path, def);
    }

    public String getString(String path, String def, String comment) {
        config.addDefault(path, def, comment);
        return config.getString(path, def);
    }

    public String getString(String path, String def) {
        config.addDefault(path, def);
        return config.getString(path, def);
    }

    public double getDouble(String path, double def, String comment) {
        config.addDefault(path, def, comment);
        return config.getDouble(path, def);
    }

    public double getDouble(String path, double def) {
        config.addDefault(path, def);
        return config.getDouble(path, def);
    }

    public int getInt(String path, int def, String comment) {
        config.addDefault(path, def, comment);
        return config.getInteger(path, def);
    }

    public int getInt(String path, int def) {
        config.addDefault(path, def);
        return config.getInteger(path, def);
    }

    public long getLong(String path, long def, String comment) {
        config.addDefault(path, def, comment);
        return config.getLong(path, def);
    }

    public long getLong(String path, long def) {
        config.addDefault(path, def);
        return config.getLong(path, def);
    }

    public List<String> getList(String path, List<String> def, String comment) {
        config.addDefault(path, def, comment);
        return config.getStringList(path);
    }

    public List<String> getList(String path, List<String> def) {
        config.addDefault(path, def);
        return config.getStringList(path);
    }

    public ConfigSection getConfigSection(String path, Map<String, Object> defaultKeyValue) {
        config.addDefault(path, null);
        config.makeSectionLenient(path);
        defaultKeyValue.forEach((string, object) -> config.addExample(path+"."+string, object));
        return config.getConfigSection(path);
    }

    public ConfigSection getConfigSection(String path, Map<String, Object> defaultKeyValue, String comment) {
        config.addDefault(path, null, comment);
        config.makeSectionLenient(path);
        defaultKeyValue.forEach((string, object) -> config.addExample(path+"."+string, object));
        return config.getConfigSection(path);
    }

    public void addComment(String path, String comment) {
        config.addComment(path, comment);
    }
}
