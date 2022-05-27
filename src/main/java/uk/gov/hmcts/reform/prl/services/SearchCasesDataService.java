package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class SearchCasesDataService {

    public Map<String, Object> updateApplicantAndChildNames(ObjectMapper objectMapper, Map<String, Object> caseDetails) {

        CaseData caseData = objectMapper.convertValue(caseDetails, CaseData.class);

        if (FL401_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            PartyDetails fl401Applicant = caseData
                .getApplicantsFL401();

            if (Objects.nonNull(fl401Applicant)) {
                log.info("adding applicant name in casedata for FL401");
                caseDetails.put("applicantName", fl401Applicant.getFirstName() + " " + fl401Applicant.getLastName());
            }
        } else {
            Optional<List<Element<PartyDetails>>> applicantsWrapped = ofNullable(caseData.getApplicants());
            if (!applicantsWrapped.isEmpty() && !applicantsWrapped.get().isEmpty()) {
                List<PartyDetails> applicants = applicantsWrapped.get()
                    .stream()
                    .map(Element::getValue)
                    .collect(Collectors.toList());
                PartyDetails applicant1 = applicants.get(0);
                log.info("adding applicant name in casedata for C100");
                if (Objects.nonNull(applicant1)) {
                    caseDetails.put("applicantName",applicant1.getFirstName() + " " + applicant1.getLastName());
                }

            }
            Optional<List<Element<Child>>> childrenWrapped = ofNullable(caseData.getChildren());
            if (!childrenWrapped.isEmpty() && !childrenWrapped.get().isEmpty()) {
                List<Child> children = childrenWrapped.get().stream().map(Element::getValue).collect(Collectors.toList());
                Child child = children.get(0);
                log.info("adding child name for  for C100");
                if (Objects.nonNull(child)) {
                    caseDetails.put("childName", child.getFirstName() + " " + child.getLastName());
                }

            }
        }

        return caseDetails;


    }

}
