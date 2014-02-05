import ch.qos.logback.classic.filter.ThresholdFilter
import ch.qos.logback.classic.jul.LevelChangePropagator

if (Boolean.getBoolean("logging.debug")) {
    statusListener(OnConsoleStatusListener)
}

context = new LevelChangePropagator()
context.resetJUL = true

def runningAsService = System.getenv("UPSTART_JOB") != null

appender("CONSOLE", ConsoleAppender) {
    filter(ThresholdFilter) {
        level = INFO
    }
    encoder(PatternLayoutEncoder) {
        pattern = "%-5level %logger - %msg%n"
    }
}

appender("FILE", FileAppender) {
    file = "banter.log"
    append = true
    encoder(PatternLayoutEncoder) {
        pattern = "%d{HH:mm:ss.SSS} %-5level %logger - %msg%n"
    }
}

logger("org.elasticsearch", WARN)
logger("org.apache.mina", WARN)

def rootLevel = runningAsService ? INFO : DEBUG
def rootAppenders = ["FILE"]
if (!runningAsService) {
    rootAppenders << "CONSOLE"
}
root(rootLevel, rootAppenders)
