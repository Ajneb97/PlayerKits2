package pk.ajneb97.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.util.List;

public class JSONMessageAdventure {

	private Player player;
	private String text;
	private Component hover;
	private String suggestCommand;
	private ClickEvent executeCommand;

	public JSONMessageAdventure(Player player, String text) {
		this.player = player;
		this.hover = null;
		this.text = text;
	}

	public JSONMessageAdventure hover(List<String> list) {
		hover = MiniMessage.miniMessage().deserialize(list.get(0));
		for(int i=1;i<list.size();i++){
			hover = hover.appendNewline().append(MiniMessage.miniMessage().deserialize(list.get(i)));
		}
		return this;
	}

	public JSONMessageAdventure setSuggestCommand(String command) {
		this.suggestCommand = command;
		return this;
	}

	public JSONMessageAdventure setExecuteCommand(String command) {
		this.executeCommand = ClickEvent.runCommand(command);
		return this;
	}

	public void send() {
		Component message = MiniMessage.miniMessage().deserialize(text);
		if(hover != null) {
			message = message.hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(hover));
		}
		if(executeCommand != null) {
			message = message.clickEvent(executeCommand);
		}

		player.sendMessage(message);
	}
}
