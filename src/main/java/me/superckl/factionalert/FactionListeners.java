package me.superckl.factionalert;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;
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

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.struct.Relation;

@ExtensionMethod({Utilities.class})
@AllArgsConstructor
public class FactionListeners implements Listener{

	@Getter
	private final SimpleAlertGroup teleport;
	@Getter
	private final SimpleAlertGroup move;
	@Getter
	private final FactionSpecificAlertGroup death;

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTeleport(final PlayerTeleportEvent e){
		if(!this.teleport.isEnabled())
			return;
		if(e.getPlayer().hasPermission("factionalert.noalert.teleport"))
			return;
		final Faction faction = Board.getFactionAt(new FLocation(e.getTo()));
		if(!faction.isValid())
			return;
		final Faction oFaction = FPlayers.i.get(e.getPlayer()).getFaction();
		if(!oFaction.isValid())
			return;
		final Relation relation = faction.getRelationTo(oFaction);
		if(!this.teleport.getTypes().contains(relation))
			return;
		if(!this.teleport.cooldown(e.getPlayer().getName()))
			return;
		for(final FPlayer player:faction.getFPlayersWhereOnline(true))
			if(!this.teleport.getExcludes().contains(player.getName()))
				player.sendMessage(this.teleport.getAlert(relation).replaceAll("%n", e.getPlayer().getName()).replaceAll("%f", oFaction.getTag()));
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerMove(final PlayerMoveEvent e){
		if(!this.move.isEnabled() || (e instanceof PlayerTeleportEvent))
			return;
		if(e.getPlayer().hasPermission("factionalert.noalert.move"))
			return;
		final Faction faction = Board.getFactionAt(new FLocation(e.getTo()));
		if(Board.getFactionAt(new FLocation(e.getFrom())).getId().equals(faction.getId()))
			return;
		if(!faction.isValid())
			return;
		final Faction oFaction = FPlayers.i.get(e.getPlayer()).getFaction();
		if(!oFaction.isValid())
			return;
		final Relation relation = faction.getRelationTo(oFaction);
		if(!this.move.getTypes().contains(relation))
			return;
		if(!this.move.cooldown(e.getPlayer().getName()))
			return;
		for(final FPlayer player:faction.getFPlayersWhereOnline(true))
			if(!this.move.getExcludes().contains(player.getName()))
				player.sendMessage(this.move.getAlert(relation).replaceAll("%n", e.getPlayer().getName()).replaceAll("%f", oFaction.getTag()));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDeath(final PlayerDeathEvent e){
		if(!this.death.isEnabled())
			return;
		if(e.getEntity().hasPermission("factionalert.noalert.death"))
			return;
		final Faction faction = FPlayers.i.get(e.getEntity()).getFaction();
		if(!faction.isValid())
			return;
		if(!this.death.cooldown(e.getEntity().getName()))
			return;
		for(final FPlayer player:faction.getFPlayersWhereOnline(true))
			if(!this.death.getExcludes().contains(player.getName()))
				player.sendMessage(this.death.getAlert().replaceAll("%n", e.getEntity().getName()).replaceAll("%f", faction.getTag()));
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

}
