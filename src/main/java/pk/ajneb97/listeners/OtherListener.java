package pk.ajneb97.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class OtherListener implements Listener {

    @EventHandler
    public void fireworkDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if(event.getEntity() instanceof Player && damager.getType().name().contains("FIREWORK")) {
            if(damager.hasMetadata("playerkits")) {
                event.setCancelled(true);
            }
        }
    }
}
