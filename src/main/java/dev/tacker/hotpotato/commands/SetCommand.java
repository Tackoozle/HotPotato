package dev.tacker.hotpotato.commands;

import dev.tacker.hotpotato.HotPotato;
import dev.tacker.hotpotato.models.Arena;
import dev.tacker.hotpotato.utils.Permissions;
import dev.tacker.hotpotato.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SetCommand extends CustomCommand{
    @Override
    protected boolean checkPermission(CommandSender sender) {
        return Permissions.ADMIN.check(sender);
    }

    @Override
    public String getCommandString() {
        return "set";
    }

    @Override
    public void printHelp(CommandSender sender) {
        sender.sendMessage(Utils.mm("<yellow>/hotpotato " + getCommandString() + "<white> - Change settings"));
    }

    @Override
    protected void execute(CommandSender sender, String[] args) {
        //hotpotato set <arena> <option> <value>
        String prefix = HotPotato.getInstance().getPrefix();
        if (args.length != 3) {
            sender.sendMessage(Utils.mm(prefix + "<red>Try it with /hotpotato set <arena> <option> <value>"));
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.mm(prefix + "<red>This command just works ingame!"));
            return;
        }
        Player player = (Player) sender;
        //check arena
        String a = args[0];
        Arena arena = HotPotato.getInstance().getManager().getArena(a);
        if (arena == null) {
            sender.sendMessage(Utils.mm(prefix + "<red>There is no arena with the name " + a + "!"));
            return;
        }

        String o = args[1];
        String v = args[2];
        int integer;
        boolean bool;
        double doubl;
        BarStyle barStyle;
        BarColor barColor;
        switch (o.toLowerCase()) {
            //check int
            case "minplayer":
                if (!Utils.isInt(v)) {
                    sender.sendMessage(Utils.mm(prefix + "<red>The value " + v + " is no integer!"));
                    return;
                }
                integer = Integer.parseInt(v);
                if (integer < 2)
                    integer = 2;
                arena.setMinPlayer(integer);
                sender.sendMessage(Utils.mm(prefix + "<green>The min. players was set successfully to " + integer + "!"));
                break;
            case "maxplayer":
                if (!Utils.isInt(v)) {
                    sender.sendMessage(Utils.mm(prefix + "<red>The value " + v + " is no integer!"));
                    return;
                }
                integer = Integer.parseInt(v);
                if (integer < arena.getMinPlayer())
                    integer = arena.getMinPlayer();
                arena.setMaxPlayer(integer);
                sender.sendMessage(Utils.mm(prefix + "<green>The max. players was set successfully to " + integer + "!"));
                break;
            case "countdown":
                if (!Utils.isInt(v)) {
                    sender.sendMessage(Utils.mm(prefix + "<red>The value " + v + " is no integer!"));
                    return;
                }
                integer = Integer.parseInt(v);
                arena.setCountdown(integer);
                sender.sendMessage(Utils.mm(prefix + "<green>The countdown was set successfully to " + integer + "!"));
                break;
            case "maxtags":
                if (!Utils.isInt(v)) {
                    sender.sendMessage(Utils.mm(prefix + "<red>The value " + v + " is no integer!"));
                    return;
                }
                integer = Integer.parseInt(v);
                arena.setMaxTags(integer);
                sender.sendMessage(Utils.mm(prefix + "<green>maxtags was set successfully to " + integer + "!"));
                break;
            case "reducepertag":
                if (!Utils.isDouble(v)) {
                    sender.sendMessage(Utils.mm(prefix + "<red>The value " + v + " is no double!"));
                    return;
                }
                doubl = Double.parseDouble(v);
                arena.setReducePerTag(doubl);
                sender.sendMessage(Utils.mm(prefix + "<green>The reducepertag was set successfully to " + doubl + "!"));
                break;
            case "potatotime":
                if (!Utils.isDouble(v)) {
                    sender.sendMessage(Utils.mm(prefix + "<red>The value " + v + " is no double!"));
                    return;
                }
                doubl = Double.parseDouble(v);
                arena.setPotatoTime(doubl);
                sender.sendMessage(Utils.mm(prefix + "<green>The potatotime was set successfully to " + doubl + "!"));
                break;
            //check location
            case "lobbypoint":
                if (!v.equalsIgnoreCase("here")) {
                    sender.sendMessage(Utils.mm(prefix + "<red>The value " + v + " is not here!"));
                    return;
                }
                arena.setLobbyPoint(player.getLocation());
                sender.sendMessage(Utils.mm(prefix + "<green>The lobbypoint was set successfully at your location!"));
                break;
            case "gamepoint":
                if (!v.equalsIgnoreCase("here")) {
                    sender.sendMessage(Utils.mm(prefix + "<red>The value " + v + " is not here!"));
                    return;
                }
                arena.setGamePoint(player.getLocation());
                sender.sendMessage(Utils.mm(prefix + "<green>The gamepoint was set successfully at your location!"));
                break;
            case "sign":
                Block block = player.getTargetBlock(5);
                if (block == null || !(block.getState() instanceof WallSign) && !(block.getState() instanceof Sign)) {
                    sender.sendMessage(Utils.mm(prefix + "<red>You are not looking at a sign!"));
                    return;
                }
                Sign sign = (Sign) block.getState();
                if (v.equalsIgnoreCase("add")) {
                    sign.line(0, Utils.mm(prefix));
                    sign.line(1, Utils.mm(arena.getName()));
                    sign.line(2, Utils.mm("Click to join!"));
                    sign.getPersistentDataContainer().set(HotPotato.getInstance().key, PersistentDataType.STRING, arena.getName());
                    sign.update(true);
                    sender.sendMessage(Utils.mm(prefix + "<green>The joinsign was set successfully to the sign!"));
                } else if (v.equalsIgnoreCase("remove")) {
                    arena.setActive(false);
                    sign.getPersistentDataContainer().remove(HotPotato.getInstance().key);
                    sender.sendMessage(Utils.mm(prefix + "<green>The joinsign was removed successfully. You cant save your arena. First set a new sign!"));
                } else {
                    sender.sendMessage(Utils.mm(prefix + "<red>The value " + v + " is not add/remove"));
                    return;
                }
                break;
            //check bool
            case "active":
                if (!Utils.isBool(v)) {
                    sender.sendMessage(Utils.mm(prefix + "<red>The value " + v + " is no bool (true/false)!"));
                    return;
                }
                bool = Boolean.parseBoolean(v);
                if (bool && arena.validate(sender)) {
                    arena.setActive(true);
                    sender.sendMessage(Utils.mm(prefix + "<green>The arena " + arena.getName() + " was set active successfully!"));
                } else {
                    arena.setActive(false);
                    sender.sendMessage(Utils.mm(prefix + "<red>The arena " + arena.getName() + " was set inactive!"));
                }
                break;
            //check string
            case "world":
                World w = Bukkit.getWorld(v);
                if (w == null) {
                    sender.sendMessage(Utils.mm(prefix + "<red>There is no world with the name " + v + "!"));
                    return;
                }
                arena.setWorld(v);
                sender.sendMessage(Utils.mm(prefix + "<green>The world " + v + " was set successfully!"));
                break;
            case "region":
                arena.setRegion(v);
                sender.sendMessage(Utils.mm(prefix + "<green>The region " + v + " was set successfully!"));
                break;
                /*
                RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer().get(new BukkitWorld(Bukkit.getWorld(arena.getWorld())));
                if (rm == null || rm.getRegion(v) == null) {
                    sender.sendMessage(Utils.mm(prefix + "<red>There is no region with the name " + v + "!"));
                    return;
                }
                arena.setRegion(v);
                sender.sendMessage(Utils.mm(prefix + "<green>The region " + v + " was set successfully!"));
                */
            case "barcolor":
                try {
                    barColor = BarColor.valueOf(v);
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(Utils.mm(prefix + "<red>There is no barColor named " + v + "!"));
                    return;
                }
                arena.setBarColor(barColor);
                sender.sendMessage(Utils.mm(prefix + "<green>The barcolor " + v + " was set successfully!"));
                break;
            case "barstyle":
                try {
                    barStyle = BarStyle.valueOf(v);
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(Utils.mm(prefix + "<red>There is no barStyle named " + v + "!"));
                    return;
                }
                arena.setBarStyle(barStyle);
                sender.sendMessage(Utils.mm(prefix + "<green>The barstyle " + v + " was set successfully!"));
                break;
            default:
                sender.sendMessage(Utils.mm(prefix + "<red>There is no option with the name " + o + "!"));
        }
        arena.save();
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> options = Arrays.asList(
                "minPlayer", "maxPlayer", "countdown", "reducePerTag", "potatoTime", "maxTags",
                "lobbyPoint", "gamePoint", "sign",
                "active",
                "region", "world", "barColor", "barStyle");
        switch (args.length) {
            case 1:
                return HotPotato.getInstance().getManager().getArenas().stream()
                        .map(Arena::getName)
                        .filter(e -> e.startsWith(args[0]))
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .collect(Collectors.toList());
            case 2:
                return options.stream()
                        .filter(e -> e.startsWith(args[1]))
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .collect(Collectors.toList());
            case 3:
                switch (args[1].toLowerCase()) {
                    case "minplayer":
                    case "maxplayer":
                        return IntStream.range(1, 100)
                                .mapToObj(String::valueOf)
                                .collect(Collectors.toList());
                    case "lobbypoint":
                    case "gamepoint":
                        return List.of("here");
                    case "sign":
                        return Arrays.asList("add", "remove");
                    case "region":
                        //TODO: add all region from world
                        break;
                    case "world":
                        return Bukkit.getWorlds().stream().map(WorldInfo::getName).collect(Collectors.toList());
                    case "active":
                        return Arrays.asList("true", "false");
                    case "barcolor":
                        return Arrays.stream(BarColor.values()).map(Enum::name).collect(Collectors.toList());
                    case "barstyle":
                        return Arrays.stream(BarStyle.values()).map(Enum::name).collect(Collectors.toList());
                }
        }
        return new ArrayList<>();
    }
}
