package com.egangotri.rest

import com.egangotri.upload.util.SettingsUtil
import com.google.gson.Gson
import groovy.util.logging.Slf4j

import okhttp3.OkHttpClient
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.Request
import okhttp3.Response

@Slf4j
class RestUtil {

    static String ITEMS_QUEUED_PATH = "itemsQueued"
    static String ITEMS_USHERED_PATH = "itemsUshered"
    static String UPLOAD_CYCLE_ROUTE = "uploadCycle"
    static String backendServer = SettingsUtil.EGANGOTRI_BACKEND_SERVER


    static makePostCall(String path,
                        Map requestData) {
        OkHttpClient client = new OkHttpClient()
        Gson gson = new Gson()
        requestData.put("superadmin_user", SettingsUtil.EGANGOTRI_BACKEND_SUPERADMIN_USER)
        requestData.put("superadmin_password", SettingsUtil.EGANGOTRI_BACKEND_SUPERADMIN_PASSWORD)

        String json = gson.toJson(requestData)
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"))
        Request request = new Request.Builder()
                .url(backendServer + path)
                .post(body)
                .build()

        Response response;
        try {
            log.info("making postcall: ${path} ")
            response = client.newCall(request).execute()
            if (!response.isSuccessful()) {
                String code = response.code().toString()
                String _err = """Mongo Call returned error. ${backendServer}${path}
                        Request failed with status code: ${code}
                        json: ${json}    
                        """
                log.info _err

                return [success: false, result: _err]
            } else {
                String responseString = response.body().string()
                log.info "Response: ${responseString}"
                return [success: true, result: responseString]
            }
        } catch (Exception e) {
            log.info "An error occurred: ${e.message}"
            String _exception = """makePostCall Exception while calling ${backendServer}${path}
                        json:${json} 
                        ${e.message}"""
            return [success: false, result: _exception]
        } finally {
            if (response) {
                response?.close()
            }
        }
    }

    static def makeGetCall(String path) {
        OkHttpClient client = new OkHttpClient()

        Request request = new Request.Builder()
                .url("${backendServer}${path}")
                .build()

        log.info("backendServer ${backendServer} path ${path}")
        Response response;
        try {
            response = client.newCall(request).execute()
            if (response.isSuccessful()) {
                println "Response Code: ${response?.message()}"
                def data = response.body().string()
                println "Response: ${data}"
                def dataMap = ['response': data, 'code': response.code()]
                return dataMap
            } else {
                println "Request failed with status code: ${response.code()}"
                return null
            }
        } catch (Exception e) {
            println "An error occurred: ${e.message}"
            log.info("MakeGetCall Error while calling ${backendServer}${path} ${e.message}", e.message)
            return null
        } finally {
            if (response) {
                response.close()
            }
        }
    }

    static boolean startDBServerIfOff() {
        try {
            log.info("startDBServerIfOff for env:${SettingsUtil.ENV_TYPE}")
            String mongoServerExecScript = ".\\bat_files\\startMongoApiServer.bat"

            if (SettingsUtil.ENV_TYPE.equalsIgnoreCase(SettingsUtil.ENV_DEV)) {
                if (!checkIfDBServerIsOn()) {
                    log.info("startDBServerIfOff:Starting DB Server")
                }
            }
        }
        catch (Exception ex) {
            log.error("Couldnt start Mongo Server", ex.message);
        }
    }

    static boolean startDashboardServerIfOff() {
        String mongoServerExecScript = "./bat_files/startDashboardServer.bat"
        if (!checkIfDashboardServerIsOn()) {
            log.info("Starting DB Server")
            Runtime.getRuntime().exec(mongoServerExecScript)
            Thread.sleep(30000)
        }
    }

    static boolean checkIfDBServerIsOn() {
        boolean _result = false
        String dbServerSuccessMsg = "eGangotri-node-backend (egangotri_upload_db)"
        try {
            LinkedHashMap result = makeGetCall("/") ?: [:] as Map
            _result = (result && result.containsKey("response")) ? result?.response?.toString()?.length() > 20 : false
            log.info("toJson.response ${result && result.containsKey("response") ? result.response : '--'}")
        }
        catch (ConnectException e) {
            log.error(e.message, "checkIfDBServerIsOn exception thrown")
            _result = false;
        }
        log.info("checkIfDBServerIsOn ${_result}");
        return _result;
    }

    static boolean checkIfDashboardServerIsOn() {
        try {
            LinkedHashMap result = makeGetCall("/") ?: [:] as Map
            log.info("toJson.response ${result && result.containsKey("response") ? result.response : '--'}")
            return (result && result.containsKey("response")) ? result.response == "eGangotri-node-backend" : false
        }
        catch (ConnectException e) {
            log.error(e.message)
            return false;
        }
    }

    static def listQueues() {
        LinkedHashMap result = makeGetCall("/${ITEMS_QUEUED_PATH}/list") ?: [:]
        log.info("toJson.response ${result && result.containsKey("response") ? result.response : '--'}")
        return (result && result.containsKey("response")) ? result.response : []
    }

    static void main(String[] args) {
        log.info("checkIfDBServerIsOn ${checkIfDBServerIsOn()}")
        log.info("listQueues ${listQueues()}")
        log.info("End of RestUtil")
    }
}
