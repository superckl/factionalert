package me.superckl.factionalert;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

import com.massivecraft.factions.struct.Relation;

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
	private final List<Relation> types;

	public String getAlert(final Relation rel){
		if(rel == Relation.ENEMY)
			return this.enemy;
		else if(rel == Relation.ALLY)
			return this.ally;
		else
			return this.neutral;
	}

}
