package me.superckl.factionalert.listeners;

import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;
import me.superckl.factionalert.AlertType;
import me.superckl.factionalert.groups.AlertGroupStorage;
import me.superckl.factionalert.groups.NameplateAlertGroup;
import me.superckl.factionalert.utils.Utilities;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.UPlayer;
import com.massivecraft.factions.event.FactionsEventMembershipChange;
import com.massivecraft.factions.event.FactionsEventMembershipChange.MembershipChangeReason;

@ExtensionMethod({Utilities.class})
@AllArgsConstructor
public class NameplateManager implements Listener{

	//TODO switch to multi-world

	private final Scoreboard scoreboard;

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(final PlayerJoinEvent e){
		if(e.getPlayer().hasPermission("factionalert.noalert.nameplate"))
			return;
		final World world = e.getPlayer().getWorld();
		final NameplateAlertGroup prefix = (NameplateAlertGroup) AlertGroupStorage.getByWorld(world).getByType(AlertType.PREFIX);
		final NameplateAlertGroup suffix = (NameplateAlertGroup) AlertGroupStorage.getByWorld(world).getByType(AlertType.SUFFIX);
		if(!prefix.isEnabled() && !suffix.isEnabled())
			return;
		e.getPlayer().setScoreboard(this.scoreboard);
		final Faction faction = UPlayer.get(e.getPlayer()).getFaction();
		if(!faction.isValid())
			return;
		this.checkTeam(e.getPlayer(), world, faction, prefix, suffix);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerQuit(final PlayerQuitEvent e){
		this.removeTeam(e.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerLeaveOrJoinFaction(final FactionsEventMembershipChange e){
		if((e.getUPlayer().getPlayer() == null) || e.getUPlayer().getPlayer().hasPermission("factionalert.noalert.nameplate"))
			return;
		final World world = e.getUPlayer().getPlayer().getWorld();
		final NameplateAlertGroup prefix = (NameplateAlertGroup) AlertGroupStorage.getByWorld(world).getByType(AlertType.PREFIX);
		final NameplateAlertGroup suffix = (NameplateAlertGroup) AlertGroupStorage.getByWorld(world).getByType(AlertType.SUFFIX);
		final MembershipChangeReason r = e.getReason();
		if((r == MembershipChangeReason.CREATE) || (r == MembershipChangeReason.JOIN))
			this.checkTeam(e.getUPlayer().getPlayer(), world, e.getNewFaction(), prefix, suffix);
		else if((r == MembershipChangeReason.DISBAND) || (r == MembershipChangeReason.KICK) || (r == MembershipChangeReason.LEAVE)){
			OfflinePlayer player = e.getUPlayer().getPlayer();
			if(player == null)
				player = Bukkit.getOfflinePlayer(e.getUPlayer().getName());
			if(player == null)
				return;
			this.removeTeam(player);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerChangeWorld(final PlayerChangedWorldEvent e){
		final World world = e.getPlayer().getWorld();
		final NameplateAlertGroup prefix = (NameplateAlertGroup) AlertGroupStorage.getByWorld(world).getByType(AlertType.PREFIX);
		final NameplateAlertGroup suffix = (NameplateAlertGroup) AlertGroupStorage.getByWorld(world).getByType(AlertType.SUFFIX);
		this.checkTeam(e.getPlayer(), world, UPlayer.get(e.getPlayer()).getFaction(), prefix, suffix);
	}

	private void removeTeam(final OfflinePlayer player){
		final Team team = this.scoreboard.getPlayerTeam(player);
		if(team == null)
			return;
		team.removePlayer(player);
	}

	private void checkTeam(final OfflinePlayer player, final World world, final Faction faction, final NameplateAlertGroup prefix, final NameplateAlertGroup suffix){
		if((!prefix.isEnabled() && !suffix.isEnabled()) || !faction.isValid())
			return;
		Team team = this.scoreboard.getTeam(faction.getName().concat("_").concat(world.getName()));
		if(team == null){
			team = this.scoreboard.registerNewTeam(faction.getName().concat("_").concat(world.getName()));
			if(suffix.isEnabled())
				team.setSuffix(suffix.getFormat().formatNameplate(faction.getName()));
			if(prefix.isEnabled())
				team.setPrefix(prefix.getFormat().formatNameplate(faction.getName()));
		}
		team.addPlayer(player);
	}

}
