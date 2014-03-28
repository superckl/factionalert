package me.superckl.factionalert.listeners;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import me.superckl.factionalert.AlertType;
import me.superckl.factionalert.events.DispatchSimpleAlertEvent;
import me.superckl.factionalert.groups.AlertGroup;
import me.superckl.factionalert.groups.FactionSpecificAlertGroup;
import me.superckl.factionalert.groups.SimpleAlertGroup;
import me.superckl.factionalert.utils.Utilities;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.BoardColls;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.UPlayer;
import com.massivecraft.mcore.ps.PS;

@ExtensionMethod({Utilities.class})
@RequiredArgsConstructor
public class FactionListeners implements Listener{

	@Getter
	private final SimpleAlertGroup teleport;
	@Getter
	private final SimpleAlertGroup move;
	@Getter
	private final SimpleAlertGroup combat;
	@Getter
	private final FactionSpecificAlertGroup death;

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTeleport(final PlayerTeleportEvent e){
		if(!this.teleport.isEnabled())
			return;
		if(e.getPlayer().hasPermission("factionalert.noalert.teleport"))
			return;
		final Faction faction = BoardColls.get().getFactionAt(PS.valueOf(e.getTo()));
		if(!faction.isValid())
			return;
		if(BoardColls.get().getFactionAt(PS.valueOf(e.getFrom())).getId().equals(faction.getId()))
			return;
		final Faction oFaction = UPlayer.get(e.getPlayer()).getFaction();
		Rel relation = null;
		if(oFaction.isValid())
			relation = faction.getRelationTo(oFaction);
		if(!this.teleport.getTypes().contains(relation))
			return;
		if(!this.teleport.cooldown(e.getPlayer().getName(), false))
			return;
		final DispatchSimpleAlertEvent dispatch = new DispatchSimpleAlertEvent(faction, this.teleport, AlertType.TELEPORT, this.teleport.getAlert(relation), Collections.unmodifiableList(Arrays.asList(e.getPlayer()))).dispatch();
		if(dispatch.isCancelled())
			return;
		String alert = dispatch.getAlert().replaceAll("%n", e.getPlayer().getName());
		alert = alert.replaceAll("%f", oFaction.isValid() ? oFaction.getName():"no faction");
		faction.alert(this.teleport, alert, dispatch.getPlayersInvolved().toNames());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerMove(final PlayerMoveEvent e){
		if(!this.move.isEnabled() || (e instanceof PlayerTeleportEvent))
			return;
		if(e.getPlayer().hasPermission("factionalert.noalert.move"))
			return;
		final Faction faction = BoardColls.get().getFactionAt(PS.valueOf(e.getTo()));
		if(!faction.isValid())
			return;
		final Faction oFaction = UPlayer.get(e.getPlayer()).getFaction();
		if(BoardColls.get().getFactionAt(PS.valueOf(e.getFrom())).getId().equals(faction.getId()))
			return;
		Rel relation = null;
		if(oFaction.isValid())
			relation = faction.getRelationTo(oFaction);
		if(!this.move.getTypes().contains(relation))
			return;
		if(!this.move.cooldown(e.getPlayer().getName(), false))
			return;
		final DispatchSimpleAlertEvent dispatch = new DispatchSimpleAlertEvent(faction, this.move, AlertType.MOVE, this.move.getAlert(relation), Collections.unmodifiableList(Arrays.asList(e.getPlayer()))).dispatch();
		if(dispatch.isCancelled())
			return;
		String alert = dispatch.getAlert().replaceAll("%n", e.getPlayer().getName());
		alert = alert.replaceAll("%f", oFaction.isValid() ? oFaction.getName():"no faction");
		faction.alert(this.move, alert, dispatch.getPlayersInvolved().toNames());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDeath(final PlayerDeathEvent e){
		if(!this.death.isEnabled())
			return;
		if(e.getEntity().hasPermission("factionalert.noalert.death"))
			return;
		final Faction faction = UPlayer.get(e.getEntity()).getFaction();
		if(!faction.isValid())
			return;
		if(!this.death.cooldown(e.getEntity().getName(), false))
			return;
		for(final UPlayer player:faction.getUPlayersWhereOnline(true)){
			if(player.getName().equals(e.getEntity().getName()))
				continue;
			final Rel relation = player.getRelationTo(faction);
			if(this.death.getReceivers().contains(relation) && !this.death.getExcludes().contains(player.getName()))
				player.sendMessage(this.death.getAlert(relation).replaceAll("%n", e.getEntity().getName()).replaceAll("%f", faction.getName()));
		}
	}

	//TODO doesn't work
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerAttack(final EntityDamageByEntityEvent e){
		if(!this.combat.isEnabled())
			return;
		if(((e.getDamager() instanceof Player) == false) || ((e.getEntity() instanceof Player) == false))
			return;
		final UPlayer damager = UPlayer.get(e.getDamager());
		final UPlayer damaged = UPlayer.get(e.getEntity());
		if(damager.getPlayer().hasPermission("factionalert.noalert.combat") || damaged.getPlayer().hasPermission("factionalert.noalert.combat"))
			return;
		Rel rel = null;
		if(damager.getFaction().isValid() && damaged.getFaction().isValid())
			rel = damager.getRelationTo(damaged);
		//damager alert

		final DispatchSimpleAlertEvent dispatch = new DispatchSimpleAlertEvent(damager.getFaction(), this.combat, AlertType.COMBAT, this.combat.getAlert(rel),
				Collections.unmodifiableList(Arrays.asList(damager.getPlayer(), damaged.getPlayer()))).dispatch();
		if(dispatch.isCancelled())
			return;
		String alert = "penis";
		if(damager.getFaction().isValid() && this.combat.cooldown(damager.getName(), true)){
			alert = dispatch.getAlert().replaceAll("%n", damaged.getName()).replaceAll("%f", damaged.getFaction().isValid() ? damaged.getFaction().getName():"no faction")
					.replaceAll("%m", damager.getName());
			damager.getFaction().alert(this.combat, alert, dispatch.getPlayersInvolved().toNames());
		}
		//damaged alert
		if(damaged.getFaction().isValid()&& this.combat.cooldown(damaged.getName(), true)){
			alert = dispatch.getAlert().replaceAll("%n", damager.getName()).replaceAll("%f", damager.getFaction().isValid() ? damager.getFaction().getName():"no faction")
					.replaceAll("%m", damaged.getName());
			damaged.getFaction().alert(this.combat, alert, dispatch.getPlayersInvolved().toNames());
		}
	}

	public void saveExcludes(@NonNull final File toSave) throws IOException{
		if(!toSave.exists())
			toSave.createNewFile();
		final YamlConfiguration config = new YamlConfiguration();
		config.set("death", new ArrayList<String>(this.death.getExcludes()));
		config.set("move", new ArrayList<String>(this.move.getExcludes()));
		config.set("teleport", new ArrayList<String>(this.teleport.getExcludes()));
		config.set("combat", new ArrayList<String>(this.combat.getExcludes()));
		config.save(toSave);
	}

	public AlertGroup[] getAlertGroups(){
		return new AlertGroup[] {this.teleport, this.move, this.combat, this.death};
	}

}
