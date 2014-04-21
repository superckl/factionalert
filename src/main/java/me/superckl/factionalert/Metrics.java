package me.superckl.factionalert;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.superckl.factionalert.groups.AlertGroup;
import me.superckl.factionalert.groups.AlertGroupStorage;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.mcstats.Metrics.Graph;
import org.mcstats.Metrics.Plotter;

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
		} catch (final IOException e) {
			FactionAlert.getInstance().getLogger().warning("Failed to start metrics!");
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
		for(final AlertType type:AlertType.values()){
			final SimplePlotter plotter = new SimplePlotter(0);
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
			final SimplePlotter enabledPlotter = new SimplePlotter(i);
			final Graph enabledGraph = Metrics.metrics.createGraph(StringUtils.capitalize(type.toString().toLowerCase()).concat(" - Enabled Worlds"));
			enabledGraph.addPlotter(enabledPlotter);
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

	@AllArgsConstructor
	public static class SimplePlotter extends Plotter{

		@Getter(onMethod = @_(@Override))
		@Setter
		private int value;

	}
}
