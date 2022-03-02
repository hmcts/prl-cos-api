package uk.gov.hmcts.reform.prl.utils;

import lombok.extern.slf4j.Slf4j;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Slf4j
public class CommonUtils {
    public static final String DATE_OF_SUBMISSION_FORMAT = "dd-MM-yyyy";
    public static final String LOG_ERROR_MSG = "Error while formatting the date from casedetails to casedata.. ";

    private CommonUtils() {

    }

    public static String getValue(Object obj) {
        return obj != null ? String.valueOf(obj) : " ";
    }

    public static String formatLocalDateTime(LocalDateTime localDateTime) {
        try {
            if (localDateTime != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_OF_SUBMISSION_FORMAT);
                return localDateTime.format(formatter);
            }
        } catch (Exception e) {
            log.error(LOG_ERROR_MSG + e.getMessage());
        }
        return " ";
    }

    public static String getIsoDateToSpecificFormat(String date, String format) {
        try {
            if (date != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                LocalDate parse = LocalDate.parse(date);
                return parse.format(formatter);
            }
        } catch (Exception e) {
            log.error(LOG_ERROR_MSG + e.getMessage());
        }
        return " ";
    }

    public static String formatCurrentDate(String pattern) {
        try {
            DateFormat dateFormat = new SimpleDateFormat(pattern);
            Date date = new Date();
            return dateFormat.format(date);
        } catch (Exception e) {
            log.error(LOG_ERROR_MSG + e.getMessage());
        }
        return "";
    }
}
