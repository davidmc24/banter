package jircer

interface MessageHandler {

    // TODO: use specialized subclasses?

    void onPing(IrcMessage message)

    void onListReply(IrcMessage message)

    void onMessage(IrcMessage message)

    void onNameReply(IrcMessage message)

    void onOther(IrcMessage message)

    void onJoin(IrcMessage message)

    void onNick(IrcMessage message)

    void onWhoisUser(IrcMessage message)

}
