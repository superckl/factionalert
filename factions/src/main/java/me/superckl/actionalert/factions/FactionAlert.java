package me.superckl.actionalert.factions;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;
import lombok.experimental.ExtensionMethod;
import lombok.extern.java.Log;
import me.superckl.actionalert.ActionAlert;
import me.superckl.actionalert.ActionAlertModule;
import me.superckl.actionalert.AlertType;
import me.superckl.actionalert.ModuleType;
import me.superckl.actionalert.commands.ACommand;
import me.superckl.actionalert.factions.commands.AlertsCommand;
import me.superckl.actionalert.factions.commands.AlertsCommandInjection;
import me.superckl.actionalert.factions.commands.FactionsCommand;
import me.superckl.actionalert.factions.groups.FactionSpecificAlertGroup;
import me.superckl.actionalert.factions.groups.NameplateAlertGroup;
import me.superckl.actionalert.factions.groups.SimpleAlertGroup;
import me.superckl.actionalert.factions.listeners.FactionListeners;
import me.superckl.actionalert.factions.listeners.NameplateManager;
import me.superckl.actionalert.factions.listeners.WorldLoadListeners;
import me.superckl.actionalert.factions.utils.Utilities;
import me.superckl.actionalert.groups.AlertGroupStorage;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scoreboard.Scoreboard;

import com.massivecraft.factions.Factions;
import com.massivecraft.factions.Rel;

@ExtensionMethod(Utilities.class)
@Log(topic = "ActionAlert:Factions")
public class FactionAlert extends ActionAlertModule{

	public FactionAlert(final ActionAlert instance) {
		super(instance);
	}

	@Getter
	private static FactionAlert fInstance;
	@Getter
	@Setter(onParam = @_({@NonNull}))
	private String[] configEntries = new String[] {"Teleport", "Move", "Combat"};
	@Getter
	@Setter(onParam = @_({@NonNull}))
	private Map<String, ACommand> baseCommands = new HashMap<String, ACommand>();
	@Getter
	private Scoreboard scoreboard;
	@Getter
	private FactionListeners listeners;
	@Getter
	private NameplateManager manager;
	private boolean cmdInjected = false;

	@Override
	public void onEnable(){
		FactionAlert.fInstance = this;
		if(this.getInstance().isVerboseLogging())
			this.getLogger().info("Registering scoreboard");
		if(this.checkScoreboardConflicts(1))
			this.getLogger().warning("Other plugins have registered scoreboards! Conflicts may occur if nameplates are modified.");
		this.scoreboard = this.getInstance().getServer().getScoreboardManager().getMainScoreboard();
		if(this.getInstance().isVerboseLogging())
			this.getLogger().info("Instantiating commands...");
		this.fillCommands();
		if(this.getInstance().isVerboseLogging())
			this.getLogger().info("Reading configuration...");
		this.readAllConfigs();
		if(this.getInstance().isVerboseLogging())
			this.getLogger().info("Registering listeners...");
		this.getInstance().getServer().getPluginManager().registerEvents(new FactionListeners(), this.getInstance());
		this.getInstance().getServer().getPluginManager().registerEvents(new NameplateManager(this.scoreboard), this.getInstance());
		this.getInstance().getServer().getPluginManager().registerEvents(new WorldLoadListeners(this), this.getInstance());
		this.getLogger().info("FactionAlert enabled!");
	}

	@Override
	public void onDisable(){
		FactionAlert.fInstance = null;
	}

	public Logger getLogger(){
		return FactionAlert.log;
	}

	/**
	 * Clears out the {@link AlertGroupStorage} and reads all configurations for all loaded worlds.
	 *
	 * Creates missing configurations by calling {@link #checkConfigs(World...)}.
	 */
	public void readAllConfigs(){
		AlertGroupStorage.getStorage().clear();
		this.checkConfigs();
		val worlds = this.getInstance().getServer().getWorlds();
		for(val world:worlds){
			val fileName = new StringBuilder("factions/config_").append(world.getName()).append(".yml").toString();
			val toRead = new File(this.getInstance().getDataFolder(), fileName);
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
			val worlds = this.getInstance().getServer().getWorlds();
			for(val world:notInList)
				if(!worlds.contains(world))
					worlds.add(world);
			for(val world:worlds)
				try {
					val fileName = new StringBuilder("factions/config_").append(world.getName()).append(".yml").toString();
					val toSave = new File(this.getInstance().getDataFolder(), fileName);
					if(toSave.exists())
						continue;
					config.save(toSave);
					if(this.getInstance().isVerboseLogging())
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
			val f = this.getInstance().getServer().getScoreboardManager().getClass().getDeclaredField("scoreboards");
			if(f == null){
				this.getLogger().warning("Failed to check for scoreboard conflicts!");
				return false;
			}
			f.setAccessible(true);
			val boards = (Collection<?>) f.get(this.getInstance().getServer().getScoreboardManager());
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
		this.getInstance().addCommand(new FactionsCommand(this));
		val commands = new ACommand[] {new AlertsCommand()};
		for(val command:commands)
			for(val alias:command.getAliases())
				this.baseCommands.put(alias, command);
		if(this.cmdInjected)
			return;
		this.getLogger().info("Injecting alerts command...");
		val f = (Factions) this.getInstance().getServer().getPluginManager().getPlugin("Factions");
		if(f == null){
			this.getLogger().severe("Factions doesn't exist but passed the dependency??? What kind of rig are you running here?");
			return;
		}
		f.getOuterCmdFactions().addSubCommand(new AlertsCommandInjection((AlertsCommand) commands[0]));
		this.cmdInjected = true;
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
			val types = new ArrayList<Rel>();
			for(val typeString:typeStrings){
				final Rel relation = typeString.equalsIgnoreCase("none") ? null:Rel.valueOf(typeString);
				if((relation == null) && !typeString.equalsIgnoreCase("none")){
					this.getLogger().warning("Failed to read type ".concat(typeString).concat(" for ").concat(entry));
					continue;
				}
				types.add(relation);
			}
			val receiverStrings = c.getStringList(entry.concat(".Receivers"));
			val receivers = new ArrayList<Rel>();
			for(val receiverString:receiverStrings){
				final Rel relation = Rel.valueOf(receiverString);
				if(relation == null){
					this.getLogger().warning("Failed to read receiver ".concat(receiverString).concat(" for ").concat(entry));
					continue;
				}
				receivers.add(relation);
			}
			final String enemy = ChatColor.translateAlternateColorCodes('&', c.getString(entry.concat(".Enemy Alert Message")).check());
			final String ally = ChatColor.translateAlternateColorCodes('&', c.getString(entry.concat(".Ally Alert Message")).check());
			final String neutral = ChatColor.translateAlternateColorCodes('&', c.getString(entry.concat(".Neutral Alert Message")).check());
			final String truce = ChatColor.translateAlternateColorCodes('&', c.getString(entry.concat(".Truce Alert Message")).check());
			final String none = ChatColor.translateAlternateColorCodes('&', c.getString(entry.concat(".None Alert Message")).check());
			val timeout = c.getInt(entry.concat(".Cooldown"), 0);
			alertGroups[i] = new SimpleAlertGroup(enabled, AlertType.valueOf(this.configEntries[i].toUpperCase()), enemy, ally, neutral, truce, none, types, receivers, timeout, this);
		}
		val enabled = c.getBoolean("Member Death.Enabled");
		val receiverStrings = c.getStringList("Member Death.Receivers");
		val receivers = new ArrayList<Rel>();
		for(val receiverString:receiverStrings){
			final Rel relation = Rel.valueOf(receiverString);
			if(relation == null){
				this.getLogger().warning("Failed to read receiver ".concat(receiverString).concat(" for ").concat("Member Death"));
				continue;
			}
			receivers.add(relation);
		}
		final String leader = ChatColor.translateAlternateColorCodes('&', c.getString("Member Death.Leader Alert Message").check());
		final String officer = ChatColor.translateAlternateColorCodes('&', c.getString("Member Death.Officer Alert Message").check());
		final String member = ChatColor.translateAlternateColorCodes('&', c.getString("Member Death.Member Alert Message").check());
		final String recruit = ChatColor.translateAlternateColorCodes('&', c.getString("Member Death.Recruit Alert Message").check());
		val timeout = c.getInt("Member Death.Cooldown", 0);
		val death = new FactionSpecificAlertGroup(enabled, AlertType.DEATH, leader, officer, recruit, member, receivers, timeout, this);
		val prefix = c.getBoolean("Faction Nameplate.Prefix.Enabled");
		final String prefixFormat = ChatColor.translateAlternateColorCodes('&', c.getString("Faction Nameplate.Prefix.Format").check());
		val suffix = c.getBoolean("Faction Nameplate.Suffix.Enabled");
		final String suffixFormat = ChatColor.translateAlternateColorCodes('&', c.getString("Faction Nameplate.Suffix.Format").check());
		val prefixGroup = new NameplateAlertGroup(prefix, AlertType.PREFIX, prefixFormat);
		val suffixGroup = new NameplateAlertGroup(suffix, AlertType.SUFFIX, suffixFormat);
		return new AlertGroupStorage(ModuleType.FACTIONS, alertGroups[0], alertGroups[1], alertGroups[2], death, prefixGroup, suffixGroup);
	}

}