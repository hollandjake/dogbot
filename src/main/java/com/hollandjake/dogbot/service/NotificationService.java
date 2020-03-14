package com.hollandjake.dogbot.service;

import com.hollandjake.dogbot.model.Event;
import com.hollandjake.dogbot.model.Notification;
import com.hollandjake.dogbot.model.Thread;
import com.hollandjake.dogbot.repository.EventRepository;
import com.hollandjake.dogbot.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class NotificationService {
    private final MessageService messageService;
    private final NotificationRepository notificationRepository;
    private final EventRepository eventRepository;

    @Autowired
    public NotificationService(MessageService messageService, NotificationRepository notificationRepository, EventRepository eventRepository) {
        this.messageService = messageService;
        this.notificationRepository = notificationRepository;
        this.eventRepository = eventRepository;
    }

    public List<Notification> getAllUnsentNotifications(Thread thread) {
        return notificationRepository.getAllUnsentNotifications(thread);
    }

    public List<Event> getAllUnsentEvents(Thread thread) {
        return eventRepository.getAllUnsentEvents(thread);
    }

    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }

    public Event save(Event event) {
        Integer numberOfNotifications = eventRepository.save(event);
        if (numberOfNotifications > 0) {
            return event;
        } else {
            return null;
        }
    }

    public void sendReminder(Notification notification) {
        messageService.sendMessage("REMINDER:\n" + notification.generateMessage(false));
        notification.setSent(true);
        save(notification);
    }
}
