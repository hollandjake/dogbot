package com.hollandjake.dogbot.module;

import com.hollandjake.dogbot.model.Message;
import com.hollandjake.dogbot.model.MessageComponent;
import com.hollandjake.dogbot.model.Text;
import com.hollandjake.dogbot.service.MessageService;
import com.hollandjake.dogbot.util.module.CommandModule;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import static com.hollandjake.dogbot.util.CONSTANTS.ACTIONIFY;
import static com.hollandjake.dogbot.util.CONSTANTS.GET_PAGE_SOURCE;

@Slf4j
public class ExtraGoodCats extends CommandModule {
    private static final String EXTRA_GOOD_CATS_REGEX = ACTIONIFY("extragoodcat");
    private static final String EGC_REGEX = ACTIONIFY("egc");
    private JSONParser parser = new JSONParser();

    public ExtraGoodCats(MessageService messageService) {
        super(messageService);
    }

    @Override
    public boolean process(Message message, boolean freshMessage, boolean moduleOutput) {
        for (MessageComponent component : message.getMessageComponents()) {
            String match = getMatch(component);
            if (!match.isEmpty()) {
                if (moduleOutput && freshMessage) {
                    if (match.equals(EXTRA_GOOD_CATS_REGEX) || match.equals(EGC_REGEX)) {
                        try {
                            JSONObject img = (JSONObject) ((JSONArray) parser.parse(GET_PAGE_SOURCE(
                                    "https://api.thecatapi.com/v1/images/search"))).get(0);
                            String url = (String) img.get("url");
                            messageService.sendMessageWithImage("MEOW!", url);
                        } catch (ParseException e) {
                            log.error("Failed to parse", e);
                            messageService.sendMessage("I am unable to meow.");
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public String getMatch(MessageComponent component) {
        if (component instanceof Text) {
            String text = ((Text) component).getData();
            if (text.matches(EXTRA_GOOD_CATS_REGEX)) {
                return EXTRA_GOOD_CATS_REGEX;
            } else if (text.matches(EGC_REGEX)) {
                return EGC_REGEX;
            }
        }
        return "";
    }
}
