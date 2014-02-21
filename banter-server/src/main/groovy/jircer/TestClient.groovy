package jircer

class TestClient {

    static void main(String... args) {
        def client = new IrcClient()
        client.connect("192.168.33.16")
        client.sendNick("testc")
        client.sendUser("testc", "testc")
        client.sendList()
        client.awaitClose()
        client.disconnect()
    }

}
