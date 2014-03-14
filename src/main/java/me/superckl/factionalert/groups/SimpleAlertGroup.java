package me.superckl.factionalert.groups;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.superckl.factionalert.FactionAlert;

import org.bukkit.scheduler.BukkitRunnable;

import com.massivecraft.factions.Rel;

@RequiredArgsConstructor
public class SimpleAlertGroup extends AlertGroup{

	@Getter
	private final boolean enabled;
	@Getter
	private final String enemy;
	@Getter
	private final String ally;
	@Getter
	private final String neutral;
	@Getter
	private final String truce;
	@Getter
	private final List<Rel> types;
	@Getter
	private final List<Rel> receivers;
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
				SimpleAlertGroup.this.cooldowns.remove(name);
			}
		}.runTaskLater(this.instance, this.cooldown);
		return true;
	}

	public String getAlert(final Rel rel){
		if(rel == Rel.ENEMY)
			return this.enemy;
		else if(rel == Rel.ALLY)
			return this.ally;
		else if(rel == Rel.TRUCE)
			return this.truce;
		else
			return this.neutral;
	}

}
