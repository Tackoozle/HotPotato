package dev.tacker.hotpotato;

import dev.tacker.hotpotato.commands.CommandHandler;
import dev.tacker.hotpotato.commands.CreateCommand;
import dev.tacker.hotpotato.commands.InfoCommand;
import dev.tacker.hotpotato.commands.JoinCommand;
import dev.tacker.hotpotato.commands.LeaveCommand;
import dev.tacker.hotpotato.commands.ListCommand;
import dev.tacker.hotpotato.commands.ReloadCommand;
import dev.tacker.hotpotato.commands.RemoveCommand;
import dev.tacker.hotpotato.commands.SetCommand;
import dev.tacker.hotpotato.commands.TeleportCommand;
import dev.tacker.hotpotato.commands.VersionCommand;
import dev.tacker.hotpotato.listeners.PlayerListener;
import dev.tacker.hotpotato.listeners.SignListener;
import dev.tacker.hotpotato.models.Manager;
import dev.tacker.hotpotato.utils.Logging;
import dev.tacker.hotpotato.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class HotPotato extends JavaPlugin {

    private static HotPotato instance = null;
    private Logging logging;
    private Manager manager;
    private final String prefix = "<white>[<red>H<gray>P<white>] ";
    public final NamespacedKey key = new NamespacedKey(this, "joinsign");

    public HotPotato() {
        instance = this;
    }

    @Override
    public void onEnable() {
        initConfig();
        logging = new Logging("[HP] ", " = DEBUG = ", getConfig().getBoolean("debug"));
        manager = new Manager();
        manager.loadAllArenas();
        listenerRegistration();
        commandRegistration();
        logging.log("HotPotato " + getDescription().getVersion() + " successfully enabled!");
    }

    @Override
    public void onDisable() {
        manager.disable();
    }

    public void reload() {
        reloadConfig();
        logging = new Logging("[HP] ", " = DEBUG = ", getConfig().getBoolean("debug"));
        manager.disable();
        manager.loadAllArenas();
    }

    public void listenerRegistration() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new PlayerListener(), this);
        pluginManager.registerEvents(new SignListener(), this);
    }

    public void commandRegistration() {
        CommandHandler cmd = new CommandHandler(logging, Utils.mm(prefix + "<red>You dont have permission to do this!"));
        cmd.registerCmd(new VersionCommand());
        cmd.registerCmd(new ReloadCommand());
        cmd.registerCmd(new LeaveCommand());
        cmd.registerCmd(new JoinCommand());
        cmd.registerCmd(new CreateCommand());
        cmd.registerCmd(new RemoveCommand());
        cmd.registerCmd(new SetCommand());
        cmd.registerCmd(new InfoCommand());
        cmd.registerCmd(new TeleportCommand());
        cmd.registerCmd(new ListCommand());
        getCommand("hotpotato").setExecutor(cmd);
    }

    public void initConfig() {
        FileConfiguration config = this.getConfig();
        config.addDefault("debug", false);
        config.addDefault("game.minPlayer", 2);
        config.addDefault("game.maxPlayer", 64);
        config.addDefault("game.countdown", 10);
        config.addDefault("game.potatoTime", 30.0);
        config.addDefault("game.reducePerTag", 1.0);
        config.addDefault("game.maxTags", 25);
        config.addDefault("game.saveTime", 3);
        config.options().copyDefaults(true);
        this.saveConfig();
    }

    public Logging getLogging() {
        return logging;
    }

    public static HotPotato getInstance() {
        return instance;
    }

    public String getPrefix() {
        return prefix;
    }

    public Manager getManager() {
        return manager;
    }
}
