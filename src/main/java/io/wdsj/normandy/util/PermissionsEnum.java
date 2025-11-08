package io.wdsj.normandy.util;

/**
 * Permission enums
 */
public enum PermissionsEnum {
    COMMAND_GENTOKEN("command.gentoken");

    private final String permission;

    PermissionsEnum(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return PREFIX + permission;
    }

    private static final String PREFIX = "normandylogin.";
}