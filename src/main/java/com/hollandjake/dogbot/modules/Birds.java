package com.hollandjake.dogbot.modules;

import com.hollandjake.chatbot.Chatbot;
import com.hollandjake.chatbot.utils.RedditModule;

import java.util.Arrays;

import static com.hollandjake.chatbot.utils.CONSTANTS.ACTIONIFY;


public class Birds extends RedditModule {
	public Birds(Chatbot chatbot) {
		super(chatbot,
				Arrays.asList(
						ACTIONIFY("bird"),
						ACTIONIFY("birb")
				)
		);
	}
}
