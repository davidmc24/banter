package jircer

import groovy.util.logging.Slf4j
import org.apache.mina.core.service.IoHandlerAdapter
import org.apache.mina.core.session.IdleStatus
import org.apache.mina.core.session.IoSession

@Slf4j
class IrcClientHandler extends IoHandlerAdapter {

    private final MessageHandler messageHandler

    IrcClientHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler
    }

    @Override
    public void sessionOpened(IoSession session) {
        // Set reader idle time to 10 seconds.
        // sessionIdle(...) method will be invoked when no data is read
        // for 10 seconds.
//        session.getConfig().setIdleTime(IdleStatus.READER_IDLE, 10);
    }

    @Override
    public void sessionClosed(IoSession session) {
        // Print out total number of bytes read from the remote peer.
//        System.err.println("Total " + session.getReadBytes() + " byte(s)");
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) {
        // Close the connection if reader is idle.
//        if (status == IdleStatus.READER_IDLE) {
//            session.close(true);
//        }
    }

    @Override
    public void messageReceived(IoSession session, Object message) {
        log.info("Got message: {}", message)
        def msg = message as IrcMessage
        switch(msg.type) {
            case IrcMessageType.PING: messageHandler.onPing(msg); break
            case IrcMessageType.RPL_LIST: messageHandler.onListReply(msg); break
            case IrcMessageType.PRIVMSG: messageHandler.onMessage(msg); break
            default: messageHandler.onOther(msg); break
        }
//        def buf = (IoBuffer) message;
//        Print out read buffer content.
//        while (buf.hasRemaining()) {
//            System.out.print((char) buf.get());
//        }
//        System.out.flush();
    }

}
