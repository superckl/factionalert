package me.superckl.actionalert.factions_1_8_2.groups;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.superckl.actionalert.AlertType;
import me.superckl.actionalert.groups.AlertGroup;

import com.massivecraft.factions.struct.Rel;

@AllArgsConstructor
public class NameplateAlertGroup extends AlertGroup<Rel>{

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
	public String getAlert(final Rel rel) {
		return this.format;
	}

	@Override
	public List<Rel> getReceivers() {
		// TODO Auto-generated method stub
		return null;
	}

}
