package me.superckl.factionalert.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public abstract class FACommand{

	public abstract boolean execute(final CommandSender sender, final Command command, final String label, final String[] args);
	public abstract String[] getAliases();
}
