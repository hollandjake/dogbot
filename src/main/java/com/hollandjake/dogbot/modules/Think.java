package com.hollandjake.dogbot.modules;

import com.hollandjake.chatbot.Chatbot;
import com.hollandjake.chatbot.exceptions.MalformedCommandException;
import com.hollandjake.chatbot.utils.CommandModule;
import com.hollandjake.messenger_bot_api.message.Message;
import com.hollandjake.messenger_bot_api.message.MessageComponent;
import com.hollandjake.messenger_bot_api.message.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hollandjake.chatbot.utils.CONSTANTS.ACTIONIFY;


public class Think extends CommandModule {
	private final String THINK_REGEX = ACTIONIFY("think");
	private final String MULTI_THINK_REGEX = ACTIONIFY("think (\\d*)");
	private final String THONK_REGEX = ACTIONIFY("thonk");
	private final String MULTI_THONK_REGEX = ACTIONIFY("thonk (\\d*)");

	public Think(Chatbot chatbot) {
		super(chatbot);
	}

	@Override
	@SuppressWarnings("Duplicates")
	public boolean process(Message message) throws MalformedCommandException {
		for (MessageComponent component : message.getComponents()) {
			String match = getMatch(component);
			if (match.equals(THINK_REGEX) || match.equals(THONK_REGEX)) {
				chatbot.sendMessage("\uD83E\uDD14");
				return true;
			} else if (match.equals(MULTI_THINK_REGEX) || match.equals(MULTI_THONK_REGEX)) {
				String text = ((Text) component).getText();
				Matcher matcher = Pattern.compile(match).matcher(text);
				if (matcher.find()) {
					int repeats = Integer.parseInt(matcher.group(1));
					if (repeats > 100) {
						chatbot.sendMessage("That's a bit too much thinking right there!");
					} else {
						chatbot.sendMessage(new String(new char[repeats]).replace("\0", "\uD83E\uDD14"));
					}
					return true;
				} else {
					throw new MalformedCommandException();
				}
			}
		}
		return false;
	}

	@Override
	@SuppressWarnings("Duplicates")
	public String getMatch(MessageComponent component) {
		if (component instanceof Text) {
			String text = ((Text) component).getText();
			if (text.matches(THINK_REGEX)) {
				return THINK_REGEX;
			} else if (text.matches(MULTI_THINK_REGEX)) {
				return MULTI_THINK_REGEX;
			} else if (text.matches(THONK_REGEX)) {
				return THONK_REGEX;
			} else if (text.matches(MULTI_THONK_REGEX)) {
				return MULTI_THONK_REGEX;
			}
		}
		return "";
	}
}
