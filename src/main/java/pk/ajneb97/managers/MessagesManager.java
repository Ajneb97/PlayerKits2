package pk.ajneb97.managers;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;

import lombok.Getter;
import lombok.Setter;
import pk.ajneb97.utils.OtherUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
public class MessagesManager {

	private String timeSeconds;
	private String timeMinutes;
	private String timeHours;
	private String timeDays;
	private String requirementsMessageStatusSymbolTrue;
	private String requirementsMessageStatusSymbolFalse;
	private String cooldownPlaceholderReady;
	private String prefix;

	public void sendMessage(CommandSender sender, String message, boolean prefix){
		if(!message.isEmpty()){
			if(prefix){
				sender.sendMessage(getColoredMessage(this.prefix+message));
			}else{
				sender.sendMessage(getColoredMessage(message));
			}
		}
	}

	public static String getColoredMessage(String message) {
		if(OtherUtils.isNew()) {
			Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
			Matcher match = pattern.matcher(message);
			
			while(match.find()) {
				String color = message.substring(match.start(),match.end());
				message = message.replace(color, ChatColor.of(color)+"");
				
				match = pattern.matcher(message);
			}
		}

		message = ChatColor.translateAlternateColorCodes('&', message);
		return message;
	}
}
