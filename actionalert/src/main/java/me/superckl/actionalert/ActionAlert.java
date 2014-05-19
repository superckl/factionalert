package me.superckl.actionalert;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import me.superckl.actionalert.commands.ACommand;
import me.superckl.actionalert.commands.ReloadCommand;
import me.superckl.actionalert.commands.SaveCommand;
import me.superckl.actionalert.groups.AlertGroupStorage;
import me.superckl.actionalert.utils.Utilities;
import me.superckl.actionalert.utils.VersionChecker;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event.Result;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class ActionAlert extends JavaPlugin{

	@Getter
	private static ActionAlert instance;
	@Getter
	@Setter
	private VersionChecker versionChecker;
	@Getter
	private boolean verboseLogging;
	@Getter
	@Setter(onParam = @_({@NonNull}))
	private Map<String, ACommand> baseCommands = new HashMap<String, ACommand>();
	@Getter
	private ModuleManager<ActionAlertModule> manager;

	@SneakyThrows
	@Override
	public void onEnable(){
		ActionAlert.instance = this;
		this.saveDefaultConfig();
		this.verboseLogging = this.getConfig().getBoolean("Verbose Logging");
		if(this.getConfig().getBoolean("Version Check")){
			if(this.verboseLogging)
				this.getLogger().info("Starting version check...");
			this.versionChecker = VersionChecker.start(0.6d, this);
			this.getServer().getPluginManager().registerEvents(this.versionChecker, this);
		}
		if(this.isVerboseLogging())
			this.getLogger().info("Instantiating commands...");
		this.fillCommands();
		if(this.verboseLogging)
			this.getLogger().info("Initializing metrics...");
		Metrics.initialize();
		if(this.isVerboseLogging())
			this.getLogger().info("Reading configuration...");
		AlertGroupStorage.readExcludes();
		this.getLogger().info("Loading modules...");
		this.manager = new ModuleManager<ActionAlertModule>();
		final Plugin plugin = this.getServer().getPluginManager().getPlugin("Factions");
		if(plugin != null){
			Result result = Utilities.checkFactionsVersion(plugin.getDescription().getVersion());
			if(result == Result.DENY)
				this.getLogger().severe("Incompatible Factions version found. Please report this to the author. Version: "+plugin.getDescription().getVersion());
			else if(result == Result.ALLOW){
				this.getLogger().info("Enabling Faction alerts for Factions 2.4.0+...");
				ActionAlertModule module = (ActionAlertModule) Class.forName("me.superckl.actionalert.factions.FactionAlert").getConstructor(this.getClass()).newInstance(this);
				module.onEnable();
				this.manager.addModule(ModuleType.FACTIONS, module);
			}else{
				this.getLogger().info("Enabling Faction alerts for Factions 1.6.9.5...");
				ActionAlertModule module = (ActionAlertModule) Class.forName("me.superckl.actionalert.factions-1.6.9.4.FactionAlert").getConstructor(this.getClass()).newInstance(this);
				module.onEnable();
				this.manager.addModule(ModuleType.FACTIONS, module);
			}
		}
	}

	@Override
	public void onDisable(){
		try {
			AlertGroupStorage.saveExcludes();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		ActionAlert.instance = null;
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args){
		ACommand aCommand;
		if((args.length <= 0) || ((aCommand = this.baseCommands.get(args[0].toLowerCase())) == null)){
			sender.sendMessage(ChatColor.RED+"Invalid arguments");
			return false;
		}
		return aCommand.execute(sender, label, Arrays.copyOfRange(args, 1, args.length));
	}

	/**
	 * Fills the {@link #baseCommands} Map.
	 */
	public void fillCommands(){
		this.baseCommands.clear();
		this.addCommand(new SaveCommand());
		this.addCommand(new ReloadCommand(this));
	}

	public void addCommand(final ACommand command){
		for(val alias:command.getAliases())
			this.baseCommands.put(alias, command);
	}

	public void reload(){
		try {
			AlertGroupStorage.saveExcludes();
		} catch (final IOException e) {
			this.getLogger().warning("Failed to save excludes.");
			e.printStackTrace();
		}
		this.reloadConfig();
		HandlerList.unregisterAll(this);
		AlertGroupStorage.readExcludes();
		if(this.getConfig().getBoolean("Version Check")){
			this.getLogger().info("Starting version check...");
			this.setVersionChecker(VersionChecker.start(0.6d, this));
			this.getServer().getPluginManager().registerEvents(this.getVersionChecker(), this);
		}
	}

}
