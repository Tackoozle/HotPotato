package dev.tacker.hotpotato.models;

import dev.tacker.hotpotato.HotPotato;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Manager {

    private final List<Arena> arenas = new ArrayList<>();

    /**
     * loading all arenas from arena folder
     */
    public void loadAllArenas() {
        HotPotato.getInstance().getLogging().log("Start loading arenas..");
        File folder = new File(HotPotato.getInstance().getDataFolder(), "arenas");
        folder.mkdirs();
        if (folder.listFiles().length == 0) {
            HotPotato.getInstance().getLogging().debug("Found no arena..");
            return;
        }
        for (File file : folder.listFiles()) {
            if (!file.isFile())
                continue;
            try {
                HotPotato.getInstance().getLogging().log("Enabling arena " + file.getName());
                Arena arena = Arena.fromFile(file);
                if (!arena.validate(Bukkit.getConsoleSender())) {
                    HotPotato.getInstance().getLogging().error("Cant activate " + arena.getName() + "!");
                    continue;
                }
                arenas.add(arena);
                HotPotato.getInstance().getLogging().debug("Arena " + arena.getName() + " successfully loaded!");
            } catch (Exception e) {
                HotPotato.getInstance().getLogging().error("Error while loading an arena!");
                e.printStackTrace();
            }
        }
    }

    /**
     * get arena by player, null if no arena found
     */
    public Arena getArena(Player player) {
        return arenas.stream().filter(a -> a.hasJoined(player)).findFirst().orElse(null);
    }

    /**
     * adds an arena
     */
    public void addArena(Arena arena) {
        if (this.getArena(arena.getName()) != null)
            throw new IllegalStateException("There is already an arena with the name + " + arena.getName() + "!");

        arenas.add(arena);
    }

    /**
     * removes an arena
     */
    public void removeArena(Arena arena) {
        if (arena == null)
            return;
        arenas.remove(arena);
        new File(HotPotato.getInstance().getDataFolder(), "/arenas/" + arena.getName() + ".yml").delete();
    }

    /**
     * get all arenas
     */
    public List<Arena> getArenas() {
        return arenas;
    }

    /**
     * get an arena by the name
     */
    public Arena getArena(String name) {
        for (Arena arena : arenas) {
            if (arena.getName().equals(name)) {
                return arena;
            }
        }
        return null;
    }

}
