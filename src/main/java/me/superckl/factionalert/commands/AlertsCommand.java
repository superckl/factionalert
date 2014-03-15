package me.superckl.factionalert.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.superckl.factionalert.FactionAlert;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class AlertsCommand extends FACommand{

	@Getter
	@Setter(onParam = @_({@NonNull}))
	private Map<String, FACommand> baseCommands = new HashMap<String, FACommand>();

	public AlertsCommand(final FactionAlert instance){
		this.baseCommands.clear();
		final FACommand[] commands = {new EnableCommand(instance), new DisableCommand(instance)};
		for(final FACommand command:commands)
			for(final String alias:command.getAliases())
				this.baseCommands.put(alias, command);
	}

	@Override
	public boolean execute(final CommandSender sender, final Command command, final String label,
			final String[] args) {
		if(!sender.hasPermission("factionalert.alerts")){
			sender.sendMessage(ChatColor.RED+"You don't have permission to do that.");
			return false;
		}
		if(args.length <= 0){
			sender.sendMessage(ChatColor.RED+"Invalid arguments");
			return false;
		}
		final FACommand faCommand = this.baseCommands.get(args[0].toLowerCase());
		if(faCommand == null){
			sender.sendMessage(ChatColor.RED+"Invalid arguments");
			return false;
		}
		return faCommand.execute(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
	}

	@Override
	public String[] getAliases() {
		return new String[] {"alerts", "alert", "al"};
	}

	@AllArgsConstructor
	private class EnableCommand extends FACommand{

		private final FactionAlert instance;

		@Override
		public boolean execute(final CommandSender sender, final Command command, final String label,
				final String[] args) {
			if(args.length != 1){
				sender.sendMessage(ChatColor.RED+"Invalid arguments");
				return false;
			}
			if(args[0].equalsIgnoreCase("teleport") || args[0].equalsIgnoreCase("tp")){
				this.instance.getListeners().getTeleport().getExcludes().remove(sender.getName());
				sender.sendMessage(ChatColor.GREEN+"You will now receive teleport alerts.");
				return true;
			}else if(args[0].equalsIgnoreCase("move")){
				this.instance.getListeners().getMove().getExcludes().remove(sender.getName());
				sender.sendMessage(ChatColor.GREEN+"You will now receive move alerts.");
				return true;
			}else if(args[0].equalsIgnoreCase("death")){
				this.instance.getListeners().getDeath().getExcludes().add(sender.getName());
				sender.sendMessage(ChatColor.GREEN+"You will now receive death alerts.");
				return true;
			}
			sender.sendMessage(ChatColor.RED+"Invalid arguments");
			return false;
		}

		@Override
		public String[] getAliases() {
			return new String[] {"enable", "true", "on"};
		}

	}

	@AllArgsConstructor
	private class DisableCommand extends FACommand{

		private final FactionAlert instance;

		@Override
		public boolean execute(final CommandSender sender, final Command command, final String label,
				final String[] args) {
			if(args.length != 1){
				sender.sendMessage(ChatColor.RED+"Invalid arguments");
				return false;
			}
			if(args[0].equalsIgnoreCase("teleport") || args[0].equalsIgnoreCase("tp")){
				this.instance.getListeners().getTeleport().getExcludes().add(sender.getName());
				sender.sendMessage(ChatColor.GREEN+"You will no longer receive teleport alerts.");
				return true;
			}else if(args[0].equalsIgnoreCase("move")){
				this.instance.getListeners().getMove().getExcludes().add(sender.getName());
				sender.sendMessage(ChatColor.GREEN+"You will no longer receive move alerts.");
				return true;
			}else if(args[0].equalsIgnoreCase("death")){
				this.instance.getListeners().getDeath().getExcludes().add(sender.getName());
				sender.sendMessage(ChatColor.GREEN+"You will no longer receive death alerts.");
				return true;
			}
			sender.sendMessage(ChatColor.RED+"Invalid arguments");
			return false;
		}

		@Override
		public String[] getAliases() {
			return new String[] {"disable", "false", "off"};
		}

	}
}