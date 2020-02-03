CREATE OR REPLACE PROCEDURE GetMessageLike(IN tId INT(10) UNSIGNED, IN mId INT(10) UNSIGNED, IN content TEXT)
BEGIN
    CALL GetMessage(
            tId,
            (
                SELECT message_id
                FROM (
                         SELECT message_id,
                                GetMessageContent(thread_id, message_id) COLLATE utf8mb4_general_ci as data
                         FROM message
                         WHERE thread_id = tId
                           AND message_id != mId
                     ) as combined
                WHERE data IS NOT NULL
                  AND data LIKE CONCAT('%', content, '%')
                ORDER BY message_id DESC
                LIMIT 1
            )
        );
END;

CREATE OR REPLACE FUNCTION GetQuotePercentageWithHumanId(tId INT(10) UNSIGNED, hId INT(10) UNSIGNED) RETURNS DOUBLE
BEGIN
    DECLARE total INT;
    DECLARE sum INT;

    IF
        (NOT ISNULL(hId))
    THEN
        SET total = (
            SELECT COUNT(*)
            FROM quote
            WHERE thread_id = tId
        );

        SET sum = (
            SELECT COUNT(*)
            FROM quote q
                     JOIN message m on q.message_id = m.message_id
            WHERE q.thread_id = tId
              AND m.sender_id = hId
        );
        RETURN sum / total;
    ELSE
        RETURN NULL;
    END IF;
END;