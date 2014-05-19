package me.superckl.actionalert;

import lombok.Getter;

import org.bukkit.command.CommandSender;

public abstract class ActionAlertModule {

	@Getter
	private final ActionAlert instance;

	public ActionAlertModule(final ActionAlert instance){
		this.instance = instance;
	}

	public void onEnable(){};
	public void onDisable(){};
	public boolean onCommand(final CommandSender sender, final String label, final String[] args){return true;};
}
