package me.superckl.factionalert.groups;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import lombok.Getter;
import lombok.NonNull;
import me.superckl.factionalert.AlertType;
import me.superckl.factionalert.FactionAlert;
import me.superckl.factionalert.utils.Utilities;

import com.massivecraft.factions.Rel;

public abstract class AlertGroup {

	@Getter
	private Set<String> excludes = new HashSet<String>();

	public abstract List<Rel> getReceivers();
	public abstract String getAlert(final Rel rel);
	public abstract boolean isEnabled();
	public abstract AlertType getType();
	
	public void setExcludes(@NonNull Set<String> excludes){
		if(!excludes.isEmpty() && Utilities.isBukkitUUIDReady()){
			Iterator<String> it = excludes.iterator();
			String uuid = it.next();
			if(!Utilities.isValidUUID(uuid)){
				if(FactionAlert.getInstance().isVerboseLogging())
					Utilities.log("Bukkit is UUID ready and FactionAlert hasn't converted yet! Converting exclusion names to UUIDs for type "+this.getType().toString(), Level.INFO);
				Set<String> uuids = new HashSet<String>();
				uuids.add(Utilities.getFromNameSupressed(uuid).getUniqueId().toString());
				while(it.hasNext())
					uuids.add(Utilities.getFromNameSupressed(it.next()).getUniqueId().toString());
				this.excludes = uuids;
				return;
			}
		}
		this.excludes = excludes;
	}
}
