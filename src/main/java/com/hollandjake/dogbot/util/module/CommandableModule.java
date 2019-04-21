package com.hollandjake.dogbot.util.module;

import com.hollandjake.dogbot.model.Message;
import com.hollandjake.dogbot.model.MessageComponent;

public interface CommandableModule {
    /**
     * Used to apply the module to the message and act upon its response
     *
     * @param message      {@link Message}
     * @param freshMessage {@link Boolean}
     * @param moduleOutput
     * @return {@link boolean}
     */
    boolean process(Message message, boolean freshMessage, boolean moduleOutput) throws Exception;

    /**
     * Used to apply the module to the message and return which regex matches
     *
     * @param component {@link MessageComponent}
     * @return {@link String}
     */
    String getMatch(MessageComponent component);
}
