package me.superckl.factionalert.commands;

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
		this.addAliases(command.getAliases());
		this.addRequiredArg("enable|disable");
		this.addRequiredArg("teleport|move|combat|death");
	}

	@Override
	public void perform(){
		this.command.execute(this.sender, null, null, this.args.toArray(new String[this.args.size()]));
	}

}
