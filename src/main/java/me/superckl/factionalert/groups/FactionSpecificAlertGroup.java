package me.superckl.factionalert.groups;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.superckl.factionalert.FactionAlert;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

@RequiredArgsConstructor
public class FactionSpecificAlertGroup extends AlertGroup implements Cooldownable{

	@Getter
	private final boolean enabled;
	@Getter
	private final String alert;
	@Getter
	private final int cooldown;
	@Getter
	private final FactionAlert instance;
	@Getter
	private final Map<String, BukkitTask> cooldowns = new HashMap<String, BukkitTask>();

	public boolean cooldown(@NonNull final String name){
		 		return this.cooldown(name, false);
		 	}
	
	public boolean cooldown(@NonNull final String name, final boolean reset){
		if(this.cooldowns.containsKey(name)){
			if(reset){
				this.cooldowns.remove(name).cancel();
				this.cooldowns.put(name,
						new BukkitRunnable() {
					public void run() {
						FactionSpecificAlertGroup.this.cooldowns.remove(name);
					}
				}.runTaskLater(this.instance, this.cooldown*20));
			}
			return false;
		}
		if(this.cooldown <= 0)
			return true;
		this.cooldowns.put(name,
				new BukkitRunnable() {
			public void run() {
				FactionSpecificAlertGroup.this.cooldowns.remove(name);
			}
		}.runTaskLater(this.instance, this.cooldown*20));
		return true;
	}
}
