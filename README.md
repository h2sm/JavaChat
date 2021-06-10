# JavaChat
Simple Java chat without GUI (yet). It can work both on Socket and NIO Selector. Supports multiple clients.   

Switching between Selector and Socket handling is located in loader/config.properties file. 

This chat also has database logging. It can resent the last 20 messages to a new connected client and it saves all the messages with client names, dates and messages. Also, it checks whether the client was registered in my abstract system and disconnects if user with given name wasnt found in database.

PostgreSQL was running in Docker.