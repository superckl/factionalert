package me.superckl.actionalert;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * An enum that contains all possible alert types
 */
@RequiredArgsConstructor
public enum AlertType {

	TELEPORT(new String[] {"%n", "%f"}),
	MOVE(new String[] {"%n", "%f"}),
	DEATH(new String[] {"%n", "%f"}),
	PREFIX(new String[] {"%f"}),
	SUFFIX(new String[] {"%f"}),
	COMBAT(new String[] {"%n", "%f", "%m"});

	@Getter
	private final String[] validPlaceholders;

}
