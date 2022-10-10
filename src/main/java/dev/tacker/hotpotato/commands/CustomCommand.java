package dev.tacker.hotpotato.commands;

import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public abstract class CustomCommand {

    protected abstract boolean checkPermission(CommandSender sender);

    public abstract String getCommandString();

    public abstract void printHelp(CommandSender sender);

    protected abstract void execute(CommandSender sender, String[] args);

    protected abstract List<String> tabComplete(CommandSender sender, String[] args);

    @Override
    public int hashCode() {
        return getCommandString().hashCode();
    }

    public boolean process(CommandSender sender, String[] args) {
        if (checkPermission(sender)) {
            execute(sender, args);
            return true;
        } else {
            return false;
        }
    }

    public List<String> processTabComplete(CommandSender sender, String[] args) {
        if (this.checkPermission(sender)) {
            return this.tabComplete(sender, args);
        } else {
            return new ArrayList<>();
        }
    }

}
