package me.superckl.factionalert.groups;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.superckl.factionalert.FactionAlert;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.massivecraft.factions.Rel;

@AllArgsConstructor
public class FactionSpecificAlertGroup extends AlertGroup implements Cooldownable{

	@Getter(onMethod = @_(@Override))
	@Setter
	private boolean enabled;
	@Getter
	@Setter
	@NonNull
	private String leader;
	@Getter
	@Setter
	@NonNull
	private String officer;
	@Getter
	@Setter
	@NonNull
	private String recruit;
	@Getter
	@Setter
	@NonNull
	private String member;
	@Getter(onMethod = @_(@Override))
	private final List<Rel> receivers;
	@Getter
	@Setter
	private int cooldown;
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

	@Override
	public String getAlert(final Rel rel){
		if(rel == Rel.LEADER)
			return this.leader;
		else if(rel == Rel.OFFICER)
			return this.officer;
		else if(rel == Rel.MEMBER)
			return this.member;
		else
			return this.recruit;
	}

}
