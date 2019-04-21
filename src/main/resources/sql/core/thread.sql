CREATE OR REPLACE PROCEDURE GetThread(IN tId INT(10) UNSIGNED)
BEGIN
    SELECT thread_id,
           url
    FROM thread
    WHERE thread_id = tId
    LIMIT 1;
END;

CREATE OR REPLACE PROCEDURE GetOrCreateThread(IN tUrl TEXT)
BEGIN
    DECLARE threadId INT(10) UNSIGNED DEFAULT (
        SELECT thread_id
        FROM thread
        where url = tUrl
    );

    IF (ISNULL(threadId)) THEN
        INSERT INTO thread (url) VALUE (tUrl);
        SET threadId = LAST_INSERT_ID();
    END IF;

    CALL GetThread(threadId);
END;



