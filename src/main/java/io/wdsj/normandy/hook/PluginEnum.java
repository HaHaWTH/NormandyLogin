package io.wdsj.normandy.hook;

import io.wdsj.normandy.NormandyLogin;

public enum PluginEnum {
    AUTHME("AuthMe"),
    AUTO("");
    private final String name;
    PluginEnum(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public boolean isEnabled() {
        return NormandyLogin.getInstance().getServer().getPluginManager().isPluginEnabled(name);
    }
}
