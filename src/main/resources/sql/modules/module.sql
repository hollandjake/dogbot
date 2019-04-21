CREATE
OR
REPLACE FUNCTION GetOrCreateModuleId(content TEXT) RETURNS INT (10) UNSIGNED
BEGIN DECLARE moduleId INT(10) UNSIGNED DEFAULT (
        SELECT module_id
        FROM module
        WHERE module_name = content
        LIMIT 1
    );
IF
    (ISNULL(moduleId))
    THEN
INSERT INTO module (module_name) VALUE (content);
SET moduleId = LAST_INSERT_ID();
END IF;

RETURN moduleId;
END;

CREATE OR
REPLACE FUNCTION GetMessageContent(tId INT(10) UNSIGNED, mId INT (10) UNSIGNED) RETURNS TEXT
BEGIN
    RETURN
(
    SELECT IF(COUNT(*) = 1, data, GROUP_CONCAT(data ORDER BY message_component_id ASC SEPARATOR ' ')) as data
    FROM message_component mc
             JOIN text t on mc.text_id = t.text_id
    WHERE thread_id = tId
      AND message_id = mId
    GROUP BY thread_id, message_id
    LIMIT 1
);
END;