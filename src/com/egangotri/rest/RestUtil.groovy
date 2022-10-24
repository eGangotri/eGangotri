package com.egangotri.rest

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j

@Slf4j
class RestUtil {
    static String makeGetCall(String urlString = 'https://postman-echo.com/get') {
        def res
        URL postmanGet = new URL(urlString)
        try {
            URLConnection connection = postmanGet.openConnection()
            connection.requestMethod = 'GET'
            log.info("connection.responseCode(GET) == 200 ?" + connection.responseCode)
            log.info("Calling ${urlString}")

            if(connection.responseCode < 300){
                res = connection.getContent()
                log.info "\nget: ${res}"
            }
        }
        catch (Exception e) {
            log.info("MakeGetCall Error while calling ${urlString}", e)
        }
        finally {
            postmanGet = null
        }
        return res?.toString()
    }

    static String makePostCall(String urlString = 'https://postman-echo.com/post') {
        def postmanPost = new URL(urlString)
        JsonSlurper jsonSlurper = new JsonSlurper()
        def text
        try {
            def connection = postmanPost.openConnection()
            connection.requestMethod = 'POST'
            connection.setRequestProperty("Content-Type", "application/json")

            def form = "param1=This is request parameter."
            connection.doOutput = true
            connection.with {
                outputStream.withWriter { outputStreamWriter ->
                    outputStreamWriter << form
                }
                text = content?.text ?: "NO RESP"
            }
            log.info " connection.responseCode(POST) == 200 ? ${connection.responseCode}"
            log.info "\npost: ${jsonSlurper.parseText(text)}"
        }
        catch (Exception e) {
            log.info("MakeRestCall Error while calling ${urlString}", e)
        }
        finally {
            postmanPost = null
        }
        return jsonSlurper.parseText(text)
    }

    static void main(String[] args) {
        makePostCall()
        makeGetCall("http://127.0.0.1:80/itemsQueued/list")
        makeGetCall("http://127.0.0.1:80/itemsQueued/listByProfile?limit=10")
    }
}
