package me.superckl.factionalert.commands;

import java.io.File;
import java.io.IOException;

import lombok.RequiredArgsConstructor;
import me.superckl.factionalert.FactionAlert;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

@RequiredArgsConstructor
public class SaveCommand extends FACommand{

	private final FactionAlert instance;

	@Override
	public boolean execute(final CommandSender sender, final Command command, final String label,
			final String[] args) {
		if(!sender.hasPermission("factionalert.save")){
			sender.sendMessage(ChatColor.RED+"Youdon't have permission to do that.");
			return false;
		}
		try {
			this.instance.getListeners().saveExcludes(new File(this.instance.getDataFolder(), "excludes.yml"));
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
