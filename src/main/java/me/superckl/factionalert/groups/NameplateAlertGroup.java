package me.superckl.factionalert.groups;

import java.util.Arrays;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import com.massivecraft.factions.Rel;

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
	public List<Rel> getReceivers() {
		return Arrays.asList(Rel.values());
	}

	@Override
	public String getAlert(final Rel rel) {
		return this.format;
	}

}
