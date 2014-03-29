package me.superckl.factionalert.commands;


import java.io.IOException;

import me.superckl.factionalert.groups.AlertGroupStorage;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SaveCommand extends FACommand{

	@Override
	public boolean execute(final CommandSender sender, final Command command, final String label,
			final String[] args) {
		if(!sender.hasPermission("factionalert.save")){
			sender.sendMessage(ChatColor.RED+"Youdon't have permission to do that.");
			return false;
		}
		try {
			AlertGroupStorage.saveExcludes();
			sender.sendMessage(ChatColor.GREEN+"All data has been saved.");
		} catch (final IOException e) {
			sender.sendMessage(ChatColor.RED+"Something went wrong! Please notify the author.");
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public String[] getAliases() {
		return new String[] {"save", "s"};
	}

}
