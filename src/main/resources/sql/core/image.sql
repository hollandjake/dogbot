CREATE OR REPLACE PROCEDURE GetImage(IN iId INT(10) UNSIGNED)
BEGIN
    SELECT image_id,
           data
    FROM image
    WHERE image_id = iId
    LIMIT 1;
END;

-- region Create Image
CREATE OR REPLACE FUNCTION GetOrCreateImageId(content LONGBLOB) RETURNS INT(10) UNSIGNED
BEGIN
    DECLARE imageId INT(10) UNSIGNED DEFAULT (
        SELECT image_id
        FROM image
        WHERE data = content
        LIMIT 1
    );
    IF (ISNULL(imageId)) THEN
        INSERT INTO image (data) VALUE (content);
        SET imageId = LAST_INSERT_ID();
    END IF;

    RETURN imageId;
END;

CREATE OR REPLACE PROCEDURE GetOrCreateImage(IN content LONGBLOB)
BEGIN
    CALL GetImage(GetOrCreateImageId(content));
END;
-- endregion