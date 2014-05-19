package me.superckl.actionalert.groups;

public interface Cooldownable {

	public boolean cooldown(final String player, final boolean reset);
	public boolean cooldown(final String player);

}
