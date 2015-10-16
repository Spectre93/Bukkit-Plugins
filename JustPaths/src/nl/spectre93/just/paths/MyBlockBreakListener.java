package nl.spectre93.just.paths;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class MyBlockBreakListener implements Listener{
	
	final JustPaths plugin;
	
	public MyBlockBreakListener(JustPaths plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockBreak(BlockBreakEvent evt) {
		Block block = evt.getBlock();
		if(plugin.getMetadata(block, "justpaths")){
			evt.setCancelled(true);
//			if(!plugin.getMetadataString(block, "owner").equals(evt.getPlayer().getName())){
//				evt.getPlayer().sendMessage("I'm sorry, but this block belongs to someone's path.");
//			}
		}
	}
}
