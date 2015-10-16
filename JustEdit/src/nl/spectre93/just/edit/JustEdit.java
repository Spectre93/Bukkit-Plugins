package nl.spectre93.just.edit;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

public class JustEdit extends JavaPlugin implements Listener{
	
	private BlockState[][][] blocks;
	private BlockState[][][] temp;

	public void onEnable(){
		getLogger().setLevel(Level.ALL);
		getLogger().fine(nameVersion() + " charging up...");
		File file = new File(getDataFolder() + File.separator + "config.yml");	
		
		if(!file.exists()){
			this.getLogger().info("Generating config.yml...");
			this.getConfig().options().header(	"JustEdit, Made by Spectre93.\n" +
												"You can change the item id of the selection tool here.\n" +
												"Default is the wooden axe (id: 271).\n" +
												"You can use anything that you can put in your hand.");
			this.getConfig().set("ID", 271);			
			this.saveConfig();
		}
		
		this.getServer().getPluginManager().registerEvents(this, this);
		getLogger().fine(nameVersion() + " enabled!");
	}
	
	public void onDisable(){
		getLogger().fine(nameVersion() + " terminating...");
		HandlerList.unregisterAll();
    	getLogger().fine(nameVersion() + " succesfully terminated.");
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void eventListener(PlayerInteractEvent evt) {
		if(evt.getPlayer().getItemInHand().getTypeId() == getConfig().getInt("ID")){
			getLogger().finer("Has item id " + getConfig().getInt("ID") + " equipped.");
			if(evt.getAction().equals(Action.LEFT_CLICK_BLOCK)){
				getLogger().finer("[LMB] " + evt.getPlayer().getDisplayName() + " selected the block at " + evt.getClickedBlock().toString() + ".");
				setMetadata(evt.getPlayer(), "leftClickedBlock", evt.getClickedBlock());	
			}else if(evt.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
				getLogger().finer("[RMB] " + evt.getPlayer().getDisplayName() + " selected the block at " + evt.getClickedBlock().toString() + ".");
				setMetadata(evt.getPlayer(), "rightClickedBlock", evt.getClickedBlock());
			}
		}	
	}
	
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command has to be called by a player!");
			return true;
    	}
		
		Player player = (Player)sender;
		
		if (command.getName().equalsIgnoreCase("replace")) {
    		if (!player.hasPermission("just.replace")){
    			player.sendMessage(ChatColor.RED + "You don't have permission to do that.");
    			return true;
    		}
    		
    		if(args.length == 0 || args.length > 2){
    			player.sendMessage("The correct usage is:");
    			return false;
    		}
    		
    		if(selectedBlocks(player) == false){
    			return true;
    		}
				
    		saveBlocks(player);	
    	
    		if(args.length == 1){ //replace all by given id
    			try {
					changeBlockState(player, -1,Integer.parseInt(args[0]));
				} catch (NumberFormatException e) {
					player.sendMessage("Please provide an integer instead of \"" + args[0] + "\".");
					return true;
				}
    			return true;
    		}
    		
    		if(args.length == 2){ //replace id by given id
    			try {
					changeBlockState(player, Integer.parseInt(args[0]),Integer.parseInt(args[1]));
				} catch (NumberFormatException e) {
					player.sendMessage("Please provide only integers.");
					return true;
				}
    			return true;
    		}
    	}else if(command.getName().equalsIgnoreCase("undo")){
    		if (!player.hasPermission("just.undo")){
    			player.sendMessage(ChatColor.RED + "You don't have permission to do that.");
    			return true;
    		}
    		
    		if(args.length > 0){
    			player.sendMessage("The correct usage is:");
    			return false;
    		}
    		
    		if(blocks != null){
    			restoreBlocks(player);
    		}
    	}
		return true;		
	}
	
	private void saveBlocks(Player player){
		getLogger().fine("[" + player.getDisplayName() + "]" + "Saving blocks....");
		
		if(selectedBlocks(player) == false){
			return;
		}
		
		temp = new BlockState[getDistX(player)][getDistY(player)][getDistZ(player)];
		for(int x = 0; x < getDistX(player); x++){
			for(int y = 0; y < getDistY(player); y++){
				for(int z = 0; z < getDistZ(player); z++){
    				temp[x][y][z] = player.getWorld().getBlockAt(x + getSmallestX(player), y + getSmallestY(player), z + getSmallestZ(player)).getState();
    				blocks = temp.clone();
    			}
			}
		}
		getLogger().fine("[" + player.getDisplayName() + "] " + "Blocks saved!");
	}
	
	private void restoreBlocks(Player player){
		getLogger().fine("[" + player.getDisplayName() + "] " + "Restoring blocks....");
		for(int x = 0; x < getDistX(player); x++){
			for(int y = 0; y < getDistY(player); y++){
				for(int z = 0; z < getDistZ(player); z++){
					temp[x][y][z].update(true);
    			}
			}
		}
		getLogger().fine("[" + player.getDisplayName() + "] " + "Blocks restored!");
	}
	
	private void changeBlockState(Player player, int from, int to){
		Boolean fromValue = false;
		if(from != -1){
			fromValue = true;
		}
		for(int x = 0; x < getDistX(player); x++){
			for(int y = 0; y < getDistY(player); y++){
				for(int z = 0; z < getDistZ(player); z++){
					if(fromValue && (blocks[x][y][z].getBlock().getTypeId() == from)){
						blocks[x][y][z].getBlock().setTypeId(to);
					}else if(fromValue == false){
						blocks[x][y][z].getBlock().setTypeId(to);
					}
					blocks[x][y][z].update();
    			}
			}
		}
	}

	public boolean selectedBlocks(Player player){
		if((getLeftClickedBlock(player) != null) && (getRightClickedBlock(player) != null)){
			return true;
		}
		player.sendMessage("Please select two points using the selection tool first.");
		return false;
	}
	
	public int getDistX(Player player){
		return Math.abs(getLeftClickedBlock(player).getX() - getRightClickedBlock(player).getX() + 1);
	}
	
	public int getDistY(Player player){
		return Math.abs(getLeftClickedBlock(player).getY() - getRightClickedBlock(player).getY() + 1);
	}
		
	public int getDistZ(Player player){
		return Math.abs(getLeftClickedBlock(player).getZ() - getRightClickedBlock(player).getZ() + 1);
	}
	
	public int getSmallestX(Player player){
		return Math.min(getLeftClickedBlock(player).getX(), getRightClickedBlock(player).getX());
	}
	
	public int getSmallestY(Player player){
		return Math.min(getLeftClickedBlock(player).getY(), getRightClickedBlock(player).getY());
	}
	
	public int getSmallestZ(Player player){
		return Math.min(getLeftClickedBlock(player).getZ(), getRightClickedBlock(player).getZ());
	}
	
	public Block getLeftClickedBlock(Player player){
		return getMetadata(player, "leftClickedBlock");
	}
	
	public Block getRightClickedBlock(Player player){
		return getMetadata(player, "rightClickedBlock");
	}
	
	public void setMetadata(Player player, String key, Object value){
		  player.setMetadata(key,new FixedMetadataValue(this,value));
	}
	
	public Block getMetadata(Player player, String key){
		List<MetadataValue> values = player.getMetadata(key);
		for(MetadataValue value : values){
			if(value.getOwningPlugin().getDescription().getName().equals(this.getDescription().getName())){
				return (Block)value.value();
			}
		}
		return null;
	}

	public String nameVersion(){
		return this.getDescription().getName() + " version " + this.getDescription().getVersion();
	}
}