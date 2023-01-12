package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.RESPONDENT;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class UpdatePartyDetailsService {
    private final ObjectMapper objectMapper;
    private final NoticeOfChangePartiesService noticeOfChangePartiesService;

    public Map<String, Object> updateApplicantAndChildNames(CallbackRequest callbackRequest) {
        Map<String, Object> updatedCaseData = callbackRequest.getCaseDetails().getData();

        CaseData caseData = objectMapper.convertValue(updatedCaseData, CaseData.class);

        if (FL401_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            PartyDetails fl401Applicant = caseData
                .getApplicantsFL401();
            PartyDetails fl401respondent = caseData
                .getRespondentsFL401();

            if (Objects.nonNull(fl401Applicant)) {
                updatedCaseData.put("applicantName", fl401Applicant.getFirstName() + " " + fl401Applicant.getLastName());
            }

            if (Objects.nonNull(fl401respondent)) {
                updatedCaseData.put("respondentName", fl401respondent.getFirstName() + " " + fl401respondent.getLastName());
            }
        } else if (C100_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            updatedCaseData.putAll(noticeOfChangePartiesService.generate(caseData, RESPONDENT));
            Optional<List<Element<PartyDetails>>> applicantsWrapped = ofNullable(caseData.getApplicants());
            if (!applicantsWrapped.isEmpty() && !applicantsWrapped.get().isEmpty()) {
                List<PartyDetails> applicants = applicantsWrapped.get()
                    .stream()
                    .map(Element::getValue)
                    .collect(Collectors.toList());
                PartyDetails applicant1 = applicants.get(0);
                if (Objects.nonNull(applicant1)) {
                    updatedCaseData.put("applicantName",applicant1.getFirstName() + " " + applicant1.getLastName());
                }
            }
        }

        return updatedCaseData;
    }
}
