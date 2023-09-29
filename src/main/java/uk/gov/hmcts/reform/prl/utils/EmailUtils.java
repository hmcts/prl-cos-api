package uk.gov.hmcts.reform.prl.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.URL_STRING;

@Slf4j
public class EmailUtils {

    @Value("${xui.url}")
    private static String manageCaseUrl;

    private EmailUtils() {

    }

    public static Map<String, String> getEmailProps(YesOrNo respondentSolicitorServingOrder,
                                                    PartyDetails partyDetails, String applicantCaseName,
                                                    String caseId) {
        Map<String, String> combinedMap = new HashMap<>();
        combinedMap.put("caseName", applicantCaseName);
        combinedMap.put("caseNumber", caseId);
        combinedMap.put("solicitorName", partyDetails.getRepresentativeFullName());
        if (respondentSolicitorServingOrder.equals(YesOrNo.Yes)) {
            combinedMap.put("orderUrLLink", manageCaseUrl + URL_STRING + caseId + "#Orders");
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
