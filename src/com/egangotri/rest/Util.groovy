package com.egangotri.rest

import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j

@Slf4j
class Util {
    static def toJson(def result) {
        def json = new JsonBuilder()
        json(result)
        println "json output: "
        println JsonOutput.prettyPrint(json.toString())
        JsonSlurper jsonSlurper = new JsonSlurper()
        def toJson = jsonSlurper.parseText(JsonOutput.prettyPrint(json.toString()))
        log.info("toJson ${toJson}")
        return toJson
    }
}
