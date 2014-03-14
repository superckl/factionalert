package me.superckl.factionalert;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import me.superckl.factionalert.groups.AlertGroup;
import me.superckl.factionalert.groups.FactionSpecificAlertGroup;
import me.superckl.factionalert.groups.SimpleAlertGroup;

import org.bukkit.configuration.file.YamlConfiguration;
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

	@Getter
	private final SimpleAlertGroup teleport;
	@Getter
	private final SimpleAlertGroup move;
	@Getter
	private final FactionSpecificAlertGroup death;
	//private final AlertGroup disband;

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTeleport(final PlayerTeleportEvent e){
		if(!this.teleport.isEnabled())
			return;
		if(e.getPlayer().hasPermission("factionalert.noalert.teleport"))
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
		if(!this.teleport.cooldown(e.getPlayer().getName()))
			return;
		for(final UPlayer player:faction.getUPlayersWhereOnline(true)){
			final Rel rel = player.getRelationTo(faction);
			if(this.teleport.getReceivers().contains(rel) && !this.teleport.getExcludes().contains(player.getName()))
				player.sendMessage(this.teleport.getAlert(relation).replaceAll("%n", e.getPlayer().getName()).replaceAll("%f", oFaction.getName()));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerMove(final PlayerMoveEvent e){
		if(!this.move.isEnabled() || (e instanceof PlayerTeleportEvent))
			return;
		if(e.getPlayer().hasPermission("factionalert.noalert.move"))
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
		if(!this.move.cooldown(e.getPlayer().getName()))
			return;
		for(final UPlayer player:faction.getUPlayersWhereOnline(true)){
			final Rel rel = player.getRelationTo(faction);
			if(this.move.getReceivers().contains(rel) && !this.move.getExcludes().contains(player.getName()))
				player.sendMessage(this.move.getAlert(relation).replaceAll("%n", e.getPlayer().getName()).replaceAll("%f", oFaction.getName()));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDeath(final PlayerDeathEvent e){
		if(!this.death.isEnabled())
			return;
		if(e.getEntity().hasPermission("factionalert.noalert.death"))
			return;
		final Faction faction = UPlayer.get(e.getEntity()).getFaction();
		if(!FactionListeners.isValid(faction))
			return;
		if(!this.death.cooldown(e.getEntity().getName()))
			return;
		for(final UPlayer player:faction.getUPlayersWhereOnline(true)){
			if(player.getName().equals(e.getEntity().getName()))
				continue;
			final Rel relation = player.getRelationTo(faction);
			if(this.death.getReceivers().contains(relation) && !this.death.getExcludes().contains(player.getName()))
				player.sendMessage(this.death.getAlert(relation).replaceAll("%n", e.getEntity().getName()).replaceAll("%f", faction.getName()));
		}
	}

	public void saveExcludes(@NonNull final File toSave) throws IOException{
		if(!toSave.exists())
			toSave.createNewFile();
		final YamlConfiguration config = new YamlConfiguration();
		config.set("death", new ArrayList<String>(this.death.getExcludes()));
		config.set("move", new ArrayList<String>(this.move.getExcludes()));
		config.set("teleport", new ArrayList<String>(this.teleport.getExcludes()));
		config.save(toSave);
	}

	public AlertGroup[] getAlertGroups(){
		return new AlertGroup[] {this.teleport, this.move, this.death};
	}

	public static boolean isValid(@NonNull final Faction faction){
		return (faction != null) && !faction.isNone() && !faction.getId().equals(UConf.get(faction).factionIdSafezone) && !faction.getId().equals(UConf.get(faction).factionIdWarzone);
	}

}
