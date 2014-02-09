package me.superckl.factionalert;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

import com.massivecraft.factions.Rel;

@AllArgsConstructor
public class FactionSpecificAlertGroup {

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
