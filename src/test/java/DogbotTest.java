import com.hollandjake.dogbot.Dogbot;
import com.hollandjake.messenger_bot_api.util.Config;

import java.sql.SQLException;

public class DogbotTest {
	public static void main(String[] args) throws SQLException {
		String configFile = args.length > 0 ? args[0] : null;
		new Dogbot(new Config(configFile));
	}
}
