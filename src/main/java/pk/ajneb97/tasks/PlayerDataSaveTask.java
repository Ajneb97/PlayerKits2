package pk.ajneb97.tasks;

import org.bukkit.scheduler.BukkitRunnable;
import pk.ajneb97.PlayerKits2;

public class PlayerDataSaveTask {

	private PlayerKits2 plugin;
	private boolean end;
	public PlayerDataSaveTask(PlayerKits2 plugin) {
		this.plugin = plugin;
		this.end = false;
	}
	
	public void end() {
		end = true;
	}
	
	public void start(int minutes) {
		long ticks = minutes*60*20;
		
		new BukkitRunnable() {
			@Override
			public void run() {
				if(end) {
					this.cancel();
				}else {
					execute();
				}
			}
			
		}.runTaskTimerAsynchronously(plugin, 0L, ticks);
	}
	
	public void execute() {
		plugin.getConfigsManager().getPlayersConfigManager().saveConfigs();
	}
}
