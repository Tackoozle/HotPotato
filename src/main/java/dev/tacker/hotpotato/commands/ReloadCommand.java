package dev.tacker.hotpotato.commands;

import dev.tacker.hotpotato.HotPotato;
import dev.tacker.hotpotato.utils.Locale;
import dev.tacker.hotpotato.utils.Permissions;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ReloadCommand extends CustomCommand {
    @Override
    protected boolean checkPermission(CommandSender sender) {
        return Permissions.ADMIN.check(sender);
    }

    @Override
    public String getCommandString() {
        return "reload";
    }

    @Override
    public void printHelp(CommandSender sender) {
        sender.sendMessage(Locale.get(Locale.MessageKey.COMMAND_HELP, getCommandString(), "Reload Config"));
    }

    @Override
    protected void execute(CommandSender sender, String[] args) {
        HotPotato.getInstance().reload();
        sender.sendMessage(Locale.get(Locale.MessageKey.COMMAND_RELOAD));
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
