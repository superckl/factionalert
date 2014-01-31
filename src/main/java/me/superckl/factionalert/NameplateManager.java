package me.superckl.factionalert;

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

public class NameplateManager implements Listener{

	private final Scoreboard scoreboard;
	private final boolean suffix;
	private final boolean prefix;
	private final String suffixFormat;
	private final String prefixFormat;
	
	public NameplateManager(final Scoreboard scoreboard, final boolean suffix, final boolean prefix, final String suffixFormat, final String prefixFormat){
		this.scoreboard = scoreboard;
		this.suffix = suffix;
		this.prefix = prefix;
		this.prefixFormat = prefixFormat;
		this.suffixFormat = suffixFormat;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(final PlayerJoinEvent e){
		e.getPlayer().setScoreboard(this.scoreboard);
		Faction faction = UPlayer.get(e.getPlayer()).getFaction();
		if(!FactionListeners.isValid(faction)){
			return;
		}
		Team team = NameplateManager.this.scoreboard.getTeam(faction.getName());
		if(team == null){
			team = this.scoreboard.registerNewTeam(faction.getName());
			if(suffix){
				team.setSuffix(this.format(this.suffixFormat, faction.getName()));
			}
			if(prefix){
				team.setPrefix(this.format(this.prefixFormat, faction.getName()));
			}
		}
		team.addPlayer(e.getPlayer());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerQuit(final PlayerQuitEvent e){
		Team team = this.scoreboard.getPlayerTeam(e.getPlayer());
		if(team == null)
			return;
		team.removePlayer(e.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerLeaveOrJoinFaction(final FactionsEventMembershipChange e){
		MembershipChangeReason r = e.getReason();
		if(r == MembershipChangeReason.CREATE || r == MembershipChangeReason.JOIN){
			Team team = this.scoreboard.getTeam(e.getNewFaction().getName());
			if(team == null){
				team = this.scoreboard.registerNewTeam(e.getNewFaction().getName());
				if(suffix){
					team.setSuffix(this.format(this.suffixFormat, e.getNewFaction().getName()));
				}
				if(prefix){
					team.setPrefix(this.format(this.prefixFormat, e.getNewFaction().getName()));
				}
			}
			team.addPlayer(e.getUPlayer().getPlayer());
		}else if(r == MembershipChangeReason.DISBAND || r == MembershipChangeReason.KICK || r == MembershipChangeReason.LEAVE){
			Team team = this.scoreboard.getPlayerTeam(e.getUPlayer().getPlayer());
			if(team == null)
				return;
			team.removePlayer(e.getUPlayer().getPlayer());
		}
	}
	
	private String format(String format, String name){
		int newLength = format.length()-2+name.length();
		if(newLength > 16)
			name = name.substring(0, name.length()-(newLength-16));
		return format.replace("%f", name);
	}
}
