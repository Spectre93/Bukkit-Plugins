package nl.spectre93.just.sponge;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class JustSponge extends JavaPlugin{
	
	protected static String spongeFile;
	protected static String configFile;
	protected static Logger log = Bukkit.getLogger();
	private static int strength;
	private static int fixedStrength;
	private boolean recipe;
	private ShapedRecipe spongeRecipe;

	public void onEnable(){	
		setUpConfiguration();
		new MyBlockBreakListener(this);
		new MyBlockPlaceListener(this);
		new MyFlowListener(this);
	}
	
	public void onDisable(){
		HandlerList.unregisterAll();
	}
	
	private void setUpConfiguration() {
		getServer().getLogger().getParent().setLevel(Level.ALL);
		
		spongeFile = getDataFolder() + File.separator + "sponges.dat"; 
		configFile = getDataFolder() + File.separator + "config.yml"; 
		
		if(!(new File(configFile)).exists()){
			getConfig().options().header("JustSponge, Made by Spectre93.");
			this.saveConfig();
		}
		
		if(!getConfig().contains("strength")) {getConfig().set("strength", 3); saveConfig();}
		if(!getConfig().contains("strength-max")) {getConfig().set("strength-max", 7); saveConfig();}
		if(!getConfig().contains("craftablesponges")) {getConfig().set("craftablesponges", true); saveConfig();}
		
		if(!(new File(spongeFile)).exists()){
			JustSponge.write(new ArrayList<SpongeBlock>());
		}else{
			JustSponge.write(getSponges());
		}
		
		initRecipe();
		setStrength(getStrength());
		
		if(getConfig().getBoolean("craftablesponges")){
			getServer().addRecipe(spongeRecipe);
		}
	}
	
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){   
    	if (command.getName().equalsIgnoreCase("spongestrength")) { 
    		if(sender.hasPermission("just.sponge.strength")){
				if(args.length == 0 || args.length > 1){
					return false;
				}else{
					int input;
					try {
						input = Integer.parseInt(args[0]);
					} catch (NumberFormatException e) {
						sender.sendMessage("Please only use uneven integers."); 
						return true;
					}
					if((input % 2 != 0) && (input >= 3) && (input <= getConfig().getInt("strength-max"))){
						setStrength(input); 
						sender.sendMessage("Sponge strength changed to " + input + "!");
					}else{
						sender.sendMessage("Please only use uneven integers. Minimum: 3, Maximum: " + getConfig().getInt("strength-max"));
						return true;
					}
				}
    		}else sender.sendMessage("You do not have permission to use this command.");
    	}
    	else if(command.getName().equalsIgnoreCase("spongerecipe")){
    		if(sender.hasPermission("just.sponge.recipe")){
	    		toggleRecipe();
	    		sender.sendMessage("Sponge recipe in game: " + recipe);
    		}else sender.sendMessage("You do not have permission to use this command.");
    	}
		return true;		
	}
	
	public void toggleRecipe(){
		recipe = !recipe;
		getConfig().set("craftablesponges", recipe);
		saveConfig();
		
		if (!recipe) {   
			removeRecipe(spongeRecipe);
	    }else{
	    	getServer().addRecipe(spongeRecipe);
	    }
	}
	
	private void removeRecipe(ShapedRecipe inputRecipe){
		Iterator<Recipe> it = getServer().recipeIterator(); 
		while(it.hasNext()){
			Recipe itRecipe = it.next();
			if(itRecipe instanceof ShapedRecipe){
				ShapedRecipe itShaped = (ShapedRecipe) itRecipe;
				
				Map<Character, ItemStack> m = itShaped.getIngredientMap();
				Map<Character, ItemStack> n = inputRecipe.getIngredientMap();
				
				if(m.values().containsAll(n.values())){
					String[] list = itShaped.getShape();
					String listString = list[0] + list[1] + list[2];
					
					String[] list2 = inputRecipe.getShape();
					String listString2 = list2[0] + list2[1] + list2[2];
					
					for(int i = 0; i < listString.length(); i++){
						if(!m.get(listString.charAt(i)).equals(n.get(listString2.charAt(i)))){
							getLogger().fine("Recipe not found.");
							return;
						}
					}	
					it.remove();
					getLogger().fine("Recipe removed!");
				}
			}
		}
	}
	
	private void initRecipe(){
		spongeRecipe = new ShapedRecipe(new ItemStack(Material.SPONGE, 1));
		spongeRecipe.shape(new String[] { "BAB", "ABA", "BAB" });
		spongeRecipe.setIngredient('A', Material.SAND);
		spongeRecipe.setIngredient('B', Material.STRING);
	}
	
	protected void addSponge(SpongeBlock block){
		ArrayList<SpongeBlock> temp = getSponges();
		
		if(temp.size() == 0){
			temp.add(block);
			JustSponge.write(temp);
			blockNear(block.getWorld(), Material.STATIONARY_WATER, block.getLocation(), Material.AIR);
			return;
		}
		
		for(SpongeBlock sb : temp){
			if(sb.blockState.equals(block.blockState)){
				return;
			}
		}
		
		temp.add(block);
		JustSponge.write(temp);
		blockNear(block.getWorld(), Material.STATIONARY_WATER, block.getLocation(), Material.AIR);
	}

	protected static void delSponge(SpongeBlock block){
		ArrayList<SpongeBlock> temp = getSponges();
		
		for(SpongeBlock sb : temp){
			if(sb.blockState.equals(block.blockState)){
				temp.remove(sb);
				JustSponge.write(temp);
				updateWater(block, false, 0);
				return;
			}
		}			
	}
	
	private static void updateWater(SpongeBlock block, boolean dueStrUpdate, int oldStrength) {
		int tempFixedStrength = fixedStrength;
		int tempStrength = strength;
		if(dueStrUpdate){
			tempFixedStrength = ((oldStrength -1) / 2);
			tempStrength = oldStrength;
		}
		 
		World world = block.getWorld(); 		
		Location location = block.getLocation();
		location.setX(location.getX() - tempFixedStrength);
		location.setZ(location.getZ() - tempFixedStrength);
		Location startLocation = location;
		
		int y = location.getBlockY();
		
		for(int x = startLocation.getBlockX() - 1; x < (startLocation.getBlockX() + tempStrength + 1); x++){
			for(y = location.getBlockY(); y < location.getBlockY() + tempFixedStrength; y++){
				for(int z = startLocation.getBlockZ() - 1; z < (startLocation.getBlockZ() + tempStrength + 1); z++){
					Material mat = world.getBlockAt(x, y, z).getType();
					if((mat.equals(Material.WATER)) || (mat.equals(Material.STATIONARY_WATER))){
						world.getBlockAt(x, y, z).setType(Material.WATER);
					}
				}
			}
		}	
		
		y = location.getBlockY() + tempFixedStrength + 1;	
			
		for(int x = startLocation.getBlockX(); x < (startLocation.getBlockX() + tempStrength); x++){
			for(int z = startLocation.getBlockZ(); z < (startLocation.getBlockZ() + tempStrength); z++){
				Material mat = world.getBlockAt(x, y, z).getType();
				if((mat.equals(Material.WATER)) || (mat.equals(Material.STATIONARY_WATER))){
					world.getBlockAt(x, y, z).setType(Material.WATER);
				}
			}
		}	
	}

	private static void write(ArrayList<SpongeBlock> list){
		try {
			PrintWriter out = new PrintWriter(new FileWriter(spongeFile));
			for(int i = 0; i < list.size(); i++){
				if(list.get(i) != null){
					out.println(list.get(i).toString());
				}
			}
			out.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}
	
	private static ArrayList<SpongeBlock> getSponges(){
		ArrayList<SpongeBlock> temp = new ArrayList<SpongeBlock>();
		
		try{
		    FileReader readfile = new FileReader(spongeFile);
		    Scanner sc = new Scanner(readfile);
		    
		    while(sc.hasNext())	temp.add(SpongeBlock.read(sc));
		    
		    sc.close();
			readfile.close();
		}
		catch (Exception ex){
		    log.severe("Your sponge.dat file was corrupted, so I had to kill it.");
		    
		}
		return temp;
	}
	
	private void update(){
		ArrayList<SpongeBlock> temp = getSponges();
		for(SpongeBlock sb : temp){
			try {
				blockNear(sb.getWorld(), Material.WATER, sb.getLocation(), Material.AIR);
			} catch (Exception e) {
				log.info("Error updating sponges, pretty much impossible to reach.");
			}
		}
		log.fine("Sponges updated.");
	}

	public int getStrength() {
		if(getConfig().getInt("strength") > getConfig().getInt("strength-max")){
			getConfig().set("strength", getConfig().getInt("strength-max"));
		}
		return getConfig().getInt("strength"); 
	}

	private void setStrength(int newStrength) {
		this.getConfig().set("strength", newStrength);
		saveConfig();
		
		int oldStrength = strength;
		strength = newStrength;
		fixedStrength = ((strength - 1) / 2);
		
		if(newStrength < oldStrength){
			ArrayList<SpongeBlock> temp = getSponges();
			for(SpongeBlock sb : temp){
				updateWater(sb, true, oldStrength);
			}
		}
		
		update();
	}
	
	protected boolean blockNear(World w, Material lookFor, Location inputLocation, Material replaceWith) {	
		World world = w; 		
		Location location = inputLocation;		
		location.setX(location.getX() - fixedStrength);
		location.setY(location.getY() - fixedStrength);
		location.setZ(location.getZ() - fixedStrength);
		Location startLocation = location;
		
		for(int x = startLocation.getBlockX(); x < (startLocation.getBlockX() + strength); x++){
			for(int y = startLocation.getBlockY(); y < (startLocation.getBlockY() + strength); y++){
				for(int z = startLocation.getBlockZ(); z < (startLocation.getBlockZ() + strength); z++){
					Material type = world.getBlockAt(x, y, z).getType();
					if(type.equals(Material.SPONGE)){
						if(lookFor.equals(Material.SPONGE)){
							return true;
						}
					}else if((type.equals(Material.WATER)) || (type.equals(Material.STATIONARY_WATER))){
						if(replaceWith != null){
							world.getBlockAt(x, y, z).setType(replaceWith);
						}
					}
				}
			}
		}
		return false;
	}
}

