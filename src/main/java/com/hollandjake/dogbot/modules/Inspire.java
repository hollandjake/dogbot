package com.hollandjake.dogbot.modules;

import com.hollandjake.chatbot.Chatbot;
import com.hollandjake.chatbot.utils.CommandModule;
import com.hollandjake.messenger_bot_api.message.Message;
import com.hollandjake.messenger_bot_api.message.MessageComponent;
import com.hollandjake.messenger_bot_api.message.Text;

import static com.hollandjake.chatbot.utils.CONSTANTS.ACTIONIFY;
import static com.hollandjake.chatbot.utils.CONSTANTS.GET_PAGE_SOURCE;


public class Inspire extends CommandModule {
	private final String INSPIRE_REGEX = ACTIONIFY("inspire");

	public Inspire(Chatbot chatbot) {
		super(chatbot);
	}

	@Override
	@SuppressWarnings("Duplicates")
	public boolean process(Message message) {
		for (MessageComponent component : message.getComponents()) {
			String match = getMatch(component);
			if (!match.isEmpty()) {
				if (match.equals(INSPIRE_REGEX)) {
					String imgURL = GET_PAGE_SOURCE("http://inspirobot.me/api?generate=true");
					chatbot.sendMessageWithImage("Inspiring.", imgURL);
				}
				return true;
			}
		}
		return false;
	}

	@Override
	@SuppressWarnings("Duplicates")
	public String getMatch(MessageComponent component) {
		if (component instanceof Text) {
			String text = ((Text) component).getText();
			if (text.matches(INSPIRE_REGEX)) {
				return INSPIRE_REGEX;
			}
		}
		return "";
	}
}
