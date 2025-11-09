package io.wdsj.normandy.command;

import io.wdsj.normandy.NormandyLogin;
import io.wdsj.normandy.util.ComponentUtils;
import io.wdsj.normandy.util.PermissionsEnum;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RevokeTokenCommand implements CommandExecutor {
    private final NormandyLogin plugin;
    public RevokeTokenCommand(NormandyLogin plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof Player player && !NormandyLogin.getHook().isPlayerLogin(player.getUniqueId())) {
            ComponentUtils.sendMessage(sender, NormandyLogin.config().message_not_login_yet);
            return true;
        }
        UUID uuid;
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                ComponentUtils.sendMessage(sender, NormandyLogin.config().message_must_be_player);
                return true;
            }
            if (!sender.hasPermission(PermissionsEnum.COMMAND_REVOKETOKEN.getPermission())) {
                ComponentUtils.sendMessage(sender, NormandyLogin.config().message_no_permission);
                return true;
            }
            uuid = player.getUniqueId();
        } else {
            if (!sender.hasPermission(PermissionsEnum.COMMAND_REVOKETOKEN_OTHERS.getPermission())) {
                ComponentUtils.sendMessage(sender, NormandyLogin.config().message_no_permission);
                return true;
            }
            String playerName = args[0];
            UUID playerUuid = Bukkit.getPlayerUniqueId(playerName);
            if (playerUuid == null) {
                ComponentUtils.sendMessage(sender, NormandyLogin.config().message_player_not_found);
                return true;
            }
            uuid = playerUuid;
        }
        CompletableFuture.runAsync(() -> {
            if (!plugin.getKeyStorage().deleteKey(uuid)) {
                ComponentUtils.sendMessage(sender, NormandyLogin.config().message_player_not_found);
            } else {
                ComponentUtils.sendMessage(sender, NormandyLogin.config().message_revoke_success);
            }
        }, NormandyLogin.EXECUTOR_POOL);
        return true;
    }
}