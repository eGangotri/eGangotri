package com.egangotri.rest

import com.egangotri.util.EGangotriUtil
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j

import java.text.SimpleDateFormat
import static groovyx.net.http.HttpBuilder.configure
import static groovy.json.JsonOutput.toJson

@Slf4j
class RestUtil {
    static SimpleDateFormat dateFormat = new SimpleDateFormat(EGangotriUtil.DATE_TIME_PATTERN)
    static postCaller(String uri = 'http://127.0.0.1:80',
                      String path = '/itemsQueued/list',
                      Map body = [title: 'food', body: 'bar', userId: 1]) {
        def posts
        try {
            posts = configure {
                request.uri = 'https://jsonplaceholder.typicode.com'
                request.uri.path = '/posts'
                request.contentType = 'application/json'
                request.body = toJson(body)
            }.post()
            log.info("posts ${posts}")
        }
        catch(Exception e){
            log.info("post call failed for ${uri}/${path}",e)
        }
        return posts
    }

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

    static String makePostCall(String urlString = 'https://postman-echo.com/post', String form = "param1=This is request parameter.") {
        def postmanPost = new URL(urlString)
        JsonSlurper jsonSlurper = new JsonSlurper()
        def text
        try {
            def connection = postmanPost.openConnection()
            connection.requestMethod = 'POST'
            connection.setRequestProperty("Content-Type", "application/json")
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

    static String createPostParamsString(String ...params) {
        if(params.length !=6 ){
            log.info("exactly 6 params expected")
            return
        }

        int counter = 0
        List paramsArray = []
        paramsArray.add ("archiveProfile=${params[counter++]}")
        paramsArray.add("uploadLink=${params[counter++]}")
        paramsArray.add("localPath=${params[counter++]}")
        paramsArray.add("title=${params[counter++]}")
        paramsArray.add("uploadCycleId=${params[counter++]}")
        paramsArray.add("csvName=${params[counter++]}")

        paramsArray.add("datetimeUploadStarted=${dateFormat.format(new Date())}")
        String paramsDecorated = ""
        paramsArray.each {
            paramsDecorated += "${it}&"
        }
        log.info("paramsDecorated ${paramsDecorated}")
        return paramsDecorated
    }
    static void main(String[] args) {
        //makePostCall()
        int counter = 1;
        String paramsDecorated = createPostParamsString("${counter++}", "${counter++}", "${counter++}", "${counter++}", "${counter++}", "${counter++}")
        postCaller()
//        makePostCall("http://127.0.0.1:80/itemsQueued/add",
//        paramsDecorated)
//        makeGetCall("http://127.0.0.1:80/itemsQueued/list")
//        makeGetCall("http://127.0.0.1:80/itemsQueued/listByProfile?limit=10")
    }
}
