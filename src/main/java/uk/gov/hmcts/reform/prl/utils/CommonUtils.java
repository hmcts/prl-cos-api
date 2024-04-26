package uk.gov.hmcts.reform.prl.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.lang.Nullable;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
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
import java.util.Locale;
import java.util.UUID;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AM_LOWER_CASE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AM_UPPER_CASE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PM_LOWER_CASE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PM_UPPER_CASE;


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
            log.error(ERROR_STRING, e);
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
            log.error(ERROR_STRING, e);
        }
        return " ";
    }

    public static String formatCurrentDate(String pattern) {
        try {
            DateFormat dateFormat = new SimpleDateFormat(pattern);
            Date date = new Date();
            return dateFormat.format(date);
        } catch (Exception e) {
            log.error(ERROR_STRING, e);
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
            if (caseData.getRespondentsFL401().getSolicitorPartyId() == null
                && (caseData.getRespondentsFL401().getRepresentativeFirstName() != null
                || caseData.getRespondentsFL401().getRepresentativeLastName() != null)) {
                caseData.getRespondentsFL401().setSolicitorPartyId(generateUuid());
            }
            if (caseData.getRespondentsFL401().getSolicitorOrgUuid() == null) {
                caseData.getRespondentsFL401().setSolicitorOrgUuid(generateUuid());
            }
        }
    }

    private static UUID generateUuid() {
        return UUID.randomUUID();
    }

    public static String formatDate(String pattern, LocalDate localDate) {
        try {
            if (localDate != null) {
                return localDate.format(DateTimeFormatter.ofPattern(pattern,  Locale.ENGLISH));
            }
        } catch (Exception e) {
            log.error(ERROR_STRING, e);
        }
        return "";
    }

    public static DynamicList getDynamicList(List<DynamicListElement> listItems) {
        return DynamicList.builder()
            .value(DynamicListElement.EMPTY)
            .listItems(listItems).build();
    }

    public static DynamicMultiSelectList getDynamicMultiselectList(List<DynamicMultiselectListElement> listItems) {
        return DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiselectListElement.EMPTY))
            .listItems(listItems).build();
    }

    public static String[] getPersonalCode(Object judgeDetails) {
        String[] personalCodes = new String[3];
        try {
            personalCodes[0] = new ObjectMapper().readValue(new ObjectMapper()
                                                                .writeValueAsString(judgeDetails), JudicialUser.class).getPersonalCode();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return personalCodes;
    }

    public static String[] getIdamId(Object judgeDetails) {
        String[] idamIds = new String[3];
        try {
            idamIds[0] = new ObjectMapper().readValue(
                new ObjectMapper()
                    .writeValueAsString(judgeDetails),
                JudicialUser.class
            ).getIdamId();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return idamIds;
    }

    public static LocalDate formattedLocalDate(String date, String pattern) {
        if (date != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            return LocalDate.parse(date, formatter);
        }
        return null;
    }

    public static String formatDateTime(String pattern, LocalDateTime localDateTime) {
        try {
            if (null != localDateTime) {
                return localDateTime.format(DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH))
                    .replace(AM_LOWER_CASE, AM_UPPER_CASE).replace(PM_LOWER_CASE, PM_UPPER_CASE);
            }
        } catch (Exception e) {
            log.error(ERROR_STRING + "in formatDateTime Method", e);
        }
        return "";
    }

    public static boolean isEmpty(@Nullable String string) {
        return string == null || string.isEmpty();
    }

    public static boolean isNotEmpty(@Nullable String string) {
        return !isEmpty(string);
    }
}
