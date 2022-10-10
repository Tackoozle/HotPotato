package dev.tacker.hotpotato.listeners;

import dev.tacker.hotpotato.HotPotato;
import dev.tacker.hotpotato.models.Arena;
import dev.tacker.hotpotato.utils.Permissions;
import dev.tacker.hotpotato.utils.Utils;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;

public class SignListener implements Listener {

    /*@EventHandler
    public void onSignCreate(SignChangeEvent event){
        if(event.getLine(0).equals("[HotPotato]")){
            Player player = event.getPlayer();
            if(player.hasPermission("hotpotato.admin")) {
                event.setLine(0, "§1[HotPotato]");
                event.setLine(1, "Click to join!");
                event.setLine(2, "Player: 0");
                HotPotato.getInstance().getGame().setSignLocation(event.getBlock());
            } else
                player.sendMessage(HotPotato.getInstance().noPerm);
        }
    }

    @EventHandler
    public void onSignBreak(BlockBreakEvent event){
        if(!(event.getBlock().getState() instanceof Sign)){
            return;
        }
        if(!event.getPlayer().hasPermission("hotpotato.admin")){
            return;
        }
        Sign sign = (Sign) event.getBlock().getState();
        if(sign.getLine(0).equals("§1[HotPotato]")){
            FileConfiguration config = HotPotato.getInstance().getConfig();
            config.set("Sign.World", null);
            config.set("Sign.X", null);
            config.set("Sign.Y", null);
            config.set("Sign.Z", null);
            HotPotato.getInstance().saveConfig();
        }
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }
        if (!(event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            return;
        }
        if (!(event.getClickedBlock().getState() instanceof Sign)) {
            return;
        }
        Sign sign = (Sign) event.getClickedBlock().getState();
        if (sign.getLine(0).equals("§1[HotPotato]")) {
            if (event.getPlayer().hasPermission("hotpotato.use")) {
                game.joinGame(event.getPlayer());
            } else
                event.getPlayer().sendMessage(HotPotato.getInstance().getNoPerm());
        }
    }
*/
    @EventHandler
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

        if (!Permissions.USE.check(event.getPlayer())) {
            event.getPlayer().sendMessage(Utils.mm(HotPotato.getInstance().getPrefix() + "<red>You dont have permission to do this!"));
            return;
        }

        arena.join(event.getPlayer());
    }
}
