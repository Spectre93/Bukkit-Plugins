package nl.spectre93.just.sponge;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.plugin.Plugin;

public class MyBlockPlaceListener implements Listener{
	
	private final JustSponge plugin;
	
	public MyBlockPlaceListener(Plugin plugin) {
		this.plugin = (JustSponge) plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockPlace(BlockPlaceEvent evt) {
		Block block = evt.getBlock();
		if(block.getType().equals(Material.SPONGE)){
			plugin.addSponge(new SpongeBlock(block));
		}else if(isWater(block)){
			if(plugin.blockNear(block.getWorld(), Material.SPONGE, block.getLocation(), null)){
				evt.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerBucketUse(PlayerBucketEmptyEvent evt){
		Block b = evt.getBlockClicked().getRelative(evt.getBlockFace());
		if(plugin.blockNear(b.getWorld(), Material.SPONGE, b.getLocation(), null)){
			evt.setCancelled(true);
		}	
	}
	
	public static boolean isWater(Block b){
		return ((b.getType().equals(Material.WATER)) || (b.getType().equals(Material.STATIONARY_WATER))); 
	}
}
