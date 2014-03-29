package me.superckl.factionalert.listeners;

import lombok.RequiredArgsConstructor;
import me.superckl.factionalert.FactionAlert;
import me.superckl.factionalert.groups.AlertGroupStorage;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

@RequiredArgsConstructor
public class WorldLoadListeners implements Listener{

	private final FactionAlert instance;

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onWorldLoad(final WorldLoadEvent e){
		if(AlertGroupStorage.getByWorld(e.getWorld()) != null)
			return;
		//They must have created a new world! We'll generate a config for you. How generous!
		this.instance.getLogger().info("An unknown world has been detected. FactionAlert will now generate a configuration file for "+e.getWorld().getName());
		this.instance.checkConfigs(e.getWorld());
	}

}