package nl.spectre93.JustDropMoreBlocks;

import java.io.File;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class JustDropMoreBlocks extends JavaPlugin implements Listener{

	private String configFile = getDataFolder() + File.separator + "config.yml";
	
	public void onEnable() {	
		getServer().getPluginManager().registerEvents(this, this);
		
		if(!(new File(configFile).exists())){
			getConfig().options().header("JustDropMoreBlocks, Made by Spectre93.\n" +
										 "The formula for the amount of EXTRA blocks dropped \n" +
										 "(so in addition to the normal drop) is: Random * FortuneLevel * multiplier\n" + 
										 "Random is a random number from 0 to 1, so you can get 0 extra drops.\n" +
										 "Here you can adjust the multiplier to get even more drops! (Use a dot and not a comma!)");
			getConfig().set("multiplier", 1.0);
			this.saveConfig();
		}
		
		if(!getConfig().contains("multiplier")) {getConfig().set("multiplier", 1.0); saveConfig();}
	}
	
	public void onDisable(){
		HandlerList.unregisterAll();
	}
	
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
		return true;
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onThis(BlockBreakEvent evt) {
		int level = evt.getPlayer().getItemInHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
		if(level <= 0) return;
		
		int drops = (int) (Math.random() * level * getConfig().getDouble("multiplier"));
		Object[] stack = evt.getBlock().getDrops().toArray();
		for(int i = 0; i < drops; i++){			
			for(int j = 0; j < stack.length; j++){
				evt.getPlayer().getWorld().dropItemNaturally(evt.getBlock().getLocation(), (ItemStack)stack[j]);
			}
		}
		getServer().getLogger().finest("Player: " + evt.getPlayer().getName() + " mined a block with fortune enchantment. Level: " + level + ", Extra drops: " + drops);
	}
}
