package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class SearchCasesDataService {

    public Map<String, Object> updateApplicantAndChildNames(ObjectMapper objectMapper, Map<String, Object> caseDetails) {

        CaseData caseData = objectMapper.convertValue(caseDetails, CaseData.class);

        final Flags caseFlags = Flags.builder().build();

        caseDetails.put("caseFlags", caseFlags);

        if (FL401_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            PartyDetails fl401Applicant = caseData
                .getApplicantsFL401();
            log.info("inside updateApplicantAndChildNames === ");

            PartyDetails fl401respondent = caseData
                .getRespondentsFL401();

            if (Objects.nonNull(fl401Applicant)) {
                fl401Applicant.setPartyId(UUID.randomUUID());
                caseDetails.put("applicantName", fl401Applicant.getFirstName() + " " + fl401Applicant.getLastName());
                log.info("inside updateApplicantAndChildNames === {}",caseData);
                setFL401ApplicantFlag(caseDetails, fl401Applicant);
            }

            if (Objects.nonNull(fl401respondent)) {
                caseDetails.put("respondentName", fl401respondent.getFirstName() + " " + fl401respondent.getLastName());
                setFL401RespondentFlag(caseDetails,fl401respondent);
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
                    caseDetails.put("applicantName", applicant1.getFirstName() + " " + applicant1.getLastName());
                }

            }

            // set applicant and respondent case flag
            setApplicantFlag(caseData, caseDetails);
            setRespondentFlag(caseData, caseDetails);
        }

        return caseDetails;
    }

    private void setApplicantFlag(CaseData caseData, Map<String, Object> caseDetails) {

        Optional<List<Element<PartyDetails>>> applicantsWrapped = ofNullable(caseData.getApplicants());
        if (applicantsWrapped.isPresent() && !applicantsWrapped.get().isEmpty()) {
            List<PartyDetails> applicants = applicantsWrapped.get()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());

            for (PartyDetails applicant : applicants) {
                final String partyName = applicant.getFirstName() + " " + applicant.getLastName();
                final Flags applicantFlag = Flags.builder().partyName(partyName)
                    .roleOnCase(PartyEnum.applicant.getDisplayedValue()).details(Collections.emptyList()).build();
                applicant.setPartyLevelFlag(applicantFlag);
            }

            caseDetails.put("applicants", applicantsWrapped);
        }
    }

    private void setRespondentFlag(CaseData caseData, Map<String, Object> caseDetails) {
        Optional<List<Element<PartyDetails>>> respondentsWrapped = ofNullable(caseData.getRespondents());
        if (respondentsWrapped.isPresent() && !respondentsWrapped.get().isEmpty()) {
            List<PartyDetails> respondents = respondentsWrapped.get()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());

            for (PartyDetails respondent : respondents) {
                final String partyName = respondent.getFirstName() + " " + respondent.getLastName();
                final Flags respondentFlag = Flags.builder().partyName(partyName)
                    .roleOnCase(PartyEnum.respondent.getDisplayedValue()).details(Collections.emptyList()).build();
                respondent.setPartyLevelFlag(respondentFlag);
            }
            caseDetails.put("respondents", respondentsWrapped);
        }
    }

    private void setFL401ApplicantFlag(Map<String, Object> caseDetails, PartyDetails fl401Applicant) {
        String partyName = fl401Applicant.getFirstName() + " " + fl401Applicant.getLastName();
        final Flags applicantFlag = Flags.builder().partyName(partyName)
            .roleOnCase(PartyEnum.applicant.getDisplayedValue()).details(Collections.emptyList()).build();
        fl401Applicant.setPartyLevelFlag(applicantFlag);

        caseDetails.put("applicantsFL401", fl401Applicant);
    }

    private void setFL401RespondentFlag(Map<String, Object> caseDetails, PartyDetails fl401respondent) {
        String partyName = fl401respondent.getFirstName() + " " + fl401respondent.getLastName();
        final Flags respondentFlag = Flags.builder().partyName(partyName)
            .roleOnCase(PartyEnum.respondent.getDisplayedValue()).details(Collections.emptyList()).build();
        fl401respondent.setPartyLevelFlag(respondentFlag);

        caseDetails.put("respondentsFL401", fl401respondent);
    }

}
