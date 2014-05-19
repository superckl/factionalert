package me.superckl.actionalert.groups;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import lombok.Getter;
import lombok.NonNull;
import me.superckl.actionalert.ActionAlert;
import me.superckl.actionalert.AlertType;
import me.superckl.actionalert.utils.Utilities;

public abstract class AlertGroup<K> {

	@Getter
	private Set<String> excludes = new HashSet<String>();

	public abstract List<K> getReceivers();
	public abstract String getAlert(final K rel);
	public abstract boolean isEnabled();
	public abstract AlertType getType();

	public void setExcludes(@NonNull final Set<String> excludes){
		if(!excludes.isEmpty() && Utilities.isBukkitUUIDReady()){
			final Iterator<String> it = excludes.iterator();
			final String uuid = it.next();
			if(!Utilities.isValidUUID(uuid)){
				if(ActionAlert.getInstance().isVerboseLogging())
					Utilities.log("Bukkit is UUID ready and FactionAlert hasn't converted yet! Converting exclusion names to UUIDs for type "+this.getType().toString(), Level.INFO);
				final Set<String> uuids = new HashSet<String>();
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
