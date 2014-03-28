package me.superckl.factionalert.groups;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.superckl.factionalert.FactionAlert;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.massivecraft.factions.Rel;

@RequiredArgsConstructor
public class SimpleAlertGroup extends AlertGroup implements Cooldownable{

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
	private final String none;
	@Getter
	private final List<Rel> types;
	@Getter(onMethod = @_(@Override))
	private final List<Rel> receivers;
	@Getter
	private final int cooldown;
	@Getter
	private final FactionAlert instance;
	@Getter
	private final Map<String, BukkitTask> cooldowns = new HashMap<String, BukkitTask>();

	public boolean cooldown(@NonNull final String name){
		return this.cooldown(name, false);
	}

	/**
	 * @return Whether or not the player is not in cooldown
	 */
	public boolean cooldown(@NonNull final String name, final boolean reset){
		if(this.cooldowns.containsKey(name)){
			if(reset){
				this.cooldowns.remove(name).cancel();
				this.cooldowns.put(name,
						new BukkitRunnable() {
					public void run() {
						SimpleAlertGroup.this.cooldowns.remove(name);
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
				SimpleAlertGroup.this.cooldowns.remove(name);
			}
		}.runTaskLater(this.instance, this.cooldown*20));
		return true;
	}

	@Override
	public String getAlert(final Rel rel){
		if(rel == Rel.ENEMY)
			return this.enemy;
		else if(rel == Rel.ALLY)
			return this.ally;
		else if(rel == Rel.TRUCE)
			return this.truce;
		else if(rel == null)
			return this.none;
		else
			return this.neutral;
	}

}
