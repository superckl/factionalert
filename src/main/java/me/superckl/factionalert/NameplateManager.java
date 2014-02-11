package me.superckl.factionalert;

import lombok.AllArgsConstructor;

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
		e.getPlayer().setScoreboard(this.scoreboard);
		final Faction faction = FPlayers.i.get(e.getPlayer()).getFaction();
		if(!FactionListeners.isValid(faction))
			return;
		Team team = NameplateManager.this.scoreboard.getTeam(faction.getTag());
		if(team == null){
			team = this.scoreboard.registerNewTeam(faction.getTag());
			if(this.suffix)
				team.setSuffix(this.format(this.suffixFormat, faction.getTag()));
			if(this.prefix)
				team.setPrefix(this.format(this.prefixFormat, faction.getTag()));
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
	public void onPlayerLeaveFaction(FPlayerLeaveEvent e){
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
	public void onPlayerJoinFaction(FPlayerJoinEvent e){
		Team team = this.scoreboard.getTeam(e.getFaction().getTag());
		if(team == null){
			team = this.scoreboard.registerNewTeam(e.getFaction().getTag());
			if(this.suffix)
				team.setSuffix(this.format(this.suffixFormat, e.getFaction().getTag()));
			if(this.prefix)
				team.setPrefix(this.format(this.prefixFormat, e.getFaction().getTag()));
		}
		team.addPlayer(e.getFPlayer().getPlayer());
	}

	private String format(final String format, String name){
		final int newLength = (format.length()-2)+name.length();
		if(newLength > 16)
			name = name.substring(0, name.length()-(newLength-16));
		return format.replace("%f", name);
	}
}
