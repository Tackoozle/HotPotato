package dev.tacker.hotpotato.models;

import dev.tacker.hotpotato.HotPotato;
import dev.tacker.hotpotato.utils.Logging;
import dev.tacker.hotpotato.utils.Utils;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Arena {
    private final ItemStack itemStack = new ItemStack(Material.BAKED_POTATO);
    private final String prefix = HotPotato.getInstance().getPrefix();
    private final Logging log = HotPotato.getInstance().getLogging();

    //loaded stuff from .yml
    private final String name;
    private double potatoTime;
    private double reducePerTag;
    private double saveTime;
    private String world;
    private String region;
    private int minPlayer;
    private int maxPlayer;
    private boolean active;
    private Location lobbyPoint;
    private Location gamePoint;
    private BarStyle barStyle;
    private BarColor barColor;
    private int countdown;
    private int maxTags;

    //temporary vars
    private final List<Player> alive = new LinkedList<>();
    private final List<Player> dead = new ArrayList<>();
    private final Set<Player> saved = new HashSet<>();
    private boolean running;
    private boolean started;
    private int tagcount;
    private int countdownMax;
    private Player potato;
    private boolean joinable;
    private int potatoTask;
    private int countdownTask;
    private BossBar bossBar;
    private Team team;
    private Scoreboard scoreboard;

    public Arena(String name, String world, String region, int minPlayer, int maxPlayer, boolean active,
                 Location lobbyPoint, Location gamePoint, BarStyle barStyle, BarColor barColor,
                 double potatoTime, double reducePerTag, int countdown, int maxTags, double saveTime) {
        this.name = name;
        this.world = world;
        this.region = region;
        this.minPlayer = minPlayer;
        this.maxPlayer = maxPlayer;
        this.active = active;
        this.lobbyPoint = lobbyPoint;
        this.gamePoint = gamePoint;
        this.barStyle = barStyle;
        this.barColor = barColor;
        this.potatoTime = potatoTime;
        this.reducePerTag = reducePerTag;
        this.countdown = countdown;
        this.countdownMax = countdown;
        this.maxTags = maxTags;
        this.saveTime = saveTime;
        this.joinable = true;
    }

    /**
     * returns new Arena object from a file
     */
    public static Arena fromFile(File file) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        String name = yaml.getString("name");
        String world = yaml.getString("world");
        String region = yaml.getString("region");
        int minPlayer = yaml.getInt("minPlayer");
        int maxPlayer = yaml.getInt("maxPlayer");
        boolean active = yaml.getBoolean("active");
        Location lobbyPoint = Utils.loadLocation(yaml, "lobbyPoint", true);
        Location gamePoint = Utils.loadLocation(yaml, "gamePoint", true);
        BarStyle barStyle = BarStyle.valueOf(yaml.getString("barStyle"));
        BarColor barColor = BarColor.valueOf(yaml.getString("barColor"));
        double potatoTime = yaml.getDouble("potatoTime");
        double reducePerTag = yaml.getDouble("reducePerTag");
        int countdown = yaml.getInt("countdown");
        int maxTags = yaml.getInt("maxTags");
        double saveTime = yaml.getDouble("saveTime");
        return new Arena(name, world, region, minPlayer, maxPlayer, active,
                lobbyPoint, gamePoint, barStyle, barColor, potatoTime,
                reducePerTag, countdown, maxTags, saveTime);
    }

    /**
     * stops arena, removes all player without getting a winner
     */
    public void stop() {
        removeOldPotato();
        alive.forEach(p -> {
            preparePlayer(p);
            p.teleport(lobbyPoint);
            p.sendMessage(Utils.mm("<red>Arenas has been stopped. Teleporting you to the lobby."));
        });
        end();
    }

    /**
     * checks if a player has joined
     * @param player the player
     * @return true if joined and alive, false if dead
     */
    public boolean hasJoined(Player player) {
        return alive.contains(player);
    }

    /**
     * new game is starting.
     */
    private void startGame() {
        alive.forEach(p -> p.teleport(getGamePoint()));
        started = true;
        countdown = countdownMax;
        this.countdownTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(HotPotato.getInstance(), () -> {
            for (Player p : alive) {
                p.showTitle(Title.title(Utils.mm("Starting in.."),
                        Utils.mm(String.valueOf(countdown)),
                        Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(1000), Duration.ofMillis(500))));
            }
            countdown--;
            if (countdown <= 3) {
                joinable = false;
            }
            if (countdown < 0) {
                Bukkit.getScheduler().cancelTask(countdownTask);
                if (!checkPlayer()) {
                    end();
                    return;
                }
                running = true;
                broadcast("<green>Arena started. Good luck!");
                prepareArena();
                setNewPotato(pickRandomPlayer());
                for (Player p : alive) {
                    bossBar.addPlayer(p);
                    team.addEntry(p.getName());
                    p.showTitle(Title.title(Utils.mm("Good luck!"),
                            Utils.mm("<gold>" + potato.getName() + " got the potato!"),
                            Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(1000), Duration.ofMillis(500))));
                }
                potatoTimer();
            }
        }, 0, 20);
    }

    /**
     * starts the timer for the potato. restarts when there is a new potato.
     */
    private void potatoTimer() {
        this.potatoTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(HotPotato.getInstance(), new Runnable() {
            final double timeAfterTags = tagcount * reducePerTag;
            final double time = 1.0 / ((potatoTime - timeAfterTags) * 20);
            double progress = 1.0;

            @Override
            public void run() {
                bossBar.setTitle(ChatColor.GOLD + getPotato().getName() + ChatColor.WHITE + " got the potato!");
                bossBar.setProgress(progress);
                for (Player p : alive) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1, false, false, false));
                }
                progress = progress - time;
                if (progress <= 0) {
                    tagcount = 0;
                    leave(getPotato());
                }
            }
        }, 0, 1);
    }

    private void removeOldPotato() {
        Player p = getPotato();
        if (p == null)
            return;
        p.setGlowing(false);
        p.getInventory().clear();
    }

    private void setNewPotato(Player p) {
        removeOldPotato();
        setPotato(p);
        p.setGlowing(true);
        fillHotbar(p);
        p.getInventory().setHelmet(itemStack);
        broadcast("<gold>" + p.getName() + " <white>got the potato!");
        p.sendMessage(Utils.mm(HotPotato.getInstance().getPrefix() + "<red><bold>You got the potato!"));
        for (Player players : alive) {
            players.playSound(players.getLocation(), Sound.BLOCK_PISTON_EXTEND, 1, 1);
        }
    }

    /**
     * broadcast msg (adventure format) to all alive player
     */
    private void broadcast(String msg) {
        alive.forEach(p -> p.sendMessage(Utils.mm(HotPotato.getInstance().getPrefix() + msg)));
    }

    /**
     * broadcast msg (adventure format) to all dead player
     */
    private void broadcastDead(String msg) {
        dead.forEach(p -> p.sendMessage(Utils.mm(HotPotato.getInstance().getPrefix() + msg)));
    }

    /**
     * checks if there are enough players left
     */
    private boolean checkPlayer() {
        return alive.size() > 1;
    }

    /**
     * end arena, resets everything
     */
    private void end() {
        if (alive.size() == 1) {
            Player p = alive.get(0);
            p.sendMessage(Utils.mm(HotPotato.getInstance().getPrefix() + "<gold>Gratz, you won!"));
            broadcastDead("<gold>Player " + p.getName() + " won the arena " + name + "!");
            preparePlayer(p);
            p.teleport(lobbyPoint);
        }
        alive.clear();
        dead.clear();
        Bukkit.getScheduler().cancelTask(potatoTask);
        Bukkit.getScheduler().cancelTask(countdownTask);
        joinable = true;
        potato = null;
        running = false;
        tagcount = 0;
        started = false;
        unprepareArena();
    }

    /**
     * removes player from arena
     */
    public void leave(Player player) {
        if (!alive.contains(player)) {
            player.sendMessage(Utils.mm(prefix + "<red>You are not in the arena " + name + "!"));
            return;
        }
        if (potato == player) {
            player.sendMessage(Utils.mm(HotPotato.getInstance().getPrefix() + "<red>You got potatoed and left the arena!"));
        } else {
            player.sendMessage(Utils.mm(HotPotato.getInstance().getPrefix() + "<red>You left the arena."));
        }
        alive.remove(player);
        dead.add(player);
        preparePlayer(player);
        removePlayerFromBossBar(player);
        removePlayerFromTeam(player);
        player.teleport(lobbyPoint);
        if (!checkPlayer()) {
            end();
            return;
        }
        if (getPotato() == player) {
            broadcast("<red>" + player.getName() + " left. Picking new potato..");
            setNewPotato(pickRandomPlayer());
        } else {
            broadcast("<red>" + player.getName() + " left the arena.");
        }
    }

    /**
     * add player to arena
     */
    public void join(Player player) {
        if (!active) {
            player.sendMessage(Utils.mm(prefix + "<red>The arena " + name + " is not active!"));
            return;
        }
        if (!joinable) {
            player.sendMessage(Utils.mm(prefix + "<red>The arena " + name + " is not joinable!"));
            return;
        }
        if (alive.size() >= maxPlayer) {
            player.sendMessage(Utils.mm(prefix + "<red>The arena " + name + " reached its player limit!"));
            return;
        }
        if (alive.contains(player)) {
            player.sendMessage(Utils.mm(prefix + "<red>You are already playing in arena " + name + "!"));
            return;
        }
        alive.add(player);
        preparePlayer(player);
        player.teleport(gamePoint);
        broadcast("<green>" + player.getName() + " joined the arena.");
        if (alive.size() >= minPlayer && !started) {
            startGame();
        }
    }

    /**
     * called when a player is tagged
     */
    public void tag(Player damager, Player damaged) {
        addSaved(damager);
        Bukkit.getScheduler().cancelTask(potatoTask);
        setNewPotato(damaged);
        if (tagcount < maxTags)
            tagcount++;
        potatoTimer();
    }

    private void addSaved(Player player) {
        if (saveTime == 0)
            return;
        saved.add(player);
        Bukkit.getScheduler().runTaskLater(HotPotato.getInstance(), () -> {
            saved.remove(player);
        }, (long) (saveTime * 20));
    }

    public boolean isSaved(Player player) {
        return saved.contains(player);
    }

    /**
     * returns a random alive player
     */
    private Player pickRandomPlayer() {
        int rnd = (int) (Math.random() * (alive.size()));
        return alive.get(rnd);
    }

    /**
     * creates bossbar and teams
     */
    private void prepareArena() {
        bossBar = Bukkit.createBossBar("HotPotato!", barColor, barStyle);
        bossBar.setVisible(true);
        scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        team = scoreboard.getTeam("hotpotato_" + name);
        if (team != null)
            team.unregister();
        team = scoreboard.registerNewTeam("hotpotato_" + name);
        team.color(NamedTextColor.GOLD);
    }

    private void unprepareArena() {
        if (team != null)
            team.unregister();
        if (bossBar != null)
            bossBar.removeAll();
        bossBar = null;
    }

    private void removePlayerFromBossBar(Player player) {
        if (bossBar != null && bossBar.getPlayers().contains(player))
            bossBar.removePlayer(player);
    }

    private void removePlayerFromTeam(Player player) {
        if (team != null && team.hasEntry(player.getName()))
            team.removeEntry(player.getName());
    }

    /**
     * fills hotbar of player with potatoes
     * @param player
     */
    private void fillHotbar(Player player) {
        Inventory inv = player.getInventory();
        inv.clear();
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, itemStack);
        }
    }

    private void preparePlayer(Player p) {
        p.setHealth(20);
        p.setSaturation(20);
        p.setGlowing(false);
        p.setGameMode(GameMode.SURVIVAL);
        p.getInventory().clear();
    }

    /**
     * checks if all things of arena is okay
     */
    public boolean validate(CommandSender sender) {
        boolean ok = true;
        if (name == null) {
            sender.sendMessage(Utils.mm(prefix + "<red>No name set!"));
            ok = false;
        }
        if (world == null) {
            sender.sendMessage(Utils.mm(prefix + "<red>World not set!"));
            ok = false;
        }
        if (world != null && Bukkit.getWorlds().contains(world)) {
            sender.sendMessage(Utils.mm(prefix + "<red>World is not existing!" + world));
            //ok = false;
        }
        /*RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer().get(new BukkitWorld(Bukkit.getWorld(world)));
        if (rm == null || region == null || rm.getRegion(region) == null) {
            sender.sendMessage(Utils.mm(prefix + "<red>Region not set or not existing!"));
            ok = false;
        }*/
        if (gamePoint == null) {
            sender.sendMessage(Utils.mm(prefix + "<red>No Location for game!"));
            ok = false;
        }
        if (lobbyPoint == null) {
            sender.sendMessage(Utils.mm(prefix + "<red>No Location for lobby!"));
            ok = false;
        }
        if (maxPlayer == 0) {
            sender.sendMessage(Utils.mm(prefix + "<red>Max players is not set!"));
            ok = false;
        }
        if (minPlayer == 0) {
            sender.sendMessage(Utils.mm(prefix + "<red>Min players is not set!"));
            ok = false;
        }
        if (minPlayer > maxPlayer) {
            sender.sendMessage(Utils.mm(prefix + "<red>Minplayer is greater than maxplayer!"));
            ok = false;
        }
        if (countdown <= 0) {
            sender.sendMessage(Utils.mm(prefix + "<red>Countdown is not allowed to be <=0!"));
            ok = false;
        }
        if (reducePerTag <= 0) {
            sender.sendMessage(Utils.mm(prefix + "<red>reducepertag is not allowed to be <=0!"));
            ok = false;
        }
        if (potatoTime <= 0) {
            sender.sendMessage(Utils.mm(prefix + "<red>potatotime is not allowed to be <=0!"));
            ok = false;
        }
        if ((maxTags * reducePerTag) > potatoTime) {
            sender.sendMessage(Utils.mm(prefix + "<red>maxtags * reducepertag is greater than potatotime!"));
            ok = false;
        }
        return ok;
    }

    /**
     * saves a arena to .yml file
     * @return true if saved, false if error occured
     */
    public boolean save() {
        YamlConfiguration y = new YamlConfiguration();
        save(y);
        try {
            y.save(new File(HotPotato.getInstance().getDataFolder(), "/arenas/" + name + ".yml"));
            return true;
        } catch (IOException e) {
            HotPotato.getInstance().getLogging().error("Error while saving arena " + name + " to file!");
            e.printStackTrace();
            return false;
        }
    }

    private void save(ConfigurationSection y) {
        y.set("name", name);
        y.set("world", world);
        y.set("region", region);
        y.set("active", active);
        y.set("minPlayer", minPlayer);
        y.set("maxPlayer", maxPlayer);
        Utils.saveLocation(y, "lobbyPoint", lobbyPoint, true);
        Utils.saveLocation(y, "gamePoint", gamePoint, true);
        y.set("barColor", barColor.name());
        y.set("barStyle", barStyle.name());
        y.set("reducePerTag", reducePerTag);
        y.set("potatoTime", potatoTime);
        y.set("countdown", countdown);
        y.set("maxTags", maxTags);
        y.set("saveTime", saveTime);
    }

    /*public boolean isInsideRegion(Location location) {
        if (!world.equals(location.getWorld().getName()))
            return false;
        ProtectedRegion rg = getWGRegion();
        BlockVector3 bv = BlockVector3.at(location.getX(), location.getY(), location.getZ());
        return rg != null && rg.contains(bv);
    }

    public ProtectedRegion getWGRegion() {
        RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer().get(new BukkitWorld(Bukkit.getWorld(world)));
        return rm == null ? null : rm.getRegion(region);
    }*/

    public String getName() {
        return name;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public int getMinPlayer() {
        return minPlayer;
    }

    public void setMinPlayer(int minPlayer) {
        this.minPlayer = minPlayer;
    }

    public int getMaxPlayer() {
        return maxPlayer;
    }

    public void setMaxPlayer(int maxPlayer) {
        this.maxPlayer = maxPlayer;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getTagcount() {
        return tagcount;
    }

    public void setTagcount(int tagcount) {
        this.tagcount = tagcount;
    }

    public Location getLobbyPoint() {
        return lobbyPoint;
    }

    public void setLobbyPoint(Location lobbyPoint) {
        this.lobbyPoint = lobbyPoint;
    }

    public Location getGamePoint() {
        return gamePoint;
    }

    public void setGamePoint(Location gamePoint) {
        this.gamePoint = gamePoint;
    }

    public List<Player> getAlive() {
        return alive;
    }

    public Player getPotato() {
        return potato;
    }

    public void setPotato(Player potato) {
        this.potato = potato;
    }

    public boolean isJoinable() {
        return joinable;
    }

    public void setJoinable(boolean joinable) {
        this.joinable = joinable;
    }

    public BossBar getBossBar() {
        return bossBar;
    }

    public void setBossBar(BossBar bossBar) {
        this.bossBar = bossBar;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public void setScoreboard(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    public BarStyle getBarStyle() {
        return barStyle;
    }

    public void setBarStyle(BarStyle barStyle) {
        this.barStyle = barStyle;
    }

    public BarColor getBarColor() {
        return barColor;
    }

    public void setBarColor(BarColor barColor) {
        this.barColor = barColor;
    }

    public int getCountdown() {
        return countdown;
    }

    public void setCountdown(int countdown) {
        this.countdown = countdown;
        this.countdownMax = countdown;
    }

    public double getPotatoTime() {
        return potatoTime;
    }

    public void setPotatoTime(double potatoTime) {
        this.potatoTime = potatoTime;
    }

    public double getReducePerTag() {
        return reducePerTag;
    }

    public void setReducePerTag(double reducePerTag) {
        this.reducePerTag = reducePerTag;
    }

    public int getMaxTags() {
        return maxTags;
    }

    public void setMaxTags(int maxTags) {
        this.maxTags = maxTags;
    }

    public double getSaveTime() {
        return saveTime;
    }

    public void setSaveTime(double saveTime) {
        this.saveTime = saveTime;
    }

    @Override
    public String toString() {
        return "Arena{" +
                "name='" + name + '\'' +
                ", potatoTime=" + potatoTime +
                ", reducePerTag=" + reducePerTag +
                ", world='" + world + '\'' +
                ", region='" + region + '\'' +
                ", minPlayer=" + minPlayer +
                ", maxPlayer=" + maxPlayer +
                ", active=" + active +
                ", lobbyPoint=" + lobbyPoint +
                ", gamePoint=" + gamePoint +
                ", barStyle=" + barStyle +
                ", barColor=" + barColor +
                ", countdown=" + countdown +
                ", maxTags=" + maxTags +
                ", saveTime=" + saveTime +
                ", running=" + running +
                ", started=" + started +
                ", tagcount=" + tagcount +
                ", joinable=" + joinable +
                '}';
    }
}
