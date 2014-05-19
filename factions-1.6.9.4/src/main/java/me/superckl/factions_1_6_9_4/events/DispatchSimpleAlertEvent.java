package me.superckl.factions_1_6_9_4.events;

import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.superckl.actionalert.AlertType;
import me.superckl.actionalert.groups.AlertGroup;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.struct.Relation;

@RequiredArgsConstructor
public class DispatchSimpleAlertEvent extends FactionAlertEvent{

	private static final HandlerList handlers = new HandlerList();

	@Getter(onMethod = @_(@Override))
	private final Faction faction;
	@Getter
	private final AlertGroup<Relation> group;
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
