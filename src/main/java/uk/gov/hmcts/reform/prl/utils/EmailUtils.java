package uk.gov.hmcts.reform.prl.utils;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class EmailUtils {

    private EmailUtils() {

    }

    public static Map<String, String> getEmailProps(PartyDetails partyDetails, String applicantCaseName, String caseId) {
        Map<String, String> combinedMap = new HashMap<>();
        combinedMap.put("caseName", applicantCaseName);
        combinedMap.put("caseNumber", caseId);
        combinedMap.put("solicitorName", partyDetails.getRepresentativeFullName());
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
