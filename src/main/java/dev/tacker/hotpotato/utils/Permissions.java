package dev.tacker.hotpotato.utils;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public enum Permissions {

    USE("hotpotato.use"),
    ADMIN("hotpotato.admin"),
    ;

    final String perm;

    Permissions(String perm) {
        this.perm = perm;
    }

    /**
     * true if sender has permission.
     */
    public boolean check(CommandSender sender) {
        if (sender.hasPermission(ADMIN.perm))
            return true;
        if (sender instanceof ConsoleCommandSender)
            return true;
        if (sender instanceof BlockCommandSender)
            return true;
        return sender.hasPermission(perm);
    }
}
