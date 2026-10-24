package pk.ajneb97.tasks;

import pk.ajneb97.PlayerKits2;
import pk.ajneb97.platform.PlatformTask;

public class PlayerDataSaveTask {

	private final PlayerKits2 plugin;
	private PlatformTask task;

	public PlayerDataSaveTask(PlayerKits2 plugin) {
		this.plugin = plugin;
	}

	public void end() {
		if (task != null) {
			task.cancel();
			task = null;
		}
	}

	public void start(int seconds) {
		long ticks = Math.max(20L, seconds * 20L);
		task = plugin.getPlatformScheduler().runAsyncTimer(this::execute, ticks, ticks);
	}

	public void execute() {
		plugin.getConfigsManager().getPlayersConfigManager().saveConfigs();
	}
}
