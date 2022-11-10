package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseflags.CaseFlag;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANT_FLAG;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RESPONDENT_FLAG;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class SearchCasesDataService {

    public Map<String, Object> updateApplicantAndChildNames(ObjectMapper objectMapper, Map<String, Object> caseDetails) {

        CaseData caseData = objectMapper.convertValue(caseDetails, CaseData.class);

        if (FL401_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            PartyDetails fl401Applicant = caseData
                .getApplicantsFL401();
            PartyDetails fl401respondent = caseData
                .getRespondentsFL401();

            if (Objects.nonNull(fl401Applicant)) {
                caseDetails.put("applicantName", fl401Applicant.getFirstName() + " " + fl401Applicant.getLastName());
            }

            if (Objects.nonNull(fl401respondent)) {
                caseDetails.put("respondentName", fl401respondent.getFirstName() + " " + fl401respondent.getLastName());
            }
        } else {
            Optional<List<Element<PartyDetails>>> applicantsWrapped = ofNullable(caseData.getApplicants());
            if (!applicantsWrapped.isEmpty() && !applicantsWrapped.get().isEmpty()) {
                List<PartyDetails> applicants = applicantsWrapped.get()
                    .stream()
                    .map(Element::getValue)
                    .collect(Collectors.toList());
                PartyDetails applicant1 = applicants.get(0);
                if (Objects.nonNull(applicant1)) {
                    caseDetails.put("applicantName",applicant1.getFirstName() + " " + applicant1.getLastName());
                    final String partyName = applicant1.getFirstName() + " " + applicant1.getLastName();
                    final CaseFlag applicantFlag = CaseFlag.builder().partyName(partyName)
                            .roleOnCase(PartyEnum.applicant.getDisplayedValue()).build();
                    caseDetails.put(APPLICANT_FLAG, applicantFlag);
                }

            }
            // set respondent case flag
            setRespondentFlag(caseData, caseDetails);

        }

        return caseDetails;


    }

    private void setRespondentFlag(CaseData caseData, Map<String, Object> caseDetails) {
        Optional<List<Element<PartyDetails>>> respondentsWrapped = ofNullable(caseData.getRespondents());
        if (respondentsWrapped.isPresent() && !respondentsWrapped.get().isEmpty()) {
            List<PartyDetails> respondents = respondentsWrapped.get()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());
            PartyDetails respondent = respondents.get(0);
            if (Objects.nonNull(respondent)) {
                final String partyName = respondent.getFirstName() + " " + respondent.getLastName();
                final CaseFlag respondentFlag = CaseFlag.builder().partyName(partyName).roleOnCase(PartyEnum.respondent.getDisplayedValue()).build();
                caseDetails.put(RESPONDENT_FLAG, respondentFlag);
            }

        }
    }

}
