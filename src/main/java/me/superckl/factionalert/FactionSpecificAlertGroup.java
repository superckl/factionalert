package me.superckl.factionalert;

import java.util.List;

import com.massivecraft.factions.Rel;

public class FactionSpecificAlertGroup {

	private final boolean enabled;
	private final String leader;
	private final String officer;
	private final String recruit;
	private final String member;
	private final List<Rel> receivers;
	
	public FactionSpecificAlertGroup(boolean enabled, String leader, String officer, String recruit, String member, List<Rel> receivers){
		this.enabled = enabled;
		this.leader = leader;
		this.officer = officer;
		this.recruit = recruit;
		this.member = member;
		this.receivers = receivers;
	}
	
	public String getAlert(Rel rel){
		if(rel == Rel.LEADER)
			return this.leader;
		else if(rel == Rel.OFFICER)
			return this.officer;
		else if(rel == Rel.MEMBER)
			return this.member;
		else
			return this.recruit;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public String getLeader() {
		return leader;
	}

	public String getOfficer() {
		return officer;
	}

	public String getRecruit() {
		return recruit;
	}

	public String getMember() {
		return member;
	}

	public List<Rel> getReceivers() {
		return receivers;
	}
	
}
