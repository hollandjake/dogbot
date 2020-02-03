package com.hollandjake.dogbot.module.quote;

import com.hollandjake.dogbot.controller.ModuleController;
import com.hollandjake.dogbot.model.Message;
import com.hollandjake.dogbot.model.MessageComponent;
import com.hollandjake.dogbot.model.Text;
import com.hollandjake.dogbot.service.MessageService;
import com.hollandjake.dogbot.util.exceptions.MalformedCommandException;
import com.hollandjake.dogbot.util.module.CommandableDatabaseModule;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hollandjake.dogbot.util.CONSTANTS.ACTIONIFY;

public class Grab extends CommandableDatabaseModule {
    private static final String GRAB_REGEX = ACTIONIFY("grab");
    private static final String GRAB_OFFSET_REGEX = ACTIONIFY("grab (\\d+)");
    private static final String LOCATE_REGEX = ACTIONIFY("(locate|grab) (.+)");
    private final ModuleController moduleController;

    public Grab(MessageService messageService,
                JdbcTemplate template,
                ModuleController moduleController) {
        super(messageService, template);
        this.moduleController = moduleController;
    }

    private void grab(Message commandMessage, int offset, boolean freshMessage, boolean moduleOutput) {
        int targetId = commandMessage.getId() - offset;
        Message targetMessage = messageService.getMessage(messageService.getThread(), targetId);
        if (targetMessage == null) {
            if (moduleOutput && freshMessage) {
                messageService.sendMessage("That grab is a little too far for me");
            }
        } else {
            save(commandMessage, targetMessage, true, freshMessage, moduleOutput);
        }
    }

    private boolean save(Message commandMessage,
                         Message message,
                         boolean failOutput,
                         boolean freshMessage,
                         boolean moduleOutput) {
        //Check if message contains a command
        if (message.getMessageComponents().isEmpty()) {
            if (failOutput && moduleOutput && freshMessage) {
                messageService.sendMessage("That message is empty");
            }
            return false;
        } else if (message.getSender().equals(commandMessage.getSender())) {
            if (failOutput && moduleOutput && freshMessage) {
                messageService.sendMessage("Did you just try and grab yourself? \uD83D\uDE20");
            }

            return false;
        } else if (moduleController.containsCommand(message)) {
            if (failOutput && moduleOutput && freshMessage) {
                messageService.sendMessage("Don't do that >:(");
            }
            return false;
        } else if (alreadyQuoted(message)) {
            if (failOutput && moduleOutput && freshMessage) {
                messageService.sendMessage("That quote has already been grabbed");
            }
            return false;
        } else {
            saveQuote(message);
            if (moduleOutput) {
                Message grabbed = Quotes.quoteMessage(message, null);
                grabbed.getMessageComponents().add(0, Text.fromString("Grabbed: "));
                messageService.sendMessage(grabbed);
            }
            return true;
        }
    }

    private void saveQuote(Message message) {
        template.update("INSERT IGNORE INTO quote (thread_id, message_id) VALUES (?, ?)",
                message.getThread().getId(),
                message.getId());
    }

    private boolean alreadyQuoted(Message message) {
        return Optional.ofNullable(
                template.queryForObject(
                        "SELECT COUNT(*)"
                                + "FROM quote q "
                                + "WHERE thread_id = ? AND message_id = ?",
                        Integer.class,
                        message.getThread().getId(),
                        message.getId())
        ).map(x -> x > 0).orElse(false);
    }

    private void locate(Message commandMessage, String query, boolean freshMessage, boolean moduleOutput) {
        Message foundMessage = messageService.getMessageLike(commandMessage, query);
        if (foundMessage != null) {
            save(commandMessage, foundMessage, false, freshMessage, moduleOutput);
        } else {
            messageService.sendMessage("I can't seem to find a message with \"" + query + "\" in it :'(");
        }
    }

    @Override
    public boolean process(Message message, boolean freshMessage, boolean moduleOutput) throws MalformedCommandException {
        for (MessageComponent component : message.getMessageComponents()) {
            String match = this.getMatch(component);
            if (!match.isEmpty()) {
                if (match.equals(GRAB_REGEX)) {
                    grab(message, 1, freshMessage, moduleOutput);
                } else if (match.equals(GRAB_OFFSET_REGEX)) {
                    String text = ((Text) component).getData();
                    Matcher matcher = Pattern.compile(GRAB_OFFSET_REGEX).matcher(text);
                    if (matcher.find()) {
                        try {
                            grab(message, Integer.parseInt(matcher.group(1)), freshMessage, moduleOutput);
                        } catch (NumberFormatException e) {
                            throw new MalformedCommandException();
                        }
                    } else {
                        throw new MalformedCommandException();
                    }
                } else if (match.equals(LOCATE_REGEX)) {
                    String text = ((Text) component).getData();
                    Matcher matcher = Pattern.compile(LOCATE_REGEX).matcher(text);
                    if (matcher.find()) {
                        locate(message, matcher.group(2), freshMessage, moduleOutput);
                    } else {
                        throw new MalformedCommandException();
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
            if (text.matches(GRAB_REGEX)) {
                return GRAB_REGEX;
            } else if (text.matches(GRAB_OFFSET_REGEX)) {
                return GRAB_OFFSET_REGEX;
            } else if (text.matches(LOCATE_REGEX)) {
                return LOCATE_REGEX;
            }
        }
        return "";
    }
}