package jircer

import org.apache.mina.core.buffer.IoBuffer
import org.apache.mina.core.service.TransportMetadata
import org.apache.mina.core.session.IoSession
import org.apache.mina.filter.codec.ProtocolDecoderOutput
import spock.lang.Specification

import static java.nio.charset.StandardCharsets.UTF_8

class IrcProtocolDecoderSpec extends Specification {

    def decoder = new IrcProtocolDecoder()
    def transportMetadata = Mock(TransportMetadata)
    def session = Mock(IoSession)
    def output = Mock(ProtocolDecoderOutput)

    def "A line is decoded into a populated message object"() {
        setup:
        session.transportMetadata >> transportMetadata
        transportMetadata.hasFragmentation() >> false
        def buffer = IoBuffer.wrap(line.getBytes(UTF_8))

        when:
        decoder.decode(session, buffer, output)

        then:
        1 * output.write({
            it instanceof IrcMessage && it.prefix == prefix && it.type == type && it.parameters == parameters
        })

        where:
        line | prefix | type | parameters
        "NICK :Paul\r\n" | null | IrcMessageType.NICK | ["Paul"]
    }

//    def "Each non-empty line is decoded into a message instance"() {
//
//    }

}
