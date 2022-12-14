package dev.tacker.hotpotato.listeners;

import dev.tacker.hotpotato.HotPotato;
import dev.tacker.hotpotato.models.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Arena arena = HotPotato.getInstance().getManager().getArena(event.getPlayer());
        if (arena == null)
            return;
        arena.leave(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Arena arena = HotPotato.getInstance().getManager().getArena(event.getPlayer());
        if (arena == null)
            return;
        arena.leave(event.getPlayer());
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Arena arena = HotPotato.getInstance().getManager().getArena(event.getPlayer());
        if (arena == null)
            return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Arena arena = HotPotato.getInstance().getManager().getArena((Player) event.getWhoClicked());
        if (arena == null)
            return;
        event.setCancelled(true);
    }

    @EventHandler
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
        if (arena_damaged.isSaved(damaged))
            return;

        event.setCancelled(true);
        if (damager != arena_damaged.getPotato())
            return;

        arena_damaged.tag(damager, damaged);
    }
}

