package me.superckl.factionalert;

import com.massivecraft.factions.Faction;

import lombok.NonNull;

public class Utilities {

	public static boolean isValid(@NonNull final Faction faction){
		return (faction != null) && !faction.isNone() && !faction.isSafeZone() && !faction.isWarZone();
	}

	public static String formatNameplate(@NonNull final String format, @NonNull String name){
		final int newLength = (format.length()-2)+name.length();
		if(newLength > 16)
			name = name.substring(0, name.length()-(newLength-16));
		return format.replace("%f", name);
	}

}
