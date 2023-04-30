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
import dev.tacker.hotpotato.models.Manager;
import dev.tacker.hotpotato.utils.Locale;
import dev.tacker.hotpotato.utils.Logging;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class HotPotato extends JavaPlugin {

    private static HotPotato instance = null;
    private Logging logging;
    private Manager manager;
    private final String prefix = "<white>[<red>H<gray>P<white>] ";
    public final NamespacedKey key = new NamespacedKey(this, "hotpotato_joinsign");

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
        Locale.setup();
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
        Locale.setup();
    }

    public void listenerRegistration() {
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
    }

    public void commandRegistration() {
        CommandHandler cmd = new CommandHandler(logging, Locale.get(Locale.MessageKey.ERROR_NO_PERM));
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
        config.addDefault("defaults.minPlayer", 2);
        config.addDefault("defaults.maxPlayer", 64);
        config.addDefault("defaults.countdown", 10);
        config.addDefault("defaults.potatoTime", 30.0);
        config.addDefault("defaults.reducePerTag", 1.0);
        config.addDefault("defaults.maxTags", 25);
        config.addDefault("defaults.saveTime", 3);
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
