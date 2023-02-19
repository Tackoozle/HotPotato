package dev.tacker.hotpotato.commands;

import dev.tacker.hotpotato.HotPotato;
import dev.tacker.hotpotato.models.Arena;
import dev.tacker.hotpotato.utils.Permissions;
import dev.tacker.hotpotato.utils.Utils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InfoCommand extends CustomCommand {
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
        String prefix = HotPotato.getInstance().getPrefix();
        if (args.length != 1) {
            sender.sendMessage(Utils.mm(prefix + "<red>Try it with /hotpotato info <arena>"));
            return;
        }
        String name = args[0];
        Arena arena = HotPotato.getInstance().getManager().getArena(name);
        if (arena == null) {
            sender.sendMessage(Utils.mm(prefix + "<red>There is no arena with the name " + name + "!"));
            return;
        }

        sender.sendMessage(Utils.mm(prefix + "<yellow>==== Information for arena <white>" + arena.getName() + " ===="));
        sender.sendMessage(Utils.mm(prefix + "<yellow><click:suggest_command:/hotpotato set " + arena.getName() + " minPlayer >Min Player: <white>" + arena.getMinPlayer()));
        sender.sendMessage(Utils.mm(prefix + "<yellow><click:suggest_command:/hotpotato set " + arena.getName() + " maxPlayer >Max Player: <white>" + arena.getMaxPlayer()));
        sender.sendMessage(Utils.mm(prefix + "<yellow><click:suggest_command:/hotpotato set " + arena.getName() + " maxTags >Max Tags: <white>" + arena.getMaxTags()));
        sender.sendMessage(Utils.mm(prefix + "<yellow><click:suggest_command:/hotpotato set " + arena.getName() + " potatoTime >Potato time: <white>" + arena.getPotatoTime()));
        sender.sendMessage(Utils.mm(prefix + "<yellow><click:suggest_command:/hotpotato set " + arena.getName() + " reducePerTag >Reduce per Tag: <white>" + arena.getReducePerTag()));
        sender.sendMessage(Utils.mm(prefix + "<yellow><click:suggest_command:/hotpotato set " + arena.getName() + " saveTime >Savetime: <white>" + arena.getSaveTime()));
        sender.sendMessage(Utils.mm(prefix + "<yellow><click:suggest_command:/hotpotato set " + arena.getName() + " tagSound >Tagsound: <white>" + arena.getTagSound()));
        sender.sendMessage(Utils.mm(prefix + "<yellow><click:suggest_command:/hotpotato set " + arena.getName() + " active >Active: <white>" + arena.isActive()));
        sender.sendMessage(Utils.mm(prefix + "<yellow><click:suggest_command:/hotpotato set " + arena.getName() + " lobbyPoint >Lobby point: <white>" + Utils.locationAsString(arena.getLobbyPoint())));
        sender.sendMessage(Utils.mm(prefix + "<yellow><click:suggest_command:/hotpotato set " + arena.getName() + " gamePoint >Game point: <white>" + Utils.locationAsString(arena.getGamePoint())));
        sender.sendMessage(Utils.mm(prefix + "<yellow>Click to edit value."));
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
