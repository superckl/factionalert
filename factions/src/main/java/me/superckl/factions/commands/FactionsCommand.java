package me.superckl.factions.commands;

import java.util.Arrays;

import lombok.RequiredArgsConstructor;
import lombok.val;
import me.superckl.actionalert.commands.ACommand;
import me.superckl.factions.FactionAlert;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@RequiredArgsConstructor
public class FactionsCommand extends ACommand{

	private final FactionAlert instance;

	@Override
	public boolean execute(final CommandSender sender, final String label, final String[] args) {
		val faCommand = this.instance.getBaseCommands().get(args[0].toLowerCase());
		if(faCommand == null){
			sender.sendMessage(ChatColor.RED+"Invalid arguments");
			return false;
		}
		return faCommand.execute(sender, label, Arrays.copyOfRange(args, 1, args.length));
	}

	@Override
	public String[] getAliases() {
		return new String[] {"factions", "f"};
	}

}
