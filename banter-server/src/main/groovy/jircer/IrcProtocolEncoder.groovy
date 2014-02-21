package jircer

import org.apache.mina.core.buffer.IoBuffer
import org.apache.mina.core.session.IoSession
import org.apache.mina.filter.codec.ProtocolEncoder
import org.apache.mina.filter.codec.ProtocolEncoderOutput

import static java.nio.charset.StandardCharsets.UTF_8

class IrcProtocolEncoder implements ProtocolEncoder {
    private static final int INITIAL_BUFFER_CAPACITY = 64
    private static final String COLON = ":"
    private static final String SPACE = " "
    private static final String CRLF = "\r\n"

    @Override
    void encode(IoSession session, Object message, ProtocolEncoderOutput out) {
        def encoder = UTF_8.newEncoder()
        def ircMessage = message as IrcMessage
        def buffer = IoBuffer.allocate(INITIAL_BUFFER_CAPACITY).setAutoExpand(true)
        if (ircMessage.prefix) {
            buffer.putString(COLON, encoder)
            buffer.putString(ircMessage.prefix, encoder)
            buffer.putString(SPACE, encoder)
        }
        buffer.putString(ircMessage.type.code, encoder)
        def params = ircMessage.parameters
        def middleParams = params.take(params.size()-1)
        def trailingParams = params.reverse().take(1)
        for (param in middleParams) {
            buffer.putString(SPACE, encoder)
            buffer.putString(param, encoder)
        }
        for (param in trailingParams) {
            buffer.putString(SPACE, encoder)
            buffer.putString(COLON, encoder)
            buffer.putString(param, encoder)
        }
        buffer.putString(CRLF, encoder)
        buffer.flip()
        out.write(buffer)
    }

    @Override
    void dispose(IoSession session) {
    }
}
