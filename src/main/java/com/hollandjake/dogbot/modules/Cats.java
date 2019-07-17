package com.hollandjake.dogbot.modules;

import com.hollandjake.chatbot.Chatbot;
import com.hollandjake.chatbot.utils.RedditModule;

import java.util.Arrays;

import static com.hollandjake.chatbot.utils.CONSTANTS.ACTIONIFY;


public class Cats extends RedditModule {
	public Cats(Chatbot chatbot) {
		super(chatbot,
				Arrays.asList(
						ACTIONIFY("cat"),
						ACTIONIFY("catto")
				)
		);
	}
}
