package dev.tacker.hotpotato.commands;

import dev.tacker.hotpotato.HotPotato;
import dev.tacker.hotpotato.models.Arena;
import dev.tacker.hotpotato.utils.Locale;
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
        sender.sendMessage(Locale.get(Locale.MessageKey.COMMAND_HELP, getCommandString(), "Information of arena"));
    }

    @Override
    protected void execute(CommandSender sender, String[] args) {
        String prefix = HotPotato.getInstance().getPrefix();
        if (args.length != 1) {
            sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_WRONG_COMMAND, "/hotpotato info <arena>"));
            return;
        }
        String name = args[0];
        Arena arena = HotPotato.getInstance().getManager().getArena(name);
        if (arena == null) {
            sender.sendMessage(Locale.get(Locale.MessageKey.ARENA_NOT_FOUND, name));
            return;
        }

        sender.sendMessage(Locale.get(Locale.MessageKey.COMMAND_INFO_HEADER, arena.getName()));
        sender.sendMessage(Locale.getWithCommand(Locale.MessageKey.COMMAND_INFO_VALUE, "<click:suggest_command:/hotpotato set " + arena.getName() + " minPlayer >","minPlayer", arena.getMinPlayer()));
        sender.sendMessage(Locale.getWithCommand(Locale.MessageKey.COMMAND_INFO_VALUE, "<click:suggest_command:/hotpotato set " + arena.getName() + " maxPlayer >","maxPlayer", arena.getMaxPlayer()));
        sender.sendMessage(Locale.getWithCommand(Locale.MessageKey.COMMAND_INFO_VALUE, "<click:suggest_command:/hotpotato set " + arena.getName() + " maxTags >","maxTags", arena.getMaxTags()));
        sender.sendMessage(Locale.getWithCommand(Locale.MessageKey.COMMAND_INFO_VALUE, "<click:suggest_command:/hotpotato set " + arena.getName() + " potatoTime >","potatoTime", arena.getPotatoTime()));
        sender.sendMessage(Locale.getWithCommand(Locale.MessageKey.COMMAND_INFO_VALUE, "<click:suggest_command:/hotpotato set " + arena.getName() + " countDown >","countDown", arena.getCountdown()));
        sender.sendMessage(Locale.getWithCommand(Locale.MessageKey.COMMAND_INFO_VALUE, "<click:suggest_command:/hotpotato set " + arena.getName() + " reducePerTag >","reducePerTag", arena.getReducePerTag()));
        sender.sendMessage(Locale.getWithCommand(Locale.MessageKey.COMMAND_INFO_VALUE, "<click:suggest_command:/hotpotato set " + arena.getName() + " saveTime >","saveTime", arena.getSaveTime()));
        sender.sendMessage(Locale.getWithCommand(Locale.MessageKey.COMMAND_INFO_VALUE, "<click:suggest_command:/hotpotato set " + arena.getName() + " tagSound >","tagSound", arena.getTagSound()));
        sender.sendMessage(Locale.getWithCommand(Locale.MessageKey.COMMAND_INFO_VALUE, "<click:suggest_command:/hotpotato set " + arena.getName() + " barColor >","barColor", arena.getBarColor()));
        sender.sendMessage(Locale.getWithCommand(Locale.MessageKey.COMMAND_INFO_VALUE, "<click:suggest_command:/hotpotato set " + arena.getName() + " barStyle >","barStyle", arena.getBarStyle()));
        sender.sendMessage(Locale.getWithCommand(Locale.MessageKey.COMMAND_INFO_VALUE, "<click:suggest_command:/hotpotato set " + arena.getName() + " active >","active", arena.isActive()));
        sender.sendMessage(Locale.getWithCommand(Locale.MessageKey.COMMAND_INFO_VALUE, "<click:suggest_command:/hotpotato set " + arena.getName() + " lobbyPoint here>","lobbyPoint", Utils.locationAsString(arena.getLobbyPoint())));
        sender.sendMessage(Locale.getWithCommand(Locale.MessageKey.COMMAND_INFO_VALUE, "<click:suggest_command:/hotpotato set " + arena.getName() + " gamePoint here>","gamePoint", Utils.locationAsString(arena.getGamePoint())));
        sender.sendMessage(Locale.get(Locale.MessageKey.COMMAND_INFO_EDIT));
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
