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

public class LeaveCommand extends CustomCommand {
    @Override
    protected boolean checkPermission(CommandSender sender) {
        return Permissions.USE.check(sender);
    }

    @Override
    public String getCommandString() {
        return "leave";
    }

    @Override
    public void printHelp(CommandSender sender) {
        sender.sendMessage(Utils.mm("<yellow>/hotpotato " + getCommandString() + "<white> - Leave a game"));
    }

    @Override
    protected void execute(CommandSender sender, String[] args) {
        String prefix = HotPotato.getInstance().getPrefix();
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Utils.mm(prefix + "<red>Try it with /hotpotato leave <arena> [Player]"));
                return;
            }
            Player player = (Player) sender;

            Arena arena = HotPotato.getInstance().getManager().getArena(player);
            if (arena == null) {
                sender.sendMessage(Utils.mm(prefix + "<red>You are in no arena!"));
                return;
            }

            arena.leave(player);
        } else if (args.length == 1) {
            if (!Permissions.ADMIN.check(sender)) {
                sender.sendMessage(Utils.mm(prefix + "<red>You dont have permission to do this!"));
                return;
            }
            String p = args[0];
            Player player = Bukkit.getPlayerExact(p);
            if (player == null) {
                sender.sendMessage(Utils.mm(prefix + "<red>There is no player with the name " + p + "!"));
                return;
            }

            Arena arena = HotPotato.getInstance().getManager().getArena(player);
            if (arena == null) {
                sender.sendMessage(Utils.mm(prefix + "<red>The Player " + p + "is in no arena!"));
                return;
            }

            arena.leave(player);
        } else
            sender.sendMessage(Utils.mm(prefix + "<red>Try it with /hotpotato leave <arena> [Player]"));
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        switch (args.length) {
            case 1:
                if (!Permissions.ADMIN.check(sender))
                    break;
                return HotPotato.getInstance().getServer().getOnlinePlayers().stream()
                    .map(HumanEntity::getName)
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
