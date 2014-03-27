package me.superckl.factionalert.utils;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;
import me.superckl.factionalert.groups.AlertGroup;

import org.bukkit.entity.Player;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;

public class Utilities {

	public static boolean isValid(@NonNull final Faction faction){
		return (faction != null) && !faction.isNone() && !faction.isSafeZone() && !faction.isWarZone();
	}

	public static void alert(@NonNull final Faction faction, @NonNull final AlertGroup group, final String alert, @NonNull final List<String> toExclude){
		for(final FPlayer player:faction.getFPlayersWhereOnline(true)){
			if(toExclude.contains(player.getName()))
				continue;
			if(!group.getExcludes().contains(player.getName()))
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

}
