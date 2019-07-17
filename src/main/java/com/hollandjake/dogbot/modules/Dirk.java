package com.hollandjake.dogbot.modules;

import com.hollandjake.chatbot.Chatbot;
import com.hollandjake.chatbot.utils.CommandModule;
import com.hollandjake.messenger_bot_api.message.Message;
import com.hollandjake.messenger_bot_api.message.MessageComponent;
import com.hollandjake.messenger_bot_api.message.Text;

import static com.hollandjake.chatbot.utils.CONSTANTS.ACTIONIFY;


public class Dirk extends CommandModule {
	private final String DIRK_REGEX = ACTIONIFY("(dirk|god)");

	public Dirk(Chatbot chatbot) {
		super(chatbot);
	}

	@Override
	@SuppressWarnings("Duplicates")
	public boolean process(Message message) {
		for (MessageComponent component : message.getComponents()) {
			String match = getMatch(component);
			if (match.equals(DIRK_REGEX)) {
				chatbot.sendMessageWithImage("Our lord and saviour has greeted us", "https://staffwww.dcs.shef.ac.uk/people/D.Sudholt/Dirk_Sudholt-cropped.jpg");
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
			if (text.matches(DIRK_REGEX)) {
				return DIRK_REGEX;
			}
		}
		return "";
	}
}
