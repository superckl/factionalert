package me.superckl.factionalert.commands;

import lombok.AllArgsConstructor;
import me.superckl.factionalert.FactionAlert;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;

@AllArgsConstructor
public class ReloadCommand extends FACommand{

	private final FactionAlert instance;

	@Override
	public boolean execute(final CommandSender sender, final Command command, final String label,
			final String[] args) {
		if(!sender.hasPermission("factionalert.reload")){
			sender.sendMessage(ChatColor.RED+"You don't have permission to do that.");
			return false;
		}
		this.instance.reloadConfig();
		HandlerList.unregisterAll(this.instance);
		this.instance.readConfig();
		this.instance.fillCommands();
		sender.sendMessage(ChatColor.GREEN+"FactionAlert reloaded.");
		return true;
	}

	@Override
	public String[] getAliases() {
		return new String[] {"reload", "rl", "r"};
	}

}
