import com.hollandjake.dogbot.modules.*;
import com.hollandjake.messenger_bot_api.message.*;
import com.hollandjake.messenger_bot_api.util.Config;
import com.hollandjake.messenger_bot_api.util.DatabaseController;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class SystemSetup {
	public JSONObject file;
	public URI uri;
	private Connection connection;
	private CallableStatement subreddit_STMT;
	private CallableStatement responseText_STMT;
	private CallableStatement responseImage_STMT;
	private PreparedStatement GET_MESSAGE_SIMILAR_TO;
	private PreparedStatement SAVE_QUOTE;
	private Config config;
	private DatabaseController db;
	private JSONParser jsonParser = new JSONParser();
	private MessageThread thread;
	private Human rexAtkinson;
	private Human johnSmith;

	private Thread messageAutoSave = new Thread(() -> {
		JSONObject fileClone = (JSONObject) file.clone();
		if (fileClone != null) {
			try {
				FileWriter f = new FileWriter(uri.getPath().replaceAll("(.+/).+?$", "$1messages.json"));
				boolean open = true;
				while (open) {
					try {
						f.write(fileClone.toJSONString());
						f.close();
						open = false;
					} catch (ConcurrentModificationException ignored) {
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Saved updated messages");
		}
	});

	public SystemSetup(Config config) throws SQLException, IOException {
		this.config = config;
		BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("https://raw.githubusercontent.com/hollandjake/messenger-bot-api/master/src/main/resources/schema.sql").openStream()));
		StringBuilder stmt = new StringBuilder();
		String r;
		while ((r = reader.readLine()) != null) {
			stmt.append("\n").append(r);
		}
		reader.close();
		InputStream str = getClass().getResourceAsStream("schema.sql");
		reader = new BufferedReader(new InputStreamReader(str));
		while ((r = reader.readLine()) != null) {
			stmt.append("\n").append(r);
		}
		reader.close();
		db = new DatabaseController(config);
		connection = db.getConnection();
		connection.prepareStatement(stmt.toString()).execute();
		db = new DatabaseController(config);
		connection = db.getConnection();

		rexAtkinson = db.getHuman("Rex Atkinson");
		johnSmith = db.getHuman("John Smith");

		connection.prepareStatement("CREATE OR REPLACE PROCEDURE responseText(IN txt TEXT, IN moduleName TEXT)" +
				"BEGIN" +
				"    DECLARE mod_id INT DEFAULT (SELECT module_id FROM module WHERE module_name = moduleName LIMIT 1);" +
				"    DECLARE txt_id INT DEFAULT (SELECT text_id FROM text WHERE text = txt LIMIT 1);" +
				"    IF (ISNULL(mod_id)) THEN" +
				"        INSERT INTO module(module_name) VALUES (moduleName);" +
				"        SET mod_id = LAST_INSERT_ID();" +
				"    END IF;" +
				"    IF (ISNULL(txt_id)) THEN" +
				"        INSERT INTO text(text) VALUES (txt);" +
				"        SET txt_id = LAST_INSERT_ID();" +
				"    END IF;" +
				"    INSERT IGNORE INTO response_text(module_id, text_id) VALUES (mod_id, txt_id);" +
				"END;" +
				"CREATE OR REPLACE PROCEDURE responseImage(IN imageData LONGBLOB, IN moduleName TEXT)" +
				"BEGIN " +
				"   DECLARE mod_id INT DEFAULT (SELECT module_id FROM module WHERE module_name = moduleName LIMIT 1);" +
				"   DECLARE img_id INT DEFAULT (SELECT image_id FROM image WHERE data = imageData LIMIT 1);" +
				"   IF (ISNULL(mod_id)) THEN" +
				"       INSERT INTO module(module_name) VALUES (moduleName);" +
				"       SET mod_id = LAST_INSERT_ID();" +
				"   END IF;" +
				"   IF (ISNULL(img_id)) THEN" +
				"       INSERT INTO image(data) VALUES (imageData);" +
				"       SET img_id = LAST_INSERT_ID();" +
				"   END IF;" +
				"   INSERT IGNORE INTO response_image(module_id, image_id) VALUES (mod_id, img_id);" +
				"END;" +
				"CREATE OR REPLACE PROCEDURE subreddit(IN lnk TEXT, IN moduleName TEXT)" +
				"BEGIN" +
				"   DECLARE mod_id INT DEFAULT (SELECT module_id FROM module WHERE module_name = moduleName LIMIT 1);" +
				"   IF (ISNULL(mod_id)) THEN" +
				"       INSERT INTO module(module_name) VALUES (moduleName);" +
				"       SET mod_id = LAST_INSERT_ID();" +
				"   END IF;" +
				"   INSERT IGNORE INTO subreddit (module_id, link) VALUES (mod_id, lnk);" +
				"END;").execute();
		subreddit_STMT = connection.prepareCall("{CALL subreddit(?, ?)}");
		responseText_STMT = connection.prepareCall("{CALL responseText(?, ?)}");
		responseImage_STMT = connection.prepareCall("{CALL responseImage(?, ?)}");

		GET_MESSAGE_SIMILAR_TO = connection.prepareStatement("" +
				"SELECT DISTINCT" +
				"   m.message_id " +
				"FROM message_text mt " +
				"JOIN text t on mt.text_id = t.text_id " +
				"JOIN message m on mt.message_id = m.message_id " +
				"JOIN human h on m.sender_id = h.human_id " +
				"WHERE mt.thread_id = ? AND " +
				"text COLLATE UTF8MB4_GENERAL_CI LIKE CONCAT('%',?,'%') AND " +
				"h.name = ? " +
				"ORDER BY message_id DESC " +
				"LIMIT 1");

		SAVE_QUOTE = connection.prepareStatement("INSERT IGNORE INTO quote (thread_id, message_id) VALUES (?, ?)");

		thread = db.getThread(config.getProperty("thread_name"));
		URL url = getClass().getResource("Messages/" + thread.getThreadName() + "/messages.json");
		if (url == null) {
			createBirds();
			createBoot();
			createCats();
			createDogs();
			createEightBall();
			createExtraGoodDogs();
			createGran();
			createReacts();
		}
		List<MessageThread> threads = Collections.singletonList(thread);
		for (MessageThread thread : threads) {
			createMessages(thread);
			createQuotes(thread);
		}

		System.out.println("Reset completed");

		connection.prepareStatement("DROP PROCEDURE IF EXISTS responseText; DROP PROCEDURE IF EXISTS responseImage; DROP PROCEDURE IF EXISTS subreddit;").execute();
	}

	private void createBirds() throws SQLException, IOException {
		try {
			//Responses
			String className = Birds.class.getSimpleName();
			Scanner scanner = new Scanner(getClass().getResourceAsStream(className + "/responses.txt"));
			responseText_STMT.setString(2, className);
			while (scanner.hasNextLine()) {
				String nxt = scanner.nextLine();
				if (!nxt.isEmpty()) {
					responseText_STMT.setString(1, nxt);
					responseText_STMT.addBatch();
				}
			}
			responseText_STMT.executeBatch();

			//Subreddits
			scanner = new Scanner(getClass().getResourceAsStream(className + "/subreddits.txt"));
			subreddit_STMT.setString(2, className);
			while (scanner.hasNextLine()) {
				String nxt = scanner.nextLine();
				if (!nxt.isEmpty()) {
					subreddit_STMT.setString(1, nxt);
					subreddit_STMT.addBatch();
				}
			}
			subreddit_STMT.executeBatch();
			System.out.println("Birds created");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Birds failed to fly");
		}
	}

	private void createBoot() throws SQLException, IOException {
		try {
			//Responses
			String className = Boot.class.getSimpleName();
			Scanner scanner = new Scanner(getClass().getResourceAsStream(className + "/responses.txt"));
			responseText_STMT.setString(2, className);
			while (scanner.hasNextLine()) {
				String nxt = scanner.nextLine();
				if (!nxt.isEmpty()) {
					responseText_STMT.setString(1, nxt);
					responseText_STMT.addBatch();
				}
			}
			responseText_STMT.executeBatch();

			//Images
			scanner = new Scanner(getClass().getResourceAsStream(className + "/images.txt"));
			responseImage_STMT.setString(2, className);
			while (scanner.hasNextLine()) {
				String x = scanner.nextLine();
				if (!x.isEmpty()) {
					Image image = (Image) Image.fromUrl(config, x);
					InputStream stream = image.toStream();
					responseImage_STMT.setBinaryStream(1, stream, stream.available());
					responseImage_STMT.execute();
					stream.close();
					System.out.print(".");
				}
			}
			System.out.println("Boot created");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Boot failure");
		}
	}

	private void createCats() throws SQLException, IOException {
		try {
			//Responses
			String className = Cats.class.getSimpleName();
			Scanner scanner = new Scanner(getClass().getResourceAsStream(className + "/responses.txt"));
			responseText_STMT.setString(2, className);
			while (scanner.hasNextLine()) {
				String nxt = scanner.nextLine();
				if (!nxt.isEmpty()) {
					responseText_STMT.setString(1, nxt);
					responseText_STMT.addBatch();
				}
			}
			responseText_STMT.executeBatch();

			//Subreddits
			scanner = new Scanner(getClass().getResourceAsStream(className + "/subreddits.txt"));
			subreddit_STMT.setString(2, className);
			while (scanner.hasNextLine()) {
				String nxt = scanner.nextLine();
				if (!nxt.isEmpty()) {
					subreddit_STMT.setString(1, nxt);
					subreddit_STMT.addBatch();
				}
			}
			subreddit_STMT.executeBatch();
			System.out.println("Cats created");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Cats lost its 9 lives");
		}
	}

	private void createDogs() throws SQLException, IOException {
		try {
			//Responses
			String className = Dogs.class.getSimpleName();
			Scanner scanner = new Scanner(getClass().getResourceAsStream(className + "/responses.txt"));
			responseText_STMT.setString(2, className);
			while (scanner.hasNextLine()) {
				String nxt = scanner.nextLine();
				if (!nxt.isEmpty()) {
					responseText_STMT.setString(1, nxt);
					responseText_STMT.addBatch();
				}
			}
			responseText_STMT.executeBatch();

			//Subreddits
			scanner = new Scanner(getClass().getResourceAsStream(className + "/subreddits.txt"));
			subreddit_STMT.setString(2, className);
			while (scanner.hasNextLine()) {
				String nxt = scanner.nextLine();
				if (!nxt.isEmpty()) {
					subreddit_STMT.setString(1, nxt);
					subreddit_STMT.addBatch();
				}
			}
			subreddit_STMT.executeBatch();
			System.out.println("Dogs created");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Doggo didn't bark");
		}
	}

	private void createEightBall() throws SQLException, IOException {
		try {
			//Responses
			String className = EightBall.class.getSimpleName();
			Scanner scanner = new Scanner(getClass().getResourceAsStream(className + "/responses.txt"));
			responseText_STMT.setString(2, className);
			while (scanner.hasNextLine()) {
				String nxt = scanner.nextLine();
				if (!nxt.isEmpty()) {
					responseText_STMT.setString(1, nxt);
					responseText_STMT.addBatch();
				}
			}
			responseText_STMT.executeBatch();
			System.out.println("EightBall created");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Ball is out");
		}
	}

	private void createExtraGoodDogs() throws SQLException, IOException {
		try {
			//Images
			String className = ExtraGoodDogs.class.getSimpleName();
			Scanner scanner = new Scanner(getClass().getResourceAsStream(className + "/images.txt"));
			responseImage_STMT.setString(2, className);
			while (scanner.hasNextLine()) {
				String x = scanner.nextLine();
				if (!x.isEmpty()) {
					Image image = (Image) Image.fromUrl(config, x);
					InputStream stream = image.toStream();
					responseImage_STMT.setBinaryStream(1, stream, stream.available());
					responseImage_STMT.execute();
					stream.close();
					System.out.print(".");
				}
			}
			System.out.println("Extra Good Borks");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("The best dogs didn't bork");
		}
	}

	private void createGran() throws SQLException, IOException {
		try {
			//Responses
			String className = Gran.class.getSimpleName();
			Scanner scanner = new Scanner(getClass().getResourceAsStream(className + "/responses.txt"));
			responseText_STMT.setString(2, className);
			while (scanner.hasNextLine()) {
				String nxt = scanner.nextLine();
				if (!nxt.isEmpty()) {
					responseText_STMT.setString(1, nxt);
					responseText_STMT.addBatch();
				}
			}
			responseText_STMT.executeBatch();
			System.out.println("Gran created");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("We were too late, we couldnt save gran");
		}
	}

	private void createMessages(MessageThread thread) throws SQLException, IOException {
		Runtime.getRuntime().addShutdownHook(messageAutoSave);
		try {
			//Responses
			URL url = getClass().getResource("Messages/" + thread.getThreadName() + "/messages.json");
			if (url == null) {
				url = getClass().getResource("Messages/" + thread.getThreadName() + "/message_1.json");
				if (url != null) {
					uri = url.toURI();
				} else {
					throw new IOException("File: Messages/" + thread.getThreadName() + " not found");
				}
			} else {
				uri = url.toURI();
			}
			FileReader r = new FileReader(uri.getPath());
			file = (JSONObject) jsonParser.parse(r);
			r.close();
			file.remove("participants");
			file.remove("title");
			file.remove("is_still_participant");
			file.remove("thread_type");
			file.remove("thread_path");

			URI finalUri = uri;

			//Save all messages in chronological order
			JSONArray messages = (JSONArray) file.get("messages");

			for (int i = messages.size() - 1; i >= 0; i--) {
				JSONObject message = (JSONObject) messages.get(i);
				String sender = (String) message.get("sender_name");
				if (sender.equals(rexAtkinson.getName()) || sender.equals(johnSmith.getName())) {
					messages.remove(i);
					continue;
				}
				if (message.containsKey("content")) {
					String text = ((String) message.get("content"));
					if (text.matches(".+? voted for \".+?\" in the poll: .+?") ||
							text.matches(".+? set the emoji to .+?") ||
							text.matches(".+? set the nickname for .+?") ||
							text.matches(".+? left the group.") ||
							text.matches(".+? named the group .+?") ||
							text.matches(".+? changed the group photo.") ||
							text.matches(".+? responded .+? to .+?") ||
							text.matches(".+? created a poll: .+?") ||
							text.matches(".+? removed their vote for .+?")) {
						messages.remove(i);
						continue;
					}
				} else if (!message.containsKey("photos")) {
					messages.remove(i);
					continue;
				}
				message.remove("type");
				message.remove("share");
				message.remove("reactions");
			}

			while (!messages.isEmpty()) {
				int lastIndex = messages.size() - 1;
				JSONObject messageObj = (JSONObject) messages.get(lastIndex);
				Human sender = db.getHuman((String) messageObj.get("sender_name"));
				new Timestamp((long) messageObj.get("timestamp_ms")).toLocalDateTime();
				LocalDateTime d = new Timestamp((long) messageObj.get("timestamp_ms")).toLocalDateTime();
				MessageDate date = MessageDate.fromLocalDateTime(d);
				List<MessageComponent> components = new ArrayList<>();
				if (messageObj.containsKey("content")) {
					Text text = Text.fromString((String) messageObj.get("content"));
					components.add(text);
				}
				if (messageObj.containsKey("photos")) {
					for (Object p : (JSONArray) messageObj.get("photos")) {
						JSONObject photo = (JSONObject) p;
						String photoName = ((String) photo.get("uri")).replaceAll(".+(/photos/.+)", "$1");
						MessageComponent image = Image.fromInputStream(config, getClass().getResourceAsStream("Messages/" + thread.getThreadName() + photoName));
						if (image != null) {
							components.add(image);
						}
					}
				}
				if (components.size() > 0) {
					Message message = new Message(null, thread, sender, date, components);
					System.out.print(lastIndex + " : " + message.prettyPrint());
					db.saveMessageNoReturn(message);
					System.out.println(" Saved");
				}
				messages.remove(lastIndex);
			}
			Runtime.getRuntime().removeShutdownHook(messageAutoSave);
			System.out.println("Messages saved");
		} catch (ParseException | URISyntaxException e) {
			e.printStackTrace();
			System.out.println("No-one can be heard");
		}
	}

	private Integer getMessageIdLike(MessageThread thread, String q, String senderName) throws SQLException {

		try {
			GET_MESSAGE_SIMILAR_TO.setInt(1, thread.getId());
			GET_MESSAGE_SIMILAR_TO.setString(2, q);
			GET_MESSAGE_SIMILAR_TO.setString(3, senderName);
			ResultSet resultSet = GET_MESSAGE_SIMILAR_TO.executeQuery();
			if (resultSet.next()) {
				return resultSet.getInt("message_id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void createQuotes(MessageThread thread) throws SQLException, IOException {
		try {
			//Responses
			String className = Quotes.class.getSimpleName();
			InputStream stream = getClass().getResourceAsStream(className + "/" + thread.getThreadName() + ".json");
			JSONArray quotes = (JSONArray) jsonParser.parse(new InputStreamReader(stream));
			for (Object q : quotes) {
				String quote = (String) ((JSONObject) q).get("message");
				String senderName = (String) ((JSONObject) q).get("name");
				saveQuote(thread, getMessageIdLike(thread, quote, senderName));
				System.out.print(".");
			}
			System.out.println("Quotes created");
		} catch (SQLException | ParseException e) {
			e.printStackTrace();
			System.out.println("No-one can be heard");
		}
	}

	private void saveQuote(MessageThread thread, Integer messageId) {
		if (messageId != null) {
			try {
				SAVE_QUOTE.setInt(1, thread.getId());
				SAVE_QUOTE.setInt(2, messageId);
				SAVE_QUOTE.execute();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private void createReacts() throws SQLException, IOException {
		try {
			//Images
			String className = Reacts.class.getSimpleName();
			Scanner scanner = new Scanner(getClass().getResourceAsStream(className + "/catReacts.txt"));
			responseImage_STMT.setString(2, className);
			while (scanner.hasNextLine()) {
				String x = scanner.nextLine();
				if (!x.isEmpty()) {
					Image image = (Image) Image.fromUrl(config, x);
					InputStream stream = image.toStream();
					responseImage_STMT.setBinaryStream(1, stream, stream.available());
					responseImage_STMT.execute();
					stream.close();
					System.out.print(".");
				}
			}
			System.out.println("Reacts created");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("The reaction was NULL");
		}
	}

	public static void main(String[] args) throws IOException, SQLException {
		String configFile = args.length > 0 ? args[0] : null;
		InputStream s = Config.class.getResourceAsStream(configFile);
		Config config = new Config(configFile);
		new SystemSetup(config);
	}
}
