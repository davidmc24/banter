package jircer

interface MessageHandler {

    // TODO: use specialized subclasses?

    void onPing(IrcMessage message)

    void onListReply(IrcMessage message)

    void onMessage(IrcMessage message)

    void onOther(IrcMessage message)

}
