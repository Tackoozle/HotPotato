package dev.tacker.hotpotato.commands;

import dev.tacker.hotpotato.HotPotato;
import dev.tacker.hotpotato.models.Arena;
import dev.tacker.hotpotato.utils.Permissions;
import dev.tacker.hotpotato.utils.Utils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InfoCommand extends CustomCommand{
    @Override
    protected boolean checkPermission(CommandSender sender) {
        return Permissions.ADMIN.check(sender);
    }

    @Override
    public String getCommandString() {
        return "info";
    }

    @Override
    public void printHelp(CommandSender sender) {
        sender.sendMessage(Utils.mm("<yellow>/hotpotato " + getCommandString() + "<white> - Information of arena"));
    }

    @Override
    protected void execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(Utils.mm(HotPotato.getInstance().getPrefix() + "<red>Try it with /blocky remove <arena>"));
            return;
        }
        String name = args[0];
        Arena arena = HotPotato.getInstance().getManager().getArena(name);
        if (arena == null) {
            sender.sendMessage(Utils.mm(HotPotato.getInstance().getPrefix() + "<red>There is no arena with the name " + name + "!"));
            return;
        }
        sender.sendMessage(arena.toString());
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
