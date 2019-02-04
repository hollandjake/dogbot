-- region DROP
-- region Reddits
DROP TABLE IF EXISTS BirdResponses;
DROP TABLE IF EXISTS CatResponses;
DROP TABLE IF EXISTS DogResponses;
DROP TABLE IF EXISTS EightBallResponses;
DROP TABLE IF EXISTS MemeResponses;

DROP TABLE IF EXISTS Subreddits;
-- endregion
-- region Images
DROP TABLE IF EXISTS ExtraGoodDogs;
DROP TABLE IF EXISTS Reacts;
-- endregion
-- region Quotes
DROP TABLE IF EXISTS Quotes;
-- endregion
-- endregion

-- region CREATE
-- region Quotes
CREATE TABLE Quotes
(
  ID        INT(8) NOT NULL,

  PRIMARY KEY (ID),
  FOREIGN KEY (ID) REFERENCES Messages (ID)
);
-- endregion
-- region Images
CREATE TABLE ExtraGoodDogs
(
  ID       INT(8) NOT NULL AUTO_INCREMENT,
  image_id INT(8) NOT NULL,

  PRIMARY KEY (ID),
  FOREIGN KEY (image_id) REFERENCES Images (ID)
);

CREATE TABLE Reacts
(
  ID       INT(8) NOT NULL AUTO_INCREMENT,
  image_id INT(8) NOT NULL,

  PRIMARY KEY (ID),
  FOREIGN KEY (image_id) REFERENCES Images (ID)
);

-- endregion
-- region Reddits
CREATE TABLE BirdResponses
(
  ID      INT(8) NOT NULL AUTO_INCREMENT,
  message TEXT   NOT NULL,

  PRIMARY KEY (ID)
);

CREATE TABLE CatResponses
(
  ID      INT(8) NOT NULL AUTO_INCREMENT,
  message TEXT   NOT NULL,

  PRIMARY KEY (ID)
);

CREATE TABLE DogResponses
(
  ID      INT(8) NOT NULL AUTO_INCREMENT,
  message TEXT   NOT NULL,

  PRIMARY KEY (ID)
);

CREATE TABLE EightBallResponses
(
  ID      INT(8) NOT NULL AUTO_INCREMENT,
  message TEXT   NOT NULL,

  PRIMARY KEY (ID)
);

CREATE TABLE MemeResponses
(
  ID      INT(8) NOT NULL AUTO_INCREMENT,
  message TEXT   NOT NULL,

  PRIMARY KEY (ID)
);

CREATE TABLE Subreddits
(
  ID   INT(8) NOT NULL AUTO_INCREMENT,
  type TEXT   NOT NULL,
  link TEXT   NOT NULL,

  PRIMARY KEY (ID)
);
-- endregion
-- endregion