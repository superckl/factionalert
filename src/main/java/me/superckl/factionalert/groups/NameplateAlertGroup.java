package me.superckl.factionalert.groups;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import com.massivecraft.factions.struct.Relation;

@AllArgsConstructor
public class NameplateAlertGroup extends AlertGroup{

	//Will not unassign nameplates
	@Getter
	@Setter
	private boolean enabled;
	@Getter
	@Setter
	@NonNull
	private String format;

	@Override
	public String getAlert(final Relation rel) {
		return this.format;
	}

}
