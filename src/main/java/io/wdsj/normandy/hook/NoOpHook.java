package io.wdsj.normandy.hook;

import java.util.UUID;

public class NoOpHook extends AbstractHook {
    @Override
    public boolean isPlayerLogin(UUID uuid) {
        return false;
    }

    @Override
    public void loginPlayer(UUID uuid) {
    }
}
