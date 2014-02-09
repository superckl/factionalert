package me.superckl.factionalert;

import lombok.AllArgsConstructor;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.BoardColls;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.UConf;
import com.massivecraft.factions.entity.UPlayer;
import com.massivecraft.mcore.ps.PS;

@AllArgsConstructor
public class FactionListeners implements Listener{

	private final SimpleAlertGroup teleport;
	private final SimpleAlertGroup move;
	private final FactionSpecificAlertGroup death;
	//private final AlertGroup disband;

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTeleport(final PlayerTeleportEvent e){
		if(!this.teleport.isEnabled())
			return;
		final Faction faction = BoardColls.get().getFactionAt(PS.valueOf(e.getTo()));
		if(!FactionListeners.isValid(faction))
			return;
		final Faction oFaction = UPlayer.get(e.getPlayer()).getFaction();
		if(!FactionListeners.isValid(oFaction))
			return;
		final Rel relation = faction.getRelationTo(oFaction);
		if(!this.teleport.getTypes().contains(relation))
			return;
		for(final UPlayer player:faction.getUPlayersWhereOnline(true)){
			final Rel rel = player.getRelationTo(faction);
			if(this.teleport.getReceivers().contains(rel))
				player.sendMessage(this.teleport.getAlert(relation).replaceAll("%n", e.getPlayer().getName()).replaceAll("%f", oFaction.getName()));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerMove(final PlayerMoveEvent e){
		if(!this.move.isEnabled() || e instanceof PlayerTeleportEvent)
			return;
		final Faction faction = BoardColls.get().getFactionAt(PS.valueOf(e.getTo()));
		if(BoardColls.get().getFactionAt(PS.valueOf(e.getFrom())).getId().equals(faction.getId()))
			return;
		if(!FactionListeners.isValid(faction))
			return;
		final Faction oFaction = UPlayer.get(e.getPlayer()).getFaction();
		if(!FactionListeners.isValid(oFaction))
			return;
		final Rel relation = faction.getRelationTo(oFaction);
		if(!this.move.getTypes().contains(relation))
			return;
		for(final UPlayer player:faction.getUPlayersWhereOnline(true)){
			final Rel rel = player.getRelationTo(faction);
			if(this.move.getReceivers().contains(rel))
				player.sendMessage(this.move.getAlert(relation).replaceAll("%n", e.getPlayer().getName()).replaceAll("%f", oFaction.getName()));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDeath(final PlayerDeathEvent e){
		if(!this.death.isEnabled())
			return;
		final Faction faction = UPlayer.get(e.getEntity()).getFaction();
		if(!FactionListeners.isValid(faction))
			return;
		for(final UPlayer player:faction.getUPlayersWhereOnline(true)){
			final Rel relation = player.getRelationTo(faction);
			if(this.death.getReceivers().contains(relation))
				player.sendMessage(this.death.getAlert(relation).replaceAll("%n", e.getEntity().getName()).replaceAll("%f", faction.getName()));
		}
	}

	/*	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onFactionDisband(FactionsEventDisband e){
		if(!this.disband.isEnabled())
			return;
		Faction faction = e.getFaction();
		if(!this.isValid(faction))
			return;
		List<Player> defensiveCopy = new ArrayList<Player>(Arrays.asList(Bukkit.getOnlinePlayers()));
		ListIterator<Player> it = defensiveCopy.listIterator();
		while(it.hasNext()){
			Faction oFaction = UPlayer.get(it.next()).getFaction();
			if(!this.isValid(oFaction))
				return;
			Rel relation = faction.getRelationTo(oFaction);
			if(!this.disband.getTypes().contains(relation))
				return;
			for(UPlayer player:oFaction.getUPlayersWhereOnline(true)){
				Rel rel = player.getRelationTo(oFaction);
				if(this.disband.getReceivers().contains(rel))
					player.sendMessage(this.disband.getAlert(relation));
				defensiveCopy.remove(player.getPlayer());
			}
		}
	}*/

	public static boolean isValid(final Faction faction){
		return (faction != null) && !faction.isNone() && !faction.getId().equals(UConf.get(faction).factionIdSafezone) && !faction.getId().equals(UConf.get(faction).factionIdWarzone);
	}

}
