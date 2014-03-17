package me.superckl.factionalert.commands;

import java.util.Arrays;

import lombok.Getter;
import lombok.Setter;

import com.massivecraft.factions.cmd.FCommand;

public class AlertsCommandInjection extends FCommand{

	@Getter
	@Setter
	private AlertsCommand command;
	
	public AlertsCommandInjection(AlertsCommand command){
		super();
		this.command = command;
		this.requiredArgs.add("enable|disable");
		this.requiredArgs.add("teleport|move|death");
		this.aliases.addAll(Arrays.asList(command.getAliases()));
		this.senderMustBePlayer = true;
	}
	
	@Override
	public void perform() {
		this.command.execute(this.sender, null, null, this.args.toArray(new String[this.args.size()]));
	}

}
