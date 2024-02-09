package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.caseflags.PartyRole;
import uk.gov.hmcts.reform.prl.mapper.citizen.confidentialdetails.ConfidentialDetailsMapper;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseaccess.OrganisationPolicy;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.caseflags.PartyLevelCaseFlagsService;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_APPLICANTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_RESPONDENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RESPONDENTS;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.CAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.CARESPONDENT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.DAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.DARESPONDENT;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class UpdatePartyDetailsService {

    public static final String RESPONDENT_CONFIDENTIAL_DETAILS = "respondentConfidentialDetails";
    private final ObjectMapper objectMapper;
    private final NoticeOfChangePartiesService noticeOfChangePartiesService;
    private final PartyLevelCaseFlagsService partyLevelCaseFlagsService;
    private final ConfidentialDetailsMapper confidentialDetailsMapper;

    @Qualifier("caseSummaryTab")
    private final  CaseSummaryTabService caseSummaryTabService;

    public Map<String, Object> updateApplicantRespondentAndChildData(CallbackRequest callbackRequest) {
        Map<String, Object> updatedCaseData = callbackRequest.getCaseDetails().getData();
        Map<String, Object> oldCaseDataMap = callbackRequest.getCaseDetailsBefore().getData();

        CaseData caseData = objectMapper.convertValue(updatedCaseData, CaseData.class);
        CaseData oldCaseData = objectMapper.convertValue(oldCaseDataMap, CaseData.class);

        CaseData caseDataTemp = confidentialDetailsMapper.mapConfidentialData(caseData, false);
        updatedCaseData.put(RESPONDENT_CONFIDENTIAL_DETAILS, caseDataTemp.getRespondentConfidentialDetails());

        updatedCaseData.putAll(caseSummaryTabService.updateTab(caseData));

        // final Flags caseFlags = Flags.builder().build();

        // updatedCaseData.put("caseFlags", caseFlags);

        if (FL401_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            updatedCaseData.putAll(noticeOfChangePartiesService.generate(caseData, DARESPONDENT));
            updatedCaseData.putAll(noticeOfChangePartiesService.generate(caseData, DAAPPLICANT));

            PartyDetails fl401Applicant = caseData
                .getApplicantsFL401();
            PartyDetails fl401respondent = caseData
                .getRespondentsFL401();
            try {
                log.info("updatedCaseData is:: " + objectMapper.writeValueAsString(updatedCaseData));
            } catch (JsonProcessingException e) {
                log.info("error");
            }
            if (Objects.nonNull(fl401Applicant)) {
                CommonUtils.generatePartyUuidForFL401(caseData);
                updatedCaseData.put("applicantName", fl401Applicant.getLabelForDynamicList());
                partyLevelCaseFlagsService.amendFl401PartyCaseFlags(oldCaseData, caseData, updatedCaseData, PartyRole.Representing.DAAPPLICANT);
                partyLevelCaseFlagsService.amendFl401PartyCaseFlags(oldCaseData, caseData, updatedCaseData, PartyRole.Representing.DAAPPLICANTSOLICITOR);
            }

            if (Objects.nonNull(fl401respondent)) {
                CommonUtils.generatePartyUuidForFL401(caseData);
                updatedCaseData.put("respondentName", fl401respondent.getLabelForDynamicList());
                partyLevelCaseFlagsService.amendFl401PartyCaseFlags(oldCaseData, caseData, updatedCaseData, PartyRole.Representing.DARESPONDENT);
                partyLevelCaseFlagsService.amendFl401PartyCaseFlags(oldCaseData, caseData, updatedCaseData, PartyRole.Representing.DARESPONDENTSOLICITOR);
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
                    .toList();
                PartyDetails applicant1 = applicants.get(0);
                if (Objects.nonNull(applicant1)) {
                    updatedCaseData.put("applicantName",applicant1.getFirstName() + " " + applicant1.getLastName());
                }
            }
            // set applicant and respondent case flag
            setApplicantSolicitorUuid(caseData, updatedCaseData);
            setRespondentSolicitorUuid(caseData, updatedCaseData);
            Optional<List<Element<PartyDetails>>> applicantList = ofNullable(caseData.getApplicants());
            if (applicantList.isPresent()) {
                setApplicantOrganisationPolicyIfOrgEmpty(updatedCaseData, ElementUtils.unwrapElements(applicantList.get()).get(0));
            }
            partyLevelCaseFlagsService.amendC100PartyCaseFlags(oldCaseData, caseData, updatedCaseData, PartyRole.Representing.CAAPPLICANT);
            partyLevelCaseFlagsService.amendC100PartyCaseFlags(oldCaseData, caseData, updatedCaseData, PartyRole.Representing.CAAPPLICANTSOLICITOR);
            partyLevelCaseFlagsService.amendC100PartyCaseFlags(oldCaseData, caseData, updatedCaseData, PartyRole.Representing.CARESPONDENT);
            partyLevelCaseFlagsService.amendC100PartyCaseFlags(oldCaseData, caseData, updatedCaseData, PartyRole.Representing.CARESPONDENTSOLICITOR);
            partyLevelCaseFlagsService.amendC100PartyCaseFlags(oldCaseData, caseData, updatedCaseData, PartyRole.Representing.CAOTHERPARTY);
        }
        cleanUpCaseDataBasedOnYesNoSelection(updatedCaseData, caseData);
        return updatedCaseData;
    }

    private void cleanUpCaseDataBasedOnYesNoSelection(Map<String, Object> updatedCaseData, CaseData caseData) {
        if (FL401_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            if (isNotEmpty(caseData.getRespondentsFL401())) {
                PartyDetails updatedRespondent = resetRespondent(caseData.getRespondentsFL401());
                updatedCaseData.put(FL401_RESPONDENTS, updatedRespondent);
            }
            if (isNotEmpty(caseData.getApplicantsFL401())) {
                PartyDetails updatedApplicant = resetApplicant(caseData.getApplicantsFL401());
                updatedCaseData.put(FL401_APPLICANTS, updatedApplicant);
            }
        } else if (C100_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            if (CollectionUtils.isNotEmpty(caseData.getRespondents())) {
                List<Element<PartyDetails>> updatedRespondents = new ArrayList<>();
                caseData.getRespondents().forEach(eachRespondent ->
                    updatedRespondents.add(element(
                        eachRespondent.getId(),
                        resetRespondent(eachRespondent.getValue())
                    ))
                );
                updatedCaseData.put(RESPONDENTS, updatedRespondents);
            }
            if (CollectionUtils.isNotEmpty(caseData.getApplicants())) {
                List<Element<PartyDetails>> updatedApplicants = new ArrayList<>();
                caseData.getApplicants().forEach(eachApplicant ->
                    updatedApplicants.add(element(
                        eachApplicant.getId(),
                        resetApplicant(eachApplicant.getValue())
                    ))
                );
                updatedCaseData.put(APPLICANTS, updatedApplicants);
            }
            if (CollectionUtils.isNotEmpty(caseData.getChildren())
                && YesNoDontKnow.no.equals(caseData.getChildrenKnownToLocalAuthority())) {
                updatedCaseData.put("childrenKnownToLocalAuthorityTextArea", null);
            }
        }
    }

    private PartyDetails resetApplicant(PartyDetails partyDetails) {
        partyDetails = partyDetails.toBuilder()
            .addressLivedLessThan5YearsDetails(YesOrNo.Yes.equals(partyDetails.getIsAtAddressLessThan5Years())
                                                   ? partyDetails.getAddressLivedLessThan5YearsDetails() : null)
            .email(YesOrNo.Yes.equals(partyDetails.getCanYouProvideEmailAddress()) ? partyDetails.getEmail() : null)
            .isEmailAddressConfidential(YesOrNo.Yes.equals(partyDetails.getCanYouProvideEmailAddress())
                                            ? partyDetails.getIsEmailAddressConfidential() : null)
            .build();

        return partyDetails;
    }

    private PartyDetails resetRespondent(PartyDetails partyDetails) {
        boolean isRepresented = YesNoDontKnow.yes.equals(partyDetails.getDoTheyHaveLegalRepresentation());
        partyDetails = partyDetails.toBuilder()
            .dateOfBirth(YesOrNo.Yes.equals(partyDetails.getIsDateOfBirthKnown()) ? partyDetails.getDateOfBirth() : null)
            .placeOfBirth(YesOrNo.Yes.equals(partyDetails.getIsPlaceOfBirthKnown()) ? partyDetails.getPlaceOfBirth() : null)
            .address(YesOrNo.Yes.equals(partyDetails.getIsCurrentAddressKnown()) ? partyDetails.getAddress() : null)
            .isAddressConfidential(YesOrNo.Yes.equals(partyDetails.getIsCurrentAddressKnown())
                                       ? partyDetails.getIsAddressConfidential() : null)
            .addressLivedLessThan5YearsDetails(YesNoDontKnow.yes.equals(partyDetails.getIsAtAddressLessThan5YearsWithDontKnow())
                                                   ? partyDetails.getAddressLivedLessThan5YearsDetails() : null)
            .email(YesOrNo.Yes.equals(partyDetails.getCanYouProvideEmailAddress()) ? partyDetails.getEmail() : null)
            .isEmailAddressConfidential(YesOrNo.Yes.equals(partyDetails.getCanYouProvideEmailAddress())
                                            ? partyDetails.getIsEmailAddressConfidential() : null)
            .phoneNumber(YesOrNo.Yes.equals(partyDetails.getCanYouProvidePhoneNumber()) ? partyDetails.getPhoneNumber() : null)
            .isPhoneNumberConfidential(YesOrNo.Yes.equals(partyDetails.getCanYouProvidePhoneNumber())
                                           ? partyDetails.getIsPhoneNumberConfidential() : null)
            .representativeFirstName(isRepresented ? partyDetails.getRepresentativeFirstName() : null)
            .representativeLastName(isRepresented ? partyDetails.getRepresentativeLastName() : null)
            .solicitorEmail(isRepresented ? partyDetails.getSolicitorEmail() : null)
            .dxNumber(isRepresented ? partyDetails.getDxNumber() : null)
            .solicitorAddress(isRepresented ? partyDetails.getSolicitorAddress() : null)
            .solicitorOrg(isRepresented ? partyDetails.getSolicitorOrg() : null)
            .build();

        return partyDetails;
    }

    private void setApplicantOrganisationPolicyIfOrgEmpty(Map<String, Object> updatedCaseData, PartyDetails partyDetails) {
        CaseData caseDataUpdated = objectMapper.convertValue(updatedCaseData, CaseData.class);
        OrganisationPolicy applicantOrganisationPolicy = caseDataUpdated.getApplicantOrganisationPolicy();
        boolean organisationNotExists = false;
        boolean roleNotExists = false;
        if (ObjectUtils.isEmpty(applicantOrganisationPolicy)) {
            applicantOrganisationPolicy = OrganisationPolicy.builder().orgPolicyCaseAssignedRole("[APPLICANTSOLICITOR]").build();
            organisationNotExists = true;
        } else if (ObjectUtils.isNotEmpty(applicantOrganisationPolicy) && (ObjectUtils.isEmpty(
            applicantOrganisationPolicy.getOrganisation()) || (ObjectUtils.isNotEmpty(
            applicantOrganisationPolicy.getOrganisation()) && StringUtils.isEmpty(
            applicantOrganisationPolicy.getOrganisation().getOrganisationID())))
        ) {
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
        updatedCaseData.put("applicantOrganisationPolicy", applicantOrganisationPolicy);
    }

    private void setApplicantSolicitorUuid(CaseData caseData, Map<String, Object> caseDetails) {
        Optional<List<Element<PartyDetails>>> applicantsWrapped = ofNullable(caseData.getApplicants());
        if (applicantsWrapped.isPresent() && !applicantsWrapped.get().isEmpty()) {
            List<PartyDetails> applicants = applicantsWrapped.get()
                .stream()
                .map(Element::getValue)
                .toList();

            for (PartyDetails applicant : applicants) {
                CommonUtils.generatePartyUuidForC100(applicant);
            }
            caseDetails.put("applicants", applicantsWrapped);
        }
    }

    private void setRespondentSolicitorUuid(CaseData caseData, Map<String, Object> caseDetails) {
        Optional<List<Element<PartyDetails>>> respondentsWrapped = ofNullable(caseData.getRespondents());
        if (respondentsWrapped.isPresent() && !respondentsWrapped.get().isEmpty()) {
            List<PartyDetails> respondents = respondentsWrapped.get()
                .stream()
                .map(Element::getValue)
                .toList();

            for (PartyDetails respondent : respondents) {
                CommonUtils.generatePartyUuidForC100(respondent);
            }
            caseDetails.put("respondents", respondentsWrapped);
        }
    }
}
