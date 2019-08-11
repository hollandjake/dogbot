package com.hollandjake.dogbot.modules;

import com.hollandjake.chatbot.Chatbot;
import com.hollandjake.chatbot.utils.CommandModule;
import com.hollandjake.messenger_bot_api.message.Message;
import com.hollandjake.messenger_bot_api.message.MessageComponent;
import com.hollandjake.messenger_bot_api.message.Text;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import static com.hollandjake.chatbot.utils.CONSTANTS.ACTIONIFY;
import static com.hollandjake.chatbot.utils.CONSTANTS.GET_PAGE_SOURCE;


public class ExtraGoodCats extends CommandModule {
	private final String EXTRA_GOOD_CATS_REGEX = ACTIONIFY("extragoodcat");
	private final String EGC_REGEX = ACTIONIFY("egc");
	private JSONParser parser = new JSONParser();

	public ExtraGoodCats(Chatbot chatbot) {
		super(chatbot);
	}

	@Override
	@SuppressWarnings("Duplicates")
	public boolean process(Message message) {
		for (MessageComponent component : message.getComponents()) {
			String match = getMatch(component);
			if (!match.isEmpty()) {
				if (match.equals(EXTRA_GOOD_CATS_REGEX) || match.equals(EGC_REGEX)) {
					try {
						JSONObject img = (JSONObject) ((JSONArray) parser.parse(GET_PAGE_SOURCE("https://api.thecatapi.com/v1/images/search"))).get(0);
						String url = (String) img.get("url");
						chatbot.sendMessageWithImage("MEOW!", url);
					} catch (ParseException e) {
						e.printStackTrace();
						chatbot.sendMessage("I am unable to meow.");
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
			if (text.matches(EXTRA_GOOD_CATS_REGEX)) {
				return EXTRA_GOOD_CATS_REGEX;
			} else if (text.matches(EGC_REGEX)) {
				return EGC_REGEX;
			}
		}
		return "";
	}
}
