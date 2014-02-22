package me.superckl.factionalert;

import lombok.AllArgsConstructor;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.struct.Relation;

@AllArgsConstructor
public class FactionListeners implements Listener{

	private final SimpleAlertGroup teleport;
	private final SimpleAlertGroup move;
	private final FactionSpecificAlertGroup death;

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTeleport(final PlayerTeleportEvent e){
		if(!this.teleport.isEnabled())
			return;
		if(e.getPlayer().hasPermission("factionalert.noalert.teleport"))
  			return;
		final Faction faction = Board.getFactionAt(new FLocation(e.getTo()));
		if(!FactionListeners.isValid(faction))
			return;
		final Faction oFaction = FPlayers.i.get(e.getPlayer()).getFaction();
		if(!FactionListeners.isValid(oFaction))
			return;
		final Relation relation = faction.getRelationTo(oFaction);
		if(!this.teleport.getTypes().contains(relation))
			return;
		for(final FPlayer player:faction.getFPlayersWhereOnline(true)){
			player.sendMessage(this.teleport.getAlert(relation).replaceAll("%n", e.getPlayer().getName()).replaceAll("%f", oFaction.getTag()));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerMove(final PlayerMoveEvent e){
		if(!this.move.isEnabled() || e instanceof PlayerTeleportEvent)
			return;
		if(e.getPlayer().hasPermission("factionalert.noalert.move"))
  			return;
		final Faction faction = Board.getFactionAt(new FLocation(e.getTo()));
		if(Board.getFactionAt(new FLocation(e.getFrom())).getId().equals(faction.getId()))
			return;
		if(!FactionListeners.isValid(faction))
			return;
		final Faction oFaction = FPlayers.i.get(e.getPlayer()).getFaction();
		if(!FactionListeners.isValid(oFaction))
			return;
		final Relation relation = faction.getRelationTo(oFaction);
		if(!this.move.getTypes().contains(relation))
			return;
		for(final FPlayer player:faction.getFPlayersWhereOnline(true)){
			player.sendMessage(this.move.getAlert(relation).replaceAll("%n", e.getPlayer().getName()).replaceAll("%f", oFaction.getTag()));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDeath(final PlayerDeathEvent e){
		if(!this.death.isEnabled())
			return;
		if(e.getEntity().hasPermission("factionalert.noalert.death"))
  			return;
		final Faction faction = FPlayers.i.get(e.getEntity()).getFaction();
		if(!FactionListeners.isValid(faction))
			return;
		for(final FPlayer player:faction.getFPlayersWhereOnline(true)){
			player.sendMessage(this.death.getAlert().replaceAll("%n", e.getEntity().getName()).replaceAll("%f", faction.getTag()));
		}
	}

	public static boolean isValid(final Faction faction){
		return (faction != null) && !faction.isNone() && !faction.isSafeZone() && !faction.isWarZone();
	}

}
