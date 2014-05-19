package me.superckl.actionalert.commands;

import org.bukkit.command.CommandSender;

public abstract class ACommand{

	public abstract boolean execute(final CommandSender sender, final String label, final String[] args);
	public abstract String[] getAliases();
}
