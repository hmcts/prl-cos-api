package uk.gov.hmcts.reform.prl.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ChildDetailsService {


    public Map<String, Object> getApplicantDetails(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();
        data.put("selectedApplicantName", getApplicantsAsDynamicList(caseData));
        return data;
    }

    public DynamicList getApplicantsAsDynamicList(CaseData caseData) {
        List<Element<PartyDetails>> parties = caseData.getApplicants();

        return ElementUtils.asDynamicList(
            parties,
            null,
            PartyDetails::getFirstName
        );
    }
}
