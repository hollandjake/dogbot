package com.hollandjake.dogbot.controller;

import com.hollandjake.dogbot.service.MessageService;
import com.hollandjake.dogbot.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class NotificationController {
    private final MessageService messageService;
    private final NotificationService notificationService;

    @Autowired
    public NotificationController(
            MessageService messageService,
            NotificationService notificationService) {
        this.messageService = messageService;
        this.notificationService = notificationService;
    }

    //Every minute
    @Scheduled(cron = "0 * * * * *")
    private void notificationScheduler() {
        LocalDateTime now = LocalDateTime.now();
        notificationService.getAllUnsentNotifications(messageService.getThread())
                .stream()
                .filter(notification -> !notification.getTime().isAfter(now))
                .forEach(notificationService::sendReminder);
    }
}
