package me.superckl.actionalert.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import lombok.NonNull;
import lombok.val;
import me.superckl.actionalert.ActionAlert;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Result;

public class Utilities {

	/**
	 * Formats strings to fit in the nameplate, ensuring the length is less than or equal to 16.
	 * @param format The format to use.
	 * @param name The name of the faction to use.
	 * @return The formatted String.
	 */
	public static String formatNameplate(@NonNull final String format, @NonNull String name){
		val newLength = (format.length()-2)+name.length();
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
	 * @param <T> The type of event.
	 * @return The event.
	 */
	public static <T extends Event> T dispatch(final T event){
		Bukkit.getPluginManager().callEvent(event);
		return event;
	}

	/**
	 * Logs an object to ActionAlerts Logger.
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
	
	public static Result checkFactionsVersion(String version){
		if(version.equalsIgnoreCase("1.6.9.4") || version.equalsIgnoreCase("1.6.9.5") || version.equalsIgnoreCase("1.6.9.3")){
			return Result.DEFAULT;
		}else{
			String[] split = version.split("\\.");
			int first = Integer.parseInt(split[0]);
			int second = Integer.parseInt(split[1]);
			if(first > 2 || (first == 2 && second >= 4))
				return Result.ALLOW;
		}
		return Result.DENY;
	}

}
