package dev.tacker.hotpotato.utils;

import dev.tacker.hotpotato.HotPotato;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class Locale {

    private static final Map<MessageKey, String> messages = new HashMap<>();

    /**
     * sets up the messages
     */
    public static void setup() {
        HotPotato.getInstance().getLogging().debug("Setting up messages.yml!");
        File f = new File(HotPotato.getInstance().getDataFolder(), "messages.yml");
        if (!f.exists()) {
            HotPotato.getInstance().getLogging().debug("Created langfile messages.yml");
            HotPotato.getInstance().saveResource("messages.yml", false);
        }

        //fill map with defaults...
        for (MessageKey key : MessageKey.values()) {
            messages.put(key, key.getDefault());
        }

        //check again, if theres a file now
        File file = new File(HotPotato.getInstance().getDataFolder(), "messages.yml");
        if (!file.exists()) {
            HotPotato.getInstance().getLogging().error("File messages.yml is not existing Using defaults..");
            return;
        }

        FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        for (MessageKey key : MessageKey.values()) {
            String msg = configuration.getString(key.name());
            if (msg == null || msg.isEmpty()) {
                HotPotato.getInstance().getLogging().error("found nothing for key " + key.name() + ".. added default");
                configuration.set(key.name(), key.getDefault());
                try {
                    configuration.save(file);
                } catch (IOException e) {
                    HotPotato.getInstance().getLogging().error("Error on saving messages.yml: " + e.getMessage());
                }
                continue;
            }
            HotPotato.getInstance().getLogging().debug("added message " + key.name() + " with value " + msg);
            messages.put(key, msg);
        }
    }

    /**
     * gets a raw message without prefix and as string
     */
    public static String getRaw(MessageKey key, Object... args) {
        String message = messages.getOrDefault(key, key.getDefault());
        return MessageFormat.format(message, args);
    }

    /**
     * gets the message
     */
    public static Component get(MessageKey key, Object... args) {
        String message = messages.getOrDefault(key, key.getDefault());
        return Utils.mm(HotPotato.getInstance().getPrefix() + MessageFormat.format(message, args));
    }

    /**
     * gets the message without prefix
     */
    public static Component getNoPrefix(MessageKey key, Object... args) {
        String message = messages.getOrDefault(key, key.getDefault());
        return Utils.mm(MessageFormat.format(message, args));
    }

    /**
     * gets the message, with option to add command like "<click:suggest_command:/hotpotato version>"
     */
    public static Component getWithCommand(MessageKey key, String pre, Object... args) {
        String message = messages.getOrDefault(key, key.getDefault());
        return Utils.mm(HotPotato.getInstance().getPrefix() + pre + MessageFormat.format(message, args));
    }

    public enum MessageKey {
        ERROR_NO_PERM("<red>You dont have the permission to do this!"),
        ERROR_WRONG_COMMAND("<red>Wrong command! Try it with {0}!"),
        ERROR_NO_COMMAND("<red>There is no command /hotpotato {0}!"),
        ERROR_PLAYER_ONLY("<red>This is for player-only!"),
        ERROR_PLAYER_ONLY_OR_TRY("<red>This is for player-only! You can also try {0}"),
        ERROR_VALIDATION("<red>Error on validation: {0}"),
        ERROR_ERROR("<red>Error: {0}"),
        ERROR_NO_BOOL("<red>This is no bool: {0} (true/false)"),
        ERROR_NO_INT("<red>This is no integer: {0} (100)"),
        ERROR_NO_DOUBLE("<red>This is no integer: {0} (1.0)"),
        ERROR_WRONG_INPUT("<red>This is the wrong input: {0}"),

        ARENA_NOT_FOUND("<red>There is no arena {0}!"),
        ARENA_NONE_FOUND("<red>There are no arenas"),
        ARENA_SETTING_SUCCESS("<green>You successfully changed {0} to {1}"),
        ARENA_SIGN_ADD("<green>Added a sign for arena {0}"),
        ARENA_SIGN_REMOVE("<red>Removed a sign for arena {0}"),
        ARENA_DELETED("<red>Successfully removed arena {0}"),
        ARENA_CREATED("<green>Successfully created arena {0}"),
        ARENA_CREATED_2("<green>Click here to edit arena {0}"),
        ARENA_NAME_ALREADY_TAKEN("<red>The arena name {0} is already taken"),
        ARENA_ERROR_SAVING("<red>There was an error while saving arena {0}"),

        ARENA_MISSING("<red>There is no {0} set"),
        ARENA_TELEPORTED("<green>Teleported you to arena {0}"),
        ARENA_TELEPORTED_OTHER("<green>Teleported {1} to arena {0}"),
        ARENA_WON("<gold>Player {0} won the arena {1}"),
        ARENA_POTATOED("Player {0} had the potato for too long and died.."),
        ARENA_LEFT("Player {0} left your arena"),
        ARENA_JOINED("Player {0} joined your arena"),
        ARENA_NEW_POTATO("<gold>Searching for new potato..."),
        ARENA_WAITING("<white>Waiting for more player to start the game"),

        ARENA_STARTED("<green>The arena started! Good luck!"),
        ARENA_STOPPED("<red>The arena stopped!"),
        ARENA_POTATO("<red>Player {0} is the new potato"),
        ARENA_POTATO_INFO("<red><bold>You are the potato!"),
        ARENA_CONFIGURED("<green>Your arena is ready. Click here to activate it!"),
        ARENA_NOT_ACTIVE("<red>The arena {0} is not active"),
        ARENA_NOT_JOINABLE("<red>The arena {0} is not joinable"),
        ARENA_LIMIT_REACHED("<red>The playerlimit of arena {0} is reached"),
        ARENA_ALREADY_PLAYING("<red>You are already playing in arena {0}"),

        PLAYER_MISSING("<red>There is no player {0}"),
        PLAYER_NO_ARENA("<red>You are in no arena"),

        TITLE_COUNTDOWN("<white>Starting in..."),
        TITLE_COUNTDOWN_SUB("<red>{0}.."),
        TITLE_STARTED("<white>Good luck!"),
        TITLE_STARTED_SUB("<gold>{0} <red>got the potato!"),

        SIGN_LINE("<black>Click to join!"),

        COMMAND_RELOAD("<green>Reloaded the plugin"),
        COMMAND_VERSION("<green>Version: {0}"),
        COMMAND_HELP("<yellow>/hotpotato {0} <white> - {1}"),
        COMMAND_LIST_HEADER("<yellow>==== All arenas ===="),
        COMMAND_INFO_HEADER("<yellow>==== Information for arena <white>{0} <yellow>===="),
        COMMAND_INFO_EDIT("<yellow>Click to edit value"),
        COMMAND_INFO_VALUE("<yellow>{0}: <white>{1}");

        private final String defaultValue;

        MessageKey(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public String getDefault() {
            return defaultValue;
        }

    }
}
