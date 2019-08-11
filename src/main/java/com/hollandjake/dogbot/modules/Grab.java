package com.hollandjake.dogbot.modules;

import com.hollandjake.chatbot.Chatbot;
import com.hollandjake.chatbot.exceptions.MalformedCommandException;
import com.hollandjake.chatbot.utils.DatabaseCommandModule;
import com.hollandjake.messenger_bot_api.message.Message;
import com.hollandjake.messenger_bot_api.message.MessageComponent;
import com.hollandjake.messenger_bot_api.message.Text;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hollandjake.chatbot.utils.CONSTANTS.ACTIONIFY;


public class Grab extends DatabaseCommandModule {
	private final String GRAB_REGEX = ACTIONIFY("grab");
	private final String GRAB_OFFSET_REGEX = ACTIONIFY("grab (\\d+)");
	private final String LOCATE_REGEX = ACTIONIFY("(locate|grab) (.+)");

	private PreparedStatement SAVE_QUOTE;
	private PreparedStatement ALREADY_QUOTED;
	private PreparedStatement GET_MESSAGE_SIMILAR_TO;

	public Grab(Chatbot chatbot) {
		super(chatbot);
	}

	private void grab(Message commandMessage, int offset) throws SQLException {
		int targetId = commandMessage.getId() - offset;
		Message targetMessage = db.getMessage(targetId);
		if (targetMessage == null) {
			chatbot.sendMessage("That grab is a little too far for me");
		} else {
			save(commandMessage, targetMessage, true);
		}
	}

	private boolean save(Message commandMessage, Message message, boolean failOutput) {
		//Check if message contains a command
		if (message.getComponents().size() == 0) {
			if (failOutput) {
				chatbot.sendMessage("That message is empty");
			}
			return false;
		} else if (message.getSender().equals(commandMessage.getSender())) {
			if (failOutput) {
				chatbot.sendMessage("Did you just try and grab yourself? \uD83D\uDE20");
			}

			return false;
		} else if (chatbot.containsCommand(message)) {
			if (failOutput) {
				chatbot.sendMessage("Don't do that >:(");
			}
			return false;
		} else if (alreadyQuoted(message)) {
			if (failOutput) {
				chatbot.sendMessage("That quote has already been grabbed");
			}
			return false;
		} else {
			saveQuote(message);
			Message grabbed = Quotes.quoteMessage(message, null);
			grabbed.getComponents().add(0, Text.fromString("Grabbed: "));
			chatbot.sendMessage(grabbed);
			return true;
		}
	}

	private void saveQuote(Message message) {
		try {
			db.checkConnection();
			SAVE_QUOTE.setInt(1, chatbot.getThread().getId());
			SAVE_QUOTE.setInt(2, message.getId());
			SAVE_QUOTE.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private boolean alreadyQuoted(Message message) {
		try {
			db.checkConnection();
			ALREADY_QUOTED.setInt(1, chatbot.getThread().getId());
			ALREADY_QUOTED.setInt(2, message.getId());
			ResultSet resultSet = ALREADY_QUOTED.executeQuery();
			return resultSet.absolute(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	private void locate(Message commandMessage, String query) {
		try {
			GET_MESSAGE_SIMILAR_TO.setInt(1, chatbot.getThread().getId());
			GET_MESSAGE_SIMILAR_TO.setString(2, query);
			ResultSet resultSet = GET_MESSAGE_SIMILAR_TO.executeQuery();
			while (resultSet.next()) {
				if (save(commandMessage, db.getMessage(resultSet.getInt("message_id")), false)) {
					return;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		chatbot.sendMessage("I can't seem to find a message with \"" + query + "\" in it :'(");
		return;
	}

	@Override
	public void prepareStatements(Connection connection) throws SQLException {
		super.prepareStatements(connection);
		SAVE_QUOTE = connection.prepareStatement("INSERT IGNORE INTO quote (thread_id, message_id) VALUES (?, ?)");
		ALREADY_QUOTED = connection.prepareStatement("" +
				"SELECT thread_id " +
				"FROM quote q " +
				"WHERE thread_id = ? AND message_id = ?");
		GET_MESSAGE_SIMILAR_TO = connection.prepareStatement("" +
				"SELECT DISTINCT" +
				"   thread_id," +
				"   message_id " +
				"FROM message_text mt " +
				"JOIN text t on mt.text_id = t.text_id " +
				"WHERE thread_id = ? AND " +
				"text COLLATE UTF8MB4_GENERAL_CI LIKE CONCAT('%',?,'%') " +
				"ORDER BY message_id DESC");
	}

	@Override
	@SuppressWarnings("Duplicates")
	public boolean process(Message message) throws MalformedCommandException, SQLException {
		for (MessageComponent component : message.getComponents()) {
			String match = this.getMatch(component);
			if (!match.isEmpty()) {
				if (match.equals(GRAB_REGEX)) {
					grab(message, 1);
				} else if (match.equals(GRAB_OFFSET_REGEX)) {
					String text = ((Text) component).getText();
					Matcher matcher = Pattern.compile(GRAB_OFFSET_REGEX).matcher(text);
					if (matcher.find()) {
						try {
							grab(message, Integer.parseInt(matcher.group(1)));
						} catch (NumberFormatException e) {
							throw new MalformedCommandException();
						}
					} else {
						throw new MalformedCommandException();
					}
				} else if (match.equals(LOCATE_REGEX)) {
					String text = ((Text) component).getText();
					Matcher matcher = Pattern.compile(LOCATE_REGEX).matcher(text);
					if (matcher.find()) {
						locate(message, matcher.group(2));
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
			if (text.matches(GRAB_REGEX)) {
				return GRAB_REGEX;
			} else if (text.matches(GRAB_OFFSET_REGEX)) {
				return GRAB_OFFSET_REGEX;
			} else if (text.matches(LOCATE_REGEX)) {
				return LOCATE_REGEX;
			}
		}
		return "";
	}
}