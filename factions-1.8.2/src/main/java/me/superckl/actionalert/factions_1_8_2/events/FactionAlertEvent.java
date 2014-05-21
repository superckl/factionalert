package me.superckl.actionalert.factions_1_8_2.events;

import lombok.Getter;
import lombok.Setter;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

import com.massivecraft.factions.Faction;

public abstract class FactionAlertEvent extends Event implements Cancellable{

	@Getter
	@Setter
	private boolean cancelled = false;

	public abstract Faction getFaction();

}
