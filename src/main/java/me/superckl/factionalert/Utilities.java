package me.superckl.factionalert;

import lombok.NonNull;

import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.UConf;

public class Utilities {

	public static boolean isValid(@NonNull final Faction faction){
		return (faction != null) && !faction.isNone() && !faction.getId().equals(UConf.get(faction).factionIdSafezone) && !faction.getId().equals(UConf.get(faction).factionIdWarzone);
	}

	public static String formatNameplate(@NonNull final String format, @NonNull String name){
		final int newLength = (format.length()-2)+name.length();
		if(newLength > 16)
			name = name.substring(0, name.length()-(newLength-16));
		return format.replace("%f", name);
	}

}
