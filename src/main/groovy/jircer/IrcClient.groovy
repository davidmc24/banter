package jircer

import banter.AcceptAllTrustManager
import groovy.util.logging.Slf4j
import org.apache.mina.core.session.IoSession
import org.apache.mina.filter.codec.ProtocolCodecFilter
import org.apache.mina.filter.logging.LoggingFilter
import org.apache.mina.filter.ssl.SslFilter
import org.apache.mina.transport.socket.SocketConnector
import org.apache.mina.transport.socket.nio.NioSocketConnector

import javax.net.ssl.KeyManager
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager

@Slf4j
class IrcClient implements MessageHandler {

    private SocketConnector connector
    private IoSession session

    void connect(String host, int port = 6667, boolean ssl = false) {
        connector = new NioSocketConnector()
        if (ssl) {
            connector.filterChain.addLast("ssl", createSslFilter())
        }
        connector.filterChain.addLast("logger", new LoggingFilter())
        connector.filterChain.addLast("codec", new ProtocolCodecFilter(IrcProtocolEncoder, IrcProtocolDecoder))
        connector.handler = new IrcClientHandler(this)
        def cf = connector.connect(new InetSocketAddress(host, port))
        cf.awaitUninterruptibly()
        session = cf.session
    }

    private SslFilter createSslFilter() {
        def sslContext = SSLContext.getInstance("SSL")
        def keyManagers = [] as KeyManager[]
        def trustManagers = [new AcceptAllTrustManager()] as TrustManager[]
        def secureRandom = null
        sslContext.init(keyManagers, trustManagers, secureRandom)
        def sslFilter = new SslFilter(sslContext)
        sslFilter.useClientMode = true
        sslFilter
    }

    void awaitClose() {
        session.closeFuture.awaitUninterruptibly()
    }

    void disconnect() {
        connector.dispose()
        session = null
        connector = null
    }

    void sendPass(String password) {
        send(new IrcMessage(type: IrcMessageType.PASS, parameters: [password]))
    }

    void sendNick(String nickname) {
        send(new IrcMessage(type: IrcMessageType.NICK, parameters: [nickname]))
    }

    void sendUser(String username, String realname) {
        // TODO: add support for user modes
        send(new IrcMessage(type: IrcMessageType.USER, parameters: [username, "0", "*", realname]))
    }

    void sendJoin(String channel, String key = null) {
        if (key) {
            send(new IrcMessage(type: IrcMessageType.JOIN, parameters: [channel, key]))
        } else {
            send(new IrcMessage(type: IrcMessageType.JOIN, parameters: [channel]))
        }
    }

    void sendList() {
        send(new IrcMessage(type: IrcMessageType.LIST))
    }

    void sendPong(String server) {
        send(new IrcMessage(type: IrcMessageType.PONG, parameters: [server]))
    }

    void sendWhois(String nick) {
        send(new IrcMessage(type: IrcMessageType.WHOIS, parameters: [nick]))
    }

    void send(IrcMessage message) {
        log.info("Sending message: {}", message)
        session.write(message)
    }

    @Override
    void onPing(IrcMessage message) {
        sendPong(message.parameters.first())
    }

    @Override
    void onListReply(IrcMessage message) {
    }

    @Override
    void onMessage(IrcMessage message) {
    }

    @Override
    void onNameReply(IrcMessage message) {
    }

    @Override
    void onOther(IrcMessage message) {
    }

    @Override
    void onJoin(IrcMessage message) {
    }

    @Override
    void onWhoisUser(IrcMessage message) {
    }

    @Override
    void onNick(IrcMessage message) {
    }

}
