package com.hollandjake.dogbot.modules;

import com.hollandjake.chatbot.Chatbot;
import com.hollandjake.chatbot.utils.DatabaseModule;
import com.hollandjake.messenger_bot_api.message.MessageComponent;

import java.sql.Connection;
import java.sql.SQLException;


public class Boot extends DatabaseModule {

	public Boot(Chatbot chatbot) {
		super(chatbot);
	}

	@Override
	public void prepareStatements(Connection connection) throws SQLException {
		super.prepareStatements(connection);
	}

	public MessageComponent getRandomImage() {
		return super.getRandomImage();
	}

	public String getRandomResponse() {
		return super.getRandomResponse();
	}
}
