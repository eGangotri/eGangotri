import ch.qos.logback.classic.AsyncAppender
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.FileAppender
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy
import static ch.qos.logback.classic.Level.INFO

def LOG_PATH = "target"
def LOG_ARCHIVE = "${LOG_PATH}/archive"
def USER_HOME = System.getProperty("user.home")
def GOOGLE_DRIVE_PATH = "$USER_HOME/eGangotri/google_drive/archive_uploader/server_logs"
def CURRENT_TIME = timestamp("yyyy-MM-dd HH-mm")

appender("Console-Appender", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        //pattern = "[%-5level] %d{yyyy-MM-dd HH:mm:ss}  %c{1} - %msg%n"
        pattern = "%msg%n"
    }
}
appender("File-Appender", FileAppender) {
    file = "${LOG_PATH}/egangotri.log"
    encoder(PatternLayoutEncoder) {
        //pattern = "[%-5level] %d{yyyy-MM-dd HH:mm:ss}  %c{1} - %msg%n"
        pattern = "%msg%n"
        outputPatternAsHeader = false
    }
}

appender("RollingFile-Appender", RollingFileAppender) {
    file = "${LOG_PATH}/egangotri_rolling.log"
    rollingPolicy(TimeBasedRollingPolicy) {
        fileNamePattern = "${LOG_ARCHIVE}/rollingfile_%d{yyyy-MM-dd}.log"
        maxFileSize = "10MB"
        maxHistory = 30
    }
    encoder(PatternLayoutEncoder) {
        pattern = "%msg%n"
    }
}

//With this Setting your logs will end up in
appender("Google-Drive-Appender", FileAppender) {
    file = "${GOOGLE_DRIVE_PATH}/egangotri_${CURRENT_TIME}.log"
    encoder(PatternLayoutEncoder) {
        //pattern = "[%-5level] %d{yyyy-MM-dd HH:mm:ss}  %c{1} - %msg%n"
        pattern = "%msg%n"
        outputPatternAsHeader = false
    }
}
logger("com.egangotri", INFO, ["Console-Appender", "File-Appender","Google-Drive-Appender", "RollingFile-Appender" ], false)
root(INFO, ["Console-Appender"])