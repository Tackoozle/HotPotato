package dev.tacker.hotpotato.commands;

import dev.tacker.hotpotato.HotPotato;
import dev.tacker.hotpotato.models.Arena;
import dev.tacker.hotpotato.utils.Locale;
import dev.tacker.hotpotato.utils.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TeleportCommand extends CustomCommand {
    @Override
    protected boolean checkPermission(CommandSender sender) {
        return Permissions.ADMIN.check(sender);
    }

    @Override
    public String getCommandString() {
        return "teleport";
    }

    @Override
    public void printHelp(CommandSender sender) {
        sender.sendMessage(Locale.get(Locale.MessageKey.COMMAND_HELP, getCommandString(), "Teleports to arena"));
    }

    @Override
    protected void execute(CommandSender sender, String[] args) {
        String prefix = HotPotato.getInstance().getPrefix();
        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_PLAYER_ONLY_OR_TRY, "/hotpotato teleport <arena> [player]"));
                return;
            }
            Player player = (Player) sender;
            String a = args[0];
            Arena arena = HotPotato.getInstance().getManager().getArena(a);
            if (arena == null) {
                sender.sendMessage(Locale.get(Locale.MessageKey.ARENA_NOT_FOUND, a));
                return;
            }
            if (arena.getLobbyPoint() == null) {
                sender.sendMessage(Locale.get(Locale.MessageKey.ARENA_MISSING, "lobbyPoint"));
                return;
            }
            player.teleport(arena.getLobbyPoint());
            sender.sendMessage(Locale.get(Locale.MessageKey.ARENA_TELEPORTED, arena.getName()));
            return;
        }
        if (args.length == 2) {
            String a = args[0];
            String p = args[1];
            Player player = Bukkit.getPlayerExact(p);
            if (player == null) {
                sender.sendMessage(Locale.get(Locale.MessageKey.PLAYER_MISSING, p));
                return;
            }
            Arena arena = HotPotato.getInstance().getManager().getArena(a);
            if (arena == null) {
                sender.sendMessage(Locale.get(Locale.MessageKey.ARENA_NOT_FOUND, a));
                return;
            }
            if (arena.getLobbyPoint() == null) {
                sender.sendMessage(Locale.get(Locale.MessageKey.ARENA_MISSING, "lobbyPoint"));
                return;
            }
            player.teleport(arena.getLobbyPoint());

            sender.sendMessage(Locale.get(Locale.MessageKey.ARENA_TELEPORTED_OTHER, arena.getName(), player.getName()));
            if (!sender.getName().equals(player.getName())) {
                sender.sendMessage(Locale.get(Locale.MessageKey.ARENA_TELEPORTED, arena.getName()));
            }
            return;
        }
        sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_WRONG_COMMAND, "/hotpotato teleport <arena> [player]"));
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
