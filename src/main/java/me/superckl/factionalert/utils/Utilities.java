package me.superckl.factionalert.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import lombok.NonNull;
import me.superckl.factionalert.FactionAlert;
import me.superckl.factionalert.groups.AlertGroup;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.UConf;
import com.massivecraft.factions.entity.UPlayer;

public class Utilities {

	public static boolean isValid(final Faction faction){
		return (faction != null) && !faction.isNone() && !faction.getId().equals(UConf.get(faction).factionIdSafezone) && !faction.getId().equals(UConf.get(faction).factionIdWarzone);
	}

	public static void alert(@NonNull final Faction faction, @NonNull final AlertGroup group, final String alert, @NonNull final List<String> toExclude){
		for(final UPlayer player:faction.getUPlayersWhereOnline(true)){
			if(toExclude.contains(player.getName()))
				continue;
			final Rel pToF = player.getRelationTo(faction);
			if(group.getReceivers().contains(pToF) && !group.getExcludes().contains(player.getName()))
				player.sendMessage(alert);
		}
	}

	public static String formatNameplate(@NonNull final String format, @NonNull String name){
		final int newLength = (format.length()-2)+name.length();
		if(newLength > 16)
			name = name.substring(0, name.length()-(newLength-16));
		return format.replace("%f", name);
	}

	public static List<String> toNames(@NonNull final List<Player> players){
		final List<String> names = new ArrayList<String>(0);
		for(final Player player:players)
			names.add(player.getName());
		return names;
	}

	public static <T extends Event> T dispatch(final T event){
		Bukkit.getPluginManager().callEvent(event);
		return event;
	}

	public static void log(final Object toLog, final Level level){
		FactionAlert.getInstance().getLogger().log(level, toLog.toString());
	}

}
