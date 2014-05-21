package me.superckl.actionalert.groups;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import me.superckl.actionalert.ActionAlert;
import me.superckl.actionalert.AlertType;
import me.superckl.actionalert.ModuleManager;
import me.superckl.actionalert.ModuleType;

import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

public class AlertGroupStorage {

	@Getter
	private final Map<AlertType, ModuleManager<AlertGroup<?>>> alerts = new ConcurrentHashMap<AlertType, ModuleManager<AlertGroup<?>>>();

	public AlertGroupStorage(final ModuleType type, final AlertGroup<?> ... groups){
		this.add(type, groups);
	}

	public void add(final ModuleType type, final AlertGroup<?> ... groups){
		for(final AlertGroup<?> group:groups){
			ModuleManager<AlertGroup<?>> manager = this.alerts.get(group.getType());
			if(manager == null)
				manager = new ModuleManager<AlertGroup<?>>();
			if(manager.getByType(type) != null)
				throw new IllegalArgumentException("Multiple groups with type "+group.getType().toString()+" were passed to AlertGroupStorage. There can only be one of each type per module!");
			manager.addModule(type, group);
			this.alerts.put(group.getType(), manager);
		}
	}

	public AlertGroup<?> getByType(final AlertType type, final ModuleType mType){
		return this.alerts.get(type).getByType(mType);
	}

	public static AlertGroupStorage getByWorld(@NonNull final World world){
		return AlertGroupStorage.storage.get(world.getName());
	}

	public static void add(@NonNull final World world, final AlertGroupStorage storage){
		AlertGroupStorage.storage.put(world.getName(), storage);
	}

	public static void readExcludes(){
		for(final ModuleType type: ModuleType.values())
			for(val name:AlertGroupStorage.storage.keySet()){
				val toRead = new File(ActionAlert.getInstance().getDataFolder(), new StringBuilder("data/").append(type.toString().toLowerCase()).append("/excludes_").append(name).append(".yml").toString());
				if(!toRead.exists())
					continue;
				val excludes = YamlConfiguration.loadConfiguration(toRead);
				if(excludes == null)
					continue;
				val st = AlertGroupStorage.storage.get(name);
				st.getByType(AlertType.DEATH, type).setExcludes(new HashSet<String>(excludes.getStringList("death")));
				st.getByType(AlertType.MOVE, type).setExcludes(new HashSet<String>(excludes.getStringList("move")));
				st.getByType(AlertType.TELEPORT, type).setExcludes(new HashSet<String>(excludes.getStringList("teleport")));
				st.getByType(AlertType.COMBAT, type).setExcludes(new HashSet<String>(excludes.getStringList("combat")));
			}
	}

	public static void saveExcludes() throws IOException{
		for(final ModuleType type: ModuleType.values())
			for(val name:AlertGroupStorage.storage.keySet()){
				val toSave = new File(ActionAlert.getInstance().getDataFolder(), new StringBuilder("data/").append(type.toString().toLowerCase()).append("/excludes_").append(name).append(".yml").toString());
				val config = new YamlConfiguration();
				val st = AlertGroupStorage.storage.get(name);
				config.set("death", new ArrayList<String>(st.getByType(AlertType.DEATH, type).getExcludes()));
				config.set("move", new ArrayList<String>(st.getByType(AlertType.MOVE, type).getExcludes()));
				config.set("teleport", new ArrayList<String>(st.getByType(AlertType.TELEPORT, type).getExcludes()));
				config.set("combat", new ArrayList<String>(st.getByType(AlertType.COMBAT, type).getExcludes()));
				config.save(toSave);
			}
	}

	@Getter
	private final static Map<String, AlertGroupStorage> storage = new ConcurrentHashMap<String, AlertGroupStorage>();
}
