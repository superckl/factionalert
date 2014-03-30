package me.superckl.factionalert.groups;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import me.superckl.factionalert.AlertType;
import me.superckl.factionalert.FactionAlert;

import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

public class AlertGroupStorage {

	@Getter
	private final Map<AlertType, AlertGroup> alerts = new HashMap<AlertType, AlertGroup>();

	public AlertGroupStorage(final SimpleAlertGroup teleport, final SimpleAlertGroup move, final SimpleAlertGroup combat, final FactionSpecificAlertGroup death,
			final NameplateAlertGroup prefix, final NameplateAlertGroup suffix){
		this.alerts.put(AlertType.TELEPORT, teleport);
		this.alerts.put(AlertType.MOVE, move);
		this.alerts.put(AlertType.COMBAT, combat);
		this.alerts.put(AlertType.DEATH, death);
		this.alerts.put(AlertType.PREFIX, prefix);
		this.alerts.put(AlertType.SUFFIX, suffix);
	}

	public AlertGroup getByType(final AlertType type){
		return this.alerts.get(type);
	}

	public static AlertGroupStorage getByWorld(@NonNull final World world){
		return AlertGroupStorage.storage.get(world.getName());
	}

	public static void add(@NonNull final World world, final AlertGroupStorage storage){
		AlertGroupStorage.storage.put(world.getName(), storage);
	}

	public static void readExcludes(){
		for(final String name:AlertGroupStorage.storage.keySet()){
			final File toRead = new File(FactionAlert.getInstance().getDataFolder(), new StringBuilder("excludes_").append(name).append(".yml").toString());
			if(!toRead.exists())
				continue;
			final YamlConfiguration excludes = YamlConfiguration.loadConfiguration(toRead);
			if(excludes == null)
				continue;
			final AlertGroupStorage st = AlertGroupStorage.storage.get(name);
			st.getByType(AlertType.DEATH).setExcludes(new HashSet<String>(excludes.getStringList("death")));
			st.getByType(AlertType.MOVE).setExcludes(new HashSet<String>(excludes.getStringList("move")));
			st.getByType(AlertType.TELEPORT).setExcludes(new HashSet<String>(excludes.getStringList("teleport")));
			st.getByType(AlertType.COMBAT).setExcludes(new HashSet<String>(excludes.getStringList("combat")));
		}
	}

	public static void saveExcludes() throws IOException{
		for(final String name:AlertGroupStorage.storage.keySet()){
			final File toSave = new File(FactionAlert.getInstance().getDataFolder(), new StringBuilder("excludes_").append(name).append(".yml").toString());
			final YamlConfiguration config = new YamlConfiguration();
			final AlertGroupStorage st = AlertGroupStorage.storage.get(name);
			config.set("death", new ArrayList<String>(st.getByType(AlertType.DEATH).getExcludes()));
			config.set("move", new ArrayList<String>(st.getByType(AlertType.MOVE).getExcludes()));
			config.set("teleport", new ArrayList<String>(st.getByType(AlertType.TELEPORT).getExcludes()));
			config.set("combat", new ArrayList<String>(st.getByType(AlertType.COMBAT).getExcludes()));
			config.save(toSave);
		}
	}

	@Getter
	private final static Map<String, AlertGroupStorage> storage = new HashMap<String, AlertGroupStorage>();
}
