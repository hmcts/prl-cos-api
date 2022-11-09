package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantRelatedToChild;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ChildDetailsService {

    @Autowired
    private ObjectMapper objectMapper;


    public Map<String, Object> getApplicantDetails(CaseData caseData) {
        List<ApplicantRelatedToChild> lists = new ArrayList<>();
        lists.add(ApplicantRelatedToChild.builder().selectedApplicantName(getApplicantsAsDynamicList(caseData)).build());
        Child child = Child.builder()
                        .applicantsRelatedToChild(lists).build();
        return  objectMapper.convertValue(child, Map.class);
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
