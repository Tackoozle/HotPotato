package dev.tacker.hotpotato.commands;

import dev.tacker.hotpotato.HotPotato;
import dev.tacker.hotpotato.models.Arena;
import dev.tacker.hotpotato.utils.Permissions;
import dev.tacker.hotpotato.utils.Utils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ListCommand extends CustomCommand {
    @Override
    protected boolean checkPermission(CommandSender sender) {
        return Permissions.ADMIN.check(sender);
    }

    @Override
    public String getCommandString() {
        return "list";
    }

    @Override
    public void printHelp(CommandSender sender) {
        sender.sendMessage(Utils.mm("<yellow>/hotpotato " + getCommandString() + "<white> - List all arenas"));
    }

    @Override
    protected void execute(CommandSender sender, String[] args) {
        List<Arena> arenaList = HotPotato.getInstance().getManager().getArenas();
        if (arenaList.isEmpty()) {
            sender.sendMessage(Utils.mm(HotPotato.getInstance().getPrefix() + "<red>There are no arenas!"));
            return;
        }
        for (Arena arena : arenaList) {
            sender.sendMessage(Utils.mm(HotPotato.getInstance()
                .getPrefix() + "<white><click:run_command:/hotpotato info " + arena.getName() + ">>" + arena.getName() + "</click><click:run_command:/hotpotato teleport " + arena.getName() + "><blue> [TP]</click>"));
        }
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
