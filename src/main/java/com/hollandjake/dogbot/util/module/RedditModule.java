package com.hollandjake.dogbot.util.module;

import com.hollandjake.dogbot.model.Message;
import com.hollandjake.dogbot.model.MessageComponent;
import com.hollandjake.dogbot.model.Text;
import com.hollandjake.dogbot.module.core.Reddit;
import com.hollandjake.dogbot.service.MessageService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.stream.Collectors;

import static com.hollandjake.dogbot.util.CONSTANTS.GET_RANDOM;

public abstract class RedditModule extends CommandableDatabaseModule {

    //region Constants
    private final List<String> regexes;
    //endregion

    public RedditModule(
            MessageService messageService,
            JdbcTemplate template,
            List<String> regexes
    ) {
        super(messageService, template);
        this.regexes = regexes;
    }

    @Override
    public boolean process(Message message, boolean freshMessage, boolean moduleOutput) {
        for (MessageComponent component : message.getMessageComponents()) {
            String match = getMatch(component);
            for (String regex : regexes) {
                if (match.equals(regex)) {
                    if (moduleOutput && freshMessage) {
                        messageService.sendMessageWithImage(getRandomResponse(), getImageUrl());
                    }
                    return true;
                }
            }

        }
        return false;
    }

    @Override
    public String getMatch(MessageComponent component) {
        if (component instanceof Text) {
            String text = ((Text) component).getData();
            for (String command : regexes) {
                if (text.matches(command)) {
                    return command;
                }
            }
        }
        return "";
    }

    protected String getImageUrl() {
        List<String> subreddits = getSubreddits();
        while (subreddits != null && !subreddits.isEmpty()) {
            String image = Reddit.getSubredditPicture(GET_RANDOM(subreddits));
            if (image != null) {
                return image;
            }
        }
        return null;
    }

    @Cacheable(value = "subreddits",
            key = "#moduleId")
    public List<String> getSubreddits(Integer moduleId) {
        return template.query(
                "SELECT link "
                        + "FROM subreddit "
                        + "WHERE module_id = ?",
                getSubredditMapper(),
                moduleId
        ).stream().map(String::trim).collect(Collectors.toList());
    }

    public List<String> getSubreddits() {
        return getSubreddits(getModuleId());
    }

    public RowMapper<String> getSubredditMapper() {
        return (resultSet, i) -> resultSet.getString("link");
    }
}