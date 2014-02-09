package banter

import com.google.inject.Inject
import groovy.util.logging.Slf4j
import org.elasticsearch.ElasticsearchException
import org.elasticsearch.client.Client

import static banter.SearchConstants.INDEX
import static banter.SearchConstants.TYPE

@Slf4j
class Indexer {

    Client client

    @Inject
    Indexer(Client client) {
        this.client = client
        initializeIndices()
    }

    void indexMessage(UserInfo userInfo, String channel, String text) {
        // Elasticsearch automatically creates a _timestamp which is indexed but not stored
        def doc = [timestamp: new Date(), nickname: userInfo.nickname, username: userInfo.username, realname: userInfo.realname, channel: channel, text: text]
        def indexResponse = client.prepareIndex(INDEX, TYPE).setSource(doc).execute().actionGet()
        log.info("Indexing result: created={}, index={}, type={}, id={}, version={}", indexResponse.created,
                indexResponse.index, indexResponse.type, indexResponse.id, indexResponse.version)
    }

    private void initializeIndices() {
        def mapping = [properties: [
                timestamp:[type:"date"],
                nickname:[type:"string", index:"not_analyzed"],
                username:[type:"string", index:"not_analyzed"],
                realname:[type:"string", index:"not_analyzed"],
                channel:[type:"string", index:"not_analyzed"],
                text:[type:"string"]
        ]]
        pushMapping(INDEX, TYPE, mapping)
    }

    private void pushMapping(String index, String type, Map<String, Object> mapping) {
        if (!indexExists(index)) {
            log.info("Index {} doesn't exist; attempting to create", index)
            def createIndexRequest = client.admin().indices().prepareCreate(index).addMapping(type, mapping)
            def createIndexResponse = createIndexRequest.get()
            if (!createIndexResponse.acknowledged) {
                throw new ElasticsearchException("Could not push mapping for type ${type} to index ${index}")
            }
        } else if (!mappingExists(index, type)) {
            log.info("Mapping for type {} doesn't exist in index {}; attempting to create", type, index)
            def putMappingRequest = client.admin().indices().preparePutMapping(index).setType(type).setSource(mapping)
            def putMappingResponse = putMappingRequest.get()
            if (!putMappingResponse.acknowledged) {
                throw new ElasticsearchException("Could not push mapping for type ${type} to index ${index}")
            }
            log.info("Pushed mapping for type {} to index {}", type, index)
        } else {
            log.info("Mapping for type {} exists in index {}", type, index)
//            TODO: handle updates???
        }
    }

    private boolean indexExists(String index) {
        def clusterState = client.admin().cluster().prepareState().setIndices(INDEX).get().state
        def indexMetaData = clusterState.metaData.index(index)
        return indexMetaData != null
    }

    private boolean mappingExists(String index, String type) {
        def clusterState = client.admin().cluster().prepareState().setIndices(INDEX).get().state
        def indexMetaData = clusterState.metaData.index(index)
        return indexMetaData?.mapping(type) != null
    }

}
