package me.superckl.factionalert;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import me.superckl.factionalert.groups.AlertGroup;
import me.superckl.factionalert.groups.AlertGroupStorage;
import me.superckl.factionalert.utils.Utilities;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.mcstats.Metrics.Graph;
import org.mcstats.Metrics.Plotter;

@ExtensionMethod(Utilities.class)
public class Metrics {

	private static org.mcstats.Metrics metrics;
	@Getter
	private static boolean started;
	@Getter
	private static Map<AlertType, SimplePlotter> countPlotters;
	@Getter
	private static Map<AlertType, SimplePlotter> worldsEnabledPlotters;

	private Metrics(){}

	public static void start(){
		try {
			Metrics.metrics = new org.mcstats.Metrics(FactionAlert.getInstance());
			Metrics.metrics.start();
			Metrics.countPlotters = new HashMap<AlertType, SimplePlotter>();
			Metrics.worldsEnabledPlotters = new HashMap<AlertType, SimplePlotter>();
			Metrics.addPlotters();
			Metrics.started = true;
			if(Metrics.metrics.isOptOut())
				"I see you have chosen to opt out of metrics... I'll shed a tear for you: ;(".log(Level.INFO);
		} catch (final IOException e) {
			"Failed to start metrics!".log(Level.WARNING);
			e.printStackTrace();
		}
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
		final Graph alertGraph = Metrics.metrics.createGraph("Alerts");
		final Graph enabledGraph = Metrics.metrics.createGraph("Enabled Worlds");
		for(final AlertType type:AlertType.values()){
			final SimplePlotter plotter = new SimplePlotter(StringUtils.capitalize(type.toString().toLowerCase()), 0);
			Metrics.countPlotters.put(type, plotter);
			alertGraph.addPlotter(plotter);
			int i = 0;
			for(final World world:Bukkit.getWorlds()){
				final AlertGroupStorage storage = AlertGroupStorage.getByWorld(world);
				if(storage == null)
					continue;
				final AlertGroup group = storage.getByType(type);
				if(group == null)
					continue;
				if(group.isEnabled())
					i++;
			}
			final SimplePlotter enabledPlotter = new SimplePlotter(StringUtils.capitalize(type.toString().toLowerCase()), i);
			enabledGraph.addPlotter(enabledPlotter);
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
				final AlertGroup group = storage.getByType(type);
				if(group == null)
					continue;
				if(group.isEnabled())
					i++;
			}
			enabledPlotter.setValue(i);
		}
	}

	public static class SimplePlotter extends Plotter{
		
		public SimplePlotter(String name, int value){
			super(name);
			this.value = value;
		}
		@Getter(onMethod = @_(@Override))
		@Setter
		private int value;
		

	}
}
