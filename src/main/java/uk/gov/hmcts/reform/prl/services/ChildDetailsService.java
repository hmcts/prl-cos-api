package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.launchdarkly.shaded.com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantRelatedToChild;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ChildDetailsService {

    @Autowired
    private ObjectMapper objectMapper;


    public Map<String, Object> getApplicantDetails(CaseData caseData) {
        log.info("child data : {}",new Gson().toJson(getApplicantsAsDynamicList(caseData)));
        return  objectMapper.convertValue(ApplicantRelatedToChild.builder()
                .selectedApplicantName(getApplicantsAsDynamicList(caseData)).build(), Map.class);
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
