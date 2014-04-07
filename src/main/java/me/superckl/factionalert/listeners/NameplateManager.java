package me.superckl.factionalert.listeners;

import lombok.AllArgsConstructor;
import lombok.val;
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

	private final Scoreboard scoreboard;

	/**
	 * Assigns the player to their team.
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(final PlayerJoinEvent e){
		if(e.getPlayer().hasPermission("factionalert.noalert.nameplate"))
			return;
		val world = e.getPlayer().getWorld();
		val prefix = (NameplateAlertGroup) AlertGroupStorage.getByWorld(world).getByType(AlertType.PREFIX);
		val suffix = (NameplateAlertGroup) AlertGroupStorage.getByWorld(world).getByType(AlertType.SUFFIX);
		if(!prefix.isEnabled() && !suffix.isEnabled())
			return;
		e.getPlayer().setScoreboard(this.scoreboard);
		val faction = UPlayer.get(e.getPlayer()).getFaction();
		if(!faction.isValid())
			return;
		this.checkTeam(e.getPlayer(), world, faction, prefix, suffix);
	}

	/**
	 * Removes the player from their team.
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerQuit(final PlayerQuitEvent e){
		val world = e.getPlayer().getWorld();
		val prefix = (NameplateAlertGroup) AlertGroupStorage.getByWorld(world).getByType(AlertType.PREFIX);
		val suffix = (NameplateAlertGroup) AlertGroupStorage.getByWorld(world).getByType(AlertType.SUFFIX);
		if(!prefix.isEnabled() && !suffix.isEnabled())
			return;
		this.removeTeam(e.getPlayer());
	}

	/**
	 * Changes the player's team based on the action.
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerLeaveOrJoinFaction(final FactionsEventMembershipChange e){
		if((e.getUPlayer().getPlayer() == null) || e.getUPlayer().getPlayer().hasPermission("factionalert.noalert.nameplate"))
			return;
		val world = e.getUPlayer().getPlayer().getWorld();
		val prefix = (NameplateAlertGroup) AlertGroupStorage.getByWorld(world).getByType(AlertType.PREFIX);
		val suffix = (NameplateAlertGroup) AlertGroupStorage.getByWorld(world).getByType(AlertType.SUFFIX);
		if(!prefix.isEnabled() && !suffix.isEnabled())
			return;
		val r = e.getReason();
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

	/**
	 * Changes the player's team.
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerChangeWorld(final PlayerChangedWorldEvent e){
		val world = e.getPlayer().getWorld();
		val prefix = (NameplateAlertGroup) AlertGroupStorage.getByWorld(world).getByType(AlertType.PREFIX);
		val suffix = (NameplateAlertGroup) AlertGroupStorage.getByWorld(world).getByType(AlertType.SUFFIX);
		this.checkTeam(e.getPlayer(), world, UPlayer.get(e.getPlayer()).getFaction(), prefix, suffix);
	}

	/**
	 * Removes a player's team.
	 * @param player The player whose team should be removed.
	 */
	private void removeTeam(final OfflinePlayer player){
		val team = this.scoreboard.getPlayerTeam(player);
		if(team == null)
			return;
		team.removePlayer(player);
	}

	/**
	 * Adds a player to his/her team.
	 * @param player The player to check.
	 * @param world The world to check with.
	 * @param faction The faction to check with.
	 * @param prefix The prefix to check with.
	 * @param suffix the suffix to check with.
	 */
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
