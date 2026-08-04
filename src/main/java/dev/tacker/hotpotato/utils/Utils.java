package dev.tacker.hotpotato.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class Utils {

    /**
     * returns component. adventure format
     */
    public static Component mm(String msg) {
        return MiniMessage.miniMessage().deserialize(msg);
    }

    /**
     * returns a component in legacy
     */
    public static String legacy(Component msg) {
        return LegacyComponentSerializer.legacySection().serialize(msg);
    }

    /**
     * returns location from configsection with given key
     */
    public static Location loadLocation(ConfigurationSection y, String key, boolean pitchyaw) {
        if (!y.getKeys(false).contains(key))
            return null;
        String world = y.getString(key + ".world");
        World w = Bukkit.getWorld(world);
        if (w == null)
            throw new IllegalStateException("world " + world + " is null. tried loading " + key + " location");
        double x = y.getDouble(key + ".x");
        double yy = y.getDouble(key + ".y");
        double z = y.getDouble(key + ".z");
        float pitch = pitchyaw ? (float) y.getDouble(key + ".pitch") : 0f;
        float yaw = pitchyaw ? (float) y.getDouble(key + ".yaw") : 0f;
        return new Location(w, x, yy, z, yaw, pitch);
    }

    /**
     * saves location to configsection with given key
     */
    public static void saveLocation(ConfigurationSection y, String key, Location location, boolean pitchyaw) {
        if (location == null)
            return;
        y.set(key + ".world", location.getWorld().getName());
        y.set(key + ".x", location.getX());
        y.set(key + ".y", location.getY());
        y.set(key + ".z", location.getZ());
        if (pitchyaw) {
            y.set(key + ".yaw", location.getYaw());
            y.set(key + ".pitch", location.getPitch());
        }
    }

    /**
     * returns signlocation from configsection with given key
     */
    public static Location loadSignLocation(ConfigurationSection y, World w, String key) {
        if (!y.getKeys(false).contains(key))
            return null;
        double x = y.getDouble(key + ".x");
        double yy = y.getDouble(key + ".y");
        double z = y.getDouble(key + ".z");
        return new Location(w, x, yy, z);
    }

    /**
     * saves sign location to configsection with given key
     */
    public static void saveSignLocation(ConfigurationSection y, String key, Location location) {
        if (location == null)
            return;
        y.set(key + ".x", location.getX());
        y.set(key + ".y", location.getY());
        y.set(key + ".z", location.getZ());
    }

    /**
     * replaces "{0}", "{1}", .. placeholders with the given args (no MessageFormat quoting quirks)
     */
    public static String format(String message, Object... args) {
        String result = message;
        for (int i = 0; i < args.length; i++) {
            result = result.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return result;
    }

    /**
     * true if value is an integer
     */
    public static boolean isInt(String value) {
        try {
            Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * true if value is a double
     */
    public static boolean isDouble(String value) {
        try {
            Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * true if value is a boolean ("true"/"false")
     */
    public static boolean isBool(String value) {
        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false");
    }

    /**
     * returns location as string
     */
    public static String locationAsString(Location l) {
        if (l == null)
            return "- not set -";
        double x = Math.round(l.getX() * 100.0 / 100.0);
        double y = Math.round(l.getY() * 100.0 / 100.0);
        double z = Math.round(l.getZ() * 100.0 / 100.0);
        return "X: " + x + ", Y: " + y + ", Z: " + z + ", World: " + l.getWorld().getName();
    }

    /**
     * resolves a Sound from its registry key (e.g. "block.note_block.pling")
     * @return the sound, or null if none matches
     */
    public static Sound soundFromString(String value) {
        try {
            return Registry.SOUNDS.get(NamespacedKey.minecraft(value.toLowerCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * returns the registry key of a sound, for saving to config
     */
    public static String soundKey(Sound sound) {
        return Registry.SOUNDS.getKey(sound).getKey();
    }

    /**
     * all known sound registry keys, for tab completion
     */
    public static List<String> soundKeys() {
        return Registry.SOUNDS.stream()
            .map(Utils::soundKey)
            .collect(Collectors.toList());
    }

    public static ItemStack getPotato() {
        ItemStack is = new ItemStack(Material.BAKED_POTATO);
        ItemMeta meta = is.getItemMeta();
        meta.displayName(Utils.mm("<gold>Heiße Kartoffel"));
        is.setItemMeta(meta);
        return is;
    }
}
