package me.superckl.factionalert.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.Cleanup;
import lombok.Getter;
import me.superckl.factionalert.FactionAlert;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class VersionChecker implements Listener{

	@Getter
	private final AtomicBoolean done = new AtomicBoolean(false);
	@Getter
	private final AtomicBoolean needsUpdates = new AtomicBoolean(false);
	@Getter
	private volatile String newVersion = "";
	@Getter
	private final Set<String> notified = new HashSet<String>();

	private VersionChecker(){};

	/**
	 * Instantiates a new version checker and begins the check asynchronously.
	 * Register this as a listener for players with the appropiate permission to be notified on join.
	 * @param version The current version.
	 * @param instance The instance of FactionAlert to use.
	 * @return The new instance of VersionChecker.
	 */
	public static VersionChecker start(final double version, final FactionAlert instance){
		final VersionChecker versionChecker = new VersionChecker();
		new BukkitRunnable() {

			public void run() {
				try {
					final URL url = new URL("https://api.curseforge.com/servermods/files?projectIds=73431");
					final URLConnection conn = url.openConnection();
					conn.addRequestProperty("User-Agent", "FactionAlert version check");
					conn.setDoOutput(true);
					@Cleanup
					final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					final String response = reader.readLine();
					final JSONArray array = (JSONArray) JSONValue.parse(response);
					final String name = ((JSONObject)array.get(array.size()-1)).get("name").toString();
					final String readVersion = name.split(" ")[1];
					final double toCheck = Double.parseDouble(readVersion.replace("v", ""));
					if(toCheck > version){
						versionChecker.needsUpdates.set(true);
						versionChecker.newVersion = name.replace(" for 1.6.9.4", "");
						instance.getLogger().info("A new version of FactionAlert is available: "+versionChecker.newVersion);
					}else
						instance.getLogger().info("No update found.");
					versionChecker.done.set(true);
				} catch (final Throwable t) {
					versionChecker.done.set(true);
					versionChecker.needsUpdates.set(false);
					instance.getLogger().warning("Failed to perform a version check!");
					t.printStackTrace();
				}
			}
		}.runTaskAsynchronously(instance);
		return versionChecker;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoinEvent(final PlayerJoinEvent e){
		if(!e.getPlayer().hasPermission("factionalert.versioncheck"))
			return;
		if(!this.done.get() || !this.needsUpdates.get() || this.notified.contains(e.getPlayer().getName()))
			return;
		e.getPlayer().sendMessage(ChatColor.GREEN+"A new version of FactionAlert is available: "+this.newVersion);
		this.notified.add(e.getPlayer().getName());
	}

}
