package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.CaseRole;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.ChangeOrganisationApprovalStatus;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.noticeofchange.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.prl.services.caseaccess.AssignCaseAccessClient;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService;
import uk.gov.hmcts.reform.prl.services.time.Time;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.CAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.CARESPONDENT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.DAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.DARESPONDENT;
import static uk.gov.hmcts.reform.prl.models.noticeofchange.DecisionRequest.decisionRequest;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class UpdatePartyDetailsService {
    private final ObjectMapper objectMapper;
    private final NoticeOfChangePartiesService noticeOfChangePartiesService;
    private final UserService userService;
    private final AssignCaseAccessClient assignCaseAccessClient;
    private final AuthTokenGenerator tokenGenerator;

    private final Time time;

    public Map<String, Object> updateApplicantAndChildNames(CallbackRequest callbackRequest, String authorisation) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> updatedCaseData = callbackRequest.getCaseDetails().getData();

        CaseData caseData = objectMapper.convertValue(updatedCaseData, CaseData.class);

        final Flags caseFlags = Flags.builder().build();

        updatedCaseData.put("caseFlags", caseFlags);

        if (FL401_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            updatedCaseData.putAll(noticeOfChangePartiesService.generate(caseData, DARESPONDENT));
            updatedCaseData.putAll(noticeOfChangePartiesService.generate(caseData, DAAPPLICANT));

            PartyDetails fl401Applicant = caseData
                .getApplicantsFL401();
            PartyDetails fl401respondent = caseData
                .getRespondentsFL401();

            if (Objects.nonNull(fl401Applicant)) {
                CommonUtils.generatePartyUuidForFL401(caseData);
                updatedCaseData.put("applicantName", fl401Applicant.getFirstName() + " " + fl401Applicant.getLastName());
                setFL401ApplicantFlag(updatedCaseData, fl401Applicant);

            }

            if (Objects.nonNull(fl401respondent)) {
                CommonUtils.generatePartyUuidForFL401(caseData);
                updatedCaseData.put("respondentName", fl401respondent.getFirstName() + " " + fl401respondent.getLastName());
                setFL401RespondentFlag(updatedCaseData, fl401respondent);
            }
        } else if (C100_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            updatedCaseData.putAll(noticeOfChangePartiesService.generate(caseData, CARESPONDENT));
            updatedCaseData.putAll(noticeOfChangePartiesService.generate(caseData, CAAPPLICANT));
            Optional<List<Element<PartyDetails>>> applicantsWrapped = ofNullable(caseData.getApplicants());
            if (applicantsWrapped.isPresent() && !applicantsWrapped.get().isEmpty()) {
                List<PartyDetails> applicants = applicantsWrapped.get()
                    .stream()
                    .map(Element::getValue)
                    .collect(Collectors.toList());
                PartyDetails applicant1 = applicants.get(0);
                if (Objects.nonNull(applicant1)) {
                    updatedCaseData.put("applicantName",applicant1.getFirstName() + " " + applicant1.getLastName());
                }
            }

            // set applicant and respondent case flag
            setApplicantFlag(caseData, updatedCaseData);
            setRespondentFlag(caseData, updatedCaseData);
        }
        updateLegalRepresentation(callbackRequest, authorisation, caseDetails, caseData);

        return updatedCaseData;
    }

    private void updateLegalRepresentation(CallbackRequest callbackRequest, String authorisation, CaseDetails caseDetails, CaseData caseData) {
        log.info("callbackRequest.getCaseDetailsBefore() ==> " + callbackRequest.getCaseDetailsBefore());
        if ("amendRespondentsDetails".equalsIgnoreCase(callbackRequest.getEventId())) {
            CaseData oldCaseData = objectMapper.convertValue(callbackRequest.getCaseDetailsBefore().getData(), CaseData.class);
            log.info("oldCaseData ==> " + oldCaseData);
            caseData.getRespondents().stream().forEach(partyDetailsElement -> {
                if (YesNoDontKnow.no.equals(partyDetailsElement.getValue().getDoTheyHaveLegalRepresentation())) {
                    int respondentIndex = caseData.getRespondents().indexOf(partyDetailsElement);
                    log.info("respondentIndex ==> " + respondentIndex);
                    PartyDetails oldRespondent = oldCaseData.getRespondents().get(respondentIndex).getValue();
                    UserDetails userDetails = userService.getUserDetails(authorisation);
                    DynamicListElement roleItem = DynamicListElement.builder()
                        .code(CaseRole.C100RESPONDENTSOLICITOR1.formattedName())
                        .label(CaseRole.C100RESPONDENTSOLICITOR1.formattedName())
                        .build();
                    ChangeOrganisationRequest changeOrganisationRequest = ChangeOrganisationRequest.builder()
                        .organisationToRemove(oldRespondent.getSolicitorOrg())
                        .createdBy(userDetails.getEmail())
                        .caseRoleId(DynamicList.builder()
                                        .value(roleItem)
                                        .listItems(List.of(roleItem))
                                        .build())
                        .approvalStatus(ChangeOrganisationApprovalStatus.APPROVED)
                        .requestTimestamp(time.now())
                        .build();
                    caseDetails.getData()
                        .put("changeOrganisationRequestField", changeOrganisationRequest);
                    assignCaseAccessClient.applyDecision(
                        authorisation,
                        tokenGenerator.generate(),
                        decisionRequest(caseDetails)
                    );
                }
            });
        }
    }

    private void setApplicantFlag(CaseData caseData, Map<String, Object> caseDetails) {

        Optional<List<Element<PartyDetails>>> applicantsWrapped = ofNullable(caseData.getApplicants());
        if (applicantsWrapped.isPresent() && !applicantsWrapped.get().isEmpty()) {
            List<PartyDetails> applicants = applicantsWrapped.get()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());

            for (PartyDetails applicant : applicants) {
                CommonUtils.generatePartyUuidForC100(applicant);
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
                CommonUtils.generatePartyUuidForC100(respondent);
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
