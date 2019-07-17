package com.hollandjake.dogbot.modules;

import com.hollandjake.chatbot.Chatbot;
import com.hollandjake.chatbot.exceptions.MalformedCommandException;
import com.hollandjake.chatbot.utils.CommandModule;
import com.hollandjake.messenger_bot_api.message.Message;
import com.hollandjake.messenger_bot_api.message.MessageComponent;
import com.hollandjake.messenger_bot_api.message.Text;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hollandjake.chatbot.utils.CONSTANTS.ACTIONIFY;
import static com.hollandjake.chatbot.utils.CONSTANTS.GET_PAGE_SOURCE;


public class XKCD extends CommandModule {
	private final String XKCD_REGEX = ACTIONIFY("xkcd");
	private final String LATEST_SHORT_XKCD_REGEX = ACTIONIFY("xkcd l");
	private final String LATEST_XKCD_REGEX = ACTIONIFY("xkcd latest");
	private final String SPECIFIC_XKCD_REGEX = ACTIONIFY("xkcd ([1-9][0-9]*)");

	private int highestNumber;


	public XKCD(Chatbot chatbot) {
		super(chatbot);
		this.highestNumber = (int) new JSONObject(GET_PAGE_SOURCE("https://xkcd.com/info.0.json")).get("num");
	}

	private void sendXKCD(int number) {
		this.highestNumber = (int) new JSONObject(GET_PAGE_SOURCE("https://xkcd.com/info.0.json")).get("num");
		if (number < 1 || highestNumber < number) {
			chatbot.sendMessage("XKCD number out of range. Please try XKCD's in range 1-" + highestNumber);
		} else {
			JSONObject xkcd = new JSONObject(GET_PAGE_SOURCE("https://xkcd.com/" + number + "/info.0.json"));

			String title = xkcd.get("safe_title").toString();
			String alt = xkcd.get("alt").toString();
			String imgURL = xkcd.get("img").toString();

			String response =
					"Title: " + title +
							"\nNumber: " + number +
							"\nAlt text: " + alt;

			chatbot.sendMessageWithImage(response, imgURL);
		}
	}

	private void sendRandomXKCD() {
		sendXKCD((int) (Math.random() * highestNumber) + 1);
	}

	@Override
	@SuppressWarnings("Duplicates")
	public boolean process(Message message) throws MalformedCommandException {
		for (MessageComponent component : message.getComponents()) {
			String match = getMatch(component);
			if (match.equals(XKCD_REGEX)) {
				sendRandomXKCD();
				return true;
			} else if (match.equals(LATEST_SHORT_XKCD_REGEX) || match.equals(LATEST_XKCD_REGEX)) {
				sendXKCD(highestNumber);
				return true;
			} else if (match.equals(SPECIFIC_XKCD_REGEX)) {
				String text = ((Text) component).getText();
				Matcher matcher = Pattern.compile(SPECIFIC_XKCD_REGEX).matcher(text);
				if (matcher.find()) {
					int number = Integer.parseInt(matcher.group(1));
					sendXKCD(number);
				} else {
					throw new MalformedCommandException();
				}
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	@Override
	@SuppressWarnings("Duplicates")
	public String getMatch(MessageComponent component) {
		if (component instanceof Text) {
			String text = ((Text) component).getText();
			if (text.matches(XKCD_REGEX)) {
				return XKCD_REGEX;
			} else if (text.matches(LATEST_XKCD_REGEX)) {
				return LATEST_XKCD_REGEX;
			} else if (text.matches(LATEST_SHORT_XKCD_REGEX)) {
				return LATEST_SHORT_XKCD_REGEX;
			} else if (text.matches(SPECIFIC_XKCD_REGEX)) {
				return SPECIFIC_XKCD_REGEX;
			}
		}
		return "";
	}
}
