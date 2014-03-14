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
public class FactionSpecificAlertGroup extends AlertGroup{

	@Getter
	private final boolean enabled;
	@Getter
	private final String leader;
	@Getter
	private final String officer;
	@Getter
	private final String recruit;
	@Getter
	private final String member;
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
				FactionSpecificAlertGroup.this.cooldowns.remove(name);
			}
		}.runTaskLater(this.instance, this.cooldown);
		return true;
	}

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
