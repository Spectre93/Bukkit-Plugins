package nl.spectre93.just.paths;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class MyPlayerMoveListener implements Listener{
	
	protected final JustPaths plugin;
	
	public MyPlayerMoveListener(JustPaths plugin){
		this.plugin = plugin;
	}
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerMove(PlayerMoveEvent evt) {
		if(!plugin.hasPath(evt.getPlayer())) return;
		Block block = evt.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN);
		if(plugin.getMetadata(block, "justpaths")) return;
		if(block.isEmpty()) return;
		if(block.isLiquid()) return;
		if(IDAPI.getIdOfMaterial(block.getType()) == plugin.getPathId(evt.getPlayer())) return;
		
		plugin.putBlock(evt.getPlayer(), block);
		BlockState temp = block.getState();
		block.setType(IDAPI.getMaterialById(plugin.getPathId(evt.getPlayer())));
		plugin.setMetadata(block, "justpaths", true);
		plugin.setMetadata(block, "owner", evt.getPlayer().getName());
		block.getState().update(true);

		Bukkit.getServer().getPluginManager().callEvent(
				new BlockPlaceEvent(block, temp, block.getRelative(BlockFace.DOWN), evt.getPlayer().getItemInHand(), evt.getPlayer(), true)
		);
	}
}
