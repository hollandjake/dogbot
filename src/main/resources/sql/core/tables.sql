CREATE TABLE IF NOT EXISTS thread
(
    thread_id INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
    url       TEXT             NOT NULL,

    PRIMARY KEY (thread_id)
) COLLATE = utf8mb4_bin;

CREATE TABLE IF NOT EXISTS human
(
    human_id INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
    name     TEXT             NOT NULL,

    PRIMARY KEY (human_id)
) COLLATE = utf8mb4_bin;

CREATE TABLE IF NOT EXISTS message
(
    message_id INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
    thread_id  INT(10) UNSIGNED NOT NULL,
    sender_id  INT(10) UNSIGNED NOT NULL,
    timestamp  TIMESTAMP        NOT NULL,

    PRIMARY KEY (message_id, thread_id),
    FOREIGN KEY (thread_id) REFERENCES thread (thread_id),
    FOREIGN KEY (sender_id) REFERENCES human (human_id)
) COLLATE = utf8mb4_bin;

CREATE TABLE IF NOT EXISTS image
(
    image_id INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
    data     LONGBLOB         NOT NULL,

    PRIMARY KEY (image_id)
) COLLATE = utf8mb4_bin;

CREATE TABLE IF NOT EXISTS text
(
    text_id INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
    data    TEXT             NOT NULL,
    PRIMARY KEY (text_id)
) COLLATE = utf8mb4_bin;

CREATE TABLE IF NOT EXISTS message_component
(
    message_component_id INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
    message_id           INT(10) UNSIGNED NOT NULL,
    thread_id            INT(10) UNSIGNED NOT NULL,
    image_id             INT(10) UNSIGNED DEFAULT NULL,
    text_id              INT(10) UNSIGNED DEFAULT NULL,
    human_id             INT(10) UNSIGNED DEFAULT NULL,

    PRIMARY KEY (message_component_id),
    FOREIGN KEY (message_id, thread_id) REFERENCES message (message_id, thread_id),
    FOREIGN KEY (image_id) REFERENCES image (image_id),
    FOREIGN KEY (text_id) REFERENCES text (text_id),
    FOREIGN KEY (human_id) REFERENCES human (human_id)
) COLLATE = utf8mb4_bin;

-- region Module
CREATE TABLE IF NOT EXISTS module
(
    module_id   INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
    module_name TEXT             NOT NULL,

    PRIMARY KEY (module_id)
) COLLATE = utf8mb4_bin;

CREATE TABLE IF NOT EXISTS module_text
(
    module_id INT(10) UNSIGNED NOT NULL,
    text_id   INT(10) UNSIGNED NOT NULL,

    PRIMARY KEY (module_id, text_id),
    FOREIGN KEY (module_id) REFERENCES module (module_id),
    FOREIGN KEY (text_id) REFERENCES text (text_id)
) COLLATE = utf8mb4_bin;

CREATE TABLE IF NOT EXISTS module_image
(
    module_id INT(10) UNSIGNED NOT NULL,
    image_id  INT(10) UNSIGNED NOT NULL,

    PRIMARY KEY (module_id, image_id),
    FOREIGN KEY (module_id) REFERENCES module (module_id),
    FOREIGN KEY (image_id) REFERENCES image (image_id)
) COLLATE = utf8mb4_bin;

CREATE TABLE IF NOT EXISTS subreddit
(
    subreddit_id INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
    module_id    INT(10) UNSIGNED NOT NULL,
    link         TEXT             NOT NULL,

    PRIMARY KEY (subreddit_id),
    FOREIGN KEY (module_id) REFERENCES module (module_id)
) COLLATE = utf8mb4_bin;

-- region Quotes
CREATE TABLE IF NOT EXISTS quote
(
    thread_id  INT(10) UNSIGNED NOT NULL,
    message_id INT(10) UNSIGNED NOT NULL,

    PRIMARY KEY (thread_id, message_id),
    FOREIGN KEY (thread_id) REFERENCES message (thread_id),
    FOREIGN KEY (message_id) REFERENCES message (message_id)
) COLLATE = utf8mb4_bin;
-- endregion
-- region Deadlines
CREATE TABLE IF NOT EXISTS events
(
    event_id  INT(10) UNSIGNED AUTO_INCREMENT NOT NULL,
    thread_id INT(10) UNSIGNED                NOT NULL,
    message   TEXT                            NOT NULL,
    time      DATETIME                        NOT NULL,

    PRIMARY KEY (event_id),
    FOREIGN KEY (thread_id) REFERENCES message (thread_id)
) COLLATE = utf8mb4_bin;
-- endregion
-- endregion