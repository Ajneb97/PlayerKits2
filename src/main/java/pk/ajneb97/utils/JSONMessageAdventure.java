package pk.ajneb97.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.util.List;

public class JSONMessageAdventure {

	private final Player player;
	private final String text;
	private Component hover;

	public JSONMessageAdventure(Player player, String text) {
		this.player = player;
		this.hover = null;
		this.text = text;
	}

	public JSONMessageAdventure hover(List<String> list) {
		hover = MiniMessage.miniMessage().deserialize(list.getFirst());
		for(int i=1;i<list.size();i++){
			hover = hover.appendNewline().append(MiniMessage.miniMessage().deserialize(list.get(i)));
		}
		return this;
	}

	public void send() {
		Component message = MiniMessage.miniMessage().deserialize(text);
		if(hover != null) {
			message = message.hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(hover));
		}

		player.sendMessage(message);
	}
}
