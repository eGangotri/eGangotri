package com.egangotri.util

class TimeUtil {
    static String formatTime(long timeInMs) {
        int timeInSecs = timeInMs / 1000
        int timeInMins = timeInMs / 60
        int timeInHours = timeInMs / 60
        if (timeInSecs < 60) {
            return "${timeInSecs} sec(s)"
        } else if (timeInMins < 60) {
            return "${timeInMins} min(s)"
        }
        return "${timeInHours} min(s)"
    }

    static String formattedTimeDff(Date endTime, Date startTime) {
        return formatTime(endTime.time - startTime.time);
    }
}
