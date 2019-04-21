package com.hollandjake.dogbot.util.module;

import com.hollandjake.dogbot.service.MessageService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class Module {
    @NonNull
    protected MessageService messageService;
}
