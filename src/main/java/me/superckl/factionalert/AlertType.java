package me.superckl.factionalert;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.superckl.factionalert.groups.AlertGroup;
import me.superckl.factionalert.groups.FactionSpecificAlertGroup;
import me.superckl.factionalert.groups.NameplateAlertGroup;
import me.superckl.factionalert.groups.SimpleAlertGroup;

@RequiredArgsConstructor
public enum AlertType {

	TELEPORT(SimpleAlertGroup.class, 1, new String[] {"%n, %f"}),
	MOVE(SimpleAlertGroup.class, 1, new String[] {"%n, %f"}),
	DEATH(FactionSpecificAlertGroup.class, 1, new String[] {"%n, %f"}),
	PREFIX(NameplateAlertGroup.class, 1, new String[] {"%f"}),
	SUFFIX(NameplateAlertGroup.class, 1, new String[] {"%f"}),
	COMBAT(SimpleAlertGroup.class, 2, new String[] {"%n, %f", "%m"});

	@Getter
	private final Class<? extends AlertGroup> subClass;
	@Getter
	private final int playersInvolved;
	@Getter
	private final String[] validPlaceholders;

}
