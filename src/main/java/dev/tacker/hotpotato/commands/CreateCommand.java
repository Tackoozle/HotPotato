package dev.tacker.hotpotato.commands;

import dev.tacker.hotpotato.HotPotato;
import dev.tacker.hotpotato.models.Arena;
import dev.tacker.hotpotato.utils.Locale;
import dev.tacker.hotpotato.utils.Permissions;
import org.bukkit.Sound;
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
        sender.sendMessage(Locale.get(Locale.MessageKey.COMMAND_HELP, getCommandString(), "Creates an arena"));
    }

    @Override
    protected void execute(CommandSender sender, String[] args) {
        String prefix = HotPotato.getInstance().getPrefix();
        if (args.length != 1) {
            sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_WRONG_COMMAND, "/hotpotato create <name>"));
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_PLAYER_ONLY));
            return;
        }
        Player player = (Player) sender;

        String name = args[0];
        if (HotPotato.getInstance().getManager().getArena(name) != null) {
            sender.sendMessage(Locale.get(Locale.MessageKey.ARENA_NAME_ALREADY_TAKEN, name));
            return;
        }
        FileConfiguration c = HotPotato.getInstance().getConfig();
        Arena arena = new Arena(name, c.getInt("defaults.minPlayer"), c.getInt("defaults.maxPlayer"), false,
            null, null, BarStyle.SEGMENTED_10, BarColor.RED, c.getDouble("defaults.potatoTime"),
            c.getDouble("defaults.reducePerTag"), c.getInt("defaults.countdown"), c.getInt("defaults.maxTags"),
            c.getInt("defaults.saveTime"), Sound.BLOCK_NOTE_BLOCK_PLING);
        HotPotato.getInstance().getManager().addArena(arena);

        if (arena.save()) {
            sender.sendMessage(Locale.get(Locale.MessageKey.ARENA_CREATED, arena.getName()));
        } else {
            HotPotato.getInstance().getManager().removeArena(arena);
            sender.sendMessage(Locale.get(Locale.MessageKey.ARENA_ERROR_SAVING, arena.getName()));
        }
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
