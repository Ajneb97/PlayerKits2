package pk.ajneb97.managers;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import pk.ajneb97.utils.OtherUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessagesManager {

	private String timeSeconds;
	private String timeMinutes;
	private String timeHours;
	private String timeDays;
	private String requirementsMessageStatusSymbolTrue;
	private String requirementsMessageStatusSymbolFalse;
	private String cooldownPlaceholderReady;
	private String prefix;


	public String getTimeSeconds() {
		return timeSeconds;
	}

	public void setTimeSeconds(String timeSeconds) {
		this.timeSeconds = timeSeconds;
	}

	public String getTimeMinutes() {
		return timeMinutes;
	}

	public void setTimeMinutes(String timeMinutes) {
		this.timeMinutes = timeMinutes;
	}

	public String getTimeHours() {
		return timeHours;
	}

	public void setTimeHours(String timeHours) {
		this.timeHours = timeHours;
	}

	public String getTimeDays() {
		return timeDays;
	}

	public void setTimeDays(String timeDays) {
		this.timeDays = timeDays;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getRequirementsMessageStatusSymbolTrue() {
		return requirementsMessageStatusSymbolTrue;
	}

	public void setRequirementsMessageStatusSymbolTrue(String requirementsMessageStatusSymbolTrue) {
		this.requirementsMessageStatusSymbolTrue = requirementsMessageStatusSymbolTrue;
	}

	public String getRequirementsMessageStatusSymbolFalse() {
		return requirementsMessageStatusSymbolFalse;
	}

	public void setRequirementsMessageStatusSymbolFalse(String requirementsMessageStatusSymbolFalse) {
		this.requirementsMessageStatusSymbolFalse = requirementsMessageStatusSymbolFalse;
	}

	public String getCooldownPlaceholderReady() {
		return cooldownPlaceholderReady;
	}

	public void setCooldownPlaceholderReady(String cooldownPlaceholderReady) {
		this.cooldownPlaceholderReady = cooldownPlaceholderReady;
	}

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
