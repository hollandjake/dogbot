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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hollandjake.chatbot.utils.CONSTANTS.ACTIONIFY;


public class Quotes extends DatabaseCommandModule {
	private final String GRAB_REGEX = ACTIONIFY("grab");
	private final String GRAB_OFFSET_REGEX = ACTIONIFY("grab (\\d+)");
	private final String LOCATE_REGEX = ACTIONIFY("(locate|grab) (.+)");
	private final String QUOTE_REGEX = ACTIONIFY("quote( (.+))?");
	private final String QUOTE_COUNT_REGEX = ACTIONIFY("quotecount (.+)");
	private final String QUOTE_TOTAL_COUNT_REGEX = ACTIONIFY("quotecount");

	private PreparedStatement SAVE_QUOTE;
	private PreparedStatement GET_RAND_QUOTE;
	private PreparedStatement GET_RAND_QUOTE_WITH_NAME;
	private PreparedStatement GET_NUM_QUOTES;
	private PreparedStatement GET_NUM_QUOTES_WITH_NAME;
	private PreparedStatement ALREADY_QUOTED;
	private PreparedStatement GET_MESSAGE_SIMILAR_TO;

	public Quotes(Chatbot chatbot) {
		super(chatbot);
	}

	private void grab(Message commandMessage, int offset) {
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
			Message grabbed = quoteMessage(message, null);
			grabbed.getComponents().add(0, Text.fromString("Grabbed: "));
			chatbot.sendMessage(grabbed);
			return true;
		}
	}

	private Message quoteMessage(Message message, String type) {
		List<MessageComponent> components = message.getComponents();
		boolean containsText = false;
		for (int i = 0; i < components.size(); i++) {
			MessageComponent component = components.remove(i);
			if (component instanceof Text) {
				containsText = true;
				String text = applyType(((Text) component).getText(), type != null ? type : "");
				components.add(i, Text.fromString(text));
			} else {
				components.add(i, component);
			}
		}
		if (containsText) {
			components.add(0, Text.fromString("\""));
			components.add(Text.fromString("\" - "));
		}
		components.add(Text.fromString(message.getSender().getName() + " [" + message.getDate().prettyPrint() + "]"));
		return message;
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

	private void quoteTotal() {
		try {
			GET_NUM_QUOTES.setInt(1, chatbot.getThread().getId());
			ResultSet resultSet = GET_NUM_QUOTES.executeQuery();
			if (resultSet.next()) {
				int count = resultSet.getInt(1);
				chatbot.sendMessage("This chat has " + count + " quote" + (count != 1 ? "s" : ""));
			} else {
				chatbot.sendMessage("This chat has 0 quotes. why not try grabbing some");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void quoteCount(String query) {
		try {
			GET_NUM_QUOTES_WITH_NAME.setInt(1, chatbot.getThread().getId());
			GET_NUM_QUOTES_WITH_NAME.setString(2, query);
			ResultSet resultSet = GET_NUM_QUOTES_WITH_NAME.executeQuery();
			String name = query;
			if (resultSet.next()) {
				name = resultSet.getString(1);
				int count = resultSet.getInt(2);
				if (count > 0) {
					chatbot.sendMessage("\"" + name + "\" has " + count + " quote" + (count != 1 ? "s" : "") + "! :O");
					return;
				}
			}
			chatbot.sendMessage("\"" + name + "\" has 0 quotes! :'(");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private String getType(Text component) {
		int numUpper = 0;
		int numLetters = 0;
		String text = component.getText();
		for (char c : text.toCharArray()) {
			if (Character.isUpperCase(c)) {
				numUpper++;
			}
			if (Character.isAlphabetic(c)) {
				numLetters++;
			}
		}

		if (numUpper == numLetters) {
			return "caps";
		} else if (numUpper > 1) {
			return "shaky";
		} else {
			return "normal";
		}
	}

	private String applyType(String message, String type) {
		switch (type) {
			case "caps":
				message = message.toUpperCase();
				return message;
			case "shaky":
				boolean isCaps = false;
				StringBuilder tempMessage = new StringBuilder();
				for (char x : message.toCharArray()) {
					if (Character.isAlphabetic(x)) {
						String c = String.valueOf(x);
						tempMessage.append(isCaps ? c.toLowerCase() : c.toUpperCase());
						isCaps = !isCaps;
					} else {
						tempMessage.append(x);
					}
				}
				message = tempMessage.toString();
		}
		return message;
	}

	private void quote(Message quote, String type) {
		if (quote != null) {
			Message quotedMessage = quoteMessage(quote, type);
			chatbot.sendMessage(quotedMessage);
		} else {
			chatbot.sendMessage("There are no quotes available, why not try !grab or !grab [x] to make some");
		}
	}

	private Message getRandomQuoteFromName(String name) {
		try {
			db.checkConnection();
			GET_RAND_QUOTE_WITH_NAME.setInt(1, chatbot.getThread().getId());
			GET_RAND_QUOTE_WITH_NAME.setString(2, name);
			ResultSet resultSet = GET_RAND_QUOTE_WITH_NAME.executeQuery();
			if (resultSet.next()) {
				return db.getMessage(resultSet.getInt("message_id"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void prepareStatements(Connection connection) throws SQLException {
		super.prepareStatements(connection);
		GET_RAND_QUOTE = connection.prepareStatement("" +
				"SELECT" +
				"   thread_id," +
				"   message_id " +
				"FROM quote " +
				"WHERE thread_id = ? " +
				"ORDER BY RAND() " +
				"LIMIT 1");
		GET_RAND_QUOTE_WITH_NAME = connection.prepareStatement("" +
				"SELECT" +
				"   m.thread_id," +
				"   m.message_id " +
				"FROM quote " +
				"JOIN message m on quote.message_id = m.message_id AND quote.thread_id = m.thread_id " +
				"JOIN human h on m.sender_id = h.human_id " +
				"WHERE m.thread_id = ? " +
				"AND h.name COLLATE UTF8MB4_GENERAL_CI LIKE CONCAT('%',?,'%') " +
				"ORDER BY RAND() " +
				"LIMIT 1");
		GET_NUM_QUOTES = connection.prepareStatement("" +
				"SELECT" +
				"   COUNT(*) " +
				"FROM quote " +
				"WHERE thread_id = ?");
		GET_NUM_QUOTES_WITH_NAME = connection.prepareStatement("" +
				"SELECT" +
				"   h.name," +
				"   COUNT(*)" +
				"FROM quote " +
				"JOIN message m on quote.message_id = m.message_id AND quote.thread_id = m.thread_id " +
				"JOIN human h on m.sender_id = h.human_id " +
				"WHERE m.thread_id = ? " +
				"AND h.name COLLATE UTF8MB4_GENERAL_CI LIKE CONCAT('%',?,'%')");
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
	public boolean process(Message message) throws MalformedCommandException {
		for (MessageComponent component : message.getComponents()) {
			String match = getMatch(component);
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
			} else if (match.equals(QUOTE_REGEX)) {
				String text = ((Text) component).getText();
				Matcher matcher = Pattern.compile(QUOTE_REGEX).matcher(text);
				Message quote;
				if (matcher.find() && matcher.group(2) != null) {
					String quoteName = matcher.group(2);
					quote = getRandomQuoteFromName(quoteName);
					if (quote == null) {
						chatbot.sendMessage("\"" + quoteName + "\" has 0 quotes! :'(");
						return true;
					}
				} else {
					quote = getRandomQuote();
				}
				quote(quote, getType((Text) component));
			} else if (match.equals(QUOTE_COUNT_REGEX)) {
				String text = ((Text) component).getText();
				Matcher matcher = Pattern.compile(QUOTE_COUNT_REGEX).matcher(text);
				if (matcher.find()) {
					quoteCount(matcher.group(1));

				} else {
					throw new MalformedCommandException();
				}
			} else if (match.equals(QUOTE_TOTAL_COUNT_REGEX)) {
				quoteTotal();
			}

			if (!match.isEmpty()) {
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
			} else if (text.matches(QUOTE_REGEX)) {
				return QUOTE_REGEX;
			} else if (text.matches(QUOTE_COUNT_REGEX)) {
				return QUOTE_COUNT_REGEX;
			} else if (text.matches(QUOTE_TOTAL_COUNT_REGEX)) {
				return QUOTE_TOTAL_COUNT_REGEX;
			}
		}
		return "";
	}

	private Message getRandomQuote() {
		try {
			db.checkConnection();
			GET_RAND_QUOTE.setInt(1, chatbot.getThread().getId());
			ResultSet resultSet = GET_RAND_QUOTE.executeQuery();
			if (resultSet.next()) {
				return db.getMessage(resultSet.getInt("message_id"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}