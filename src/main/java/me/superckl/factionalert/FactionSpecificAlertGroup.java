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

	public FactionSpecificAlertGroup(final boolean enabled, final String leader, final String officer, final String recruit, final String member, final List<Rel> receivers){
		this.enabled = enabled;
		this.leader = leader;
		this.officer = officer;
		this.recruit = recruit;
		this.member = member;
		this.receivers = receivers;
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

	public boolean isEnabled() {
		return this.enabled;
	}

	public String getLeader() {
		return this.leader;
	}

	public String getOfficer() {
		return this.officer;
	}

	public String getRecruit() {
		return this.recruit;
	}

	public String getMember() {
		return this.member;
	}

	public List<Rel> getReceivers() {
		return this.receivers;
	}

}
