CREATE OR REPLACE PROCEDURE GetText(IN tId INT(10) UNSIGNED)
BEGIN
    SELECT text_id,
           data
    FROM text
    WHERE text_id = tId
    LIMIT 1;
END;

-- region Create Text
CREATE OR REPLACE FUNCTION GetOrCreateTextId(content TEXT) RETURNS INT(10) UNSIGNED
BEGIN
    DECLARE textId INT(10) UNSIGNED DEFAULT (
        SELECT text_id
        FROM text
        WHERE data = content
        LIMIT 1
    );
    IF (ISNULL(textId)) THEN
        INSERT INTO text (data) VALUE (content);
        SET textId = LAST_INSERT_ID();
    END IF;

    RETURN textId;
END;

CREATE OR REPLACE PROCEDURE GetOrCreateText(IN content TEXT)
BEGIN
    CALL GetText(GetOrCreateTextId(content));
END;
-- endregion