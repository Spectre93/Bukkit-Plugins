package nl.spectre93.just.sponge;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.plugin.Plugin;

public class MyFlowListener implements Listener{
	
	private final JustSponge plugin;
	
	public MyFlowListener(Plugin plugin) {
		this.plugin = (JustSponge)plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void flowWaterEvent(BlockFromToEvent evt) {
		if(!evt.isCancelled()){
			Material type = evt.getBlock().getType();
			if((type.equals(Material.WATER)) || (type.equals(Material.STATIONARY_WATER))){
				evt.setCancelled(true);
				Block toBlock = evt.getToBlock();
				if(!plugin.blockNear(toBlock.getWorld(), Material.SPONGE, toBlock.getLocation(), null)){
					evt.setCancelled(false);
				}
			}
		}
	}
}
