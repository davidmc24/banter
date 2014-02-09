import banter.BanterBot
import banter.BanterModule
import banter.HttpHeaderConstants
import banter.Searcher
import org.elasticsearch.client.Client
import org.joda.time.DateTimeZone
import org.joda.time.format.ISODateTimeFormat
import org.slf4j.LoggerFactory
import ratpack.jackson.JacksonModule
import ratpack.jackson.Jackson
import ratpack.groovy.Groovy

import static io.netty.handler.codec.http.HttpResponseStatus.*

def log = LoggerFactory.getLogger("banter.ratpack.handler")

Groovy.ratpack {
    modules {
        register new JacksonModule()
        register new BanterModule()
    }
    handlers {
        prefix("channel") {
            get { BanterBot bot ->
                render Jackson.json(bot.channelMembership.sort())
            }
            prefix(":channel") {
                handler { BanterBot bot ->
                    byMethod {
                        get {
                            def channel = pathTokens["channel"]
                            response.status(bot.isInChannel(channel) ? NO_CONTENT : NOT_FOUND)
                            response.send()
                        }
                        put {
                            def channel = pathTokens["channel"]
                            if (bot.isInChannel(channel)) {
                                response.status(NO_CONTENT)
                            } else {
                                log.info("Attempting to join channel {}", channel)
                                bot.sendJoin(channel)
                                response.status(ACCEPTED)
                            }
                            def encodedChannel = URLEncoder.encode(channel, "UTF-8")
                            response.headers.set(HttpHeaderConstants.LOCATION, "/channel/${encodedChannel}")
                            response.send()
                        }
                    }
                }
            }
        }
        get { Searcher searcher ->
            def channel = request.queryParams["channel"]
            def query = request.queryParams["query"]
            def hits = searcher.search(channel, query)
            render Groovy.groovyTemplate("index.html", title: "My Ratpack App", query: query, hits: hits)
        }
        get ("context") { Searcher searcher ->
            def channel = request.queryParams["channel"]
            def query = request.queryParams["query"]
            def timestamp = request.queryParams["timestamp"]?.parseDateTime()
            def hits = searcher.searchContext(channel, query, timestamp)
            render Groovy.groovyTemplate("context.html", hits: hits)
        }
        assets "public"
    }
}
