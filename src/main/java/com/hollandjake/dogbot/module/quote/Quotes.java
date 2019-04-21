package com.hollandjake.dogbot.module.quote;

import com.hollandjake.dogbot.model.*;
import com.hollandjake.dogbot.service.MessageService;
import com.hollandjake.dogbot.util.exceptions.MalformedCommandException;
import com.hollandjake.dogbot.util.module.CommandableDatabaseModule;
import org.springframework.cache.annotation.CachePut;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hollandjake.dogbot.util.CONSTANTS.ACTIONIFY;
import static com.hollandjake.dogbot.util.CONSTANTS.pluralize;

public class Quotes extends CommandableDatabaseModule {
    private static final String QUOTE_REGEX = ACTIONIFY("quote( (.+))?");
    private static final String QUOTE_COUNT_REGEX = ACTIONIFY("quotecount (.+)");
    private static final String QUOTE_TOTAL_COUNT_REGEX = ACTIONIFY("quotecount");
    private static final String QUOTE_PERCENTAGE_REGEX = ACTIONIFY("(quotepercentage|qp|quote%|q%|%) (.+)");
    private static final String QUOTE_LEADERBOARD_REGEX = ACTIONIFY("(quoteleaderboard|qlb)( (.+))?");
    private static final String QUOTE_IMG_REGEX = ACTIONIFY("quoteimg( (.+))?");

    public Quotes(MessageService messageService,
                  JdbcTemplate template) {
        super(messageService, template);
    }

    private static String applyType(String message, String type) {
        switch (type) {
            case "caps":
                message = message.toUpperCase();
                return message;
            case "shaky":
                boolean isCaps = false;
                StringBuilder tempMessage = new StringBuilder();
                for (char x : message.toCharArray()) {
                    if (Character.isAlphabetic(x)) {
                        String c = String.valueOf(x);
                        tempMessage.append(isCaps ? c.toLowerCase() : c.toUpperCase());
                        isCaps = !isCaps;
                    } else {
                        tempMessage.append(x);
                    }
                }
                return tempMessage.toString();
            default:
                return message;
        }
    }

    static Message quoteMessage(Message message, String type) {
        List<MessageComponent> components = message.getMessageComponents();
        boolean containsText = false;
        for (int i = 0; i < components.size(); i++) {
            MessageComponent component = components.get(i);
            if (component instanceof Text) {
                containsText = true;
                String text = applyType(((Text) component).getData(), type != null ? type : "");
                components.set(i, Text.fromString(text));
            } else {
                components.set(i, component);
            }
        }
        if (containsText) {
            components.add(0, Text.fromString("\""));
            components.add(Text.fromString("\" - "));
        }
        components.add(Text.fromString(
                message.getSender().getName() + " [" + MessageDate.prettyPrint(message.getTimestamp()) + "]"));
        return message;
    }

    private static String getType(Text component) {
        int numUpper = 0;
        int numLetters = 0;
        String text = component.getData();
        for (char c : text.toCharArray()) {
            if (Character.isUpperCase(c)) {
                numUpper++;
            }
            if (Character.isAlphabetic(c)) {
                numLetters++;
            }
        }

        if (numUpper == numLetters) {
            return "caps";
        } else if (numUpper > 1) {
            return "shaky";
        } else {
            return "normal";
        }
    }

    private void quoteTotal() {
        int total = Optional.ofNullable(template.queryForObject(
                "SELECT COUNT(*) FROM quote WHERE thread_id = ?",
                Integer.class,
                messageService.getThread().getId()
        )).orElse(0);
        if (total > 0) {
            messageService.sendMessage("This chat has " + total + " quote" + pluralize(total));
        } else {
            messageService.sendMessage("This chat has 0 quotes. why not try grabbing some");
        }
    }

    private void quoteCount(String query) {
        Human human = messageService.getHumanService().findNameLike(query);

        if (human != null) {
            int count = Optional.ofNullable(template.queryForObject(
                    "SELECT COUNT(*) "
                            + "FROM quote q "
                            + "LEFT JOIN message m on q.message_id = m.message_id and q.thread_id = m.thread_id "
                            + "WHERE q.thread_id = ? "
                            + "AND m.sender_id = ?",
                    Integer.class,
                    messageService.getThread().getId(),
                    human.getId()
            )).orElse(0);

            if (count > 0) {
                messageService.sendMessage(
                        "\"" + human.getName() + "\" has " + count + " quote" + (count != 1 ? "s" : "") + "! :O");
            } else {
                messageService.sendMessage("\"" + human.getName() + "\" has 0 quotes! :'(");
            }
        } else {
            messageService.sendMessage("I'm sorry I don't know who '" + query + "' is");
        }
    }

    private void quote(Message quote, String type) {
        if (quote != null) {
            Message quotedMessage = quoteMessage(quote, type);
            messageService.sendMessage(quotedMessage);
        } else {
            messageService.sendMessage("There are no quotes available, why not try !grab or !grab [x] to make some");
        }
    }

    @CachePut(value = "messages", key = "{#result.thread.id, #result.id}")
    public Message getRandomQuoteFromName(String name) {
        return template.query("CALL GetMessage(?,("
                        + "SELECT q.message_id "
                        + "FROM quote q "
                        + "LEFT JOIN message m on q.message_id = m.message_id and q.thread_id = m.thread_id "
                        + "WHERE q.thread_id = ? "
                        + "AND m.sender_id = GetHumanIdWithNameLike(?) "
                        + "ORDER BY RAND() "
                        + "LIMIT 1 "
                        + "))",
                messageService.getMapper(),
                messageService.getThread().getId(),
                messageService.getThread().getId(),
                name
        ).stream().findAny().orElse(null);
    }

    private void getPercentageFor(String query) {
        Human human = messageService.getHumanService().findNameLike(query);
        if (human != null) {
            double percentage = Optional.ofNullable(template.queryForObject(
                    "SELECT GetQuotePercentageWithHumanId(?,?)",
                    Double.class,
                    messageService.getThread().getId(),
                    query
            )).orElse(0D);

            if (percentage > 0) {
                messageService.sendMessage(
                        "\"" + query + "\" contributes " + Math.round(percentage * 10000) / 100.0 + "%");
            } else {
                messageService.sendMessage("\"" + query + "\" has 0 quotes! :'(");
            }
        } else {
            messageService.sendMessage("I'm sorry I don't know who '" + query + "' is");
        }
    }

    private void leaderboard(int n) {
        StringBuilder message = new StringBuilder("Quote Leaderboard [#Quotes]:\n\n");
        AtomicInteger index = new AtomicInteger();
        template.query(
                "SELECT name, COUNT(*) as count "
                        + "FROM quote q "
                        + "JOIN message m on q.message_id = m.message_id and q.thread_id = m.thread_id "
                        + "JOIN human h on m.sender_id = h.human_id "
                        + "WHERE q.thread_id = ? "
                        + "GROUP BY sender_id "
                        + "ORDER BY count DESC "
                        + "LIMIT ?",
                resultSet -> {
                    index.incrementAndGet();
                    message.append("#")
                            .append(index.get())
                            .append(" \t")
                            .append(resultSet.getString("name"))
                            .append(" [")
                            .append(resultSet.getInt("count"))
                            .append("]\n");
                }, messageService.getThread().getId(), n);
        if (index.get() > 0) {
            messageService.sendMessage(message.toString());
        } else {
            messageService.sendMessage("No-one has been quoted");
        }
    }

    @CachePut(value = "messages",
            key = "{#result.thread.id, #result.id}")
    public Message randomQuote() {
        return template.query("CALL GetMessage(?,("
                        + "SELECT message_id "
                        + "FROM quote "
                        + "WHERE thread_id = ? "
                        + "ORDER BY RAND()"
                        + "LIMIT 1 "
                        + "))",
                messageService.getMapper(),
                messageService.getThread().getId(),
                messageService.getThread().getId()
        ).stream().findAny().orElse(null);
    }

    @CachePut(value = "messages",
            key = "{#result.thread.id, #result.id}")
    public Message randomImageQuote() {
        return template.query("CALL GetMessage(?,("
                        + "SELECT q.message_id "
                        + "FROM quote q "
                        + "RIGHT JOIN message_component mc on q.message_id = mc.message_id and q.thread_id = mc.thread_id "
                        + "WHERE q.thread_id = ? "
                        + "AND mc.image_id IS NOT NULL "
                        + "ORDER BY RAND() "
                        + "LIMIT 1 "
                        + "))",
                messageService.getMapper(),
                messageService.getThread().getId(),
                messageService.getThread().getId()
        ).stream().findAny().orElse(null);
    }

    @CachePut(value = "messages",
            key = "{#result.thread.id, #result.id}")
    public Message getRandomImageQuoteFromName(String name) {
        return template.query("CALL GetMessage(?,("
                        + "SELECT q.message_id "
                        + "FROM quote q "
                        + "JOIN message m on q.message_id = m.message_id and q.thread_id = m.thread_id "
                        + "RIGHT JOIN message_component mc on q.message_id = mc.message_id and q.thread_id = mc.thread_id "
                        + "WHERE q.thread_id = ? "
                        + "AND m.sender_id = GetHumanIdWithNameLike(?) "
                        + "AND mc.image_id IS NOT NULL "
                        + "ORDER BY RAND() "
                        + "LIMIT 1 "
                        + "))",
                messageService.getMapper(),
                messageService.getThread().getId(),
                messageService.getThread().getId(),
                name
        ).stream().findAny().orElse(null);
    }

    @Override
    @SuppressWarnings("Duplicates")
    public boolean process(Message message, boolean freshMessage, boolean moduleOutput) throws MalformedCommandException {
        for (MessageComponent component : message.getMessageComponents()) {
            String match = getMatch(component);
            if (!match.isEmpty()) {
                if (moduleOutput && freshMessage) {
                    if (match.equals(QUOTE_REGEX)) {
                        String text = ((Text) component).getData();
                        Matcher matcher = Pattern.compile(QUOTE_REGEX).matcher(text);
                        Message quote;
                        if (matcher.find() && matcher.group(2) != null) {
                            String quoteName = matcher.group(2);
                            quote = getRandomQuoteFromName(quoteName);
                            if (quote == null) {
                                messageService.sendMessage("\"" + quoteName + "\" has 0 quotes! :'(");
                            }
                        } else {
                            quote = randomQuote();
                        }
                        quote(quote, getType((Text) component));
                    } else if (match.equals(QUOTE_COUNT_REGEX)) {
                        String text = ((Text) component).getData();
                        Matcher matcher = Pattern.compile(QUOTE_COUNT_REGEX).matcher(text);
                        if (matcher.find()) {
                            quoteCount(matcher.group(1));
                        } else {
                            throw new MalformedCommandException();
                        }
                    } else if (match.equals(QUOTE_TOTAL_COUNT_REGEX)) {
                        quoteTotal();
                    } else if (match.equals(QUOTE_PERCENTAGE_REGEX)) {
                        String text = ((Text) component).getData();
                        Matcher matcher = Pattern.compile(QUOTE_PERCENTAGE_REGEX).matcher(text);
                        if (matcher.find() && matcher.group(2) != null) {
                            String quoteName = matcher.group(2);
                            getPercentageFor(quoteName);
                        } else {
                            throw new MalformedCommandException();
                        }
                    } else if (match.equals(QUOTE_LEADERBOARD_REGEX)) {
                        String text = ((Text) component).getData();
                        Matcher matcher = Pattern.compile(QUOTE_LEADERBOARD_REGEX).matcher(text);
                        if (matcher.find() && matcher.group(3) != null) {
                            int num = Integer.parseInt(matcher.group(3));
                            if (num > 0) {
                                leaderboard(num);
                                return true;
                            }
                        }
                        leaderboard(5);
                    } else if (match.equals(QUOTE_IMG_REGEX)) {
                        String text = ((Text) component).getData();
                        Matcher matcher = Pattern.compile(QUOTE_IMG_REGEX).matcher(text);
                        Message quote;
                        if (matcher.find() && matcher.group(2) != null) {
                            String quoteName = matcher.group(2);
                            quote = getRandomImageQuoteFromName(quoteName);
                            if (quote == null) {
                                messageService.sendMessage("\"" + quoteName + "\" has 0 image quotes! :'(");
                            }
                        } else {
                            quote = randomImageQuote();
                        }
                        quote(quote, getType((Text) component));
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
            String text = ((Text) component).getData();
            if (text.matches(QUOTE_REGEX)) {
                return QUOTE_REGEX;
            } else if (text.matches(QUOTE_COUNT_REGEX)) {
                return QUOTE_COUNT_REGEX;
            } else if (text.matches(QUOTE_TOTAL_COUNT_REGEX)) {
                return QUOTE_TOTAL_COUNT_REGEX;
            } else if (text.matches(QUOTE_PERCENTAGE_REGEX)) {
                return QUOTE_PERCENTAGE_REGEX;
            } else if (text.matches(QUOTE_LEADERBOARD_REGEX)) {
                return QUOTE_LEADERBOARD_REGEX;
            } else if (text.matches(QUOTE_IMG_REGEX)) {
                return QUOTE_IMG_REGEX;
            }
        }
        return "";
    }
}