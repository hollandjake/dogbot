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
	private final String QUOTE_REGEX = ACTIONIFY("quote( (.+))?");
	private final String QUOTE_COUNT_REGEX = ACTIONIFY("quotecount (.+)");
	private final String QUOTE_TOTAL_COUNT_REGEX = ACTIONIFY("quotecount");
	private final String QUOTE_PERCENTAGE_REGEX = ACTIONIFY("(quotepercentage|qp|quote%|q%|%) (.+)");
	private final String QUOTE_LEADERBOARD_REGEX = ACTIONIFY("(quoteleaderboard|qlb)( (.+))?");

	private PreparedStatement GET_RAND_QUOTE;
	private PreparedStatement GET_RAND_QUOTE_WITH_HUMAN_ID;
	private PreparedStatement GET_NUM_QUOTES;
	private PreparedStatement GET_NUM_QUOTES_WITH_HUMAN_ID;
	private PreparedStatement GET_PERCENTAGE_WITH_HUMAN_ID;
	private PreparedStatement GET_QUOTE_LEADERBOARD;

	public Quotes(Chatbot chatbot) {
		super(chatbot);
	}

	private void quoteTotal() throws SQLException {
		GET_NUM_QUOTES.setInt(1, chatbot.getThread().getId());
		ResultSet resultSet = GET_NUM_QUOTES.executeQuery();
		if (resultSet.next()) {
			int count = resultSet.getInt(1);
			chatbot.sendMessage("This chat has " + count + " quote" + (count != 1 ? "s" : ""));
		} else {
			chatbot.sendMessage("This chat has 0 quotes. why not try grabbing some");
		}
	}

	private void quoteCount(String query) throws SQLException {
		db.checkConnection();
		Integer humanId = db.getHumanIdWithNameLike(query);
		if (humanId != null) {
			GET_NUM_QUOTES_WITH_HUMAN_ID.setInt(1, chatbot.getThread().getId());
			GET_NUM_QUOTES_WITH_HUMAN_ID.setInt(2, humanId);
			ResultSet resultSet = GET_NUM_QUOTES_WITH_HUMAN_ID.executeQuery();
			if (resultSet.next()) {
				query = resultSet.getString(1);
				int count = resultSet.getInt(2);
				if (count > 0) {
					chatbot.sendMessage("\"" + query + "\" has " + count + " quote" + (count != 1 ? "s" : "") + "! :O");
					return;
				}
			}
			chatbot.sendMessage("\"" + query + "\" has 0 quotes! :'(");
		} else {
			chatbot.sendMessage("I'm sorry I don't know who '" + query + "' is");
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

	private void quote(Message quote, String type) {
		if (quote != null) {
			Message quotedMessage = quoteMessage(quote, type);
			chatbot.sendMessage(quotedMessage);
		} else {
			chatbot.sendMessage("There are no quotes available, why not try !grab or !grab [x] to make some");
		}
	}

	private Message getRandomQuoteFromName(String name) throws SQLException {
		db.checkConnection();
		Integer humanId = db.getHumanIdWithNameLike(name);
		if (humanId != null) {
			GET_RAND_QUOTE_WITH_HUMAN_ID.setInt(1, chatbot.getThread().getId());
			GET_RAND_QUOTE_WITH_HUMAN_ID.setInt(2, humanId);
			ResultSet resultSet = GET_RAND_QUOTE_WITH_HUMAN_ID.executeQuery();
			if (resultSet.next()) {
				return db.getMessage(resultSet.getInt("message_id"));
			}
		}
		return null;
	}

	private void getPercentageFor(String query) throws SQLException {
		db.checkConnection();
		Integer humanId = db.getHumanIdWithNameLike(query);
		if (humanId != null) {
			GET_PERCENTAGE_WITH_HUMAN_ID.setInt(1, chatbot.getThread().getId());
			GET_PERCENTAGE_WITH_HUMAN_ID.setInt(2, humanId);
			ResultSet resultSet = GET_PERCENTAGE_WITH_HUMAN_ID.executeQuery();
			if (resultSet.next()) {
				query = resultSet.getString(1);
				double count = resultSet.getDouble(2);
				if (count > 0) {
					chatbot.sendMessage("\"" + query + "\" contributes " + Math.round(count * 10000) / 100.0 + "%");
					return;
				}
			}
			chatbot.sendMessage("\"" + query + "\" has 0 quotes! :'(");
		} else {
			chatbot.sendMessage("I'm sorry I don't know who '" + query + "' is");
		}
	}

	private void leaderboard(int n) throws SQLException {
		db.checkConnection();
		GET_QUOTE_LEADERBOARD.setInt(1, chatbot.getThread().getId());
		GET_QUOTE_LEADERBOARD.setInt(2, n);
		ResultSet resultSet = GET_QUOTE_LEADERBOARD.executeQuery();
		StringBuilder message = new StringBuilder("Quote Leaderboard [#Quotes]:\n\n");
		int index = 0;
		while (resultSet.next()) {
			index++;
			String name = resultSet.getString("name");
			int count = resultSet.getInt("count");
			message.append("#").append(index).append(" \t").append(name).append(" [").append(count).append("]\n");
		}
		if (index > 0) {
			chatbot.sendMessage(message.toString());
		} else {
			chatbot.sendMessage("No one has been quoted");
		}
	}

	private static String applyType(String message, String type) {
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

	public static Message quoteMessage(Message message, String type) {
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
		GET_RAND_QUOTE_WITH_HUMAN_ID = connection.prepareStatement("" +
				"SELECT" +
				"   m.thread_id," +
				"   m.message_id " +
				"FROM quote " +
				"JOIN message m on quote.message_id = m.message_id AND quote.thread_id = m.thread_id " +
				"WHERE m.thread_id = ? " +
				"AND sender_id = ? " +
				"ORDER BY RAND() " +
				"LIMIT 1");
		GET_NUM_QUOTES = connection.prepareStatement("" +
				"SELECT" +
				"   COUNT(*) " +
				"FROM quote " +
				"WHERE thread_id = ?");
		GET_NUM_QUOTES_WITH_HUMAN_ID = connection.prepareStatement("" +
				"SELECT" +
				"   h.name," +
				"   COUNT(*)" +
				"FROM quote " +
				"JOIN message m on quote.message_id = m.message_id " +
				"AND quote.thread_id = m.thread_id " +
				"JOIN human h on m.sender_id = h.human_id " +
				"WHERE m.thread_id = ? " +
				"AND sender_id = ?");

		GET_PERCENTAGE_WITH_HUMAN_ID = connection.prepareStatement("" +
				"SELECT" +
				"   h.name, " +
				"   GetPercentageWithHumanId(q.thread_id, h.human_id)" +
				"FROM quote q " +
				"JOIN message m on q.message_id = m.message_id " +
				"AND q.thread_id = m.thread_id " +
				"JOIN human h on m.sender_id = h.human_id " +
				"WHERE m.thread_id = ? " +
				"AND sender_id = ?");

		GET_QUOTE_LEADERBOARD = connection.prepareStatement("" +
				"SELECT" +
				"       h.name," +
				"       COUNT(*) as count " +
				"FROM quote q " +
				"JOIN message m on q.message_id = m.message_id " +
				"JOIN human h on m.sender_id = h.human_id " +
				"WHERE m.thread_id = ? " +
				"GROUP BY sender_id " +
				"ORDER BY count DESC " +
				"LIMIT ?");
	}

	@Override
	@SuppressWarnings("Duplicates")
	public boolean process(Message message) throws MalformedCommandException, SQLException {
		for (MessageComponent component : message.getComponents()) {
			String match = getMatch(component);
			if (!match.isEmpty()) {
				if (match.equals(QUOTE_REGEX)) {
					String text = ((Text) component).getText();
					Matcher matcher = Pattern.compile(QUOTE_REGEX).matcher(text);
					Message quote;
					if (matcher.find() && matcher.group(2) != null) {
						String quoteName = matcher.group(2);
						quote = getRandomQuoteFromName(quoteName);
						if (quote == null) {
							chatbot.sendMessage("\"" + quoteName + "\" has 0 quotes! :'(");
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
				} else if (match.equals(QUOTE_PERCENTAGE_REGEX)) {
					String text = ((Text) component).getText();
					Matcher matcher = Pattern.compile(QUOTE_PERCENTAGE_REGEX).matcher(text);
					if (matcher.find() && matcher.group(2) != null) {
						String quoteName = matcher.group(2);
						getPercentageFor(quoteName);
					} else {
						throw new MalformedCommandException();
					}
				} else if (match.equals(QUOTE_LEADERBOARD_REGEX)) {
					String text = ((Text) component).getText();
					Matcher matcher = Pattern.compile(QUOTE_LEADERBOARD_REGEX).matcher(text);
					if (matcher.find() && matcher.group(3) != null) {
						int num = Integer.parseInt(matcher.group(3));
						if (num > 0) {
							leaderboard(num);
							return true;
						}
					}
					leaderboard(5);
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
			if (text.matches(QUOTE_REGEX)) {
				return QUOTE_REGEX;
			} else if (text.matches(QUOTE_COUNT_REGEX)) {
				return QUOTE_COUNT_REGEX;
			} else if (text.matches(QUOTE_TOTAL_COUNT_REGEX)) {
				return QUOTE_TOTAL_COUNT_REGEX;
			} else if (text.matches(QUOTE_PERCENTAGE_REGEX)) {
				return QUOTE_PERCENTAGE_REGEX;
			} else if (text.matches(QUOTE_LEADERBOARD_REGEX)) {
				return QUOTE_LEADERBOARD_REGEX;
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