package dev.tacker.hotpotato.commands;

import dev.tacker.hotpotato.HotPotato;
import dev.tacker.hotpotato.models.Arena;
import dev.tacker.hotpotato.utils.Locale;
import dev.tacker.hotpotato.utils.Permissions;
import dev.tacker.hotpotato.utils.Utils;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SetCommand extends CustomCommand {
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
        sender.sendMessage(Locale.get(Locale.MessageKey.COMMAND_HELP, getCommandString(), "Change settings of arena"));
    }

    @Override
    protected void execute(CommandSender sender, String[] args) {
        //hotpotato set <arena> <option> <value>
        String prefix = HotPotato.getInstance().getPrefix();
        if (args.length != 3) {
            sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_WRONG_COMMAND, "/hotpotato set <arena> <option> <value>"));
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_PLAYER_ONLY));
            return;
        }
        Player player = (Player) sender;
        //check arena
        String a = args[0];
        Arena arena = HotPotato.getInstance().getManager().getArena(a);
        if (arena == null) {
            sender.sendMessage(Locale.get(Locale.MessageKey.ARENA_NOT_FOUND, a));
            return;
        }

        String o = args[1];
        String v = args[2];
        int integer;
        boolean bool;
        double doubl;
        BarStyle barStyle;
        BarColor barColor;
        Sound tagSound;
        switch (o.toLowerCase()) {
            //check int
            case "minplayer":
                if (!Utils.isInt(v)) {
                    sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_NO_INT, v));
                    return;
                }
                integer = Integer.parseInt(v);
                if (integer < 2)
                    integer = 2;
                arena.setMinPlayer(integer);
                sender.sendMessage(Locale.get(Locale.MessageKey.ARENA_SETTING_SUCCESS, "minPlayer", integer));
                break;
            case "maxplayer":
                if (!Utils.isInt(v)) {
                    sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_NO_INT, v));
                    return;
                }
                integer = Integer.parseInt(v);
                if (integer < arena.getMinPlayer())
                    integer = arena.getMinPlayer();
                arena.setMaxPlayer(integer);
                sender.sendMessage(Locale.get(Locale.MessageKey.ARENA_SETTING_SUCCESS, "maxPlayer", integer));
                break;
            case "countdown":
                if (!Utils.isInt(v)) {
                    sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_NO_INT, v));
                    return;
                }
                integer = Integer.parseInt(v);
                arena.setCountdown(integer);
                sender.sendMessage(Locale.get(Locale.MessageKey.ARENA_SETTING_SUCCESS, "countDown", integer));
                break;
            case "maxtags":
                if (!Utils.isInt(v)) {
                    sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_NO_INT, v));
                    return;
                }
                integer = Integer.parseInt(v);
                arena.setMaxTags(integer);
                sender.sendMessage(Locale.get(Locale.MessageKey.ARENA_SETTING_SUCCESS, "maxTags", integer));
                break;
            case "savetime":
                if (!Utils.isDouble(v)) {
                    sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_NO_DOUBLE, v));
                    return;
                }
                doubl = Double.parseDouble(v);
                arena.setSaveTime(doubl);
                sender.sendMessage(Locale.get(Locale.MessageKey.ARENA_SETTING_SUCCESS, "saveTime", doubl));
                break;
            case "reducepertag":
                if (!Utils.isDouble(v)) {
                    sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_NO_DOUBLE, v));
                    return;
                }
                doubl = Double.parseDouble(v);
                arena.setReducePerTag(doubl);
                sender.sendMessage(Locale.get(Locale.MessageKey.ARENA_SETTING_SUCCESS, "reducePerTag", doubl));
                break;
            case "potatotime":
                if (!Utils.isDouble(v)) {
                    sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_NO_DOUBLE, v));
                    return;
                }
                doubl = Double.parseDouble(v);
                arena.setPotatoTime(doubl);
                sender.sendMessage(Locale.get(Locale.MessageKey.ARENA_SETTING_SUCCESS, "potatoTime", doubl));
                break;
            //check location
            case "lobbypoint":
                if (!v.equalsIgnoreCase("here")) {
                    sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_WRONG_INPUT, v));
                    return;
                }
                arena.setLobbyPoint(player.getLocation());
                sender.sendMessage(Locale.get(Locale.MessageKey.ARENA_SETTING_SUCCESS, "lobbyPoint", player.getLocation()));
                break;
            case "gamepoint":
                if (!v.equalsIgnoreCase("here")) {
                    sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_WRONG_INPUT, v));
                    return;
                }
                arena.setGamePoint(player.getLocation());
                sender.sendMessage(Locale.get(Locale.MessageKey.ARENA_SETTING_SUCCESS, "gamePoint", player.getLocation()));
                break;
            case "sign":
                Block block = player.getTargetBlock(5);
                if (block == null || !(block.getState() instanceof WallSign) && !(block.getState() instanceof Sign)) {
                    sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_ERROR, "Not looking on a sign"));
                    return;
                }
                Sign sign = (Sign) block.getState();
                if (v.equalsIgnoreCase("add")) {
                    sign.line(0, Utils.mm(HotPotato.getInstance().getPrefix()));
                    sign.line(1, Utils.mm(arena.getName()));
                    sign.line(2, Locale.getNoPrefix(Locale.MessageKey.SIGN_LINE));
                    sign.getPersistentDataContainer().set(HotPotato.getInstance().key, PersistentDataType.STRING, arena.getName());
                    sign.update(true);
                    sender.sendMessage(Locale.get(Locale.MessageKey.ARENA_SIGN_ADD, arena.getName()));
                } else if (v.equalsIgnoreCase("remove")) {
                    sign.getPersistentDataContainer().remove(HotPotato.getInstance().key);
                    sender.sendMessage(Locale.get(Locale.MessageKey.ARENA_SIGN_REMOVE, arena.getName()));
                } else {
                    sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_WRONG_INPUT, v));
                    return;
                }
                break;
            //check bool
            case "active":
                if (!Utils.isBool(v)) {
                    sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_NO_BOOL, v));
                    return;
                }
                bool = Boolean.parseBoolean(v);
                if (bool && arena.validate(sender)) {
                    arena.setActive(true);
                } else {
                    arena.setActive(false);
                }
                sender.sendMessage(Locale.get(Locale.MessageKey.ARENA_SETTING_SUCCESS, "active", v));
                break;
            //check string
            case "barcolor":
                try {
                    barColor = BarColor.valueOf(v);
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_WRONG_INPUT, v));
                    return;
                }
                arena.setBarColor(barColor);
                sender.sendMessage(Locale.get(Locale.MessageKey.ARENA_SETTING_SUCCESS, "barColor", v));
                break;
            case "barstyle":
                try {
                    barStyle = BarStyle.valueOf(v);
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_WRONG_INPUT, v));
                    return;
                }
                arena.setBarStyle(barStyle);
                sender.sendMessage(Locale.get(Locale.MessageKey.ARENA_SETTING_SUCCESS, "barStyle", v));
                break;
            case "tagsound":
                try {
                    tagSound = Sound.valueOf(v);
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_WRONG_INPUT, v));
                    return;
                }
                arena.setTagSound(tagSound);
                sender.sendMessage(Locale.get(Locale.MessageKey.ARENA_SETTING_SUCCESS, "tagSound", v));
                break;
            default:
                sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_WRONG_INPUT, o));
        }
        boolean validate = arena.validate(sender);
        if (!validate) {
            arena.setActive(false);
            arena.save();
            return;
        }
        arena.save();
        if (!arena.isActive())
            sender.sendMessage(Locale.getWithCommand(Locale.MessageKey.ARENA_CONFIGURED, "<click:run_command:/hotpotato set " + arena.getName() + " active true>"));
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> options = Arrays.asList(
            "minPlayer", "maxPlayer", "countdown", "reducePerTag", "potatoTime", "maxTags", "saveTime",
            "lobbyPoint", "gamePoint", "sign",
            "active",
            "barColor", "barStyle", "tagSound");
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
                    case "countdown":
                    case "maxtags":
                        return IntStream.range(1, 100)
                            .mapToObj(String::valueOf)
                            .filter(e -> e.startsWith(args[2]))
                            .sorted(String.CASE_INSENSITIVE_ORDER)
                            .collect(Collectors.toList());
                    case "lobbypoint":
                    case "gamepoint":
                        return List.of("here");
                    case "sign":
                        return Arrays.asList("add", "remove");
                    case "reducepertag":
                    case "potatotime":
                    case "savetime":
                        return new ArrayList<>();
                    case "active":
                        return Arrays.asList("true", "false");
                    case "barcolor":
                        return Arrays.stream(BarColor.values())
                            .map(Enum::name)
                            .filter(e -> e.startsWith(args[2]))
                            .sorted(String.CASE_INSENSITIVE_ORDER)
                            .collect(Collectors.toList());
                    case "barstyle":
                        return Arrays.stream(BarStyle.values())
                            .map(Enum::name)
                            .filter(e -> e.startsWith(args[2]))
                            .sorted(String.CASE_INSENSITIVE_ORDER)
                            .collect(Collectors.toList());
                    case "tagsound":
                        return Arrays.stream(Sound.values())
                            .map(Enum::name)
                            .filter(e -> e.startsWith(args[2]))
                            .sorted(String.CASE_INSENSITIVE_ORDER)
                            .collect(Collectors.toList());
                }
        }
        return new ArrayList<>();
    }
}
