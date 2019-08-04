package com.hollandjake.dogbot;

import com.hollandjake.chatbot.Chatbot;
import com.hollandjake.chatbot.modules.OneLinkCommand;
import com.hollandjake.dogbot.modules.*;
import com.hollandjake.messenger_bot_api.util.Config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

public class Dogbot extends Chatbot {
	private Boot boot;

	public Dogbot(Config config) throws SQLException {
		super(config);
	}

	public static void main(String[] args) throws SQLException {
		String configFile = args.length > 0 ? args[0] : null;
		new Dogbot(new Config(configFile));
	}

	@Override
	protected void loadModules(Connection connection) throws SQLException {
		//Overrides
		modules.put("Commands", new OneLinkCommand(this,
				Arrays.asList("commands", "help"),
				"A list of commands can be found at",
				"https://github.com/hollandjake/dogbot"));
		modules.put("Github", new OneLinkCommand(this,
				Arrays.asList("github", "repo", "git"),
				"Github repository",
				"https://github.com/hollandjake/dogbot"));

		//Modules
		modules.put("Birds", new Birds(this));
		modules.put("Cats", new Cats(this));
		modules.put("Dirk", new Dirk(this));
		modules.put("Dogs", new Dogs(this));
		modules.put("EightBall", new EightBall(this));
		modules.put("ExtraGoodCats", new ExtraGoodCats(this));
		modules.put("ExtraGoodDogs", new ExtraGoodDogs(this));
		modules.put("Inspire", new Inspire(this));
		modules.put("Quotes", new Quotes(this));
		modules.put("Reacts", new Reacts(this));
		modules.put("Roll", new Roll(this));
		modules.put("Tab", new Tab(this));
		modules.put("Think", new Think(this));
		modules.put("XKCD", new XKCD(this));

		//Extra commands
		modules.put("Feedback", new OneLinkCommand(this,
				Arrays.asList("feedback"),
				"Feedback form",
				"https://docs.google.com/document/d/19Vquu0fh8LCqUXH0wwpm9H9MSq1LrEx1Z2Xg9NknKmg/edit?usp=sharing"));
		modules.put("Trello", new OneLinkCommand(this,
				Arrays.asList("trello"),
				"Trello",
				"https://trello.com/b/9f49WSW0/second-year-compsci"));

		boot = new Boot(this);
		boot.prepareStatements(connection);
	}

	@Override
	public void loaded(Connection connection) throws SQLException {
		super.loaded(connection);
		if ((Boolean) config.get("startup_message")) {
			sendMessageWithImage(boot.getRandomResponse(), boot.getRandomImage());
		}
	}
}