package io.wdsj.normandy.listener;

import io.wdsj.normandy.NormandyLogin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener implements Listener {
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        NormandyLogin.getInstance().getPendingChallenges().remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onKick(PlayerKickEvent event) {
        NormandyLogin.getInstance().getPendingChallenges().remove(event.getPlayer().getUniqueId());
    }
}
