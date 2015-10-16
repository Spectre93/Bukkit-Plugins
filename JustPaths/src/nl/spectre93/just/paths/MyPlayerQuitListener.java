package nl.spectre93.just.paths;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class MyPlayerQuitListener implements Listener{
	
	protected final JustPaths plugin;
	
	public MyPlayerQuitListener(JustPaths plugin){
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerQuit(PlayerQuitEvent evt) {
		plugin.clear(evt.getPlayer());
	}
}
