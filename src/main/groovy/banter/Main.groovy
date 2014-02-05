package banter

import static org.elasticsearch.node.NodeBuilder.*

class Main {

    static void main(String... args) {
        // TODO: consider using clustering
        def node = nodeBuilder().local(true).node()
        def client = node.client()
        try {
            // TODO: impl
        } finally {
            node.close()
        }
    }

}
