package dev.tacker.hotpotato.commands;

import dev.tacker.hotpotato.utils.Locale;
import dev.tacker.hotpotato.utils.Logging;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class CommandHandler implements CommandExecutor, TabCompleter {
    private static final HashMap<String, CustomCommand> registeredCommands = new HashMap<>();
    private final Logging logging;
    private final Component noPermission;

    public CommandHandler(Logging logging, Component noPermission) {
        this.logging = logging;
        this.noPermission = noPermission;
    }

    public void registerCmd(CustomCommand cmd) {
        logging.debug(cmd.getCommandString() + " registered.");
        registeredCommands.put(cmd.getCommandString().toLowerCase(), cmd);
    }

    public void printAllCommands(CommandSender sender) {
        sender.sendMessage("==== All Commands ====");
        for (CustomCommand bc : registeredCommands.values()) {
            bc.printHelp(sender);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            printAllCommands(sender);
            return true;
        }
        if (args[0] != null) {
            CustomCommand cmd = registeredCommands.get(args[0].toLowerCase());
            if (cmd == null) {
                sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_NO_COMMAND, args[0]));
                return false;
            }
            args = Arrays.copyOfRange(args, 1, args.length);
            if (!cmd.process(sender, args)) {
                sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_NO_PERM));
            }
            return true;
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 0) {
            return registeredCommands.keySet().stream().filter(cmd -> registeredCommands.get(cmd).checkPermission(sender))
                .collect(Collectors.toList());
        } else if (args.length == 1) {
            return registeredCommands.keySet().stream().filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                .filter(cmd -> registeredCommands.get(cmd).checkPermission(sender))
                .collect(Collectors.toList());
        } else {
            List<String> matchingCmds = registeredCommands.keySet().stream().filter(cmd -> cmd.equals(args[0].toLowerCase()))
                .filter(cmd -> registeredCommands.get(cmd).checkPermission(sender))
                .collect(Collectors.toList());
            if (matchingCmds.size() == 1)
                return registeredCommands.get(matchingCmds.get(0)).processTabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
            return new ArrayList<>();
        }
    }

}
