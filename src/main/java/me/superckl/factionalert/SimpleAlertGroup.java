package me.superckl.factionalert;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.scheduler.BukkitRunnable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.massivecraft.factions.struct.Relation;

@RequiredArgsConstructor
public class SimpleAlertGroup {

	@Getter
	private final boolean enabled;
	@Getter
	private final String enemy;
	@Getter
	private final String ally;
	@Getter
	private final String neutral;
	@Getter
	private final List<Relation> types;
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
 				SimpleAlertGroup.this.cooldowns.remove(name);
 			}
 		}.runTaskLater(this.instance, this.cooldown);
 		return true;
 	}

	public String getAlert(final Relation rel){
		if(rel == Relation.ENEMY)
			return this.enemy;
		else if(rel == Relation.ALLY)
			return this.ally;
		else
			return this.neutral;
	}

}
