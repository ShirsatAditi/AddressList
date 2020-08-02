package com.framwork.useraddress.lib.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Aditi Shirsat on 24 Sep 2019 at 12:47.
 */
object DateUtil {
    fun getDate(actualDate: String): Int {
        try {
            return SimpleDateFormat("E dd-MM-yyyy").parse(actualDate).date
        } catch (e: ParseException) {
            e.printStackTrace()
            return 0
        }

    }

    fun getInFormetNeeded(actualDate: String): String {
        return try {
            val completeDate = SimpleDateFormat("E").parse(actualDate)
            (completeDate.year + 1900).toString() + "-" + (completeDate.month + 1) + "-" + completeDate.date
        } catch (e: ParseException) {
            e.printStackTrace()
            "0"
        }

    }

    fun getDateForBookingApppointment(actualDate: String): String {
        try {
            val completeDate = SimpleDateFormat("E dd-MM-yyyy").parse(actualDate)
            return (completeDate.year + 1900).toString() + "-" + (completeDate.month + 1) + "-" + completeDate.date
        } catch (e: ParseException) {
            e.printStackTrace()
            return "0"
        }

    }

    fun getDay(actualDate: String): Int {
        try {
            return SimpleDateFormat("E dd-MM-yyyy").parse(actualDate).date
        } catch (e: ParseException) {
            e.printStackTrace()
            return 0
        }

    }

    fun getFormnattedDateTime(dateTime: String): String {
        val simpledateformat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val df = SimpleDateFormat("ddMMMyy hh:mm a")
        var stringDate: Date? = null
        try {

            stringDate = simpledateformat.parse(dateTime)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return df.format(stringDate)
    }


    fun getFormattedDate(date: String?): String {
        if (date != null && date != "") {
            val simpledateformat = SimpleDateFormat("yyyy-MM-dd")
            val df = SimpleDateFormat("dd MMM yyyy")
            var stringDate: Date? = null
            try {

                stringDate = simpledateformat.parse(date)
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            return df.format(stringDate)
        }
        return ""
    }

    fun getFormattedTime(time: String?): String {
        if (time != null && time != "") {
            val simpledateformat = SimpleDateFormat("HH:mm:ss")
            val df = SimpleDateFormat("hh:mm a")
            var stringTime: Date? = null
            try {

                stringTime = simpledateformat.parse(time)
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            return df.format(stringTime)
        }
        return ""
    }

    fun getTimeInCalendarFormat(time: String, is24Hours: Boolean): Calendar {
        /**
         * It require time in 12:00 PM format
         */
        if (is24Hours) {
            var datee: Date? = null
            val simpledateformat = SimpleDateFormat("hh:mm a")
            val df = SimpleDateFormat("HH:mm:ss")
            try {
                datee = simpledateformat.parse(time)
                df.format(datee)
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            val cal = Calendar.getInstance()
            cal.time = datee
            return cal
        } else {
            var datee: Date? = null
            val simpledateformat = SimpleDateFormat("hh:mm a")
            try {
                datee = simpledateformat.parse(time)
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            val cal = Calendar.getInstance()
            cal.time = datee
            return cal
        }
    }

    fun getTimeFromCalendar(calendar: Calendar, is24Hour: Boolean): String {
        val date = ""
        if (is24Hour) {
            val dateFormat = SimpleDateFormat("HH:mm:ss")
            return dateFormat.format(calendar.time)
        } else {
            val dateFormat = SimpleDateFormat("hh:mm a")
            return dateFormat.format(calendar.time)

        }
        /*String time ="";
        if(calendar != null){
            time =  calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE) + " "
                    + (calendar.get(Calendar.AM_PM)==0?"AM":"PM");
        }
        return time;*/
    }

    fun getCurrentDateTimeFrom(): String {
        val calendar = Calendar.getInstance()
        val date = ""
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }


    fun getDateFromCalendar(calendar: Calendar?): String {
        var date = ""
        //        DateFormat dateFormat = new SimpleDateFormat("HH:mm a");
        //        return dateFormat.format(calendar);
        if (calendar != null) {
            date = (calendar.get(Calendar.YEAR).toString() + "-" + getEntityAppendWithZero(
                calendar.get(Calendar.MONTH) + 1
            ) + "-"
                    + getEntityAppendWithZero(calendar.get(Calendar.DAY_OF_MONTH)))
        }
        return date
    }

    /**
     * @param unixTime time in millis
     * function to convert the time into normal format
     *
     */
    fun getDateFromUnixTime(unixTime: Long): String{
        val mCalendar = Calendar.getInstance()
        mCalendar.timeInMillis = unixTime * 1000
        return getFormattedDate(mCalendar.get(Calendar.YEAR).toString()+"-"
                +(mCalendar.get(Calendar.MONTH)+1).toString()+"-"+mCalendar.get(Calendar.DAY_OF_MONTH).toString())
    }



    fun getTimeFromUnixTime(unixTime: Long): String{
        val mCalendar = Calendar.getInstance()
        mCalendar.timeInMillis = unixTime
        return getTimeFromCalendar(mCalendar,false)
    }


    /*val date = Date(mCalendar.get(Calendar.YEAR),mCalendar.get(Calendar.MONTH),mCalendar.get(Calendar.DAY_OF_MONTH))
        val sdf = SimpleDateFormat("dd MMM yyyy")
        //sdf.timeZone = java.util.TimeZone.getTimeZone("GMT+5:30")
        return sdf.format(date)*/

    private fun getEntityAppendWithZero(i: Int): String {
        return if (i < 10)
            "0$i"
        else
            i.toString() + ""
    }
}