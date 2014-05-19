package me.superckl.factions_1_6_9_4.groups;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.superckl.actionalert.AlertType;
import me.superckl.actionalert.groups.AlertGroup;
import me.superckl.actionalert.groups.Cooldownable;
import me.superckl.factions_1_6_9_4.FactionAlert;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.massivecraft.factions.struct.Relation;

@AllArgsConstructor
public class FactionSpecificAlertGroup extends AlertGroup<Relation> implements Cooldownable{

	@Getter(onMethod = @_(@Override))
	@Setter
	private boolean enabled;
	@Getter(onMethod = @_(@Override))
	private final AlertType type;
	@Setter
	private String alert;
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

	public boolean cooldown(@NonNull final String name, final boolean reset){
		if(this.cooldowns.containsKey(name)){
			if(reset){
				this.cooldowns.remove(name).cancel();
				this.cooldowns.put(name,
						new BukkitRunnable() {
					public void run() {
						FactionSpecificAlertGroup.this.cooldowns.remove(name);
					}
				}.runTaskLater(this.instance.getInstance(), this.cooldown*20));
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
		}.runTaskLater(this.instance.getInstance(), this.cooldown*20));
		return true;
	}

	@Override
	public String getAlert(final Relation rel) {
		return this.alert;
	}

	@Override
	public List<Relation> getReceivers() {
		return null;
	}
}
