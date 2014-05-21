package me.superckl.actionalert;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import me.superckl.actionalert.groups.AlertGroup;
import me.superckl.actionalert.groups.AlertGroupStorage;
import me.superckl.actionalert.utils.Utilities;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.mcstats.Metrics.Graph;
import org.mcstats.Metrics.Plotter;

@ExtensionMethod(Utilities.class)
public class Metrics {

	@Getter
	private static org.mcstats.Metrics metrics;
	@Getter
	private static boolean started;
	@Getter
	private static Map<AlertType, SimplePlotter> countPlotters;
	@Getter
	private static Map<AlertType, SimplePlotter> worldsEnabledPlotters;
	@Getter
	private static Graph modulesGraph;
	@Getter
	private static Graph alertGraph;
	@Getter
	private static Graph enabledGraph;
	@Getter
	private static Graph versionGraph;

	private Metrics(){}

	public static void initialize(){
		try {
			Metrics.metrics = new org.mcstats.Metrics(ActionAlert.getInstance());
			Metrics.countPlotters = new HashMap<AlertType, SimplePlotter>();
			Metrics.worldsEnabledPlotters = new HashMap<AlertType, SimplePlotter>();
			Metrics.addPlotters();
		} catch (final IOException e) {
			"Failed to start metrics!".log(Level.WARNING);
			e.printStackTrace();
		}
	}

	public static void start(){
		Metrics.metrics.start();
		Metrics.started = true;
		if(Metrics.metrics.isOptOut() && ActionAlert.getInstance().isVerboseLogging())
			"I see you have chosen to opt out of metrics... I'll shed a tear for you: ;(".log(Level.INFO);
	}

	public static void submitAlert(final AlertType type){
		if(!Metrics.isStarted())
			return;
		final SimplePlotter plotter = Metrics.countPlotters.get(type);
		if(plotter == null)
			return;
		plotter.setValue(plotter.getValue()+1);
	}

	private static void addPlotters(){
		Metrics.modulesGraph = Metrics.metrics.createGraph("Enabled Modules");
		Metrics.versionGraph = Metrics.metrics.createGraph("Factions Version");
		Metrics.alertGraph = Metrics.metrics.createGraph("Alerts");
		Metrics.enabledGraph = Metrics.metrics.createGraph("Enabled Worlds");
		for(final AlertType type:AlertType.values()){
			final SimplePlotter plotter = new SimplePlotter(StringUtils.capitalize(type.toString().toLowerCase()), 0);
			Metrics.countPlotters.put(type, plotter);
			Metrics.alertGraph.addPlotter(plotter);
			int i = 0;
			for(final World world:Bukkit.getWorlds()){
				final AlertGroupStorage storage = AlertGroupStorage.getByWorld(world);
				if(storage == null)
					continue;
				for(final ModuleType mType:ModuleType.values()){
					final AlertGroup<?> group = storage.getByType(type, mType);
					if(group == null)
						continue;
					if(group.isEnabled())
						i++;
				}
			}
			final SimplePlotter enabledPlotter = new SimplePlotter(StringUtils.capitalize(type.toString().toLowerCase()), i);
			Metrics.enabledGraph.addPlotter(enabledPlotter);
			Metrics.worldsEnabledPlotters.put(type, enabledPlotter);
		}
	}

	public static void updateEnabledAlerts(){
		for(final AlertType type:AlertType.values()){
			final SimplePlotter enabledPlotter = Metrics.worldsEnabledPlotters.get(type);
			if(enabledPlotter == null)
				continue;
			int i = 0;
			for(final World world:Bukkit.getWorlds()){
				final AlertGroupStorage storage = AlertGroupStorage.getByWorld(world);
				if(storage == null)
					continue;
				for(final ModuleType mType:ModuleType.values()){
					final AlertGroup<?> group = storage.getByType(type, mType);
					if(group == null)
						continue;
					if(group.isEnabled())
						i++;
				}
			}
			enabledPlotter.setValue(i);
		}
	}

	public static class SimplePlotter extends Plotter{

		public SimplePlotter(final String name, final int value){
			super(name);
			this.value = value;
		}
		@Getter(onMethod = @_(@Override))
		@Setter
		private int value;


	}
}
