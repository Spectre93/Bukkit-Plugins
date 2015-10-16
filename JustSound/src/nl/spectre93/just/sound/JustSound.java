package nl.spectre93.just.sound;

import java.io.File;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class JustSound extends JavaPlugin implements Listener {

	final static String[] soundList = { "AMBIENCE_CAVE", "AMBIENCE_RAIN",
			"AMBIENCE_THUNDER", "ANVIL_BREAK", "ANVIL_LAND", "ANVIL_USE",
			"ARROW_HIT", "BAT_DEATH", "BAT_HURT", "BAT_IDLE", "BAT_LOOP",
			"BAT_TAKEOFF", "BLAZE_BREATH", "BLAZE_DEATH", "BLAZE_HIT",
			"BREATH", "BURP", "CAT_HISS", "CAT_HIT", "CAT_MEOW", "CAT_PURR",
			"CAT_PURREOW", "CHEST_CLOSE", "CHEST_OPEN", "CHICKEN_EGG_POP",
			"CHICKEN_HURT", "CHICKEN_IDLE", "CHICKEN_WALK", "CLICK",
			"COW_HURT", "COW_IDLE", "COW_WALK", "CREEPER_DEATH",
			"CREEPER_HISS", "DIG_GRASS", "DIG_GRAVEL", "DIG_SAND", "DIG_SNOW",
			"DIG_STONE", "DIG_WOOD", "DIG_WOOL", "DOOR_CLOSE", "DOOR_OPEN",
			"DRINK", "EAT", "ENDERDRAGON_DEATH", "ENDERDRAGON_GROWL",
			"ENDERDRAGON_HIT", "ENDERDRAGON_WINGS", "ENDERMAN_DEATH",
			"ENDERMAN_HIT", "ENDERMAN_IDLE", "ENDERMAN_SCREAM",
			"ENDERMAN_STARE", "ENDERMAN_TELEPORT", "EXPLODE", "FALL_BIG",
			"FALL_SMALL", "FIRE", "FIRE_IGNITE", "FIZZ", "FUSE",
			"GHAST_CHARGE", "GHAST_DEATH", "GHAST_FIREBALL", "GHAST_MOAN",
			"GHAST_SCREAM", "GHAST_SCREAM2", "GLASS", "HURT", "HURT_FLESH",
			"IRONGOLEM_DEATH", "IRONGOLEM_HIT", "IRONGOLEM_THROW",
			"IRONGOLEM_WALK", "ITEM_BREAK", "ITEM_PICKUP", "LAVA", "LAVA_POP",
			"LEVEL_UP", "MAGMACUBE_JUMP", "MAGMACUBE_WALK", "MAGMACUBE_WALK2",
			"MINECART_BASE", "MINECART_INSIDE", "NOTE_BASS", "NOTE_BASS_DRUM",
			"NOTE_BASS_GUITAR", "NOTE_PIANO", "NOTE_PLING", "NOTE_SNARE_DRUM",
			"NOTE_STICKS", "ORB_PICKUP", "PIG_DEATH", "PIG_IDLE", "PIG_WALK",
			"PISTON_EXTEND", "PISTON_RETRACT", "PORTAL", "PORTAL_TRAVEL",
			"PORTAL_TRIGGER", "SHEEP_IDLE", "SHEEP_SHEAR", "SHEEP_WALK",
			"SHOOT_ARROW", "SILVERFISH_HIT", "SILVERFISH_IDLE",
			"SILVERFISH_KILL", "SILVERFISH_WALK", "SKELETON_DEATH",
			"SKELETON_HURT", "SKELETON_IDLE", "SKELETON_WALK", "SLIME_ATTACK",
			"SLIME_WALK", "SLIME_WALK2", "SPIDER_DEATH", "SPIDER_IDLE",
			"SPIDER_WALK", "SPLASH", "SPLASH2", "STEP_GRASS", "STEP_GRAVEL",
			"STEP_LADDER", "STEP_SAND", "STEP_SNOW", "STEP_STONE", "STEP_WOOD",
			"STEP_WOOL", "SWIM", "WATER", "WITHER_DEATH", "WITHER_HURT",
			"WITHER_IDLE", "WITHER_SHOOT", "WITHER_SPAWN", "WOLF_BARK",
			"WOLF_DEATH", "WOLF_GROWL", "WOLF_HOWL", "WOLF_HURT", "WOLF_PANT",
			"WOLF_SHAKE", "WOLF_WALK", "WOLF_WHINE", "WOOD_CLICK",
			"ZOMBIE_DEATH", "ZOMBIE_HURT", "ZOMBIE_IDLE", "ZOMBIE_INFECT",
			"ZOMBIE_METAL", "ZOMBIE_PIG_ANGRY", "ZOMBIE_PIG_DEATH",
			"ZOMBIE_PIG_HURT", "ZOMBIE_PIG_IDLE", "ZOMBIE_REMEDY",
			"ZOMBIE_UNFECT", "ZOMBIE_WOOD", "ZOMBIE_WOODBREAK" };

	final JustSound plugin;

	public JustSound() {
		plugin = this;
	}

	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);				//Registers listener

		String configFile = getDataFolder() + File.separator + "config.yml";	

		if (!(new File(configFile)).exists()) {
			getConfig().options().header(
					"JustSound, Made by Spectre93.\n"
							+ "Put player names here with their sounds like:\n"
							+ "Spectre93: ENDERDRAGON_GROWL");
			this.saveConfig();
		}
	}

	public void onDisable() {
		HandlerList.unregisterAll();	//Unregisters listener
	}

	public boolean onCommand(CommandSender sender, Command command,
			String commandLabel, String[] args) {
		if (command.getName().equalsIgnoreCase("jsr"))			//Just Sound Reload (config)
			return reloadConfig(sender);
		if (command.getName().equalsIgnoreCase("soundset"))		//Sets someone's sound
			return soundSetCommand(sender, args);
		if (command.getName().equalsIgnoreCase("sound"))		//Plays a sound
			return sound(sender, args);
		if (command.getName().equalsIgnoreCase("soundall"))		//Plays a sound to everyone
			return soundall(sender, args);
		if (command.getName().equalsIgnoreCase("soundlist"))	//Displays a list of sounds
			return soundList(sender, args);
		return true;
	}

	private boolean soundList(CommandSender sender, String[] args) {		//args = [pageNumber]
		if (args.length != 1)
			return false;
		if (!sender.hasPermission("just.sound.list")) {
			sendNoPerm(sender);
			return true;
		}
		try {
			showPage(sender, Integer.parseInt(args[0]));
		} catch (NumberFormatException e) {
			return false;
		}
		return true;

	}

	private boolean soundall(CommandSender sender, String[] args) {			//args = [soundName]
		if (args.length != 1)
			return false;
		if (!isPlayer(sender) && !isCommandBlock(sender)) {
			sender.sendMessage("Only players can do this!");
			return true;
		}
		if (!sender.hasPermission("just.sound.play.all")
				&& !isCommandBlock(sender)) {
			sendNoPerm(sender);
			return true;
		}
		if (soundExist(sender, args[0])) {
			List<Player> list;
			if (isCommandBlock(sender))
				list = ((CommandBlock) sender).getWorld().getPlayers();
			else
				list = ((Player) sender).getWorld().getPlayers();

			for (Player p : list) {
				p.playSound(p.getLocation(), Sound.valueOf(args[0]), 10F, 1F);
			}
		}
		return true;
	}

	private boolean sound(CommandSender sender, String[] args) {		//args = [sound] <player> <pitch> OR [x] [y] [z] <pitch>
		if (args.length < 1 || args.length > 3)
			return false;

		if (args.length == 1) {											//args = [sound]
			if (!sender.hasPermission("just.sound.play")) {
				sendNoPerm(sender);
				return true;
			}
			if (!isPlayer(sender)) {
				sender.sendMessage("Only players can do this!");
				return true;
			}
			if (soundExist(sender, args[0]))
				((Player) sender).getWorld().playSound(
						((Player) sender).getLocation(),
						Sound.valueOf(args[0]), 10F, 1F);
			return true;

		} else if (args.length == 2) {									//args = [sound] <player>
			if (!sender.hasPermission("just.sound.play.other")
					&& !isCommandBlock(sender)) {
				sendNoPerm(sender);
				return true;
			}
			if (soundExist(sender, args[0]))
				getServer().getPlayer(args[1]).playSound(
						getServer().getPlayer(args[1]).getLocation(),
						Sound.valueOf(args[0]), 10F, 1F);
			return true;
			
		}  else if (args.length == 3) {
			if (!sender.hasPermission("just.sound.play.other")
					&& !isCommandBlock(sender)) {
				sendNoPerm(sender);
				return true;
			}
			if (soundExist(sender, args[0]))
				getServer().getPlayer(args[1]).playSound(
						getServer().getPlayer(args[1]).getLocation(),
						Sound.valueOf(args[0]), 10F, 1F);
			return true;
		} 
		return false;
	}

	private boolean soundSetCommand(CommandSender sender, String[] args) {		//args = [sound] <player>
		if (args.length < 1 || args.length > 2)
			return false;

		if (args.length == 1) {
			if (!isPlayer(sender)) {
				sender.sendMessage("Only players can do this!");
				return true;
			}
			if (args[0].equalsIgnoreCase("null")){
				setSound(sender, ((Player) sender).getName(), null, false);
				return true;
			}
			if (soundExist(sender, args[0])){
				setSound(sender, ((Player) sender).getName(), args[0], false);
			}
			return true;

		} else if (args.length == 2) {
			if (args[0].equalsIgnoreCase("null"))
				setSound(sender, args[1], null, true);
			if (soundExist(sender, args[0]))
				if(args[1].equals("default")){
					if(!sender.hasPermission("just.sound.set.default")){
						return false;
					}
				}
				setSound(sender, args[1], args[0], true);
			return true;
		}
		return false;
	}

	
	private boolean soundExist(CommandSender sender, String string) {
		try {
			Sound.valueOf(string);
		} catch (Exception e) {
			sender.sendMessage("Sorry but that sound does not exist.");
			return false;
		}
		return true;
	}
	
	/*
	private boolean playerExist(CommandSender sender, String string) {
		if (getServer().getPlayer(string) == null) {
			sender.sendMessage("404: Player not found!");
			return false;
		} else {
			return true;
		}
	}
	*/

	private void setSound(CommandSender sender, String playerName,
			Object soundName, boolean soundOther) {
		if (!soundOther) {
			if (!sender.hasPermission("just.sound.set")) {
				sendNoPerm(sender);
				return;
			}
		} else {
			if (!sender.hasPermission("just.sound.set.other")
					&& !isCommandBlock(sender)) {
				sendNoPerm(sender);
				return;
			}
		}

		getConfig().set(playerName, soundName);
		saveConfig();

		if (soundName == null) {
			sender.sendMessage("Log-in sound removed for "
					+ getServer().getPlayer(playerName).getName() + "!");
		} else {
			sender.sendMessage("Log-in sound set!");
		}
	}

	private boolean reloadConfig(CommandSender sender) {
		if (sender.hasPermission("just.sound.reload")) {
			reloadConfig();
			sender.sendMessage("Config reloaded.");
		} else {
			sendNoPerm(sender);
		}
		return true;
	}

	@EventHandler
	private void onPlayerJoin(final PlayerJoinEvent evt) {
		getServer().getScheduler().runTaskLater(this, new BukkitRunnable() {
			@Override
			public void run() {
				String playerName = evt.getPlayer().getName();
				if (getConfig().getString(playerName) == null) {
					getLogger().fine(playerName + " has no personal log-in sound.");
					
					if(getConfig().getString("default") == null){
						getLogger().fine(" And no default sound is specified.");
						return;
					}
					
					playerName = "default";
					
				}
				Sound sound;
				try {
					sound = Sound.valueOf(getConfig().getString(playerName));
				} catch (IllegalArgumentException e1) {
					getLogger().warning(
							"----------------------------------------------");
					getLogger().warning("ERROR IN CONFIG @" + playerName);
					getLogger().warning(
							"No sound exists with name: "
									+ getConfig().getString(playerName));
					getLogger().warning(
							"----------------------------------------------");
					return;
				}

				getLogger().fine(playerName + " log-in sound triggered!");
				List<Player> list = evt.getPlayer().getWorld().getPlayers();
				for (Player p : list) {
					p.playSound(p.getLocation(), sound, 20F, 1F);
				}
			}
		}, 1L);
	}

	private static void showPage(CommandSender p, int page) {
		int nrPages = (soundList.length / 8 + 1);
		if (page > nrPages || page < 1) {
			p.sendMessage("That page doesn't exist, try 0-" + nrPages + ".");
			return;
		} else {
			p.sendMessage("Showing page " + page + " out of " + nrPages + ":");
		}
		for (int i = (page - 1) * 8; i < (page - 1) * 8 + 8
				&& i < soundList.length; i++) {
			p.sendMessage(soundList[i]);
		}
	}

	public boolean isCommandBlock(CommandSender sender) {
		return (sender instanceof CommandBlock);
	}

	public boolean isPlayer(CommandSender sender) {
		return (sender instanceof Player);
	}

	public void sendNoPerm(CommandSender sender) {
		sender.sendMessage(ChatColor.RED
				+ "You don't not have permission to do this.");
	}
}