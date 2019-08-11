package com.hollandjake.dogbot.modules;

import com.hollandjake.chatbot.Chatbot;
import com.hollandjake.chatbot.exceptions.MalformedCommandException;
import com.hollandjake.chatbot.utils.DatabaseCommandModule;
import com.hollandjake.messenger_bot_api.message.Message;
import com.hollandjake.messenger_bot_api.message.MessageComponent;
import com.hollandjake.messenger_bot_api.message.Text;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hollandjake.chatbot.utils.CONSTANTS.ACTIONIFY;


public class EightBall extends DatabaseCommandModule {
	private final String NO_QUESTION_REGEX = ACTIONIFY("(8ball|ask)");
	private final String QUESTION_REGEX = ACTIONIFY("(8ball|ask) (.*)");

	public EightBall(Chatbot chatbot) {
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
				if (match.equals(NO_QUESTION_REGEX)) {
					chatbot.sendMessage("Please enter a question after the command");
				} else if (match.equals(QUESTION_REGEX)) {
					String text = ((Text) component).getText();
					Matcher matcher = Pattern.compile(QUESTION_REGEX).matcher(text);
					if (matcher.find() && !matcher.group(2).isEmpty()) {
						chatbot.sendMessage(getRandomResponse());
					} else {
						throw new MalformedCommandException();
					}
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
			if (text.matches(NO_QUESTION_REGEX)) {
				return NO_QUESTION_REGEX;
			} else if (text.matches(QUESTION_REGEX)) {
				return QUESTION_REGEX;
			}
		}
		return "";
	}
}
