package me.superckl.factionalert.groups;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.superckl.factionalert.FactionAlert;

import org.bukkit.scheduler.BukkitRunnable;

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
	private final Set<String> cooldowns = new HashSet<String>();

	public boolean cooldown(@NonNull final String name){
		if(this.cooldowns.contains(name))
			return false;
		if(this.cooldown <= 0)
			return true;
		this.cooldowns.add(name);
		new BukkitRunnable() {
			public void run() {
				FactionSpecificAlertGroup.this.cooldowns.remove(name);
			}
		}.runTaskLater(this.instance, this.cooldown*20);
		return true;
	}
}
