package me.superckl.factionalert.events;

import lombok.Getter;
import lombok.Setter;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

import com.massivecraft.factions.entity.Faction;

public abstract class FactionAlertEvent extends Event implements Cancellable{

	@Getter
	@Setter
	private boolean cancelled = false;

	public abstract Faction getFaction();

}
