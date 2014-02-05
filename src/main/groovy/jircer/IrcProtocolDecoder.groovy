package jircer

import org.apache.mina.core.buffer.IoBuffer
import org.apache.mina.core.session.IoSession
import org.apache.mina.filter.codec.CumulativeProtocolDecoder
import org.apache.mina.filter.codec.ProtocolDecoderOutput

import static java.nio.charset.StandardCharsets.UTF_8

class IrcProtocolDecoder extends CumulativeProtocolDecoder {

    @Override
    protected boolean doDecode(IoSession session, IoBuffer buffer, ProtocolDecoderOutput out) {
        def start = buffer.position()
        byte previous = 0
        while (buffer.hasRemaining()) {
            def current = buffer.get()
            if (current == "\n") {
                def position = buffer.position()
                def limit = buffer.limit()
                try {
                    buffer.position(start)
                    if (previous == "\r") {
                        buffer.limit(position-2)
                    } else {
                        buffer.limit(position-1)
                    }
                    out.write(parseMessage(buffer.slice().getString(UTF_8.newDecoder())))
                } finally {
                    buffer.limit(limit)
                    buffer.position(position)
                }
                return true // will be called again for the next line
            }
            previous = current
        }
        // no CLRF in buffer; reset so we can properly parse it later
        buffer.position(start)
        return false
    }

    static IrcMessage parseMessage(String line) {
        def sb = new StringBuilder(line)
        def prefix = null
        def code
        def params = []
        if (sb.startsWith(":")) {
            prefix = consumeToken(sb, 1)
        }
        code = consumeToken(sb)
        while (!sb.empty && params.size() < 14) {
            if (sb.startsWith(":")) {
                params << sb.substring(1)
                sb.clear()
            } else {
                params << consumeToken(sb)
            }
        }
        if (!sb.isEmpty()) {
            params << sb.startsWith(":") ? sb.substring(1) : sb.toString()
        }
        def message = new IrcMessage(prefix: prefix, type: IrcMessageType.forCode(code), parameters: params)
        return message
    }

    private static String consumeToken(StringBuilder sb, int offset = 0) {
        def token
        def spaceIndex = sb.indexOf(" ")
        if (spaceIndex == -1) {
            token = sb.substring(offset)
            sb.clear()
        } else {
            token = sb.substring(offset, spaceIndex)
            sb.delete(0, spaceIndex + 1)
        }
        return token
    }

}
