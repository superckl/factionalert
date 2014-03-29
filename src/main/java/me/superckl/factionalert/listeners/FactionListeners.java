package me.superckl.factionalert.listeners;

import java.util.Arrays;
import java.util.Collections;

import lombok.experimental.ExtensionMethod;
import me.superckl.factionalert.AlertType;
import me.superckl.factionalert.events.DispatchSimpleAlertEvent;
import me.superckl.factionalert.groups.AlertGroupStorage;
import me.superckl.factionalert.groups.FactionSpecificAlertGroup;
import me.superckl.factionalert.groups.SimpleAlertGroup;
import me.superckl.factionalert.utils.Utilities;

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
public class FactionListeners implements Listener{

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTeleport(final PlayerTeleportEvent e){
		final AlertGroupStorage storage = AlertGroupStorage.getByWorld(e.getTo().getWorld());
		if(storage == null)
			return;
		final SimpleAlertGroup teleport = (SimpleAlertGroup) storage.getByType(AlertType.TELEPORT);
		if((teleport == null) || !teleport.isEnabled())
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
		if(!teleport.getTypes().contains(relation))
			return;
		if(!teleport.cooldown(e.getPlayer().getName(), false))
			return;
		final DispatchSimpleAlertEvent dispatch = new DispatchSimpleAlertEvent(faction, teleport, AlertType.TELEPORT, teleport.getAlert(relation), e.getTo().getWorld(), Collections.unmodifiableList(Arrays.asList(e.getPlayer()))).dispatch();
		if(dispatch.isCancelled())
			return;
		String alert = dispatch.getAlert().replaceAll("%n", e.getPlayer().getName());
		alert = alert.replaceAll("%f", oFaction.isValid() ? oFaction.getName():"no faction");
		faction.alert(teleport, alert, dispatch.getPlayersInvolved().toNames());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerMove(final PlayerMoveEvent e){
		final AlertGroupStorage storage = AlertGroupStorage.getByWorld(e.getTo().getWorld());
		if(storage == null)
			return;
		final SimpleAlertGroup move = (SimpleAlertGroup) storage.getByType(AlertType.MOVE);
		if((move == null) || !move.isEnabled() || (e instanceof PlayerTeleportEvent))
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
		if(!move.getTypes().contains(relation))
			return;
		if(!move.cooldown(e.getPlayer().getName(), false))
			return;
		final DispatchSimpleAlertEvent dispatch = new DispatchSimpleAlertEvent(faction, move, AlertType.MOVE, move.getAlert(relation), e.getTo().getWorld(), Collections.unmodifiableList(Arrays.asList(e.getPlayer()))).dispatch();
		if(dispatch.isCancelled())
			return;
		String alert = dispatch.getAlert().replaceAll("%n", e.getPlayer().getName());
		alert = alert.replaceAll("%f", oFaction.isValid() ? oFaction.getName():"no faction");
		faction.alert(move, alert, dispatch.getPlayersInvolved().toNames());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDeath(final PlayerDeathEvent e){
		final AlertGroupStorage storage = AlertGroupStorage.getByWorld(e.getEntity().getWorld());
		if(storage == null)
			return;
		final FactionSpecificAlertGroup death = (FactionSpecificAlertGroup) storage.getByType(AlertType.DEATH);
		if((death == null) || !death.isEnabled())
			return;
		if(e.getEntity().hasPermission("factionalert.noalert.death"))
			return;
		final Faction faction = UPlayer.get(e.getEntity()).getFaction();
		if(!faction.isValid())
			return;
		if(!death.cooldown(e.getEntity().getName(), false))
			return;
		for(final UPlayer player:faction.getUPlayersWhereOnline(true)){
			if(player.getName().equals(e.getEntity().getName()))
				continue;
			final Rel relation = player.getRelationTo(faction);
			if(death.getReceivers().contains(relation) && !death.getExcludes().contains(player.getName()))
				player.sendMessage(death.getAlert(relation).replaceAll("%n", e.getEntity().getName()).replaceAll("%f", faction.getName()));
		}
	}

	//TODO doesn't work
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerAttack(final EntityDamageByEntityEvent e){
		final AlertGroupStorage storage = AlertGroupStorage.getByWorld(e.getDamager().getWorld());
		if(storage == null)
			return;
		final SimpleAlertGroup combat = (SimpleAlertGroup) storage.getByType(AlertType.COMBAT);
		if((combat == null) || !combat.isEnabled())
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

		final DispatchSimpleAlertEvent dispatch = new DispatchSimpleAlertEvent(damager.getFaction(), combat, AlertType.COMBAT, combat.getAlert(rel), e.getDamager().getWorld(),
				Collections.unmodifiableList(Arrays.asList(damager.getPlayer(), damaged.getPlayer()))).dispatch();
		if(dispatch.isCancelled())
			return;
		String alert = "penis";
		if(damager.getFaction().isValid() && combat.cooldown(damager.getName(), true)){
			alert = dispatch.getAlert().replaceAll("%n", damaged.getName()).replaceAll("%f", damaged.getFaction().isValid() ? damaged.getFaction().getName():"no faction")
					.replaceAll("%m", damager.getName());
			damager.getFaction().alert(combat, alert, dispatch.getPlayersInvolved().toNames());
		}
		//damaged alert
		if(damaged.getFaction().isValid()&& combat.cooldown(damaged.getName(), true)){
			alert = dispatch.getAlert().replaceAll("%n", damager.getName()).replaceAll("%f", damager.getFaction().isValid() ? damager.getFaction().getName():"no faction")
					.replaceAll("%m", damaged.getName());
			damaged.getFaction().alert(combat, alert, dispatch.getPlayersInvolved().toNames());
		}
	}

}
