package me.superckl.factionalert;

import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
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
	private final boolean suffix;
	private final boolean prefix;
	private final String suffixFormat;
	private final String prefixFormat;

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(final PlayerJoinEvent e){
		if(!this.prefix && !this.suffix)
			return;
		if(e.getPlayer().hasPermission("factionalert.noalert.nameplate"))
			return;
		e.getPlayer().setScoreboard(this.scoreboard);
		final Faction faction = UPlayer.get(e.getPlayer()).getFaction();
		if(!faction.isValid())
			return;
		Team team = NameplateManager.this.scoreboard.getTeam(faction.getName());
		if(team == null){
			team = this.scoreboard.registerNewTeam(faction.getName());
			if(this.suffix)
				team.setSuffix(this.suffixFormat.formatNameplate(faction.getName()));
			if(this.prefix)
				team.setPrefix(this.prefixFormat.formatNameplate(faction.getName()));
		}
		team.addPlayer(e.getPlayer());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerQuit(final PlayerQuitEvent e){
		final Team team = this.scoreboard.getPlayerTeam(e.getPlayer());
		if(team == null)
			return;
		team.removePlayer(e.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerLeaveOrJoinFaction(final FactionsEventMembershipChange e){
		if((e.getUPlayer().getPlayer() != null) && e.getUPlayer().getPlayer().hasPermission("factionalert.noalert.nameplate"))
			return;
		final MembershipChangeReason r = e.getReason();
		if((r == MembershipChangeReason.CREATE) || (r == MembershipChangeReason.JOIN)){
			Team team = this.scoreboard.getTeam(e.getNewFaction().getName());
			if(team == null){
				team = this.scoreboard.registerNewTeam(e.getNewFaction().getName());
				if(this.suffix)
					team.setSuffix(this.suffixFormat.formatNameplate(e.getNewFaction().getName()));
				if(this.prefix)
					team.setPrefix(this.prefixFormat.formatNameplate(e.getNewFaction().getName()));
			}
			team.addPlayer(e.getUPlayer().getPlayer());
		}else if((r == MembershipChangeReason.DISBAND) || (r == MembershipChangeReason.KICK) || (r == MembershipChangeReason.LEAVE)){
			OfflinePlayer player = e.getUPlayer().getPlayer();
			if(player == null)
				player = Bukkit.getOfflinePlayer(e.getUPlayer().getName());
			if(player == null)
				return;
			final Team team = this.scoreboard.getPlayerTeam(player);
			if(team == null)
				return;
			team.removePlayer(e.getUPlayer().getPlayer());
		}
	}

}
