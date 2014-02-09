package banter

import groovy.util.logging.Slf4j
import jircer.IrcClient
import jircer.IrcMessage

import javax.inject.Inject
import javax.inject.Named

@Slf4j
class BanterBot extends IrcClient {

    String host
    int port
    String nick
    String username
    String realname
    String password
    Indexer indexer

    final Set<String> channelMembership = []
    final Map<String, UserInfo> userInfo = [:]

    @Inject
    BanterBot(@Named("ircHostname") String host, @Named("ircPort") int port, @Named("ircNickname") String nick,
              @Named("ircUsername") String username, @Named("ircRealname") String realname,
              @Named("ircPassword") String password,
              Indexer indexer) {
        this.host = host
        this.port = port
        this.nick = nick
        this.username = username
        this.realname = realname
        this.password = password
        this.indexer = indexer
        connect()
    }

    private void connect() {
        log.info("Starting banterbot: {}, {}, {}", host, port, nick)
        connect(host, port)
        if (password) {
            sendPass(password)
        }
        sendNick(nick)
        sendUser(username, realname)
        // TODO: reconnects
    }

    @Override
    void onMessage(IrcMessage message) {
        def nickname = prefixToNickname(message.prefix)
        def channel = message.parameters[0]
        def text = message.parameters[1]
        // TODO: only index messages to channels, not private messages
        def info = userInfo[nickname] ?: new UserInfo(nickname)
        indexer.indexMessage(info, channel, text)
    }

    private String getPrefixedNick() {
        return "@${nick}"
    }

    @Override
    void onNameReply(IrcMessage message) {
        assert message.parameters.size() >= 4
        def channelName = message.parameters[2]
        def members = message.parameters[3]
        log.info("Got members for channel {}: {}", channelName, members)
        for (member in members.split(" ").collect {stripNickPrefixes(it)}) {
            if (!userInfo[member]) {
                sendWhois(member)
            }
        }
    }

    @Override
    void onJoin(IrcMessage message) {
        log.info("Got join message: {}", message)
        def who = prefixToNickname(message.prefix)
        if (who == nick) {
            for (channelName in message.parameters[0].split(",")) {
                log.info("Received notice that I joined channel {}", channelName)
                channelMembership.add(channelName)
            }
        }
        if (!userInfo[who]) {
            sendWhois(who)
        }
    }

    @Override
    void onWhoisUser(IrcMessage message) {
        def nickname = message.parameters[1]
        def username = stripUsernamePrefixes(message.parameters[2])
        def hostname = message.parameters[3]
        def server = message.parameters[5]
        def info = new UserInfo(nickname, username, hostname, server)
        log.info("Setting user info for {} to {}", nickname, info)
        userInfo[nickname] = info
    }

    @Override
    void onNick(IrcMessage message) {
        log.info("Get nick message: {}", message)
        def oldNick = prefixToNickname(message.prefix)
        def newNick = message.parameters[0]
        log.info("{} is now {}", oldNick, newNick)
        def info = userInfo[oldNick]
        if (info) {
            info.nickname = newNick
            userInfo[newNick] = info
        }
    }

    boolean isInChannel(String channelName) {
        // TODO: case insensitive?
        return channelMembership.contains(channelName)
    }

    private String prefixToNickname(String prefix) {
        def nickname = prefix
        // TODO: use leftSplit
        def exclamationIndex = nickname.indexOf("!")
        if (exclamationIndex >= 0) {
            nickname = nickname.substring(0, exclamationIndex)
        }
        return nickname
    }

    private String stripUsernamePrefixes(String nick) {
        return nick.replaceAll(/[\~]/, "")
    }

    private String stripNickPrefixes(String nick) {
        return nick.replaceAll(/[\~&@%\+]/, "")
    }

}
