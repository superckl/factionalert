package me.superckl.factionalert;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class FactionSpecificAlertGroup {

	@Getter
	private final boolean enabled;
	@Getter
	private final String alert;

}
