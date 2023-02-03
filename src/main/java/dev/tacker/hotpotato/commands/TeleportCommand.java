package dev.tacker.hotpotato.commands;

import dev.tacker.hotpotato.HotPotato;
import dev.tacker.hotpotato.models.Arena;
import dev.tacker.hotpotato.utils.Permissions;
import dev.tacker.hotpotato.utils.Utils;
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
        sender.sendMessage(Utils.mm("<yellow>/hotpotato " + getCommandString() + "<white> - Teleports to arena"));
    }

    @Override
    protected void execute(CommandSender sender, String[] args) {
        String prefix = HotPotato.getInstance().getPrefix();
        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Utils.mm(prefix + "<red>Try it with /hotpotato teleport <arena> [player]"));
                return;
            }
            Player player = (Player) sender;
            String a = args[0];
            Arena arena = HotPotato.getInstance().getManager().getArena(a);
            if (arena == null) {
                sender.sendMessage(Utils.mm(prefix + "<red>There is no arena with the name " + a + "!"));
                return;
            }
            if (arena.getLobbyPoint() == null) {
                sender.sendMessage(Utils.mm(prefix + "<red>There is no lobbypoint set for arena " + arena.getName() + "!"));
                return;
            }
            player.teleport(arena.getLobbyPoint());
            player.sendMessage(Utils.mm(prefix + "<green>Teleported you to arena " + arena.getName() + "!"));
            return;
        }
        if (args.length == 2) {
            String a = args[0];
            String p = args[1];
            Player player = Bukkit.getPlayerExact(p);
            if (player == null) {
                sender.sendMessage(Utils.mm(prefix + "<red>There is no player with the name " + p + "!"));
                return;
            }
            Arena arena = HotPotato.getInstance().getManager().getArena(a);
            if (arena == null) {
                sender.sendMessage(Utils.mm(prefix + "<red>There is no arena with the name " + a + "!"));
                return;
            }
            if (arena.getLobbyPoint() == null) {
                sender.sendMessage(Utils.mm(prefix + "<red>There is no lobbypoint set for arena " + arena.getName() + "!"));
                return;
            }
            player.teleport(arena.getLobbyPoint());
            sender.sendMessage(Utils.mm(prefix + "<green>Teleported" + player.getName() + " to arena " + arena.getName() + "!"));
            if (!sender.getName().equals(player.getName())) {
                player.sendMessage(Utils.mm(prefix + "<green>You were teleported to the arena " + arena.getName() + "!"));
            }
            return;
        }
        sender.sendMessage(Utils.mm(prefix + "<red> Try it with /hotpotato teleport <arena> [player]"));
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
