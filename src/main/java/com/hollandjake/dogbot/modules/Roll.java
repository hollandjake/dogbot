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


public class Roll extends CommandModule {
	private final String ROLL_DICE_REGEX = ACTIONIFY("roll");
	private final String ROLL_REGEX = ACTIONIFY("roll (\\d+)");

	public Roll(Chatbot chatbot) {
		super(chatbot);
	}

	private void roll(int lower, int upper) {
		int number = (int) (Math.random() * (upper - lower) + lower);
		chatbot.sendMessage("You rolled " + number);
	}

	@Override
	@SuppressWarnings("Duplicates")
	public boolean process(Message message) throws MalformedCommandException {
		for (MessageComponent component : message.getComponents()) {
			String match = getMatch(component);
			if (match.equals(ROLL_DICE_REGEX)) {
				roll(1, 6);
				return true;
			} else if (match.equals(ROLL_REGEX)) {
				String text = ((Text) component).getText();
				Matcher matcher = Pattern.compile(ROLL_REGEX).matcher(text);
				if (matcher.find() && !matcher.group(1).isEmpty()) {
					try {
						roll(1, Integer.parseInt(matcher.group(1)));
						return true;
					} catch (NumberFormatException e) {
						throw new MalformedCommandException();
					}
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
			if (text.matches(ROLL_DICE_REGEX)) {
				return ROLL_DICE_REGEX;
			} else if (text.matches(ROLL_REGEX)) {
				return ROLL_REGEX;
			}
		}
		return "";
	}
}
