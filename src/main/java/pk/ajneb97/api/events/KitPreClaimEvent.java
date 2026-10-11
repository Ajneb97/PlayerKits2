package pk.ajneb97.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import pk.ajneb97.model.Kit;

public class KitPreClaimEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private Kit kit;
    private boolean cancelled;
    private boolean isFromCommand;
    private String cancelReason;

    public KitPreClaimEvent(Player player, Kit kit, boolean isFromCommand) {
        this.player = player;
        this.kit = kit;
        this.cancelled = false;
        this.isFromCommand = isFromCommand;
        this.cancelReason = null;
    }

    public Player getPlayer() {
        return player;
    }

    public Kit getKit() {
        return kit;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isFromCommand() {
        return this.isFromCommand;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}