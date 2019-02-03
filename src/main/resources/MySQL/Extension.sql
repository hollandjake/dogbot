DROP TABLE IF EXISTS MemeResponses;
CREATE TABLE MemeResponses
(
  ID      INT(8) NOT NULL AUTO_INCREMENT,
  message TEXT   NOT NULL,

  PRIMARY KEY (ID)
);

INSERT INTO MemeResponses (message)
VALUES ('Sending meme');