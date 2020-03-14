CREATE
OR
REPLACE FUNCTION SaveNotificationId(tId INT(10) UNSIGNED, nMessage TEXT, nTime DATETIME,
    sTime DATETIME,
    nSent BOOLEAN) RETURNS INT (10)
BEGIN DECLARE existingId INT(10) UNSIGNED DEFAULT (SELECT notification_id
                                                 FROM notification
                                                 WHERE thread_id = tId
                                                   AND message = nMessage
                                                   AND time = nTime
                                                   AND show_time = sTime
                                                 LIMIT 1);
IF
    (ISNULL(existingId))
    THEN
INSERT INTO notification (thread_id, message, time, show_time, sent) VALUE (tId, nMessage, nTime, sTime, nSent);
SET existingId = LAST_INSERT_ID();
ELSE
UPDATE notification
SET sent = nSent
WHERE thread_id = tId
  AND message = nMessage
  AND time = nTime
  AND show_time = sTime;
END IF;

RETURN existingId;
END;

CREATE OR
REPLACE PROCEDURE SaveNotification(IN tId INT(10) UNSIGNED,
    IN nMessage TEXT,
    IN nTime DATETIME,
    IN sTime DATETIME,
    IN nSent BOOLEAN)
BEGIN DECLARE notificationId INT(10) DEFAULT SaveNotificationId(tId, nMessage, nTime, sTime, nSent);
SELECT notification_id,
       thread_id,
       message,
       time,
       show_time,
       sent
FROM notification
WHERE notification_id = notificationId;
END;

CREATE OR
REPLACE FUNCTION SaveEvent(tId INT(10) UNSIGNED, nMessage TEXT, nTime DATETIME) RETURNS INT
BEGIN DECLARE eventExists BOOLEAN DEFAULT (SELECT COUNT(en.event_id) > 0
                                         FROM event_notification en
                                                  JOIN notification n on en.notification_id = n.notification_id
                                         WHERE n.time = nTime
                                         LIMIT 1);
DECLARE eventId INT(10);
DECLARE now DATETIME DEFAULT NOW();
DECLARE gap INT DEFAULT DATEDIFF(nTime, now);
DECLARE numReminders INT DEFAULT 0;

IF
    (NOT eventExists)
    THEN
INSERT INTO event (thread_id, time, message)
VALUES (tId, nTime, nMessage);
SET eventId = LAST_INSERT_ID();

IF
    (gap > 7)
    THEN
INSERT INTO event_notification (event_id, notification_id)
VALUES (eventId, SaveNotificationId(
        tId,
        CONCAT('ONE WEEK TILL: ', nMessage),
        DATE_SUB(nTime, INTERVAL 1 WEEK),
        nTime,
        false
    ));
SET numReminders = numReminders + 1;
END IF;

IF
    (gap > 1)
    THEN
INSERT INTO event_notification (event_id, notification_id)
VALUES (eventId, SaveNotificationId(
        tId,
        CONCAT('TOMORROW: ', nMessage),
        DATE_SUB(nTime, INTERVAL 1 DAY),
        nTime,
        false
    ));
SET numReminders = numReminders + 1;
END IF;

IF
    (TIMEDIFF(nTime, now) > 0)
    THEN
INSERT INTO event_notification (event_id, notification_id)
VALUES (eventId, SaveNotificationId(
        tId,
        nMessage,
        nTime,
        nTime,
        false
    ));
SET numReminders = numReminders + 1;
END IF;
ELSE
SET numReminders = -1;
END IF;
RETURN numReminders;
END;