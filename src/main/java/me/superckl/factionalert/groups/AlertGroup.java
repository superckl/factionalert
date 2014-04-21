package me.superckl.factionalert.groups;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import com.massivecraft.factions.struct.Relation;

public abstract class AlertGroup {

	@Getter
	@Setter(onParam = @_({@NonNull}))
	private Set<String> excludes = new HashSet<String>();

	public abstract String getAlert(final Relation rel);
	public abstract boolean isEnabled();
}
