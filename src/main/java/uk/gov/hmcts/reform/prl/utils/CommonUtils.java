package uk.gov.hmcts.reform.prl.utils;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public class CommonUtils {
    public static final String DATE_OF_SUBMISSION_FORMAT = "dd-MM-yyyy";
    public static final String ERROR_STRING = "Error while formatting the date from casedetails to casedata.. ";

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
            log.error(ERROR_STRING + e.getMessage());
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
            log.error(ERROR_STRING + e.getMessage());
        }
        return " ";
    }

    public static String formatCurrentDate(String pattern) {
        try {
            DateFormat dateFormat = new SimpleDateFormat(pattern);
            Date date = new Date();
            return dateFormat.format(date);
        } catch (Exception e) {
            log.error(ERROR_STRING + e.getMessage());
        }
        return "";
    }

    public static String getYesOrNoValue(YesOrNo value) {
        return value != null ? value.getDisplayedValue() : null;
    }

    public static String getYesOrNoDontKnowValue(YesNoDontKnow value) {
        return value != null ? value.getDisplayedValue() : null;
    }

    public static String getSolicitorId(PartyDetails party) {
        if (party.getSolicitorOrg() != null && party.getSolicitorOrg().getOrganisationID() != null) {
            return "SOL_" + party.getSolicitorOrg().getOrganisationID();
        }
        return null;
    }


    public static String renderCollapsible() {
        final List<String> collapsible = new ArrayList<>();
        collapsible.add("<div class='width-50'>");
        collapsible.add("<details class='govuk-details'>");
        collapsible.add("<summary class='govuk-details__summary'>");
        collapsible.add("<span class='govuk-details__summary-text'>");
        collapsible.add("When should I fill this in?");
        collapsible.add("</span>");
        collapsible.add("</summary>");
        collapsible.add("<div class='govuk-details__text'>");
        collapsible.add("Only fill the following if you haven't requested the hearing yet");
        collapsible.add("</div>");
        collapsible.add("</details>");
        collapsible.add("</div>");
        return String.join("\n\n", collapsible);
    }
}
