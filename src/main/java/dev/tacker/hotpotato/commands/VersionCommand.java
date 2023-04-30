package dev.tacker.hotpotato.commands;

import dev.tacker.hotpotato.HotPotato;
import dev.tacker.hotpotato.utils.Locale;
import dev.tacker.hotpotato.utils.Permissions;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class VersionCommand extends CustomCommand {
    @Override
    protected boolean checkPermission(CommandSender sender) {
        return Permissions.ADMIN.check(sender);
    }

    @Override
    public String getCommandString() {
        return "version";
    }

    @Override
    public void printHelp(CommandSender sender) {
        sender.sendMessage(Locale.get(Locale.MessageKey.COMMAND_HELP, getCommandString(), "Shows pluginversion"));
    }

    @Override
    protected void execute(CommandSender sender, String[] args) {
        sender.sendMessage(Locale.get(Locale.MessageKey.COMMAND_VERSION, HotPotato.getInstance().getDescription().getVersion()));
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
