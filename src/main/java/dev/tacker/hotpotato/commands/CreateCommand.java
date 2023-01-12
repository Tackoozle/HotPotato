package dev.tacker.hotpotato.commands;

import dev.tacker.hotpotato.HotPotato;
import dev.tacker.hotpotato.models.Arena;
import dev.tacker.hotpotato.utils.Permissions;
import dev.tacker.hotpotato.utils.Utils;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CreateCommand extends CustomCommand {
    @Override
    protected boolean checkPermission(CommandSender sender) {
        return Permissions.ADMIN.check(sender);
    }

    @Override
    public String getCommandString() {
        return "create";
    }

    @Override
    public void printHelp(CommandSender sender) {
        sender.sendMessage(Utils.mm("<yellow>/hotpotato " + getCommandString() + "<white> - Creates an arena"));

    }

    @Override
    protected void execute(CommandSender sender, String[] args) {
        String prefix = HotPotato.getInstance().getPrefix();
        if (args.length != 1) {
            sender.sendMessage(Utils.mm(prefix + "<red>Wrong arguments!"));
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.mm(prefix + "<red>Ingame only command!"));
            return;
        }
        Player player = (Player) sender;

        String name = args[0];
        if (HotPotato.getInstance().getManager().getArena(name) != null) {
            sender.sendMessage(Utils.mm(prefix + "<red>There is already an arena with the name " + name + "!"));
            return;
        }
        FileConfiguration c = HotPotato.getInstance().getConfig();
        Arena arena = new Arena(name, player.getWorld().getName(), "test", c.getInt("game.minPlayer"),
            c.getInt("game.maxPlayer"), false, null, null, BarStyle.SEGMENTED_10,
            BarColor.RED, c.getDouble("game.potatoTime"), c.getDouble("game.reducePerTag"),
            c.getInt("game.countdown"), c.getInt("game.maxTags"), c.getInt("game.saveTime"));
        HotPotato.getInstance().getManager().addArena(arena);
        if (arena.save()) {
            sender.sendMessage(Utils.mm(prefix + "<green>Arena " + arena.getName() + " created successfully!"));
        } else {
            sender.sendMessage(Utils.mm(prefix + "<red>There was error while saving arena " + arena.getName() + "!"));
        }
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
