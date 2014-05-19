package me.superckl.factions.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;
import me.superckl.actionalert.AlertType;
import me.superckl.actionalert.commands.ACommand;
import me.superckl.actionalert.groups.AlertGroupStorage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AlertsCommand extends ACommand{

	@Getter
	@Setter(onParam = @_({@NonNull}))
	private Map<String, ACommand> baseCommands = new HashMap<String, ACommand>();

	public AlertsCommand(){
		this.baseCommands.clear();
		val commands = new ACommand[] {new EnableCommand(), new DisableCommand()};
		for(val command:commands)
			for(val alias:command.getAliases())
				this.baseCommands.put(alias, command);
	}

	@Override
	public boolean execute(final CommandSender sender, final String label,
			final String[] args) {
		if((sender instanceof Player) == false){
			sender.sendMessage(ChatColor.RED+"You must be a player to execute this command.");
			return false;
		}
		if(!sender.hasPermission("factionalert.alerts")){
			sender.sendMessage(ChatColor.RED+"You don't have permission to do that.");
			return false;
		}
		if(args.length <= 0){
			sender.sendMessage(ChatColor.RED+"Invalid arguments");
			return false;
		}
		val faCommand = this.baseCommands.get(args[0].toLowerCase());
		if(faCommand == null){
			sender.sendMessage(ChatColor.RED+"Invalid arguments");
			return false;
		}
		return faCommand.execute(sender, label, Arrays.copyOfRange(args, 1, args.length));
	}

	@Override
	public String[] getAliases() {
		return new String[] {"alerts", "alert", "al"};
	}

	private class EnableCommand extends ACommand{

		@Override
		public boolean execute(final CommandSender sender, final String label,
				final String[] args) {
			if(args.length < 1){
				sender.sendMessage(ChatColor.RED+"Invalid arguments");
				return false;
			}
			final Player player = (Player) sender;
			final World world = args.length > 1 ? Bukkit.getWorld(args[1]):player.getWorld();
			if(world == null){
				sender.sendMessage(ChatColor.RED+"World not found.");
				return false;
			}
			switch(args[0].toLowerCase()){
			case "teleport":
				AlertGroupStorage.getByWorld(world).getByType(AlertType.TELEPORT).getExcludes().remove(player.getUniqueId().toString());
				sender.sendMessage(ChatColor.GREEN+"You will now receive teleport alerts in "+world.getName());
				return true;
			case "tp":
				AlertGroupStorage.getByWorld(world).getByType(AlertType.TELEPORT).getExcludes().remove(player.getUniqueId().toString());
				sender.sendMessage(ChatColor.GREEN+"You will now receive teleport alerts in "+world.getName());
				return true;
			case "move":
				AlertGroupStorage.getByWorld(world).getByType(AlertType.MOVE).getExcludes().remove(player.getUniqueId().toString());
				sender.sendMessage(ChatColor.GREEN+"You will now receive move alerts in "+world.getName());
				return true;
			case "death":
				AlertGroupStorage.getByWorld(world).getByType(AlertType.DEATH).getExcludes().remove(player.getUniqueId().toString());
				sender.sendMessage(ChatColor.GREEN+"You will now receive death alerts in "+world.getName());
				return true;
			case "combat":
				AlertGroupStorage.getByWorld(world).getByType(AlertType.COMBAT).getExcludes().remove(player.getUniqueId().toString());
				sender.sendMessage(ChatColor.GREEN+"You will now receive combat alerts in "+world.getName());
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

	private class DisableCommand extends ACommand{

		@Override
		public boolean execute(final CommandSender sender, final String label,
				final String[] args) {
			if(args.length < 1){
				sender.sendMessage(ChatColor.RED+"Invalid arguments");
				return false;
			}
			final Player player = (Player) sender;
			final World world = args.length > 1 ? Bukkit.getWorld(args[1]):player.getWorld();
			if(world == null){
				sender.sendMessage(ChatColor.RED+"World not found.");
				return false;
			}
			switch(args[0].toLowerCase()){
			case "teleport":
				AlertGroupStorage.getByWorld(world).getByType(AlertType.TELEPORT).getExcludes().add(player.getUniqueId().toString());
				sender.sendMessage(ChatColor.GREEN+"You will no longer receive teleport alerts in "+world.getName());
				return true;
			case "tp":
				AlertGroupStorage.getByWorld(world).getByType(AlertType.TELEPORT).getExcludes().add(player.getUniqueId().toString());
				sender.sendMessage(ChatColor.GREEN+"You will no longer receive teleport alerts in "+world.getName());
				return true;
			case "move":
				AlertGroupStorage.getByWorld(world).getByType(AlertType.MOVE).getExcludes().add(player.getUniqueId().toString());
				sender.sendMessage(ChatColor.GREEN+"You will no longer receive move alerts in "+world.getName());
				return true;
			case "death":
				AlertGroupStorage.getByWorld(world).getByType(AlertType.DEATH).getExcludes().add(player.getUniqueId().toString());
				sender.sendMessage(ChatColor.GREEN+"You will no longer receive death alerts in "+world.getName());
				return true;
			case "combat":
				AlertGroupStorage.getByWorld(world).getByType(AlertType.COMBAT).getExcludes().add(player.getUniqueId().toString());
				sender.sendMessage(ChatColor.GREEN+"You will no longer receive combat alerts in "+world.getName());
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