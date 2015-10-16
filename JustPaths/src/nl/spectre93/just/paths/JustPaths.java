package nl.spectre93.just.paths;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

public class JustPaths extends JavaPlugin{
	
	private Set<Player> pathList = new HashSet<Player>();
	private Map<String, Integer> pathId = new HashMap<String, Integer>();
	private Map<String, Integer> pathLength = new HashMap<String, Integer>();
	private Map<String, LinkedList<BlockState>> blocks = new HashMap<String, LinkedList<BlockState>>();
	private List<Integer> whiteList = new ArrayList<Integer>();

	public void onEnable(){
		String configFile = getDataFolder() + File.separator + "config.yml";
		
		if(!(new File(configFile).exists())){
			getConfig().options().header("JustPaths, Made by Spectre93.");
			getConfig().set("length-max", 10);
			int[] in = {1, 2, 3, 4, 5, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 24, 35, 41, 42, 45, 46, 47, 48, 49, 56, 57, 58, 73, 79, 80, 82, 87, 88, 89, 98, 110, 121, 129, 133};
			for(Integer i : in){
				whiteList.add(i);
			}
			getConfig().set("whiteList", whiteList);
			this.saveConfig();
		}
		
		if(!getConfig().contains("length-max")) {getConfig().set("length-max", 10); saveConfig();}
		if(!getConfig().contains("whiteList")) {getConfig().set("whiteList", whiteList); saveConfig();}
		
		whiteList = getConfig().getIntegerList("whiteList");
		
		getServer().getPluginManager().registerEvents(new MyPlayerMoveListener(this), this);
		getServer().getPluginManager().registerEvents(new MyPlayerQuitListener(this), this);
		getServer().getPluginManager().registerEvents(new MyBlockBreakListener(this), this);
	}
	
	public void onDisable(){
		Player[] onlinePlayerList = getServer().getOnlinePlayers();
		for(Player p : onlinePlayerList){
			clear(p);
		}
		HandlerList.unregisterAll();
	}
	
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
		if(command.getName().equalsIgnoreCase("pathmax")){
			if(sender.hasPermission("just.paths.max")){
				if(args.length != 1) {
					sender.sendMessage("Correct usage:");
					return false;
				}
				int newLength;
				try {
					newLength = Integer.parseInt(args[0]);
					getConfig().set("length-max", newLength);
					saveConfig();
				} catch (NumberFormatException e) {
					sender.sendMessage("That's not possible. Correct usage:");
					return false;
				}
				
				Player[] onlinePlayerList = getServer().getOnlinePlayers();
				for(Player p : onlinePlayerList){
					clear(p);
					if(getPathLength(p) > newLength){
						setPathLength(p, newLength);
					}
				}
				return true;
			}else sender.sendMessage("You don't have permission to use this command.");
			return true;
		}
		
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command has to be called by a player!");
    		return true;
    	}
    	
    	Player player = (Player)sender;
    	
    	if(player.hasPermission("just.paths")){
    		if(command.getName().equalsIgnoreCase("paths")){
    			setCarpet(player, !hasPath(player));
    			player.sendMessage("Paths enabled: " + hasPath(player));
        		
        	}else if(command.getName().equalsIgnoreCase("setpath")){
        		int blockId = 35;
        		int length = 3;
        		
        		if((args.length < 1) || args.length > 2) {return false;}
        		
        		else if(args.length == 1){
    				try {
    					blockId = Integer.parseInt(args[0]);
    				} catch (NumberFormatException e) {
    					return false;
    				}
    				setPathId(player, blockId);
    			} 
        		else if(args.length == 2){
        			try {
        				blockId = Integer.parseInt(args[0]);
    					length = Integer.parseInt(args[1]);
    				} catch (NumberFormatException e) {
    					return false;
    				}
        			setPathId(player, blockId);
    				setPathLength(player, length);
    			}
        	}
    	}else player.sendMessage("You don't have permission to use this command."); 
		return true;		
	}

	public boolean hasPath(Player player){
		return pathList.contains(player);
	}
	
	public void setCarpet(Player player, boolean enabled){
		if(enabled){
			pathList.add(player);
		}else{
			clear(player);
			pathList.remove(player);
		}
	}
	
	private LinkedList<BlockState> getBlockList(Player player){
		LinkedList<BlockState> result = blocks.get(player.getName());
		
		if(result == null){
			result = new LinkedList<BlockState>();
			blocks.put(player.getName(), result);
		}
		return result;
	}
	
	protected int getPathId(Player player){
		int temp = 35;		//default wool

		try {
			temp = pathId.get(player.getName());
		} catch (Exception e) {
			pathId.put(player.getName(), temp);
		}
		
		return temp;
	}
	
	private void setPathId(Player player, int input){
		if(whiteList.contains(input)){
			clear(player);
			pathId.put(player.getName(), input);
			player.sendMessage("Path set to blockId " + input + "!");
		}else{
			player.sendMessage("Can't set it to that block, sorry.");
		}
	}
	
	protected int getPathLength(Player player){
		int temp = 3; 	//default length
		
		try {
			temp = pathLength.get(player.getName());
		} catch (Exception e) {
			pathLength.put(player.getName(), temp);
		}
		return temp;
	}
	
	private void setPathLength(Player player, int length) {
		if(length <= getConfig().getInt("length-max")){
			clear(player);
			pathLength.put(player.getName(), length);
			player.sendMessage("Path length set to " + length + "!");
		}else{
			player.sendMessage("Can't set it that long, sorry.");
		}		
	}
	
	
	public void putBlock(Player player, Block block){
		LinkedList<BlockState> list = getBlockList(player);
		if(list.size() >= getPathLength(player)){
			BlockState old = list.removeFirst();
			Bukkit.getServer().getPluginManager().callEvent(new BlockBreakEvent(player.getWorld().getBlockAt(old.getLocation()), player));
			old.update(true);
			old.removeMetadata("justpaths", this);
			old.removeMetadata("owner", this);
		}
		list.add(block.getState());
	}
	
	public void clear(Player player){
		LinkedList<BlockState> list = getBlockList(player);
		while(list.size() != 0){
			BlockState old = getBlockList(player).removeFirst();
			Bukkit.getServer().getPluginManager().callEvent(new BlockBreakEvent(player.getWorld().getBlockAt(old.getLocation()), player));
			old.update(true);
			old.removeMetadata("justpaths", this);
			old.removeMetadata("owner", this);
		}
	}
	
	public void setMetadata(Block block, String key, Object value){
		  block.setMetadata(key,new FixedMetadataValue(this,value));
	}
	
	public Boolean getMetadata(Block block, String key){
		List<MetadataValue> values = block.getMetadata(key);
		for(MetadataValue value : values){
			if(value.getOwningPlugin().getDescription().getName().equals(this.getDescription().getName())){
				if(value.value() != null){
					return value.asBoolean();
				}else{
					return false;
				}
			}
		}
		return false;
	}
	
	public String getMetadataString(Block block, String key){
		List<MetadataValue> values = block.getMetadata(key);
		for(MetadataValue value : values){
			if(value.getOwningPlugin().getDescription().getName().equals(this.getDescription().getName())){
				if(value.value() != null){
					return value.asString();
				}else{
					return null;
				}
			}
		}
		return null;
	}
}