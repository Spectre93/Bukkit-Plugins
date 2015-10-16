package nl.spectre93.just.sponge;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.Plugin;

public class MyBlockBreakListener implements Listener{
	
	public MyBlockBreakListener(Plugin plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockBreak(BlockBreakEvent evt) {
		Block block = evt.getBlock();
		
		if(block.getType().equals(Material.SPONGE)){
			JustSponge.delSponge(new SpongeBlock(block));
		}
	}
}
