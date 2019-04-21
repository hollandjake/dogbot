package com.hollandjake.dogbot.module.core;

import com.hollandjake.dogbot.controller.ModuleController;
import com.hollandjake.dogbot.model.Message;
import com.hollandjake.dogbot.model.MessageComponent;
import com.hollandjake.dogbot.model.Text;
import com.hollandjake.dogbot.service.MessageService;
import com.hollandjake.dogbot.util.module.CommandModule;
import com.hollandjake.dogbot.util.module.CommandableModule;
import com.hollandjake.dogbot.util.module.RedditModule;
import lombok.Builder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hollandjake.dogbot.util.CONSTANTS.ACTIONIFY;
import static com.hollandjake.dogbot.util.CONSTANTS.GET_PAGE_SOURCE;

public class Reddit extends CommandModule {
    //region Constants
    private static final String REDDITS_REGEX = ACTIONIFY("reddits");
    private static final String REDDIT_REGEX = ACTIONIFY("reddit");
    private final ModuleController moduleController;

    @Builder
    public Reddit(
            MessageService messageService,
            ModuleController moduleController) {
        super(messageService);
        this.moduleController = moduleController;
    }
    //endregion

    public static String getSubredditPicture(String subreddit) {
        //Get reddit path
        String redditPath = "https://www.reddit.com/r/" + subreddit + "/random.json";

        String data = GET_PAGE_SOURCE(redditPath);
        Matcher matcher = Pattern.compile("https://i\\.redd\\.it/\\S+?\\.jpg").matcher(data);
        if (matcher.find()) {
            return matcher.group();
        } else {
            return null;
        }
    }

    @Override
    public boolean process(Message message, boolean freshMessage, boolean moduleOutput) {
        for (MessageComponent component : message.getMessageComponents()) {
            String match = getMatch(component);
            if (!match.isEmpty()) {
                if (moduleOutput && freshMessage) {
                    if (match.equals(REDDITS_REGEX)) {
                        StringBuilder response = new StringBuilder("Reddits currently in use\n");
                        int numReddits = 0;
                        for (CommandableModule module : moduleController.getModules().values()) {
                            if (module instanceof RedditModule) {
                                response.append("\n");
                                for (String subreddit : ((RedditModule) module).getSubreddits()) {
                                    numReddits++;
                                    response.append("\thttps://www.reddit.com/r/").append(subreddit).append("\n");
                                }
                            }
                        }
                        response.append("\n")
                                .append(numReddits)
                                .append(" reddit module")
                                .append(numReddits != 1 ? "s" : "")
                                .append(" being used");
                        messageService.sendMessage(response.toString());
                    } else if (match.equals(REDDIT_REGEX)) {
                        messageService.sendMessage("https://www.reddit.com/");
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
            if (text.matches(REDDITS_REGEX)) {
                return REDDITS_REGEX;
            } else if (text.matches(REDDIT_REGEX)) {
                return REDDIT_REGEX;
            }
        }
        return "";
    }
}