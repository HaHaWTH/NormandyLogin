package io.wdsj.normandy.hook.authme;

import fr.xephi.authme.api.v3.AuthMeApi;
import io.wdsj.normandy.hook.AbstractHook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class AuthMeHook extends AbstractHook {
    @Override
    public boolean isPlayerLogin(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return false;
        return AuthMeApi.getInstance().isAuthenticated(player);
    }

    @Override
    public void loginPlayer(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;
        AuthMeApi.getInstance().forceLogin(player);
    }
}
