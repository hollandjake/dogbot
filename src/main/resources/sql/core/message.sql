CREATE
OR
REPLACE PROCEDURE GetMessage(IN tId INT(10) UNSIGNED, IN mId INT (10) UNSIGNED)
BEGIN
SELECT *
FROM message
WHERE thread_id = tId
  AND message_id = mId
LIMIT 1;
END;

CREATE OR
REPLACE PROCEDURE SaveMessage(IN tId INT(10) UNSIGNED, IN sId INT (10) UNSIGNED, IN mTime TIMESTAMP)
BEGIN DECLARE messageId INT(10) UNSIGNED DEFAULT (GetLatestMessageId(tId) + 1);

INSERT INTO message (message_id, thread_id, sender_id, timestamp) VALUE (messageId, tId, sId, mTime);

CALL GetMessage(tId, messageId);
END;

CREATE OR
REPLACE FUNCTION GetLatestMessageId(tId INT(10) UNSIGNED) RETURNS INT
BEGIN DECLARE bestId INT DEFAULT (SELECT MAX(message_id) FROM message WHERE thread_id = tId);
IF
    (ISNULL(bestId))
    THEN
SET bestId = 0;
END IF;

RETURN bestId;
END;

