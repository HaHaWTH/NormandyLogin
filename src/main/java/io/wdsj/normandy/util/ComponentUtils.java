package io.wdsj.normandy.util;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ComponentUtils {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final char AMPERSAND_CHAR = '&';
    public static Component parseMiniMessage(String message) {
        return miniMessage.deserialize(convertLegacyToMiniMessage(message, false, AMPERSAND_CHAR, true));
    }
    public static void sendMessage(Audience player, String message) {
        if (message.isBlank()) return;
        Component messageComponent = parseMiniMessage(message);
        player.sendMessage(messageComponent);
    }
    public static void kick(Player player, String message) {
        Component messageComponent = parseMiniMessage(message);
        player.kick(messageComponent);
    }
    private static String convertLegacyToMiniMessage(String legacy, boolean concise, char charCode, boolean rgb) {
        String miniMessage = legacy.replaceAll(Pattern.quote(String.valueOf(charCode)) + "0", "<black>")
                .replaceAll(Pattern.quote(String.valueOf(charCode)) + "1", "<dark_blue>")
                .replaceAll(Pattern.quote(String.valueOf(charCode)) + "2", "<dark_green>")
                .replaceAll(Pattern.quote(String.valueOf(charCode)) + "3", "<dark_aqua>")
                .replaceAll(Pattern.quote(String.valueOf(charCode)) + "4", "<dark_red>")
                .replaceAll(Pattern.quote(String.valueOf(charCode)) + "5", "<dark_purple>")
                .replaceAll(Pattern.quote(String.valueOf(charCode)) + "6", "<gold>")
                .replaceAll(Pattern.quote(String.valueOf(charCode)) + "7", "<gray>")
                .replaceAll(Pattern.quote(String.valueOf(charCode)) + "8", "<dark_gray>")
                .replaceAll(Pattern.quote(String.valueOf(charCode)) + "9", "<blue>")
                .replaceAll(Pattern.quote(String.valueOf(charCode)) + "a", "<green>")
                .replaceAll(Pattern.quote(String.valueOf(charCode)) + "b", "<aqua>")
                .replaceAll(Pattern.quote(String.valueOf(charCode)) + "c", "<red>")
                .replaceAll(Pattern.quote(String.valueOf(charCode)) + "d", "<light_purple>")
                .replaceAll(Pattern.quote(String.valueOf(charCode)) + "e", "<yellow>")
                .replaceAll(Pattern.quote(String.valueOf(charCode)) + "f", "<white>");

        if (concise) {
            miniMessage = miniMessage.replaceAll(Pattern.quote(String.valueOf(charCode)) + "n", "<u>")
                    .replaceAll(Pattern.quote(String.valueOf(charCode)) + "m", "<st>")
                    .replaceAll(Pattern.quote(String.valueOf(charCode)) + "k", "<obf>")
                    .replaceAll(Pattern.quote(String.valueOf(charCode)) + "o", "<i>")
                    .replaceAll(Pattern.quote(String.valueOf(charCode)) + "l", "<b>")
                    .replaceAll(Pattern.quote(String.valueOf(charCode)) + "r", "<r>");
        } else {
            miniMessage = miniMessage.replaceAll(Pattern.quote(String.valueOf(charCode)) + "n", "<underlined>")
                    .replaceAll(Pattern.quote(String.valueOf(charCode)) + "m", "<strikethrough>")
                    .replaceAll(Pattern.quote(String.valueOf(charCode)) + "k", "<obfuscated>")
                    .replaceAll(Pattern.quote(String.valueOf(charCode)) + "o", "<italic>")
                    .replaceAll(Pattern.quote(String.valueOf(charCode)) + "l", "<bold>")
                    .replaceAll(Pattern.quote(String.valueOf(charCode)) + "r", "<reset>");
        }

        if (rgb) {
            Pattern pattern = Pattern.compile(Pattern.quote(String.valueOf(charCode)) + "#([0-9a-fA-F]{6})");
            Matcher matcher = pattern.matcher(miniMessage);
            miniMessage = matcher.replaceAll("<#$1>");
        }

        return miniMessage;
    }
}