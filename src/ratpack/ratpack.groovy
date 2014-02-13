import banter.BanterBot
import banter.BanterModule
import banter.HttpHeaderConstants
import banter.MessageSearchHit
import banter.Searcher
import banter.ThymeleafLayoutModule
import com.google.inject.util.Modules
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.slf4j.LoggerFactory
import ratpack.jackson.JacksonModule
import ratpack.jackson.Jackson
import ratpack.groovy.Groovy
import ratpack.form.Form
import ratpack.thymeleaf.Template
import ratpack.thymeleaf.ThymeleafModule

import static io.netty.handler.codec.http.HttpResponseStatus.*

def log = LoggerFactory.getLogger("banter.ratpack.handler")

Groovy.ratpack {
    modules {
        register new JacksonModule()
        // TODO: update based on https://github.com/ratpack/ratpack/issues/253
        register Modules.override(new ThymeleafModule()).with(new ThymeleafLayoutModule())
        register new BanterModule()
    }
    handlers {
        prefix("api") {
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
        }
        get { BanterBot bot ->
            render Template.thymeleafTemplate("index", channels: bot.knownChannels.sort())
        }
        get ("admin") { BanterBot bot ->
            def notice = request.queryParams["notice"] ?: ""
            render Template.thymeleafTemplate("admin", channels: bot.knownChannels.sort(), notice: notice)
        }
        get ("search") { Searcher searcher, BanterBot bot ->
            def channel = request.queryParams["channel"] ?: ""
            def q = request.queryParams["q"] ?: ""
            def hits = searcher.search(channel, q)
            render Template.thymeleafTemplate("search", q: q, channel: channel, hits: hits.hits.collect {new MessageSearchHit(it)}, channels: bot.knownChannels.sort())
        }
        get ("context") { Searcher searcher, BanterBot bot ->
            def channel = request.queryParams["channel"] ?: ""
            def q = request.queryParams["q"]
            def timestamp = request.queryParams["timestamp"]?.parseDateTime() ?: DateTime.now()
            def dateString = DateTimeFormat.longDate().print(timestamp)
            def hits = searcher.searchContext(channel, q, timestamp)
            render Groovy.groovyTemplate("context.html", q: q, channel: channel, hits: hits, channels: bot.knownChannels.sort(), dateString: dateString)
        }
        post ("addChannel") { BanterBot banterBot ->
            def form = parse(Form)
            def channel = form["channel"]
            if (channel) {
                if (!channel.startsWith("#")) {
                    channel = "#${channel}"
                }
                banterBot.attemptToJoin(channel)
                redirect("/admin?notice=Registered channel ${channel.encodeURIComponent()}")
            } else {
                response.status(400).send()
            }
        }
        assets "public"
    }
}
