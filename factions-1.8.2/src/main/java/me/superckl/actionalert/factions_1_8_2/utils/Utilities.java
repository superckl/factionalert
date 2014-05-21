package me.superckl.actionalert.factions_1_8_2.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import lombok.NonNull;
import lombok.val;
import me.superckl.actionalert.ActionAlert;
import me.superckl.actionalert.groups.AlertGroup;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.struct.Rel;

public class Utilities {

	/**
	 * Checks if a faction is not null, wilderness, safezone, nor warzone.
	 * @param faction The faction to check.
	 * @return If the faction is none of the above things.
	 */
	public static boolean isValid(@NonNull final Faction faction){
		return (faction != null) && faction.isNormal();
	}

	/**
	 * Alerts a faction of an event.
	 * @param faction The faction to alert.
	 * @param group The AlertGroup that corresponds to this event.
	 * @param alert The actual alert to be dispatched.
	 * @param toExclude A list of players to exclude from the alert.
	 */
	public static void alert(@NonNull final Faction faction, @NonNull final AlertGroup<Rel> group, final String alert, @NonNull final List<String> toExclude){
		if(ActionAlert.getInstance().isVerboseLogging())
			Utilities.log(new StringBuilder("Notifying Faction ").append(faction.getTag()).append(" of alert type ").append(group.getType().toString()).toString(), Level.INFO);
		for(val player:faction.getFPlayersWhereOnline(true)){
			if(toExclude.contains(player.getName()))
				continue;
			if(!group.getExcludes().contains(player.getName()))
				player.sendMessage(alert);
		}
	}

	/**
	 * Formats strings to fit in the nameplate, ensuring the length is <= 16.
	 * @param format The format to use.
	 * @param name The name of the faction to use.
	 * @return The formatted String.
	 */
	public static String formatNameplate(@NonNull final String format, @NonNull String name){
		final int newLength = (format.length()-2)+name.length();
		if(newLength > 16)
			name = name.substring(0, name.length()-(newLength-16));
		return format.replace("%f", name);
	}

	/**
	 * Converts a list of Players to a list of their names, in the same order.
	 * @param players The list to convert.
	 * @return The converted list.
	 */
	public static List<String> toNames(@NonNull final List<Player> players){
		val names = new ArrayList<String>(0);
		for(val player:players)
			names.add(player.getName());
		return names;
	}

	/**
	 * Simply dispatches an event using the Bukkit Event system.
	 * @param event The event to dispatch.
	 * @return The event.
	 */
	public static <T extends Event> T dispatch(final T event){
		Bukkit.getPluginManager().callEvent(event);
		return event;
	}

	/**
	 * Logs an object to ActionAlert's Logger.
	 * @param toLog The object to log.
	 * @param level The level with which to log.
	 */
	public static void log(final Object toLog, final Level level){
		ActionAlert.getInstance().getLogger().log(level, toLog.toString());
	}

	public static String check(final String string){
		return string == null ? "":string;
	}

	public static boolean isBukkitUUIDReady(){
		try{
			Bukkit.class.getMethod("getPlayer", UUID.class);
			return true;
		}catch(final Throwable t){
			return false;
		}
	}

	public static boolean isValidUUID(final String uuid) {
		return uuid.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
	}

	@SuppressWarnings("deprecation")
	public static OfflinePlayer getFromNameSupressed(final String name){
		return Bukkit.getOfflinePlayer(name);
	}


}
