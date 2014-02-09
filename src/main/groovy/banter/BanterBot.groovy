package banter

import groovy.util.logging.Slf4j
import jircer.IrcClient
import jircer.IrcMessage

import javax.inject.Inject
import javax.inject.Named
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.CopyOnWriteArraySet

@Slf4j
class BanterBot extends IrcClient {

    String host
    int port
    String nick
    String username
    String realname
    String password
    boolean ssl
    Indexer indexer
    Searcher searcher

    final Set<String> knownChannels = new CopyOnWriteArraySet<>()
    final Set<String> channelMembership = new ConcurrentSkipListSet<>()
    final Map<String, UserInfo> userInfo = new ConcurrentHashMap<>()
    final Set<String> pendingWhois = new ConcurrentSkipListSet<>()

    @Inject
    BanterBot(@Named("irc.hostname") String host, @Named("irc.port") int port, @Named("irc.nickname") String nick,
              @Named("irc.username") String username, @Named("irc.realname") String realname,
              @Named("irc.password") String password, @Named("irc.ssl") boolean ssl,
              Indexer indexer, Searcher searcher) {
        this.host = host
        this.port = port
        this.ssl = ssl
        this.nick = nick
        this.username = username
        this.realname = realname
        this.password = password
        this.indexer = indexer
        this.searcher = searcher
        knownChannels.addAll(searcher.knownChannels)
        connect()
        knownChannels.each {sendJoin(it)}
    }

    private void connect() {
        log.info("Starting banterbot: {}, {}, {}, {}", nick, host, port, ssl)
        connect(host, port, ssl)
        if (password) {
            sendPass(password)
        }
        sendNick(nick)
        sendUser(username, realname)
        // TODO: reconnects
    }

    void attemptToJoin(String channel) {
        knownChannels.add(channel)
        sendJoin(channel)
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

//    private String getPrefixedNick() {
//        return "@${nick}"
//    }

    void requestWhois(String nickname) {
        if (!userInfo[nickname]) {
            if (pendingWhois.add(nickname)) {
                sendWhois(nickname)
            } else {
                log.debug("Already requested whois info for {}", nickname)
            }
        }
    }

    @Override
    void onNameReply(IrcMessage message) {
        assert message.parameters.size() >= 4
        def channelName = message.parameters[2]
        def members = message.parameters[3]
        log.info("Got members for channel {}: {}", channelName, members)
        for (member in members.split(" ").collect {stripNickPrefixes(it)}) {
            requestWhois(member)
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
        requestWhois(who)
    }

    @Override
    void onWhoisUser(IrcMessage message) {
        def nickname = message.parameters[1]
//        def username = stripUsernamePrefixes(message.parameters[2])
        def username = message.parameters[2]
        def hostname = message.parameters[3]
        def server = message.parameters[5]
        def info = new UserInfo(nickname, username, hostname, server)
        log.info("Setting user info for {} to {}", nickname, info)
        pendingWhois.remove(nickname)
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
