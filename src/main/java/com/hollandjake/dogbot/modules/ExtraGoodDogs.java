package com.hollandjake.dogbot.modules;

import com.hollandjake.chatbot.Chatbot;
import com.hollandjake.chatbot.utils.DatabaseCommandModule;
import com.hollandjake.messenger_bot_api.message.Message;
import com.hollandjake.messenger_bot_api.message.MessageComponent;
import com.hollandjake.messenger_bot_api.message.Text;

import java.sql.Connection;
import java.sql.SQLException;

import static com.hollandjake.chatbot.utils.CONSTANTS.ACTIONIFY;


public class ExtraGoodDogs extends DatabaseCommandModule {
	private final String EXTRA_GOOD_DOG_REGEX = ACTIONIFY("extragooddog");
	private final String EDG_REGEX = ACTIONIFY("egd");

	public ExtraGoodDogs(Chatbot chatbot) {
		super(chatbot);
	}

	@Override
	public void prepareStatements(Connection connection) throws SQLException {
		super.prepareStatements(connection);
	}

	@Override
	@SuppressWarnings("Duplicates")
	public boolean process(Message message) {
		for (MessageComponent component : message.getComponents()) {
			String match = getMatch(component);
			if (!match.isEmpty()) {
				if (match.equals(EXTRA_GOOD_DOG_REGEX) || match.equals(EDG_REGEX)) {
					Message sending = Message.fromString(chatbot.getThread(), chatbot.getMe(), "Extra good woof!");
					sending.getComponents().add(getRandomImage());
					chatbot.sendMessage(sending);
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
			if (text.matches(EXTRA_GOOD_DOG_REGEX)) {
				return EXTRA_GOOD_DOG_REGEX;
			} else if (text.matches(EDG_REGEX)) {
				return EDG_REGEX;
			}
		}
		return "";
	}
}
