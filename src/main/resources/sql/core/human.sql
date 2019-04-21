CREATE
OR
REPLACE PROCEDURE GetHuman(IN hId INT(10) UNSIGNED)
BEGIN
SELECT human_id,
       name
FROM human
WHERE human_id = hId
LIMIT 1;
END;

-- region Create Human
CREATE OR
REPLACE FUNCTION GetOrCreateHumanId(hName TEXT) RETURNS INT (10) UNSIGNED
BEGIN DECLARE humanId INT(10) UNSIGNED DEFAULT (
        SELECT human_id
        FROM human
        WHERE name COLLATE utf8mb4_general_ci = hName
        LIMIT 1
    );
IF
    (ISNULL(humanId))
    THEN
INSERT INTO human (name) VALUE (hName);
SET humanId = LAST_INSERT_ID();
END IF;

RETURN humanId;
END;

CREATE OR
REPLACE PROCEDURE GetOrCreateHuman(IN hName TEXT)
BEGIN
CALL GetHuman(GetOrCreateHumanId(hName));
END;
-- endregion

-- region Get Human With Name Like
CREATE OR
REPLACE FUNCTION GetHumanIdWithNameLike(hName TEXT) RETURNS INT (10) UNSIGNED
BEGIN DECLARE humanId INT(10) UNSIGNED DEFAULT (
        SELECT human_id
        FROM human
        WHERE name = hName
        LIMIT 1
    );

IF
    (ISNULL(humanId))
    THEN
SET humanId = (
    SELECT human_id
    FROM human
    WHERE name COLLATE utf8mb4_general_ci LIKE CONCAT('%', hName, '%')
    LIMIT 1
);
END IF;

RETURN humanId;
END;

CREATE OR
REPLACE PROCEDURE GetHumanWithNameLike(IN hName TEXT)
BEGIN
CALL GetHuman(GetHumanIdWithNameLike(hName));
END;
-- endregion


