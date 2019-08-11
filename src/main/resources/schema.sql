CREATE TABLE IF NOT EXISTS module
(
    module_id   INT UNSIGNED NOT NULL AUTO_INCREMENT,
    module_name TEXT         NOT NULL,

    PRIMARY KEY (module_id)
);

CREATE TABLE IF NOT EXISTS response_text
(
    module_id INT UNSIGNED NOT NULL,
    text_id   INT UNSIGNED NOT NULL,

    PRIMARY KEY (module_id, text_id),
    FOREIGN KEY (module_id) REFERENCES module (module_id),
    FOREIGN KEY (text_id) REFERENCES text (text_id)
);

CREATE TABLE IF NOT EXISTS response_image
(
    module_id INT UNSIGNED NOT NULL,
    image_id  INT UNSIGNED NOT NULL,

    PRIMARY KEY (module_id, image_id),
    FOREIGN KEY (module_id) REFERENCES module (module_id),
    FOREIGN KEY (image_id) REFERENCES image (image_id)
);

CREATE TABLE IF NOT EXISTS subreddit
(
    subreddit_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    module_id    INT UNSIGNED NOT NULL,
    link         TEXT         NOT NULL,

    PRIMARY KEY (subreddit_id),
    FOREIGN KEY (module_id) REFERENCES module (module_id)
);

CREATE TABLE IF NOT EXISTS quote
(
    thread_id  INT UNSIGNED NOT NULL,
    message_id INT UNSIGNED NOT NULL,

    PRIMARY KEY (thread_id, message_id),
    FOREIGN KEY (thread_id) REFERENCES message (thread_id),
    FOREIGN KEY (message_id) REFERENCES message (message_id)
);

DROP FUNCTION IF EXISTS GetPercentageWithHumanId;
CREATE FUNCTION GetPercentageWithHumanId(threadId INT, humanId INT) RETURNS DOUBLE
BEGIN
    DECLARE total INT;
    DECLARE sum INT;

    IF (NOT ISNULL(humanId)) THEN
        SET total = (SELECT COUNT(*) FROM quote WHERE thread_id = threadId);
        SET sum = (SELECT COUNT(*)
                   FROM quote q
                            JOIN message m on q.message_id = m.message_id
                   WHERE q.thread_id = threadId
                     and m.sender_id = humanId);
        RETURN sum / total;
    ELSE
        RETURN NULL;
    END IF;
END;