package me.superckl.factionalert.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public abstract class FACommand{

	public abstract boolean execute(CommandSender sender, Command command, String label, String[] args);
	public abstract String[] getAliases();
}
