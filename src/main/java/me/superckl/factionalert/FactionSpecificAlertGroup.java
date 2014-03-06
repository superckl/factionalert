package me.superckl.factionalert;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.scheduler.BukkitRunnable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FactionSpecificAlertGroup {

	@Getter
	private final boolean enabled;
	@Getter
	private final String alert;
	@Getter
 	private final int cooldown;
 	@Getter
 	private final FactionAlert instance;
 	private final Set<String> cooldowns = new HashSet<String>();
 	
 	public boolean cooldown(final String name){
 		if(this.cooldowns.contains(name))
 			return false;
 		if(this.cooldown <= 0)
 			return true;
 		this.cooldowns.add(name);
 		new BukkitRunnable() {
 			public void run() {
 				FactionSpecificAlertGroup.this.cooldowns.remove(name);
 			}
 		}.runTaskLater(this.instance, this.cooldown);
 		return true;
 	}
}
