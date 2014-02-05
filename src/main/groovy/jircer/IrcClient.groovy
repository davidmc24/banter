package jircer

import groovy.util.logging.Slf4j
import org.apache.mina.core.session.IoSession
import org.apache.mina.filter.codec.ProtocolCodecFilter
import org.apache.mina.transport.socket.SocketConnector
import org.apache.mina.transport.socket.nio.NioSocketConnector

@Slf4j
class IrcClient implements MessageHandler {

    private SocketConnector connector
    private IoSession session

    void connect(String host, int port = 6667) {
        connector = new NioSocketConnector()
//        connector.filterChain.addLast("logger", new LoggingFilter())
        connector.filterChain.addLast("codec", new ProtocolCodecFilter(IrcProtocolEncoder, IrcProtocolDecoder))
        connector.handler = new IrcClientHandler(this)
        def cf = connector.connect(new InetSocketAddress(host, port))
        cf.awaitUninterruptibly()
        session = cf.session
    }

    void awaitClose() {
        session.closeFuture.awaitUninterruptibly()
    }

    void disconnect() {
        connector.dispose()
        session = null
        connector = null
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
    void onOther(IrcMessage message) {
    }
}
