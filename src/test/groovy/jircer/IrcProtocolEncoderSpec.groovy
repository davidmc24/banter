package jircer

import org.apache.mina.core.buffer.IoBuffer
import org.apache.mina.core.session.IoSession
import org.apache.mina.filter.codec.ProtocolEncoderOutput
import spock.lang.Specification

import static java.nio.charset.StandardCharsets.UTF_8

class IrcProtocolEncoderSpec extends Specification {

    def encoder = new IrcProtocolEncoder()
    def session = Mock(IoSession)
    def output = Mock(ProtocolEncoderOutput)

    def "Messages are encoded with protocol, command, arguments, and newline"() {
        setup:
        def message = new IrcMessage(prefix, type, parameters)

        when:
        encoder.encode(session, message, output)

        then:
        1 * output.write({it instanceof IoBuffer && it.getString(UTF_8.newDecoder()) == expectedEncoding})

        where:
        prefix                   | type              | parameters                                   | expectedEncoding
        null                     | IrcMessageType.NICK              | ["Paul"]                                     | "NICK :Paul\r\n"
        "kornbluth.freenode.net" | IrcMessageType.ERR_NICKNAMEINUSE | ["*", "Paul", "Nickname is already in use."] | ":kornbluth.freenode.net 433 * Paul :Nickname is already in use.\r\n"
        null                     | IrcMessageType.USER              | ["paul", "8", "*", "Paul Mutton"]            | "USER paul 8 * :Paul Mutton\r\n"
        null                     | IrcMessageType.PING              | ["kornbluth.freenode.net"]                   | "PING :kornbluth.freenode.net\r\n"
        null                     | IrcMessageType.PONG              | ["kornbluth.freenode.net"]                   | "PONG :kornbluth.freenode.net\r\n"
        null                     | IrcMessageType.JOIN              | ["#irchacks"]                                | "JOIN :#irchacks\r\n"
    }

}
