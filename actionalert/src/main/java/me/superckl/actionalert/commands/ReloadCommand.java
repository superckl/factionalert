package me.superckl.actionalert.commands;

import lombok.AllArgsConstructor;
import me.superckl.actionalert.ActionAlert;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@AllArgsConstructor
public class ReloadCommand extends ACommand{

	private final ActionAlert instance;

	@Override
	public boolean execute(final CommandSender sender, final String label,
			final String[] args) {
		if(!sender.hasPermission("factionalert.reload")){
			sender.sendMessage(ChatColor.RED+"You don't have permission to do that.");
			return false;
		}
		this.instance.reload();
		sender.sendMessage(ChatColor.GREEN+"FactionAlert reloaded.");
		return true;
	}

	@Override
	public String[] getAliases() {
		return new String[] {"reload", "rl", "r"};
	}

}
