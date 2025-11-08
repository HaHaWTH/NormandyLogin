package io.wdsj.normandy.command;

import io.wdsj.normandy.NormandyLogin;
import io.wdsj.normandy.util.ComponentUtils;
import io.wdsj.normandy.util.PermissionsEnum;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class GenerateTokenCommand implements CommandExecutor {
    private final NormandyLogin plugin;
    public GenerateTokenCommand(NormandyLogin plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission(PermissionsEnum.COMMAND_GENTOKEN.getPermission())) {
            ComponentUtils.sendMessage(sender, NormandyLogin.config().message_no_permission);
            return true;
        }
        if (!(sender instanceof Player player)) {
            ComponentUtils.sendMessage(sender, NormandyLogin.config().message_must_be_player);
            return true;
        }
        if (args.length != 0) {
            ComponentUtils.sendMessage(sender, NormandyLogin.config().message_invalid_arguments);
            return true;
        }
        UUID uuid = player.getUniqueId();
        if (!NormandyLogin.getHook().isPlayerLogin(uuid)) {
            ComponentUtils.sendMessage(sender, NormandyLogin.config().message_not_login_yet);
            return true;
        }
        CompletableFuture.supplyAsync(() -> {
            var keyData = plugin.getKeyStorage().getKeyData(uuid);
            if (keyData != null) {
                return keyData.timestamp();
            }
            return 0L;
        }, NormandyLogin.EXECUTOR_POOL).thenAccept(timestamp -> {
            if (System.currentTimeMillis() - timestamp < 1000L * NormandyLogin.config().generation_rate_limit) {
                ComponentUtils.sendMessage(sender, NormandyLogin.config().message_generation_rate_limit);
                return;
            }
            plugin.getPacketSender().sendKeyGenerationRequest(player);
            ComponentUtils.sendMessage(sender, NormandyLogin.config().message_key_generation_sent);
        });
        return true;
    }
}
