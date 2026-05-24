package pk.ajneb97.tasks;

import io.github.projectunified.minelib.scheduler.async.AsyncScheduler;
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
		AsyncScheduler.get(plugin).runTimer(() -> {
			if(end) {
				return false;
			}else {
				execute();
				return true;
			}
		}, 0L, ticks);
	}
	
	public void execute() {
		plugin.getConfigsManager().getPlayersConfigManager().saveConfigs();
	}
}
