package me.superckl.factionalert.events;

import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.superckl.factionalert.AlertType;
import me.superckl.factionalert.groups.AlertGroup;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import com.massivecraft.factions.Faction;

@RequiredArgsConstructor
public class DispatchSimpleAlertEvent extends FactionAlertEvent{

	private static final HandlerList handlers = new HandlerList();

	@Getter(onMethod = @_(@Override))
	private final Faction faction;
	@Getter
	private final AlertGroup group;
	@Getter
	private final AlertType type;
	@Getter
	@Setter
	@NonNull
	private String alert;
	@Getter
	private final World world;
	@Getter
	@Setter
	@NonNull
	private List<Player> playersInvolved;

	@Override
	public HandlerList getHandlers() {
		return DispatchSimpleAlertEvent.handlers;
	}

	public static HandlerList getHandlerList(){
		return DispatchSimpleAlertEvent.handlers;
	}

}
