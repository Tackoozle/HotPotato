package dev.tacker.hotpotato.commands;

import dev.tacker.hotpotato.HotPotato;
import dev.tacker.hotpotato.models.Arena;
import dev.tacker.hotpotato.utils.Permissions;
import dev.tacker.hotpotato.utils.Utils;
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
        sender.sendMessage(Utils.mm("<yellow>/hotpotato " + getCommandString() + "<white> - Joins a game"));
    }

    @Override
    protected void execute(CommandSender sender, String[] args) {
        String prefix = HotPotato.getInstance().getPrefix();
        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Utils.mm(prefix + "<red>Try it with /HotPotato join <arena> [Player]"));
                return;
            }
            Player player = (Player) sender;
            String a = args[0];
            Arena arena = HotPotato.getInstance().getManager().getArena(a);
            if (arena == null) {
                sender.sendMessage(Utils.mm(prefix + "<red>There is no arena with the name " + a + "!"));
                return;
            }
            arena.join(player);
        } else if (args.length == 2) {
            if (!Permissions.ADMIN.check(sender)) {
                sender.sendMessage(Utils.mm(prefix + "<red>You dont have permission to do this!"));
                return;
            }
            String p = args[1];
            Player player = Bukkit.getPlayer(p);
            if (player == null) {
                sender.sendMessage(Utils.mm(prefix + "<red>There is no player with the name " + p + "!"));
                return;
            }
            String a = args[0];
            Arena arena = HotPotato.getInstance().getManager().getArena(a);
            if (arena == null) {
                sender.sendMessage(Utils.mm(prefix + "<red>There is no arena with the name " + a + "!"));
                return;
            }
            arena.join(player);
        } else
            sender.sendMessage(Utils.mm(prefix + "<red>Try it with /hotpotato join <arena> [Player]"));
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
