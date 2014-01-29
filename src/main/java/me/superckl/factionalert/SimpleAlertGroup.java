package me.superckl.factionalert;

import java.util.List;

import com.massivecraft.factions.Rel;

public class SimpleAlertGroup {

	private final boolean enabled;
	private final String enemy;
	private final String ally;
	private final String neutral;
	private final String truce;
	private final List<Rel> types;
	private final List<Rel> receivers;

	public SimpleAlertGroup(final boolean enabled, final String enemy, final String ally, final String neutral, final String truce, final List<Rel> types, final List<Rel> receivers){
		this.enabled = enabled;
		this.enemy = enemy;
		this.ally = ally;
		this.neutral = neutral;
		this.truce = truce;
		this.types = types;
		this.receivers = receivers;
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

	public boolean isEnabled() {
		return this.enabled;
	}

	public String getEnemy() {
		return this.enemy;
	}

	public String getAlly() {
		return this.ally;
	}

	public String getNeutral() {
		return this.neutral;
	}

	public String getTruce(){
		return this.truce;
	}

	public List<Rel> getTypes() {
		return this.types;
	}

	public List<Rel> getReceivers() {
		return this.receivers;
	}

}
