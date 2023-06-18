package dev.tacker.hotpotato.listeners;

import dev.tacker.hotpotato.HotPotato;
import dev.tacker.hotpotato.models.Arena;
import dev.tacker.hotpotato.utils.Locale;
import dev.tacker.hotpotato.utils.Permissions;
import dev.tacker.hotpotato.utils.Utils;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Arena arena = HotPotato.getInstance().getManager().getArena(event.getPlayer());
        if (arena == null)
            return;
        arena.leave(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Arena arena = HotPotato.getInstance().getManager().getArena(event.getPlayer());
        if (arena == null)
            return;
        event.setCancelled(true);
        arena.death(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemDrop(PlayerDropItemEvent event) {
        Arena arena = HotPotato.getInstance().getManager().getArena(event.getPlayer());
        if (arena == null)
            return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        Arena arena = HotPotato.getInstance().getManager().getArena((Player) event.getWhoClicked());
        if (arena == null)
            return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTag(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;
        if (!(event.getDamager() instanceof Player))
            return;
        Player damaged = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();
        Arena arena_damaged = HotPotato.getInstance().getManager().getArena(damaged);
        Arena arena_damager = HotPotato.getInstance().getManager().getArena(damager);
        if (arena_damaged == null)
            return;
        if (arena_damager == null)
            return;

        if (!arena_damaged.getName().equals(arena_damager.getName()))
            return;

        if (!arena_damaged.isRunning())
            return;

        event.setCancelled(true);

        if (arena_damaged.isSaved(damaged))
            return;

        if (damager != arena_damaged.getPotato())
            return;

        arena_damaged.tag(damager, damaged);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSignClick(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null)
            return;

        Block block = event.getClickedBlock();
        if (!(block.getState() instanceof WallSign) && !(block.getState() instanceof Sign))
            return;
        Sign sign = (Sign) block.getState();

        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
            return;

        if (sign.getPersistentDataContainer().isEmpty())
            return;

        String s = sign.getPersistentDataContainer().get(HotPotato.getInstance().key, PersistentDataType.STRING);
        Arena arena = HotPotato.getInstance().getManager().getArena(s);
        if (arena == null)
            return;

        event.setCancelled(true);

        if (!Permissions.USE.check(event.getPlayer())) {
            event.getPlayer().sendMessage(Locale.get(Locale.MessageKey.ERROR_NO_PERM));
            return;
        }

        arena.join(event.getPlayer());
    }

    @EventHandler
    public void onSighChange(SignChangeEvent event) {
        if (!Utils.legacy(event.line(0)).equalsIgnoreCase("[hotpotato]"))
            return;

        Player player = event.getPlayer();
        if (!Permissions.ADMIN.check(event.getPlayer())) {
            player.sendMessage(Locale.get(Locale.MessageKey.ERROR_NO_PERM));
            return;
        }

        String a = Utils.legacy(event.line(1));
        Arena arena = HotPotato.getInstance().getManager().getArena(a);
        if (arena == null) {
            player.sendMessage(Locale.get(Locale.MessageKey.ARENA_NOT_FOUND, a));
            return;
        }

        Sign sign = (Sign) event.getBlock().getState();
        sign.line(0, Utils.mm(HotPotato.getInstance().getPrefix()));
        sign.line(1, Locale.getNoPrefix(Locale.MessageKey.SIGN_ARENA, arena.getName()));
        sign.line(2, Locale.getNoPrefix(Locale.MessageKey.SIGN_LINE));
        sign.getPersistentDataContainer().set(HotPotato.getInstance().key, PersistentDataType.STRING, arena.getName());
        sign.update(true);
        player.sendMessage(Locale.get(Locale.MessageKey.ARENA_SIGN_ADD, arena.getName()));
    }
}

