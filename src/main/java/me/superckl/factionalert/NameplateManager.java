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

import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.event.FPlayerJoinEvent;
import com.massivecraft.factions.event.FPlayerLeaveEvent;

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
		final Faction faction = FPlayers.i.get(e.getPlayer()).getFaction();
		if(!faction.isValid())
			return;
		Team team = NameplateManager.this.scoreboard.getTeam(faction.getTag());
		if(team == null){
			team = this.scoreboard.registerNewTeam(faction.getTag());
			if(this.suffix)
				team.setSuffix(this.suffixFormat.formatNameplate(faction.getTag()));
			if(this.prefix)
				team.setPrefix(this.prefixFormat.formatNameplate(faction.getTag()));
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
	public void onPlayerLeaveFaction(final FPlayerLeaveEvent e){
		OfflinePlayer player = e.getFPlayer().getPlayer();
		if(player == null)
			player = Bukkit.getOfflinePlayer(e.getFPlayer().getName());
		if(player == null)
			return;
		final Team team = this.scoreboard.getPlayerTeam(player);
		if(team == null)
			return;
		team.removePlayer(e.getFPlayer().getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoinFaction(final FPlayerJoinEvent e){
		if(e.getFPlayer().getPlayer().hasPermission("factionalert.noalert.nameplate"))
			return;
		Team team = this.scoreboard.getTeam(e.getFaction().getTag());
		if(team == null){
			team = this.scoreboard.registerNewTeam(e.getFaction().getTag());
			if(this.suffix)
				team.setSuffix(this.suffixFormat.formatNameplate(e.getFaction().getTag()));
			if(this.prefix)
				team.setPrefix(this.prefixFormat.formatNameplate(e.getFaction().getTag()));
		}
		team.addPlayer(e.getFPlayer().getPlayer());
	}
}
