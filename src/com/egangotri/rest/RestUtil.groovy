package com.egangotri.rest

import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.groovy.json.internal.LazyMap

import static groovyx.net.http.HttpBuilder.configure
import static groovy.json.JsonOutput.toJson

@Slf4j
class RestUtil {
    static String REST_CALL_URI = 'http://127.0.0.1:80'
    static String ITEMS_QUEUED_PATH = "itemsQueued"
    static String ITEMS_USHERED_PATH = "itemsUshered"

    static makePostCall(String path,
                        Map body,
                        String urlString = REST_CALL_URI) {
        def posts
        try {
            posts = configure {
                request.uri = urlString
                request.uri.path = path
                request.contentType = 'application/json'
                request.body = toJson(body)
            }.post()
            log.info("posts ${posts}")
        }
        catch (Exception e) {
            log.info("MakeRestCall Error while calling ${urlString}${path}", e)
        }
        return posts
    }

    static def makeGetCall(String path, Map queryMap = [name: 'Bob'], String urlString = REST_CALL_URI) {
        try {
            def doGet = configure {
                request.uri = urlString
            }.get {
                request.uri.path = path
                request.uri.query = queryMap ?: [:]
            }
            log.info("doGet ${doGet}")
            return doGet
        }
        catch (Exception e) {
            log.info("MakeGetCall Error while calling ${urlString}/${path}", e)
            return null
        }
    }

    static boolean startDBServerIfOff() {
        String mongoServerExecScript = "./bat_files/startMongoApiServer.bat"
        if (!checkIfDBServerIsOn()) {
            log.info("Starting DB Server")
            Runtime.getRuntime().exec(mongoServerExecScript)
            Thread.sleep(10000)
        }
    }

    static boolean startDashboardServerIfOff() {
        String mongoServerExecScript = "./bat_files/startDashboardServer.bat"
        if (!checkIfDashboardServerIsOn()) {
            log.info("Starting DB Server")
            Runtime.getRuntime().exec(mongoServerExecScript)
            Thread.sleep(10000)
        }
    }

    static boolean checkIfDBServerIsOn() {
        try {
            LazyMap result = makeGetCall("/") ?: [:] as Map
            log.info("toJson.response ${result && result.containsKey("response") ? result.response : '--'}")
            return (result && result.containsKey("response")) ? result.response == "eGangotri-node-backend" : false
        }
        catch (ConnectException e) {
            log.error(e.message)
            return false;
        }
    }

    static boolean checkIfDashboardServerIsOn() {
        try {
            LazyMap result = makeGetCall("/") ?: [:] as Map
            log.info("toJson.response ${result && result.containsKey("response") ? result.response : '--'}")
            return (result && result.containsKey("response")) ? result.response == "eGangotri-node-backend" : false
        }
        catch (ConnectException e) {
            log.error(e.message)
            return false;
        }
    }

    static def listQueues() {
        LazyMap result = makeGetCall("/${ITEMS_QUEUED_PATH}/list") ?: [:]
        log.info("toJson.response ${result && result.containsKey("response") ? result.response : '--'}")
        return (result && result.containsKey("response")) ? result.response : []
    }

    static void main(String[] args) {
        log.info("checkIfDBServerIsOn ${checkIfDBServerIsOn()}")
        log.info("listQueues ${listQueues()}")
        log.info("End of RestUtil")
    }
}
