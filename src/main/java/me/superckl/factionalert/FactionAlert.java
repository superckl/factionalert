package me.superckl.factionalert;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;
import lombok.experimental.ExtensionMethod;
import me.superckl.factionalert.commands.AlertsCommand;
import me.superckl.factionalert.commands.AlertsCommandInjection;
import me.superckl.factionalert.commands.FACommand;
import me.superckl.factionalert.commands.ReloadCommand;
import me.superckl.factionalert.commands.SaveCommand;
import me.superckl.factionalert.groups.AlertGroupStorage;
import me.superckl.factionalert.groups.FactionSpecificAlertGroup;
import me.superckl.factionalert.groups.NameplateAlertGroup;
import me.superckl.factionalert.groups.SimpleAlertGroup;
import me.superckl.factionalert.listeners.FactionListeners;
import me.superckl.factionalert.listeners.NameplateManager;
import me.superckl.factionalert.listeners.WorldLoadListeners;
import me.superckl.factionalert.utils.Utilities;
import me.superckl.factionalert.utils.VersionChecker;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

import com.massivecraft.factions.P;
import com.massivecraft.factions.struct.Relation;

@ExtensionMethod(Utilities.class)
public class FactionAlert extends JavaPlugin{

	@Getter
	private static FactionAlert instance;
	@Getter
	@Setter(onParam = @_({@NonNull}))
	private String[] configEntries = new String[] {"Teleport", "Move", "Combat"};
	@Getter
	@Setter(onParam = @_({@NonNull}))
	private Map<String, FACommand> baseCommands = new HashMap<String, FACommand>();
	@Getter
	private Scoreboard scoreboard;
	@Getter
	private FactionListeners listeners;
	@Getter
	private NameplateManager manager;
	@Getter
	@Setter
	private VersionChecker versionChecker;
	@Getter
	private boolean verboseLogging;
	private boolean cmdInjected = false;

	@Override
	public void onEnable(){
		FactionAlert.instance = this;
		this.saveDefaultConfig();
		this.verboseLogging = this.getConfig().getBoolean("Verbose Logging");
		if(this.getConfig().getBoolean("Version Check")){
			if(this.verboseLogging)
				this.getLogger().info("Starting version check...");
			this.versionChecker = VersionChecker.start(0.53d, this);
			this.getServer().getPluginManager().registerEvents(this.versionChecker, this);
		}
		if(this.verboseLogging)
			this.getLogger().info("Registering scoreboard");
		if(this.checkScoreboardConflicts(1))
			this.getLogger().warning("Other plugins have registered scoreboards! Conflicts may occur if nameplates are modified.");
		this.scoreboard = this.getServer().getScoreboardManager().getMainScoreboard();
		if(this.verboseLogging)
			this.getLogger().info("Instantiating commands...");
		this.fillCommands();
		if(this.verboseLogging)
			this.getLogger().info("Reading configuration...");
		this.readAllConfigs();
		AlertGroupStorage.readExcludes();
		if(this.verboseLogging)
			this.getLogger().info("Registering listeners...");
		this.getServer().getPluginManager().registerEvents(new FactionListeners(), this);
		this.getServer().getPluginManager().registerEvents(new NameplateManager(this.scoreboard), this);
		this.getServer().getPluginManager().registerEvents(new WorldLoadListeners(this), this);
		if(this.verboseLogging)
			this.getLogger().info("Starting metrics...");
		Metrics.start();
		this.getLogger().info("FactionAlert enabled!");
	}


	@Override
	public void onDisable(){
		try {
			AlertGroupStorage.saveExcludes();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		FactionAlert.instance = null;
	}

	/**
	 * Clears out the {@link AlertGroupStorage} and reads all configurations for all loaded worlds.
	 *
	 * Creates missing configurations by calling {@link #checkConfigs(World...)}.
	 */
	public void readAllConfigs(){
		AlertGroupStorage.getStorage().clear();
		this.checkConfigs();
		val worlds = this.getServer().getWorlds();
		for(val world:worlds){
			val fileName = new StringBuilder("config_").append(world.getName()).append(".yml").toString();
			val toRead = new File(this.getDataFolder(), fileName);
			if(!toRead.exists()){
				this.getLogger().severe("Configuration not found for world "+world.getName());
				continue;
			}
			val config = YamlConfiguration.loadConfiguration(toRead);
			if(config == null){
				this.getLogger().severe("Configuration not found for world "+world.getName());
				continue;
			}
			val storage = this.generateGroupStorage(config);
			AlertGroupStorage.add(world, storage);
		}
	}

	/**
	 * Checks that all loaded worlds have corresponding configurations saved to the disk. Creates any that are missing.
	 *
	 * @param notInList Worlds that will not be Bukkit's list of loaded worlds.
	 */
	public void checkConfigs(final World ... notInList){
		try {
            @SuppressWarnings("deprecation") //Supressed until we are no longer backwards compatible
			val config = YamlConfiguration.loadConfiguration(this.getClass().getResourceAsStream("/default.yml"));
			val worlds = this.getServer().getWorlds();
			for(val world:notInList)
				if(!worlds.contains(world))
					worlds.add(world);
			for(val world:worlds)
				try {
					val fileName = new StringBuilder("config_").append(world.getName()).append(".yml").toString();
					val toSave = new File(this.getDataFolder(), fileName);
					if(toSave.exists())
						continue;
					config.save(toSave);
					if(this.verboseLogging)
						this.getLogger().info("Generated configuration file for world ".concat(world.getName()));
				} catch (final Exception e) {
					this.getLogger().warning("Failed to generate configuration file for world "+world.getName());
					e.printStackTrace();
				}
		} catch (final Exception e) {
			this.getLogger().warning("Failed to generate world specific configuration files.");
			e.printStackTrace();
		}
	}

	/**
	 * Checks for other plugins being registered.
	 * @param tolerance The amount of Scoreboards allowed to be registered. Use 1 to check for any other plugin registering a scoreboard.
	 * @return Whether or not there may be a conflict.
	 */
	public boolean checkScoreboardConflicts(final int tolerance){
		try {
			val f = this.getServer().getScoreboardManager().getClass().getDeclaredField("scoreboards");
			if(f == null){
				this.getLogger().warning("Failed to check for scoreboard conflicts!");
				return false;
			}
			f.setAccessible(true);
			val boards = (Collection<?>) f.get(this.getServer().getScoreboardManager());
			if(boards.size() > tolerance)
				return true;
		} catch (final Throwable t) {
			this.getLogger().warning("Failed to check for scoreboard conflicts!");
			t.printStackTrace();
		}
		return false;
	}

	/**
	 * Fills the {@link #baseCommands} Map.
	 */
	public void fillCommands(){
		this.baseCommands.clear();
		val commands = new FACommand[] {new AlertsCommand(), new ReloadCommand(this), new SaveCommand()};
		for(val command:commands)
			for(val alias:command.getAliases())
				this.baseCommands.put(alias, command);
		if(this.cmdInjected)
			return;
		this.getLogger().info("Injecting alerts command...");
		if(P.p == null){
			this.getLogger().severe("Factions isn't enabled???");
			return;
		}
		P.p.cmdBase.addSubCommand(new AlertsCommandInjection((AlertsCommand) commands[0]));
		this.cmdInjected = true;
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args){
		FACommand faCommand;
		if((args.length <= 0) || ((faCommand = this.baseCommands.get(args[0].toLowerCase())) == null)){
			sender.sendMessage(ChatColor.RED+"Invalid arguments");
			return false;
		}
		return faCommand.execute(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
	}

	/**
	 * Helper method that actually does the reading of the configurations.
	 * @param c The configuration to be read.
	 * @return The generated {@link AlertGroupStorage}
	 */
	public AlertGroupStorage generateGroupStorage(final YamlConfiguration c){
		val alertGroups = new SimpleAlertGroup[this.configEntries.length];
		for(int i = 0; i < this.configEntries.length; i++){
			val entry = this.configEntries[i];
			val enabled = c.getBoolean(entry.concat(".Enabled"));
			val typeStrings = c.getStringList(entry.concat(".Types"));
			val types = new ArrayList<Relation>();
			for(val typeString:typeStrings){
				val relation = typeString.equalsIgnoreCase("none") ? null:Relation.valueOf(typeString);
				if((relation == null) && !typeString.equalsIgnoreCase("none")){
					this.getLogger().warning("Failed to read type ".concat(typeString).concat(" for ").concat(entry));
					continue;
				}
				types.add(relation);
			}
			final String enemy = ChatColor.translateAlternateColorCodes('&', c.getString(entry.concat(".Enemy Alert Message")).check());
			final String ally = ChatColor.translateAlternateColorCodes('&', c.getString(entry.concat(".Ally Alert Message")).check());
			final String neutral = ChatColor.translateAlternateColorCodes('&', c.getString(entry.concat(".Neutral Alert Message")).check());
			final String none = ChatColor.translateAlternateColorCodes('&', c.getString(entry.concat(".None Alert Message")).check());
			val timeout = c.getInt(entry.concat(".Cooldown"), 0);
			alertGroups[i] = new SimpleAlertGroup(enabled, AlertType.valueOf(this.configEntries[i].toUpperCase()), enemy, ally, neutral, none, types, timeout, this);
		}
		val enabled = c.getBoolean("Member Death.Enabled");
		final String alert = ChatColor.translateAlternateColorCodes('&', c.getString("Member Death.Member Alert Message").check());
		val timeout = c.getInt("Member Death.Cooldown", 0);
		val death = new FactionSpecificAlertGroup(enabled, AlertType.DEATH, alert, timeout, this);
		val prefix = c.getBoolean("Faction Nameplate.Prefix.Enabled");
		final String prefixFormat = ChatColor.translateAlternateColorCodes('&', c.getString("Faction Nameplate.Prefix.Format").check());
		val suffix = c.getBoolean("Faction Nameplate.Suffix.Enabled");
		final String suffixFormat = ChatColor.translateAlternateColorCodes('&', c.getString("Faction Nameplate.Suffix.Format").check());
		val prefixGroup = new NameplateAlertGroup(prefix, AlertType.PREFIX, prefixFormat);
		val suffixGroup = new NameplateAlertGroup(suffix, AlertType.SUFFIX, suffixFormat);
		return new AlertGroupStorage(alertGroups[0], alertGroups[1], alertGroups[2], death, prefixGroup, suffixGroup);
	}

}