package dev.tacker.hotpotato.commands;

import dev.tacker.hotpotato.HotPotato;
import dev.tacker.hotpotato.models.Arena;
import dev.tacker.hotpotato.utils.Locale;
import dev.tacker.hotpotato.utils.Permissions;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RemoveCommand extends CustomCommand {
    @Override
    protected boolean checkPermission(CommandSender sender) {
        return Permissions.ADMIN.check(sender);
    }

    @Override
    public String getCommandString() {
        return "remove";
    }

    @Override
    public void printHelp(CommandSender sender) {
        sender.sendMessage(Locale.get(Locale.MessageKey.COMMAND_HELP, getCommandString(), "Removes an arena"));
    }

    @Override
    protected void execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_WRONG_COMMAND, "/hotpotato remove <arena>"));
            return;
        }
        String name = args[0];
        Arena arena = HotPotato.getInstance().getManager().getArena(name);
        if (arena == null) {
            sender.sendMessage(Locale.get(Locale.MessageKey.ARENA_NOT_FOUND,name));
            return;
        }
        HotPotato.getInstance().getManager().removeArena(arena);
        sender.sendMessage(Locale.get(Locale.MessageKey.ARENA_DELETED, arena.getName()));
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        switch (args.length) {
            case 1:
                return HotPotato.getInstance().getManager().getArenas().stream()
                    .map(Arena::getName)
                    .filter(e -> e.startsWith(args[0]))
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
