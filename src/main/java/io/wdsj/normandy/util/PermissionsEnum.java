package io.wdsj.normandy.util;

/**
 * Permission enums
 */
public enum PermissionsEnum {
    COMMAND_GENTOKEN("command.gentoken"),
    COMMAND_REVOKETOKEN("command.revoketoken.self"),
    COMMAND_REVOKETOKEN_OTHERS("command.revoketoken.others");

    private final String permission;

    PermissionsEnum(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return PREFIX + permission;
    }

    private static final String PREFIX = "normandylogin.";
}