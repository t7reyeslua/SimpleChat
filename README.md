# SimpleChat
Plain and simple chat application

1. The user connects to the server by a TCP connection at a specific port.
2. The user is greeted by the server and asked to enter a login name and will be afterwards prompted for a password.
3. If the user is unknown, this will become his new password, otherwise server checks whether the user credentials are valid. 
4. Once in the system, the user is greeted again with the last time he was connected. 
5. After the login, the server automatically displays all messages of the user and clearly indicates which ones the user has not yet seen. 
6. A user can compose and send a new message through the keyword @. Example: “@alice Let’s go for some drinks”. 
7. If you want to talk to several people at once, you can send your message to a group instead. Example: “#awesomeGroup Party tonight!”
8. In order to join a group, a user sends the command “#groupName:join” to the server.
9. “#groupName:leave” removes you from a group chat. 
10. Only members can post messages to the group. 
11. Messages sent to a person show up on his client right away if he is connected.