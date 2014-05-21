package me.superckl.actionalert.factions_1_8_2.commands;

import java.util.Arrays;

import lombok.Getter;
import lombok.Setter;

import com.massivecraft.factions.cmd.FCommand;

public class AlertsCommandInjection extends FCommand{

	@Getter
	@Setter
	private AlertsCommand command;

	public AlertsCommandInjection(final AlertsCommand command){
		super();
		this.command = command;
		this.requiredArgs.add("enable|disable");
		this.requiredArgs.add("teleport|move|combat|death");
		this.aliases.addAll(Arrays.asList(command.getAliases()));
		this.senderMustBePlayer = true;
		this.errorOnToManyArgs = false;
	}

	@Override
	public void perform() {
		this.command.execute(this.sender, null, this.args.toArray(new String[this.args.size()]));
	}

}