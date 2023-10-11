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
        if (isRespondent.equals(true) && StringUtils.isNotEmpty(partyDetails.getSolicitorEmail())) {
            combinedMap.put("orderURLLinkNeeded", YES);
            combinedMap.put("orderSubject", "New order issued: ");
        }
        if (isNotEmpty(isFinalOrder) && SelectTypeOfOrderEnum.finl
                .getDisplayedValue().equals(isFinalOrder.getDisplayedValue())) {
            combinedMap.put("finalOrder", YES);
            combinedMap.put("orderSubject", "Final court order issued for this case: ");
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
