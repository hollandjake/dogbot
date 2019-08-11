package com.hollandjake.dogbot.modules;

import com.hollandjake.chatbot.Chatbot;
import com.hollandjake.chatbot.utils.DatabaseCommandModule;
import com.hollandjake.messenger_bot_api.message.Message;
import com.hollandjake.messenger_bot_api.message.MessageComponent;
import com.hollandjake.messenger_bot_api.message.Text;

import java.sql.Connection;
import java.sql.SQLException;

import static com.hollandjake.chatbot.utils.CONSTANTS.ACTIONIFY;


public class Reacts extends DatabaseCommandModule {
	private final String REACT_REGEX = ACTIONIFY("react( (.*))?");
	private final String REAC_REGEX = ACTIONIFY("reac+( (.*))?");

	public Reacts(Chatbot chatbot) {
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
				if (match.equals(REACT_REGEX) || match.equals(REAC_REGEX)) {
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
			if (text.matches(REACT_REGEX)) {
				return REACT_REGEX;
			} else if (text.matches(REAC_REGEX)) {
				return REAC_REGEX;
			}
		}
		return "";
	}
}
