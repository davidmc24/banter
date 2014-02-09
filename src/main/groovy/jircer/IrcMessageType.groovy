package jircer

import groovy.util.logging.Slf4j

@Slf4j
enum IrcMessageType {
    NICK, USER, JOIN, LIST, PING, PONG, PRIVMSG, NOTICE, MODE, QUIT, WHOIS, PART, PASS, ERROR,
    // https://www.alien.net.au/irc/irc2numerics.html
    RPL_WELCOME("001"), RPL_YOURHOST("002"), RPL_CREATED("003"), RPL_MYINFO("004"), // RFC2812
    RPL_ISUPPORT("005"), // https://tools.ietf.org/html/draft-brocklesby-irc-isupport-03; conflicts with RFC2812
    RPL_CONNECTING("020"), // observed with ircd-irc2
    RPL_YOURID("042"), // non-spec
    RPL_STATSCONN("250"), // non-spec
    RPL_LUSERCLIENT("251"), RPL_LUSEROP("252"), RPL_LUSERUNKNOWN("253"), RPL_LUSERCHANNELS("254"), // RFC1459
    RPL_LUSERME("255"), // RFC1459
    RPL_LOCALUSERS("265"), RPL_GLOBALUSERS("266"), // non-spec
    RPL_WHOISUSER("311"), RPL_WHOISSERVER("312"), // RFC1459
    RPL_WHOISIDLE("317"), RPL_ENDOFWHOIS("318"), RPL_WHOISCHANNELS("319"), // RFC1459
    RPL_LISTSTART("321"), // RFC1459, deprecated
    RPL_LIST("322"), RPL_LISTEND("323"), // RFC1459
    RPL_NAMREPLY("353"), RPL_ENDOFNAMES("366"), // RFC1459
    RPL_MOTD("372"), RPL_MOTDSTART("375"), RPL_ENDOFMOTD("376"), // RFC1459
    RPL_WHOISHOST("378"), RPL_WHOISMODES("379"), // non-spec
    ERR_NOSUCHCHANNEL("403"), // RFC1459
    ERR_NOMOTD("422"),
    ERR_NICKNAMEINUSE("433"),
    ERR_NOTREGISTERED("451"),
    OTHER("")

    String code

    IrcMessageType() {
        this.code = name()
    }

    IrcMessageType(String code) {
        this.code = code
    }

    static IrcMessageType forCode(String code) {
        def value = values().find {it.code == code}
        if (!value) {
            log.warn("Unknown message type: {}", code)
            value = OTHER
        }
        return value
    }

}
