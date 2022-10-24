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
        catch(Exception e){
            log.info("MakeRestCall Error while calling ${urlString}/${path}", e)
        }
        return posts
    }

    static def makeGetCall(String path, Map queryMap = [name: 'Bob'], String urlString = REST_CALL_URI)
    {
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
        catch(Exception e){
            log.info("MakeGetCall Error while calling ${urlString}/${path}", e)
            return null
        }
    }

    static boolean checkIfDBServerIsOn(){
        LazyMap result = makeGetCall("/")?: [:] as Map
        log.info("toJson.response ${result && result.containsKey("response") ? result.response : '--'}")
        return (result && result.containsKey("response")) ? result.response == "eGangotri-node-backend" : false
    }

    static def listQueues(){
        LazyMap<String,Object> result = makeGetCall("/itemsQueued/list")?:[:]
        log.info("toJson.response ${result && result.containsKey("response") ? result.response : '--'}")
        return (result && result.containsKey("response")) ? result.response : []
    }

    static void main(String[] args) {
        log.info ("checkIfDBServerIsOn ${checkIfDBServerIsOn()}")
        log.info ("listQueues ${listQueues()}")
        log.info ("End of RestUtil")
    }
}
