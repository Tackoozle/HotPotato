package dev.tacker.hotpotato.models;

import dev.tacker.hotpotato.HotPotato;
import dev.tacker.hotpotato.utils.Locale;
import dev.tacker.hotpotato.utils.Logging;
import dev.tacker.hotpotato.utils.Utils;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
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
    private final Logging log = HotPotato.getInstance().getLogging();

    //loaded stuff from .yml
    private final String name;
    private double potatoTime;
    private double reducePerTag;
    private double saveTime;
    private int minPlayer;
    private int maxPlayer;
    private boolean active;
    private Location lobbyPoint;
    private Location gamePoint;
    private BarStyle barStyle;
    private BarColor barColor;
    private int countdown;
    private int maxTags;
    private Sound tagSound;

    //temporary vars
    private final List<Player> alive = new LinkedList<>();
    private final List<Player> dead = new ArrayList<>();
    private final Set<Player> saved = new HashSet<>();
    private boolean running;
    private boolean started;
    private int tagCount;
    private Player potato;
    private boolean joinable;
    private int potatoTask;
    private int countdownTask;
    private BossBar bossBar;
    private Team team;
    private Scoreboard scoreboard;

    public Arena(String name, int minPlayer, int maxPlayer, boolean active,
                 Location lobbyPoint, Location gamePoint, BarStyle barStyle, BarColor barColor,
                 double potatoTime, double reducePerTag, int countdown, int maxTags, double saveTime,
                 Sound tagSound) {
        this.name = name;
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
        this.maxTags = maxTags;
        this.saveTime = saveTime;
        this.tagSound = tagSound;
        this.joinable = true;
    }

    /**
     * returns new Arena object from a file
     */
    public static Arena fromFile(File file) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        String name = yaml.getString("name");
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
        Sound tagSound = Sound.valueOf(yaml.getString("tagSound"));
        return new Arena(name, minPlayer, maxPlayer, active,
            lobbyPoint, gamePoint, barStyle, barColor, potatoTime,
            reducePerTag, countdown, maxTags, saveTime, tagSound);
    }

    /**
     * stops arena, removes all player without getting a winner
     */
    public void stop() {
        removeOldPotato();
        alive.forEach(p -> {
            preparePlayer(p);
            p.teleport(lobbyPoint);
            p.sendMessage(Locale.get(Locale.MessageKey.ARENA_STOPPED));
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
    int timer;
    private void startGame() {
        alive.forEach(p -> p.teleport(getGamePoint()));
        started = true;
        timer = countdown;
        this.countdownTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(HotPotato.getInstance(), () -> {
            for (Player p : alive) {
                p.showTitle(Title.title(Locale.getNoPrefix(Locale.MessageKey.TITLE_COUNTDOWN),
                    Locale.getNoPrefix(Locale.MessageKey.TITLE_COUNTDOWN_SUB, String.valueOf(timer)),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(1000), Duration.ofMillis(500))));
            }
            timer--;
            if (timer <= 3) {
                joinable = false;
            }
            if (timer < 0) {
                Bukkit.getScheduler().cancelTask(countdownTask);
                if (!checkPlayer()) {
                    end();
                    return;
                }
                running = true;
                broadcast(Locale.MessageKey.ARENA_STARTED);
                prepareArena();
                setNewPotato(pickRandomPlayer());
                for (Player p : alive) {
                    bossBar.addPlayer(p);
                    team.addEntry(p.getName());
                    p.showTitle(Title.title(Locale.getNoPrefix(Locale.MessageKey.TITLE_STARTED, potato.getName()),
                        Locale.getNoPrefix(Locale.MessageKey.TITLE_STARTED_SUB, potato.getName()),
                        Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(1000), Duration.ofMillis(500))));
                }
            }
        }, 0, 20);
    }

    /**
     * starts the timer for the potato. restarts when there is a new potato.
     */
    private void potatoTimer() {
        Bukkit.getScheduler().cancelTask(potatoTask);
        this.potatoTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(HotPotato.getInstance(), new Runnable() {
            final double timeAfterTags = tagCount * reducePerTag;
            final double time = 1.0 / ((potatoTime - timeAfterTags) * 20);
            double progress = 1.0;

            @Override
            public void run() {
                bossBar.setTitle(ChatColor.GOLD + getPotato().getName() + ChatColor.WHITE + " got the potato!");
                if (progress > 1.0) {
                    log.error("Progress of arena " + getName() + " is not in range: " + progress);
                    log.error("timeAfterTags: " + timeAfterTags + ", tagCount: " + tagCount + ", reducePerTag: " + reducePerTag);
                    log.error("time: " + time);
                    progress = 1;
                }
                if (progress < 0.0) {
                    log.error("Progress of arena " + getName() + " is not in range: " + progress);
                    log.error("timeAfterTags: " + timeAfterTags + ", tagCount: " + tagCount + ", reducePerTag: " + reducePerTag);
                    log.error("time: " + time);
                    progress = 0;
                }
                bossBar.setProgress(progress);
                for (Player p : alive) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1, false, false, false));
                }
                progress = progress - time;
                if (progress <= 0) {
                    tagCount = 1;
                    if (checkPlayer()) {
                        leave(potato);
                    } else {
                        end();
                    }
                }
            }
        }, 0, 1);
    }

    /**
     * removes old potato
     */
    private void removeOldPotato() {
        Player p = getPotato();
        if (p == null)
            return;
        p.setGlowing(false);
        p.getInventory().clear();
    }

    /**
     * sets player as the new potatop
     * @param p the player
     */
    private void setNewPotato(Player p) {
        removeOldPotato();
        setPotato(p);
        preparePotato();
        broadcast(Locale.MessageKey.ARENA_POTATO, p.getName());
        p.sendMessage(Locale.get(Locale.MessageKey.ARENA_POTATO_INFO));
        for (Player players : alive) {
            players.playSound(players.getLocation(), tagSound, 1, 1);
        }
        potatoTimer();
    }

    /**
     * broadcast msg to all alive player
     */
    private void broadcast(Locale.MessageKey key, Object... args) {
        alive.forEach(p -> p.sendMessage(Locale.get(key, args)));
    }

    /**
     * broadcast msg to all dead player
     */
    private void broadcastDead(Locale.MessageKey key, Object... args) {
        dead.forEach(p -> p.sendMessage(Locale.get(key, args)));
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
            broadcast(Locale.MessageKey.ARENA_WON, p.getName(), name);
            broadcastDead(Locale.MessageKey.ARENA_WON, p.getName(), name);
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
        tagCount = 1;
        started = false;
        unprepareArena();
    }

    /**
     * removes player from arena
     * @param player the player
     */
    public void leave(Player player) {
        if (!alive.contains(player)) {
            player.sendMessage(Locale.get(Locale.MessageKey.PLAYER_NO_ARENA));
            return;
        }
        if (isPotato(player)) {
            player.sendMessage(Locale.get(Locale.MessageKey.ARENA_POTATOED, player.getName()));
        } else {
            player.sendMessage(Locale.get(Locale.MessageKey.ARENA_LEFT, player.getName()));
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
        if (isPotato(player)) {
            broadcast(Locale.MessageKey.ARENA_NEW_POTATO);
            setNewPotato(pickRandomPlayer());
        }
    }

    /**
     * adds player to the arena
     * @param player the player
     */
    public void join(Player player) {
        if (!active) {
            player.sendMessage(Locale.get(Locale.MessageKey.ARENA_NOT_ACTIVE, name));
            return;
        }
        if (!joinable) {
            player.sendMessage(Locale.get(Locale.MessageKey.ARENA_NOT_JOINABLE, name));
            return;
        }
        if (alive.size() >= maxPlayer) {
            player.sendMessage(Locale.get(Locale.MessageKey.ARENA_LIMIT_REACHED, name));
            return;
        }
        if (alive.contains(player)) {
            player.sendMessage(Locale.get(Locale.MessageKey.ARENA_ALREADY_PLAYING, name));
            return;
        }
        alive.add(player);
        preparePlayer(player);
        player.teleport(gamePoint);
        broadcast(Locale.MessageKey.ARENA_JOINED, player.getName());
        if (alive.size() >= minPlayer && !started) {
            startGame();
        }
    }

    /**
     * called when a player is tagged
     */
    public void tag(Player damager, Player damaged) {
        addSaved(damager);
        if (tagCount < maxTags)
            tagCount++;
        setNewPotato(damaged);
    }

    /**
     * called when a player died in the arena
     */
    public void death(Player player) {
        preparePlayer(player);
        preparePotato();
        addSaved(player);
        player.teleport(gamePoint);
        player.sendMessage(Locale.get(Locale.MessageKey.ARENA_DEATH));
    }

    /**
     * tags the player as saved
     * @param player the player
     */
    private void addSaved(Player player) {
        if (saveTime == 0)
            return;
        saved.add(player);
        Bukkit.getScheduler().runTaskLater(HotPotato.getInstance(), () -> saved.remove(player), (long) (saveTime * 20));
    }

    /**
     * checks if player is still saved
     * @param player the player
     * @return true, if player cant be tagged due to savetime
     */
    public boolean isSaved(Player player) {
        return saved.contains(player);
    }

    /**
     * checks if player is the potato
     * @param player the player
     * @return true, if player is the potato
     */
    public boolean isPotato(Player player) {
        if (potato == null)
            return false;
        return potato == player;
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
        if (team == null)
            team = scoreboard.registerNewTeam("hotpotato_" + name);
        team.color(NamedTextColor.GOLD);
    }

    /**
     * removes team and bossbar from arena
     */
    private void unprepareArena() {
        try {
            team.unregister();
        } catch (Exception ignored) {
        }
        if (bossBar != null)
            bossBar.removeAll();
        bossBar = null;
    }

    /**
     * removes the player from the arena bossbar
     * @param player the player
     */
    private void removePlayerFromBossBar(Player player) {
        if (bossBar != null && bossBar.getPlayers().contains(player))
            bossBar.removePlayer(player);
    }

    /**
     * removes the player from the arena team
     * @param player the player
     */
    private void removePlayerFromTeam(Player player) {
        if (team == null)
            return;
        if (scoreboard.getTeam("hotpotato_" + name) == null)
            return;
        if (team.hasEntry(player.getName()))
            team.removeEntry(player.getName());
    }

    /**
     * prepares the potato
     */
    private void preparePotato() {
        if (potato == null)
            return;
        potato.setGlowing(true);
        PlayerInventory inv = potato.getInventory();
        inv.clear();
        inv.setHelmet(Utils.getPotato());
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, Utils.getPotato());
        }
    }

    /**
     * heals, feeds, .. the player
     * @param p the player
     */
    private void preparePlayer(Player p) {
        p.setHealth(20);
        p.setSaturation(20);
        p.setFoodLevel(20);
        p.setGlowing(false);
        p.setExp(0f);
        p.setLevel(0);
        p.setGameMode(GameMode.SURVIVAL);
        p.getInventory().clear();
    }

    /**
     * checks if all things of arena is okay
     */
    public boolean validate(CommandSender sender) {
        boolean ok = true;
        if (name == null) {
            sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_VALIDATION, "name"));
            ok = false;
        }
        if (gamePoint == null) {
            sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_VALIDATION,"gamePoint"));
            ok = false;
        }
        if (lobbyPoint == null) {
            sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_VALIDATION,"lobbyPoint"));
            ok = false;
        }
        if (maxPlayer == 0) {
            sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_VALIDATION,"maxPlayer"));
            ok = false;
        }
        if (minPlayer == 0) {
            sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_VALIDATION,"minPlayer"));
            ok = false;
        }
        if (minPlayer > maxPlayer) {
            sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_VALIDATION,"minPlayer > maxPlayer"));
            ok = false;
        }
        if (countdown <= 0) {
            sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_VALIDATION,"countDown <= 0"));
            ok = false;
        }
        if (reducePerTag <= 0) {
            sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_VALIDATION,"reducePerTag <=0"));
            ok = false;
        }
        if (potatoTime <= 0) {
            sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_VALIDATION,"potatoTime <=0"));
            ok = false;
        }
        if ((maxTags * reducePerTag) > potatoTime) {
            sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_VALIDATION,"maxTags * reducePerTag > potatoTime!"));
            ok = false;
        }
        if (tagSound == null) {
            sender.sendMessage(Locale.get(Locale.MessageKey.ERROR_VALIDATION,"tagSound"));
            ok = false;
        }
        return ok;
    }

    /**
     * saves a arena to .yml file
     * @return true if saved, false if error occured or not valid
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

    /**
     * saves the config section
     * @param y the section
     */
    private void save(ConfigurationSection y) {
        y.set("name", name);
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
        y.set("tagSound", tagSound.name());
    }

    public String getName() {
        return name;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
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

    public int getTagCount() {
        return tagCount;
    }

    public void setTagCount(int tagCount) {
        this.tagCount = tagCount;
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

    public Sound getTagSound() {
        return tagSound;
    }

    public void setTagSound(Sound tagSound) {
        this.tagSound = tagSound;
    }

    @Override
    public String toString() {
        return "Arena{" +
            "name='" + name + '\'' +
            ", potatoTime=" + potatoTime +
            ", reducePerTag=" + reducePerTag +
            ", saveTime=" + saveTime +
            ", minPlayer=" + minPlayer +
            ", maxPlayer=" + maxPlayer +
            ", active=" + active +
            ", lobbyPoint=" + lobbyPoint +
            ", gamePoint=" + gamePoint +
            ", barStyle=" + barStyle +
            ", barColor=" + barColor +
            ", countdown=" + countdown +
            ", maxTags=" + maxTags +
            ", tagSound=" + tagSound +
            ", started=" + started +
            ", tagCount=" + tagCount +
            ", timer=" + timer +
            ", joinable=" + joinable +
            '}';
    }
}
