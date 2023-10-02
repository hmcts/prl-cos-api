package uk.gov.hmcts.reform.prl.utils;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.YES;

@Slf4j
public class EmailUtils {


    private EmailUtils() {

    }

    public static Map<String, String> getEmailProps(Boolean isRespondent, PartyDetails partyDetails, String applicantCaseName, String caseId) {
        Map<String, String> combinedMap = new HashMap<>();
        combinedMap.put("caseName", applicantCaseName);
        combinedMap.put("caseNumber", caseId);
        combinedMap.put("solicitorName", partyDetails.getRepresentativeFullName());
        if (isRespondent.equals(true) && StringUtils.isNotEmpty(partyDetails.getSolicitorEmail())) {
            combinedMap.put("orderURLLinkNeeded", YES);
        }
        combinedMap.putAll(getCommonEmailProps());
        return combinedMap;
    }

    private static Map<String, String> getCommonEmailProps() {
        Map<String, String> emailProps = new HashMap<>();
        emailProps.put("subject", "Case documents for : ");
        emailProps.put("content", "Case details");
        emailProps.put("attachmentType", "pdf");
        emailProps.put("disposition", "attachment");
        return emailProps;
    }
}
