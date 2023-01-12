package dev.tacker.hotpotato.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public class Utils {

    /**
     * returns component. adventure format
     */
    public static Component mm(String msg) {
        return MiniMessage.miniMessage().deserialize(msg);
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
        double y = Math.round(l.getX() * 100.0 / 100.0);
        double z = Math.round(l.getX() * 100.0 / 100.0);
        return "X: " + x + ", Y: " + y + ", Z: " + z + ", World: " + l.getWorld().getName();
    }
}
