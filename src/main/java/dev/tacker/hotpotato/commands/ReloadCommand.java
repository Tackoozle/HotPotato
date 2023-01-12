package dev.tacker.hotpotato.commands;

import dev.tacker.hotpotato.HotPotato;
import dev.tacker.hotpotato.utils.Permissions;
import dev.tacker.hotpotato.utils.Utils;
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
        sender.sendMessage(Utils.mm("<yellow>/hotpotato " + getCommandString() + "<white> - Reload plugin"));
    }

    @Override
    protected void execute(CommandSender sender, String[] args) {
        HotPotato.getInstance().reload();
        sender.sendMessage(Utils.mm(HotPotato.getInstance().getPrefix() + "<green>Plugin reloaded."));
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
