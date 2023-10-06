package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.caseflags.FlagsVisibiltyEnum;
import uk.gov.hmcts.reform.prl.mapper.citizen.confidentialdetails.ConfidentialDetailsMapper;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseaccess.OrganisationPolicy;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

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

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class UpdatePartyDetailsService {

    public static final String RESPONDENT_CONFIDENTIAL_DETAILS = "respondentConfidentialDetails";
    private final ObjectMapper objectMapper;
    private final NoticeOfChangePartiesService noticeOfChangePartiesService;
    private final ConfidentialDetailsMapper confidentialDetailsMapper;

    @Qualifier("caseSummaryTab")
    private final CaseSummaryTabService caseSummaryTabService;

    public Map<String, Object> updateApplicantRespondentAndChildData(CallbackRequest callbackRequest) {
        Map<String, Object> updatedCaseData = callbackRequest.getCaseDetails().getData();

        CaseData caseData = objectMapper.convertValue(updatedCaseData, CaseData.class);

        CaseData caseDataTemp = confidentialDetailsMapper.mapConfidentialData(caseData, false);
        updatedCaseData.put(RESPONDENT_CONFIDENTIAL_DETAILS, caseDataTemp.getRespondentConfidentialDetails());

        updatedCaseData.putAll(caseSummaryTabService.updateTab(caseData));

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
                updatedCaseData.put(
                    "applicantName",
                    fl401Applicant.getFirstName() + " " + fl401Applicant.getLastName()
                );
                setFL401ApplicantFlag(updatedCaseData, fl401Applicant);
            }

            if (Objects.nonNull(fl401respondent)) {
                CommonUtils.generatePartyUuidForFL401(caseData);
                updatedCaseData.put(
                    "respondentName",
                    fl401respondent.getFirstName() + " " + fl401respondent.getLastName()
                );
                setFL401RespondentFlag(updatedCaseData, fl401respondent);
            }
            setApplicantOrganisationPolicyIfOrgEmpty(updatedCaseData, caseData.getApplicantsFL401());
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
                    updatedCaseData.put("applicantName", applicant1.getFirstName() + " " + applicant1.getLastName());
                }
            }
            // set applicant and respondent case flag
            setApplicantFlag(caseData, updatedCaseData);
            setRespondentFlag(caseData, updatedCaseData);
            log.info("*** Updating flags for other parties.");
            setOtherPeopleInTheCaseFlag(caseData, updatedCaseData);
            log.info("*** Updating flags for other parties done.");
            Optional<List<Element<PartyDetails>>> applicantList = ofNullable(caseData.getApplicants());
            if (applicantList.isPresent()) {
                setApplicantOrganisationPolicyIfOrgEmpty(
                    updatedCaseData,
                    ElementUtils.unwrapElements(applicantList.get()).get(0)
                );
            }
        }

        return updatedCaseData;
    }

    private void setApplicantOrganisationPolicyIfOrgEmpty(Map<String, Object> updatedCaseData, PartyDetails partyDetails) {
        CaseData caseDataUpdated = objectMapper.convertValue(updatedCaseData, CaseData.class);
        OrganisationPolicy applicantOrganisationPolicy = caseDataUpdated.getApplicantOrganisationPolicy();
        boolean organisationNotExists = false;
        boolean roleNotExists = false;
        log.info("Organisation policy before  override : {}", applicantOrganisationPolicy);
        if (ObjectUtils.isEmpty(applicantOrganisationPolicy)) {
            applicantOrganisationPolicy = OrganisationPolicy.builder().orgPolicyCaseAssignedRole("[APPLICANTSOLICITOR]").build();
            organisationNotExists = true;
        } else if (ObjectUtils.isNotEmpty(applicantOrganisationPolicy) && ObjectUtils.isEmpty(
            applicantOrganisationPolicy.getOrganisation())) {
            if (StringUtils.isEmpty(applicantOrganisationPolicy.getOrgPolicyCaseAssignedRole())) {
                roleNotExists = true;
            }
            organisationNotExists = true;
        } else if (ObjectUtils.isNotEmpty(applicantOrganisationPolicy) && ObjectUtils.isNotEmpty(
            applicantOrganisationPolicy.getOrganisation()) && StringUtils.isEmpty(
            applicantOrganisationPolicy.getOrganisation().getOrganisationID())) {
            if (StringUtils.isEmpty(applicantOrganisationPolicy.getOrgPolicyCaseAssignedRole())) {
                roleNotExists = true;
            }
            organisationNotExists = true;
        }
        if (organisationNotExists) {
            applicantOrganisationPolicy.setOrganisation(partyDetails.getSolicitorOrg());
        }
        if (roleNotExists) {
            applicantOrganisationPolicy.setOrgPolicyCaseAssignedRole("[APPLICANTSOLICITOR]");
        }
        log.info("Organisation policy after  override : {}", applicantOrganisationPolicy);
        updatedCaseData.put("applicantOrganisationPolicy", applicantOrganisationPolicy);
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
                // Generating groupId
                String applicantGroupId = String.format(
                    "%s%s",
                    PartyEnum.applicant.getDisplayedValue(),
                    applicants.indexOf(applicant)
                );
                // Internal flags for applicant with same groupId as external
                applicant.setPartyLevelFlag(generateFlags(
                    applicant.getPartyFullName(),
                    PartyEnum.applicant.getDisplayedValue(),
                    applicantGroupId,
                    FlagsVisibiltyEnum.INTERNAL.getLabel()
                ));
                // External flags for applicant with same groupId as internal
                applicant.setPartyExternalFlags(generateFlags(
                    applicant.getPartyFullName(),
                    PartyEnum.applicant.getDisplayedValue(),
                    applicantGroupId,
                    FlagsVisibiltyEnum.EXTERNAL.getLabel()
                ));

                if (!StringUtils.isEmpty(applicant.getRepresentativeFullNameForCaseFlags())) {
                    String applicantSolicitorGroupId = String.format(
                        "%s%s",
                        PartyEnum.applicant_solicitor.getDisplayedValue(),
                        applicants.indexOf(applicant)
                    );
                    // Internal flags for applicant solicitor with same groupId as external flag
                    applicant.setPartySolicitorInternalFlag(generateFlags(
                        applicant.getRepresentativeFullNameForCaseFlags(),
                        PartyEnum.applicant_solicitor.getDisplayedValue(),
                        applicantSolicitorGroupId,
                        FlagsVisibiltyEnum.INTERNAL.getLabel()
                    ));
                    // External flags for applicant solicitor with same groupId as internal flag
                    applicant.setPartySolicitorExternalFlags(generateFlags(
                        applicant.getRepresentativeFullNameForCaseFlags(),
                        PartyEnum.applicant_solicitor.getDisplayedValue(),
                        applicantSolicitorGroupId,
                        FlagsVisibiltyEnum.EXTERNAL.getLabel()
                    ));
                }
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

                // Generating groupId
                String respondentGroupId = String.format(
                    "%s%s",
                    PartyEnum.respondent.getDisplayedValue(),
                    respondents.indexOf(respondent)
                );
                // Internal flags for respondent with same groupId as external
                respondent.setPartyLevelFlag(generateFlags(
                    respondent.getPartyFullName(),
                    PartyEnum.respondent.getDisplayedValue(),
                    respondentGroupId,
                    FlagsVisibiltyEnum.INTERNAL.getLabel()
                ));
                // External flags for respondent with same groupId as internal
                respondent.setPartyExternalFlags(generateFlags(
                    respondent.getPartyFullName(),
                    PartyEnum.respondent.getDisplayedValue(),
                    respondentGroupId,
                    FlagsVisibiltyEnum.EXTERNAL.getLabel()
                ));

                if (!StringUtils.isEmpty(respondent.getRepresentativeFullNameForCaseFlags())) {
                    String respondentSolicitorGroupId = String.format(
                        "%s%s",
                        PartyEnum.respondent_solicitor.getDisplayedValue(),
                        respondents.indexOf(respondent)
                    );
                    // Internal flags for respondent solicitor with same groupId as external flag
                    respondent.setPartySolicitorInternalFlag(generateFlags(
                        respondent.getRepresentativeFullNameForCaseFlags(),
                        PartyEnum.respondent_solicitor.getDisplayedValue(),
                        respondentSolicitorGroupId,
                        FlagsVisibiltyEnum.INTERNAL.getLabel()
                    ));
                    // External flags for respondent solicitor with same groupId as internal flag
                    respondent.setPartySolicitorExternalFlags(generateFlags(
                        respondent.getRepresentativeFullNameForCaseFlags(),
                        PartyEnum.respondent_solicitor.getDisplayedValue(),
                        respondentSolicitorGroupId,
                        FlagsVisibiltyEnum.EXTERNAL.getLabel()
                    ));
                }
            }

            caseDetails.put("respondents", respondentsWrapped);
        }
    }

    private void setOtherPeopleInTheCaseFlag(CaseData caseData, Map<String, Object> caseDetails) {
        Optional<List<Element<PartyDetails>>> otherPartyInTheCaseRevised = ofNullable(caseData.getOtherPartyInTheCaseRevised());
        if (otherPartyInTheCaseRevised.isPresent() && !otherPartyInTheCaseRevised.get().isEmpty()) {
            List<PartyDetails> otherParties = otherPartyInTheCaseRevised.get()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());
            for (PartyDetails otherParty : otherParties) {
                if (!StringUtils.isEmpty(otherParty.getPartyFullName())) {
                    // Generating groupId
                    String otherPartyGroupId = String.format(
                        "%s%s",
                        PartyEnum.other.getDisplayedValue(),
                        otherParties.indexOf(otherParty)
                    );
                    // Internal flags for otherParty with same groupId as external
                    otherParty.setPartyLevelFlag(generateFlags(
                        otherParty.getPartyFullName(),
                        PartyEnum.other.getDisplayedValue(),
                        otherPartyGroupId,
                        FlagsVisibiltyEnum.INTERNAL.getLabel()
                    ));
                    // External flags for otherParty with same groupId as internal
                    otherParty.setPartyExternalFlags(generateFlags(
                        otherParty.getPartyFullName(),
                        PartyEnum.other.getDisplayedValue(),
                        otherPartyGroupId,
                        FlagsVisibiltyEnum.EXTERNAL.getLabel()
                    ));
                }
            }

            caseDetails.put("otherPartyInTheCaseRevised", otherPartyInTheCaseRevised);
        }
    }

    private void setFL401ApplicantFlag(Map<String, Object> caseDetails, PartyDetails fl401Applicant) {
        final Flags applicantFlag = Flags.builder().partyName(fl401Applicant.getPartyFullName())
            .roleOnCase(PartyEnum.applicant.getDisplayedValue()).details(Collections.emptyList()).build();
        fl401Applicant.setPartyLevelFlag(applicantFlag);
        fl401Applicant.setPartyExternalFlags(applicantFlag);

        if (!StringUtils.isEmpty(fl401Applicant.getRepresentativeFullNameForCaseFlags())) {
            final Flags applicantSolicitorFlag = Flags.builder().partyName(fl401Applicant.getRepresentativeFullNameForCaseFlags())
                .roleOnCase(PartyEnum.applicant_solicitor.getDisplayedValue()).details(Collections.emptyList()).build();
            fl401Applicant.setPartySolicitorInternalFlag(applicantSolicitorFlag);
            fl401Applicant.setPartySolicitorExternalFlags(applicantSolicitorFlag);
        }

        caseDetails.put("applicantsFL401", fl401Applicant);
    }

    private void setFL401RespondentFlag(Map<String, Object> caseDetails, PartyDetails fl401respondent) {
        final Flags respondentFlag = Flags.builder().partyName(fl401respondent.getPartyFullName())
            .roleOnCase(PartyEnum.respondent.getDisplayedValue()).details(Collections.emptyList()).build();
        fl401respondent.setPartyLevelFlag(respondentFlag);
        fl401respondent.setPartyExternalFlags(respondentFlag);

        if (!StringUtils.isEmpty(fl401respondent.getRepresentativeFullNameForCaseFlags())) {
            final Flags respondentSolicitorFlag = Flags.builder().partyName(fl401respondent.getRepresentativeFullNameForCaseFlags())
                .roleOnCase(PartyEnum.respondent_solicitor.getDisplayedValue()).details(Collections.emptyList()).build();
            fl401respondent.setPartySolicitorInternalFlag(respondentSolicitorFlag);
            fl401respondent.setPartySolicitorExternalFlags(respondentSolicitorFlag);
        }

        caseDetails.put("respondentsFL401", fl401respondent);
    }

    private static Flags generateFlags(String partyFullName, String roleOnCase, String groupId, String visibility) {
        return Flags.builder().partyName(partyFullName)
            .roleOnCase(roleOnCase)
            .groupId(groupId)
            .visibility(visibility)
            .details(Collections.emptyList())
            .build();
    }
}
