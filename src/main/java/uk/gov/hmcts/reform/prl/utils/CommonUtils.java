package uk.gov.hmcts.reform.prl.utils;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class CommonUtils {
    public static String getValue(Object obj) {
        return obj != null ? String.valueOf(obj) : " ";
    }

    public static String formatLocalDateTime(LocalDateTime localDateTime) {
        try {
            if (localDateTime != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                String format = localDateTime.format(formatter);
                return format;
            }
        } catch (Exception e) {
            log.error("Error while formatting the date from casedetails to casedata.. " + e.getMessage());
        }
        return " ";
    }
}
