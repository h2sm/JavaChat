CREATE TABLE oneUser (
  userID serial NOT NULL,
  userName text NOT NULL,
  passHashcode bigint NOT NULL,
  passSalt bigint NOT NULL,
  sessionKey bigint NOT NULL,
  PRIMARY KEY (userID)
);

CREATE TABLE LogChat (
  userMsg text NOT NULL,
  dateSent date NOT NULL,
  idofuser serial NOT NULL,
  timeSent time NOT NULL,
  CONSTRAINT LogChat_userID_oneUser_userID_foreign FOREIGN KEY (idofuser) REFERENCES oneUser (userID)
);

insert into oneUser (userid, username, passhashcode, passsalt, sessionkey) values (1, 'EGORIK', 2043416039, 2043416038, 1234567);
insert into oneUser (userid, username, passhashcode, passsalt, sessionkey) values(2, 'MASHA', 73129592, 73129591, 11);

insert into LogChat (userMsg, dateSent, idofuser, timeSent) values ('TEST MESSAGE HAHA', '2021-06-10',1,'18:11:12');
insert into LogChat (userMsg, dateSent, idofuser, timeSent) values ('TEST MESSAGE not HAHA', '2021-06-11',2,'18:12:11');