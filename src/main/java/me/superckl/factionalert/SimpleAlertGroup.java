package me.superckl.factionalert;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

import com.massivecraft.factions.Rel;

@AllArgsConstructor
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
	private final String truce;
	@Getter
	private final List<Rel> types;
	@Getter
	private final List<Rel> receivers;

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
