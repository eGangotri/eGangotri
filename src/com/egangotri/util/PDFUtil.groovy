package com.egangotri.util

/**
 * Created by user on 1/16/2016.
 */
class PDFUtil {

    static enum PROFILE_ENUMS {
        ib,dt,rk,jg
    }

    static final String ARCHIVE_PROFILE = PROFILE_ENUMS.rk.toString() //ib/dt/jg/rk
    static final String HOME = System.getProperty('user.home')


    public static Hashtable loadProperties(String fileName) {
        Properties properties = new Properties()
        File propertiesFile = new File(fileName)
        propertiesFile.withInputStream {
            properties.load(it)
        }

        Hashtable<String, String> metaDataMap = [:]
        for (Enumeration e = properties.keys() ; e.hasMoreElements() ;) {
            String key = (String)e.nextElement();
            String val = new String(properties.get(key).getBytes("ISO-8859-1"), "UTF-8")
            metaDataMap.put(key, val);
        }
        metaDataMap.each { k, v ->
           // println "$k $v"
        }
        return metaDataMap
    }
}
