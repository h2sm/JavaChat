# JavaChat
Simple Java chat without GUI (yet). It can work both on Socket and NIO Selector. Supports multiple clients.   

Switching between Selector and Socket handling is located in loader/config.properties file. 

Also, I added a PostgreSQL support. It can resent the last 20 messages to a new connected client.