package me.superckl.factionalert.groups;

import java.util.Arrays;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.superckl.factionalert.AlertType;

import com.massivecraft.factions.Rel;

@AllArgsConstructor
public class NameplateAlertGroup extends AlertGroup{

	//Will not unassign nameplates
	@Getter(onMethod = @_(@Override))
	@Setter
	private boolean enabled;
	@Getter(onMethod = @_(@Override))
	private final AlertType type;
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
