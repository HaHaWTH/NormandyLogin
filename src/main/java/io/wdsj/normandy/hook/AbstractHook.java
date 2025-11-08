package io.wdsj.normandy.hook;

import java.util.UUID;

public abstract class AbstractHook {
    public abstract boolean isPlayerLogin(UUID uuid);
    public abstract void loginPlayer(UUID uuid);
}
