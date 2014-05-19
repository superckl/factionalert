package me.superckl.actionalert.factions_1_6_9_4.listeners;

import java.util.Arrays;
import java.util.Collections;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.experimental.ExtensionMethod;
import me.superckl.actionalert.AlertType;
import me.superckl.actionalert.Metrics;
import me.superckl.actionalert.ModuleType;
import me.superckl.actionalert.factions_1_6_9_4.events.DispatchSimpleAlertEvent;
import me.superckl.actionalert.factions_1_6_9_4.groups.FactionSpecificAlertGroup;
import me.superckl.actionalert.factions_1_6_9_4.groups.SimpleAlertGroup;
import me.superckl.actionalert.factions_1_6_9_4.utils.Utilities;
import me.superckl.actionalert.groups.AlertGroupStorage;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.struct.Relation;

@ExtensionMethod({Utilities.class})
@RequiredArgsConstructor
public class FactionListeners implements Listener{

	/**
	 * Handles teleport alerts.
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTeleport(final PlayerTeleportEvent e){
		val storage = AlertGroupStorage.getByWorld(e.getTo().getWorld());
		if(storage == null)
			return;
		val teleport = (SimpleAlertGroup) storage.getByType(AlertType.TELEPORT, ModuleType.FACTIONS);
		if((teleport == null) || !teleport.isEnabled())
			return;
		if(e.getPlayer().hasPermission("factionalert.noalert.teleport"))
			return;
		val faction = Board.getFactionAt(new FLocation(e.getTo()));
		if(!faction.isValid())
			return;
		if(Board.getFactionAt(new FLocation(e.getFrom())).getId().equals(faction.getId()))
			return;
		val oFaction = FPlayers.i.get(e.getPlayer()).getFaction();
		Relation relation = null;
		if(oFaction.isValid())
			relation = faction.getRelationTo(oFaction);
		if(!teleport.getTypes().contains(relation))
			return;
		if(!teleport.cooldown(e.getPlayer().getName(), false))
			return;
		final DispatchSimpleAlertEvent dispatch = new DispatchSimpleAlertEvent(faction, teleport, AlertType.TELEPORT, teleport.getAlert(relation), e.getTo().getWorld(), Collections.unmodifiableList(Arrays.asList(e.getPlayer()))).dispatch();
		if(dispatch.isCancelled())
			return;
		String alert = dispatch.getAlert().replaceAll("%n", e.getPlayer().getName());
		alert = alert.replaceAll("%f", oFaction.isValid() ? oFaction.getTag():"no faction");
		faction.alert(teleport, alert, dispatch.getPlayersInvolved().toNames());
		Metrics.submitAlert(AlertType.TELEPORT);
	}

	/**
	 * Handles moving alerts.
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerMove(final PlayerMoveEvent e){
		val storage = AlertGroupStorage.getByWorld(e.getTo().getWorld());
		if(storage == null)
			return;
		val move = (SimpleAlertGroup) storage.getByType(AlertType.MOVE, ModuleType.FACTIONS);
		if((move == null) || !move.isEnabled() || (e instanceof PlayerTeleportEvent))
			return;
		if(e.getPlayer().hasPermission("factionalert.noalert.move"))
			return;
		val faction = Board.getFactionAt(new FLocation(e.getTo()));
		if(Board.getFactionAt(new FLocation(e.getFrom())).getId().equals(faction.getId()))
			return;
		if(!faction.isValid())
			return;
		if(Board.getFactionAt(new FLocation(e.getFrom())).getId().equals(faction.getId()))
			return;
		val oFaction = FPlayers.i.get(e.getPlayer()).getFaction();
		Relation relation = null;
		if(oFaction.isValid())
			relation = faction.getRelationTo(oFaction);
		if(!move.getTypes().contains(relation))
			return;
		if(!move.cooldown(e.getPlayer().getName(), false))
			return;
		final DispatchSimpleAlertEvent dispatch = new DispatchSimpleAlertEvent(faction, move, AlertType.MOVE, move.getAlert(relation), e.getTo().getWorld(), Collections.unmodifiableList(Arrays.asList(e.getPlayer()))).dispatch();
		if(dispatch.isCancelled())
			return;
		String alert = dispatch.getAlert().replaceAll("%n", e.getPlayer().getName());
		alert = alert.replaceAll("%f", oFaction.isValid() ? oFaction.getTag():"no faction");
		faction.alert(move, alert, dispatch.getPlayersInvolved().toNames());
		Metrics.submitAlert(AlertType.MOVE);
	}

	/**
	 * Handles combat alerts.
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerAttack(final EntityDamageByEntityEvent e){
		val storage = AlertGroupStorage.getByWorld(e.getDamager().getWorld());
		if(storage == null)
			return;
		val combat = (SimpleAlertGroup) storage.getByType(AlertType.COMBAT, ModuleType.FACTIONS);
		if((combat == null) || !combat.isEnabled())
			return;
		if(((e.getDamager() instanceof Player) == false) || ((e.getEntity() instanceof Player) == false))
			return;
		val damager = FPlayers.i.get((Player) e.getDamager());
		val damaged = FPlayers.i.get((Player) e.getEntity());
		if(damager.getPlayer().hasPermission("factionalert.noalert.combat") || damaged.getPlayer().hasPermission("factionalert.noalert.combat"))
			return;
		Relation rel = null;
		if(damager.getFaction().isValid() && damaged.getFaction().isValid())
			rel = damager.getRelationTo(damaged);
		//damager alert
		final DispatchSimpleAlertEvent dispatch = new DispatchSimpleAlertEvent(damager.getFaction(), combat, AlertType.COMBAT, combat.getAlert(rel), e.getDamager().getWorld(),
				Collections.unmodifiableList(Arrays.asList(damager.getPlayer(), damaged.getPlayer()))).dispatch();
		if(dispatch.isCancelled())
			return;
		String alert = "";
		if(damager.getFaction().isValid() && combat.cooldown(damager.getName(), true)){
			alert = dispatch.getAlert().replaceAll("%n", damaged.getName()).replaceAll("%f", damaged.getFaction().isValid() ? damaged.getFaction().getTag():"no faction")
					.replaceAll("%m", damager.getName());
			damager.getFaction().alert(combat, alert, dispatch.getPlayersInvolved().toNames());
		}
		//damaged alert
		if(damaged.getFaction().isValid()&& combat.cooldown(damaged.getName(), true)){
			alert = dispatch.getAlert().replaceAll("%n", damager.getName()).replaceAll("%f", damager.getFaction().isValid() ? damager.getFaction().getTag():"no faction")
					.replaceAll("%m", damaged.getName());
			damaged.getFaction().alert(combat, alert, dispatch.getPlayersInvolved().toNames());
		}
		Metrics.submitAlert(AlertType.COMBAT);
	}

	/**
	 * Handles death alerts.
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDeath(final PlayerDeathEvent e){
		val storage = AlertGroupStorage.getByWorld(e.getEntity().getWorld());
		if(storage == null)
			return;
		val death = (FactionSpecificAlertGroup) storage.getByType(AlertType.DEATH, ModuleType.FACTIONS);
		if((death == null) || !death.isEnabled())
			return;
		if(e.getEntity().hasPermission("factionalert.noalert.death"))
			return;
		val faction = FPlayers.i.get(e.getEntity()).getFaction();
		if(!faction.isValid())
			return;
		if(!death.cooldown(e.getEntity().getName(), false))
			return;
		for(val player:faction.getFPlayersWhereOnline(true))
			if(!death.getExcludes().contains(player.getName()))
				player.sendMessage(death.getAlert(null).replaceAll("%n", e.getEntity().getName()).replaceAll("%f", faction.getTag()));
		Metrics.submitAlert(AlertType.DEATH);
	}

}
