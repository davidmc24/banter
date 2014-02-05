package jircer

import groovy.transform.Canonical

@Canonical
class IrcMessage {

    String prefix
    IrcMessageType type
    List<String> parameters = []

}
