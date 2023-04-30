package dev.tacker.hotpotato.commands;

import dev.tacker.hotpotato.HotPotato;
import dev.tacker.hotpotato.models.Arena;
import dev.tacker.hotpotato.utils.Locale;
import dev.tacker.hotpotato.utils.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JoinCommand extends CustomCommand {
    @Override
    protected boolean checkPermission(CommandSender sender) {
        return Permissions.USE.check(sender);
    }

    @Override
    public String getCommandString() {
        return "join";
    }

    @Override
    public void printHelp(CommandSender sender) {
        sender.sendMessage(Locale.get(Locale.MessageKey.COMMAND_HELP, getCommandString(), "Joins a game"));
    }

    @Override
    protected void execute(CommandSender sender, String[] args) {
        String prefix = HotPotato.getInstance().getPrefix();
        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_PLAYER_ONLY_OR_TRY, "/hotpotato join <arena> [player]"));
                return;
            }
            Player player = (Player) sender;
            String a = args[0];
            Arena arena = HotPotato.getInstance().getManager().getArena(a);
            if (arena == null) {
                sender.sendMessage(Locale.get(Locale.MessageKey.ARENA_NOT_FOUND, a));
                return;
            }
            arena.join(player);
        } else if (args.length == 2) {
            if (!Permissions.ADMIN.check(sender)) {
                sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_NO_PERM));
                return;
            }
            String p = args[1];
            Player player = Bukkit.getPlayer(p);
            if (player == null) {
                sender.sendMessage(Locale.get(Locale.MessageKey.PLAYER_MISSING, p));
                return;
            }
            String a = args[0];
            Arena arena = HotPotato.getInstance().getManager().getArena(a);
            if (arena == null) {
                sender.sendMessage(Locale.get(Locale.MessageKey.ARENA_NOT_FOUND, a));
                return;
            }
            arena.join(player);
        } else
            sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_WRONG_COMMAND, "/hotpotato join <arena> [player]"));
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
            case 2:
                if (!Permissions.ADMIN.check(sender))
                    break;
                return HotPotato.getInstance().getServer().getOnlinePlayers().stream()
                    .map(HumanEntity::getName)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
