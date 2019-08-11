package com.hollandjake.dogbot.modules;

import com.hollandjake.chatbot.Chatbot;
import com.hollandjake.chatbot.exceptions.MalformedCommandException;
import com.hollandjake.chatbot.utils.DatabaseCommandModule;
import com.hollandjake.messenger_bot_api.message.Message;
import com.hollandjake.messenger_bot_api.message.MessageComponent;
import com.hollandjake.messenger_bot_api.message.Text;

import java.sql.Connection;
import java.sql.SQLException;

import static com.hollandjake.chatbot.utils.CONSTANTS.ACTIONIFY;


public class Gran extends DatabaseCommandModule {
	private final String GRAN_REGEX = ACTIONIFY("gran");

	public Gran(Chatbot chatbot) {
		super(chatbot);
	}

	@Override
	public void prepareStatements(Connection connection) throws SQLException {
		super.prepareStatements(connection);
	}

	@Override
	@SuppressWarnings("Duplicates")
	public boolean process(Message message) throws MalformedCommandException {
		for (MessageComponent component : message.getComponents()) {
			String match = getMatch(component);
			if (!match.isEmpty()) {
				if (match.equals(GRAN_REGEX)) {
					chatbot.sendMessage("Granny says: \"" + getRandomResponse() + "\"");
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
			if (text.matches(GRAN_REGEX)) {
				return GRAN_REGEX;
			}
		}
		return "";
	}
}
