package uk.gov.hmcts.reform.prl.utils;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.prl.enums.manageorders.SelectTypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.YES;

@Slf4j
public class EmailUtils {


    private EmailUtils() {

    }

    public static Map<String, String> getEmailProps(SelectTypeOfOrderEnum isFinalOrder,
                                                    Boolean isRespondent, PartyDetails partyDetails,
                                                    String applicantCaseName, String caseId) {
        Map<String, String> combinedMap = new HashMap<>();
        combinedMap.put("caseName", applicantCaseName);
        combinedMap.put("caseNumber", caseId);
        combinedMap.put("solicitorName", partyDetails.getRepresentativeFullName());
        combinedMap.put("subject", "Case documents for : ");
        if (isRespondent.equals(true) && StringUtils.isNotEmpty(partyDetails.getSolicitorEmail())) {
            combinedMap.put("orderURLLinkNeeded", YES);
            combinedMap.replace("subject", "New order issued: " + applicantCaseName);
        }
        if (isNotEmpty(isFinalOrder) && SelectTypeOfOrderEnum.finl.equals(isFinalOrder)) {
            combinedMap.put("finalOrder", YES);
            combinedMap.replace("subject", "Final court order issued for this case: " + applicantCaseName);
        }
        combinedMap.putAll(getCommonEmailProps());
        return combinedMap;
    }

    private static Map<String, String> getCommonEmailProps() {
        Map<String, String> emailProps = new HashMap<>();
        emailProps.put("content", "Case details");
        emailProps.put("attachmentType", "pdf");
        emailProps.put("disposition", "attachment");
        return emailProps;
    }
}
