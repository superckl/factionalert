package me.superckl.factionalert;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.BoardColls;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.UConf;
import com.massivecraft.factions.entity.UPlayer;
import com.massivecraft.mcore.ps.PS;

public class FactionListeners implements Listener{

	private final AlertGroup teleport;
	private final AlertGroup move;
	//private final AlertGroup disband;
	
	public FactionListeners(AlertGroup teleport, AlertGroup move){
		this.teleport = teleport;
		this.move = move;
		//this.disband = disband;
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTeleport(PlayerTeleportEvent e){
		if(!this.teleport.isEnabled())
			return;
		Faction faction = BoardColls.get().getFactionAt(PS.valueOf(e.getTo()));
		if(!this.isValid(faction))
			return;
		Faction oFaction = UPlayer.get(e.getPlayer()).getFaction();
		if(!this.isValid(oFaction))
			return;
		Rel relation = faction.getRelationTo(oFaction);
		if(!this.teleport.getTypes().contains(relation))
			return;
		for(UPlayer player:faction.getUPlayersWhereOnline(true)){
			Rel rel = player.getRelationTo(faction);
			if(this.teleport.getReceivers().contains(rel))
				player.sendMessage(this.teleport.getAlert(relation).replaceAll("%n", e.getPlayer().getName()).replaceAll("%f", oFaction.getName()));
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent e){
		if(!this.move.isEnabled())
			return;
		Faction faction = BoardColls.get().getFactionAt(PS.valueOf(e.getTo()));
		if(BoardColls.get().getFactionAt(PS.valueOf(e.getFrom())).getId().equals(faction.getId()))
			return;
		if(!this.isValid(faction))
			return;
		Faction oFaction = UPlayer.get(e.getPlayer()).getFaction();
		if(!this.isValid(oFaction))
			return;
		Rel relation = faction.getRelationTo(oFaction);
		if(!this.move.getTypes().contains(relation))
			return;
		for(UPlayer player:faction.getUPlayersWhereOnline(true)){
			Rel rel = player.getRelationTo(faction);
			if(this.move.getReceivers().contains(rel))
				player.sendMessage(this.move.getAlert(relation).replaceAll("%n", e.getPlayer().getName()).replaceAll("%f", oFaction.getName()));
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
	
	private boolean isValid(Faction faction){
		return !faction.isNone() && !faction.getId().equals(UConf.get(faction).factionIdSafezone) && !faction.getId().equals(UConf.get(faction).factionIdWarzone);
	}
	
}
