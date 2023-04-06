package uk.gov.hmcts.reform.prl.utils;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


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
        collapsible.add("<details class='govuk-details'>");
        collapsible.add("<summary class='govuk-details__summary'>");
        collapsible.add("<span class='govuk-details__summary-text'>");
        collapsible.add("When should I fill this in?");
        collapsible.add("</span>");
        collapsible.add("</summary>");
        collapsible.add("<div class='govuk-details__text'>");
        collapsible.add("<p><strong>Only fill the following if you haven't requested the hearing yet</strong></p></br>");
        collapsible.add("</div>");
        collapsible.add("</details>");
        return String.join("\n\n", collapsible);
    }

    public static void generatePartyUuidForC100(PartyDetails partyDetails) {

        if (partyDetails.getSolicitorPartyId() == null) {
            partyDetails.setSolicitorPartyId(generateUuid());
        }
        if (partyDetails.getSolicitorOrgUuid() == null) {
            partyDetails.setSolicitorOrgUuid(generateUuid());
        }
    }

    public static void generatePartyUuidForFL401(CaseData caseData) {
        if (caseData.getApplicantsFL401() != null) {
            if (caseData.getApplicantsFL401().getPartyId() == null) {
                caseData.getApplicantsFL401().setPartyId(generateUuid());
            }
            if (caseData.getApplicantsFL401().getSolicitorPartyId() == null
                && (caseData.getApplicantsFL401().getRepresentativeFirstName() != null
                || caseData.getApplicantsFL401().getRepresentativeLastName() != null)) {
                caseData.getApplicantsFL401().setSolicitorPartyId(generateUuid());
            }
            if (caseData.getApplicantsFL401().getSolicitorOrgUuid() == null) {
                caseData.getApplicantsFL401().setSolicitorOrgUuid(generateUuid());
            }
        }
        if (caseData.getRespondentsFL401() != null) {
            if (caseData.getRespondentsFL401().getPartyId() == null) {
                caseData.getRespondentsFL401().setPartyId(generateUuid());
            }
        }
    }

    private static UUID generateUuid() {
        return UUID.randomUUID();
    }

    public static String formatDate(String pattern, LocalDate localDate) {
        try {
            log.info("date ===> " + localDate);
            DateFormat dateFormat = new SimpleDateFormat(pattern);
            return dateFormat.format(localDate);
        } catch (Exception e) {
            log.error(ERROR_STRING + e.getMessage());
        }
        return "";
    }

}
