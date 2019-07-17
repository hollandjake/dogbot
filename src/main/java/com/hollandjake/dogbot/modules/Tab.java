package com.hollandjake.dogbot.modules;

import com.hollandjake.chatbot.Chatbot;
import com.hollandjake.chatbot.utils.CommandModule;
import com.hollandjake.messenger_bot_api.message.Message;
import com.hollandjake.messenger_bot_api.message.MessageComponent;
import com.hollandjake.messenger_bot_api.message.Text;

import static com.hollandjake.chatbot.utils.CONSTANTS.ACTIONIFY;


public class Tab extends CommandModule {
	private final String TAB_REGEX = ACTIONIFY("tab");

	public Tab(Chatbot chatbot) {
		super(chatbot);
	}

	@Override
	@SuppressWarnings("Duplicates")
	public boolean process(Message message) {
		for (MessageComponent component : message.getComponents()) {
			String match = getMatch(component);
			if (match.equals(TAB_REGEX)) {
				chatbot.sendMessageWithImage("\uD83D\uDEA8 WEE WOO WEE WOO \uD83D\uDEA8", "https://www.hollandjake.com/dogbot/tabulance.png");
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
			if (text.matches(TAB_REGEX)) {
				return TAB_REGEX;
			}
		}
		return "";
	}
}
