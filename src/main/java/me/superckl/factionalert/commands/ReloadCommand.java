package me.superckl.factionalert.commands;

import java.io.IOException;

import lombok.AllArgsConstructor;
import me.superckl.factionalert.FactionAlert;
import me.superckl.factionalert.groups.AlertGroupStorage;
import me.superckl.factionalert.listeners.FactionListeners;
import me.superckl.factionalert.listeners.WorldLoadListeners;

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
		try {
			AlertGroupStorage.saveExcludes();
		} catch (final IOException e) {
			this.instance.getLogger().warning("Failed to save excludes.");
			e.printStackTrace();
		}
		this.instance.reloadConfig();
		HandlerList.unregisterAll(this.instance);
		this.instance.readAllConfigs();
		AlertGroupStorage.readExcludes();
		this.instance.fillCommands();
		this.instance.getServer().getPluginManager().registerEvents(new FactionListeners(), this.instance);
		this.instance.getServer().getPluginManager().registerEvents(new WorldLoadListeners(this.instance), this. instance);
		sender.sendMessage(ChatColor.GREEN+"FactionAlert reloaded.");
		return true;
	}

	@Override
	public String[] getAliases() {
		return new String[] {"reload", "rl", "r"};
	}

}
