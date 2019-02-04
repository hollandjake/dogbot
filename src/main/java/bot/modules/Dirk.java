package bot.modules;

import bot.Chatbot;
import bot.utils.CommandModule;
import bot.utils.Message;

import java.util.ArrayList;

import static bot.utils.CONSTANTS.ACTIONIFY;
import static bot.utils.CONSTANTS.DEACTIONIFY;

public class Dirk implements CommandModule {
    //region Constants
    private final String DIRK_REGEX = ACTIONIFY("dirk");
    private final Chatbot chatbot;
    //endregion

    public Dirk(Chatbot chatbot) {
        this.chatbot = chatbot;
    }

    //region Overrides
    @Override
    @SuppressWarnings("Duplicates")
    public boolean process(Message message) {
        String match = getMatch(message);
        if (match.equals(DIRK_REGEX)) {
            String response = "Our lord and saviour has greeted us";
            String image = "https://staffwww.dcs.shef.ac.uk/people/D.Sudholt/Dirk_Sudholt-cropped.jpg";
            chatbot.sendImageWithMessage(image, response);
            return true;
        } else {
            return false;
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    public String getMatch(Message message) {
        String messageBody = message.getMessage();
        if (messageBody.matches(DIRK_REGEX)) {
            return DIRK_REGEX;
        } else {
            return "";
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    public ArrayList<String> getCommands() {
        ArrayList<String> commands = new ArrayList<>();
        commands.add(DEACTIONIFY(DIRK_REGEX));
        return commands;
    }

    //endregion
}