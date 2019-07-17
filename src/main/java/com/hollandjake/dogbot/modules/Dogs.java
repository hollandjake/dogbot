package com.hollandjake.dogbot.modules;

import com.hollandjake.chatbot.Chatbot;
import com.hollandjake.chatbot.utils.RedditModule;

import java.util.Arrays;

import static com.hollandjake.chatbot.utils.CONSTANTS.ACTIONIFY;


public class Dogs extends RedditModule {
	public Dogs(Chatbot chatbot) {
		super(chatbot,
				Arrays.asList(
						ACTIONIFY("dog"),
						ACTIONIFY("doggo")
				)
		);
	}
}
