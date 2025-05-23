package uk.gov.hmcts.reform.prl.services;

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
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.citizen.ConfidentialityListEnum;
import uk.gov.hmcts.reform.prl.mapper.citizen.confidentialdetails.ConfidentialDetailsMapper;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseaccess.OrganisationPolicy;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.confidentiality.KeepDetailsPrivate;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.models.refuge.RefugeConfidentialDocumentsRecord;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.C100RespondentSolicitorService;
import uk.gov.hmcts.reform.prl.services.caseflags.PartyLevelCaseFlagsService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C8_RESP_FINAL_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C8_RESP_FL401_FINAL_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CHILDREN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_APPLICANTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_RESPONDENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HISTORICAL_REFUGE_DOCUMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.NEW_CHILDREN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.OTHER_PARTY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.REFUGE_DOCUMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RESPONDENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RESPONDENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V3;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.CAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.CARESPONDENT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.DAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.DARESPONDENT;
import static uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.C100RespondentSolicitorService.IS_CONFIDENTIAL_DATA_PRESENT;
import static uk.gov.hmcts.reform.prl.utils.CommonUtils.getPartyResponse;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class UpdatePartyDetailsService {

    public static final String RESPONDENT_CONFIDENTIAL_DETAILS = "respondentConfidentialDetails";
    protected static final String[] HISTORICAL_DOC_TO_RETAIN_FOR_EVENTS = {CaseEvent.AMEND_APPLICANTS_DETAILS.getValue(),
        CaseEvent.AMEND_RESPONDENTS_DETAILS.getValue(), CaseEvent.AMEND_OTHER_PEOPLE_IN_THE_CASE_REVISED.getValue()};
    public static final String C_8_OF = "C8 of ";
    private final ObjectMapper objectMapper;
    private final NoticeOfChangePartiesService noticeOfChangePartiesService;
    private final ConfidentialDetailsMapper confidentialDetailsMapper;
    private final C100RespondentSolicitorService c100RespondentSolicitorService;
    private final DocumentGenService documentGenService;
    private final ConfidentialityTabService confidentialityTabService;
    private final ConfidentialityC8RefugeService confidentialityC8RefugeService;
    private final DocumentLanguageService documentLanguageService;
    private final PartyLevelCaseFlagsService partyLevelCaseFlagsService;
    private final ManageOrderService manageOrderService;

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    @Qualifier("caseSummaryTab")
    private final  CaseSummaryTabService caseSummaryTabService;

    public Map<String, Object> updateApplicantRespondentAndChildData(CallbackRequest callbackRequest,
                                                                     String authorisation) {
        Map<String, Object> updatedCaseData = callbackRequest.getCaseDetails().getData();
        Map<String, Object> caseDataMap = callbackRequest.getCaseDetailsBefore().getData();
        CaseData caseData = objectMapper.convertValue(updatedCaseData, CaseData.class);
        CaseData caseDataBefore = objectMapper.convertValue(caseDataMap, CaseData.class);

        CaseData caseDataTemp = confidentialDetailsMapper.mapConfidentialData(caseData, false);
        updatedCaseData.put(RESPONDENT_CONFIDENTIAL_DETAILS, caseDataTemp.getRespondentConfidentialDetails());
        updatedCaseData.putAll(confidentialityTabService.updateConfidentialityDetails(caseData));

        //Added partyId for Hearings Api Spec, C100 applications
        //Applicants
        if (caseData.getApplicants() != null) {
            for (Element<PartyDetails> applicant : caseData.getApplicants()) {
                applicant.getValue().setPartyId(applicant.getId());
            }
        }
        //Respondents
        if (caseData.getRespondents() != null) {
            for (Element<PartyDetails> respondent : caseData.getRespondents()) {
                respondent.getValue().setPartyId(respondent.getId());
            }
        }

        updatedCaseData.putAll(caseSummaryTabService.updateTab(caseData));

        if (FL401_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            updatedCaseData.putAll(noticeOfChangePartiesService.generate(caseData, DARESPONDENT));
            updatedCaseData.putAll(noticeOfChangePartiesService.generate(caseData, DAAPPLICANT));

            PartyDetails fl401Applicant = caseData
                .getApplicantsFL401();
            PartyDetails fl401respondent = caseData
                .getRespondentsFL401();

            setFl401PartyNames(fl401Applicant, caseData, updatedCaseData, fl401respondent);
            setApplicantOrganisationPolicyIfOrgEmpty(updatedCaseData, caseData.getApplicantsFL401());
            confidentialityC8RefugeService.processForcePartiesConfidentialityIfLivesInRefugeForFL401(
                ofNullable(caseData.getApplicantsFL401()),
                updatedCaseData,
                FL401_APPLICANTS,
                false
            );
            confidentialityC8RefugeService.processForcePartiesConfidentialityIfLivesInRefugeForFL401(
                ofNullable(caseData.getRespondentsFL401()),
                updatedCaseData,
                FL401_RESPONDENTS,
                false
            );
            if (CaseEvent.AMEND_APPLICANTS_DETAILS.getValue().equals(callbackRequest.getEventId())
                || CaseEvent.APPLICANT_DETAILS.getValue().equals(callbackRequest.getEventId())) {
                caseData = caseData
                    .toBuilder()
                    .applicantsFL401(setCitizenConfidentialDetailsFL401(caseData.getApplicantsFL401(),
                        caseDataBefore.getApplicantsFL401()))
                    .build();
            }
            try {
                generateC8DocumentsForRespondents(updatedCaseData,
                                                  callbackRequest,
                                                  authorisation,
                                                  caseData,
                                                  List.of(ElementUtils.element(fl401respondent.getPartyId(), fl401respondent)));
            } catch (Exception e) {
                log.error("Failed to generate C8 document for Fl401 case {}", e.getMessage());
            }
            cleanUpCaseDataBasedOnYesNoSelection(updatedCaseData, caseData);
            findAndListRefugeDocsForFL401(callbackRequest, caseData, updatedCaseData);
        } else if (C100_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            updatedCaseData.putAll(noticeOfChangePartiesService.generate(caseData, CARESPONDENT));
            updatedCaseData.putAll(noticeOfChangePartiesService.generate(caseData, CAAPPLICANT));
            Optional<List<Element<PartyDetails>>> applicantsWrapped = ofNullable(caseData.getApplicants());
            setC100ApplicantPartyName(applicantsWrapped, updatedCaseData);
            // set applicant and respondent case flag
            setApplicantSolicitorUuid(caseData, updatedCaseData);
            setRespondentSolicitorUuid(caseData, updatedCaseData);
            confidentialityC8RefugeService.processForcePartiesConfidentialityIfLivesInRefugeForC100(
                ofNullable(caseData.getApplicants()),
                updatedCaseData,
                APPLICANTS,
                false
            );
            confidentialityC8RefugeService.processForcePartiesConfidentialityIfLivesInRefugeForC100(
                ofNullable(caseData.getRespondents()),
                updatedCaseData,
                RESPONDENTS,
                false
            );
            if (CaseEvent.AMEND_APPLICANTS_DETAILS.getValue().equals(callbackRequest.getEventId())
                || CaseEvent.APPLICANT_DETAILS.getValue().equals(callbackRequest.getEventId())) {
                caseData = caseData
                    .toBuilder()
                    .applicants(setCitizenConfidentialDetailsInResponseC100(caseData.getApplicants(),
                        caseDataBefore.getApplicants()))
                    .build();
            }
            Optional<List<Element<PartyDetails>>> applicantList = ofNullable(caseData.getApplicants());
            applicantList.ifPresent(elements -> setApplicantOrganisationPolicyIfOrgEmpty(updatedCaseData,
                    ElementUtils.unwrapElements(elements).get(0)));
            try {
                generateC8DocumentsForRespondents(updatedCaseData,
                                                  callbackRequest,
                                                  authorisation,
                                                  caseData,
                                                  caseData.getRespondents());
            } catch (Exception e) {
                log.error("Failed to generate C8 document for C100 case {}", e.getMessage());
            }
        }
        if (Objects.nonNull(callbackRequest.getCaseDetailsBefore())) {
            Map<String, Object> oldCaseDataMap = callbackRequest.getCaseDetailsBefore().getData();
            partyLevelCaseFlagsService.amendCaseFlags(oldCaseDataMap, updatedCaseData, callbackRequest.getEventId());
        }
        cleanUpCaseDataBasedOnYesNoSelection(updatedCaseData, caseData);
        findAndListRefugeDocsForC100(callbackRequest, caseData, updatedCaseData);
        return updatedCaseData;
    }

    public Map<String, Object> amendOtherPeopleInTheCase(CallbackRequest callbackRequest) {
        Map<String, Object> oldCaseDataMap = callbackRequest.getCaseDetailsBefore().getData();
        Map<String, Object> updatedCaseData = callbackRequest.getCaseDetails().getData();
        partyLevelCaseFlagsService.amendCaseFlags(oldCaseDataMap, updatedCaseData, callbackRequest.getEventId());
        return updatedCaseData;
    }

    private List<Element<PartyDetails>> setCitizenConfidentialDetailsInResponseC100(List<Element<PartyDetails>> applicantDetailsWrappedList,
                                                                 List<Element<PartyDetails>> applicantDetailsBeforeList) {
        List<Element<PartyDetails>> updatedPartyDetailsList = null;

        if (CollectionUtils.isNotEmpty(applicantDetailsWrappedList) && CollectionUtils.isNotEmpty(applicantDetailsBeforeList)) {
            updatedPartyDetailsList = new ArrayList<>();
            for (Element<PartyDetails> partyDetailsElement : applicantDetailsWrappedList) {
                PartyDetails partyDetails = partyDetailsElement.getValue();
                int index = applicantDetailsWrappedList.indexOf(partyDetailsElement);

                if (indexExists(applicantDetailsBeforeList, index)) {
                    PartyDetails partyDetailsBefore = applicantDetailsBeforeList.get(index).getValue();
                    partyDetails = checkConfidentialDetailsForExistingUser(partyDetails, partyDetailsBefore);
                } else {
                    partyDetails = partyDetails
                        .toBuilder()
                        .response(Response
                            .builder()
                            .keepDetailsPrivate(updateRespondentKeepYourDetailsPrivateInformation(partyDetails))
                            .build())
                        .build();
                }
                updatedPartyDetailsList.add(element(partyDetailsElement.getId(), partyDetails));
            }
        }
        return updatedPartyDetailsList;
    }

    private PartyDetails setCitizenConfidentialDetailsFL401(PartyDetails partyDetails,
                                                                   PartyDetails partyDetailsBefore) {
        if (null != partyDetailsBefore && null != partyDetails) {
            partyDetails = checkConfidentialDetailsForExistingUser(partyDetails, partyDetailsBefore);
        }
        return partyDetails;
    }

    private PartyDetails checkConfidentialDetailsForExistingUser(PartyDetails partyDetails,
                                                                        PartyDetails partyDetailsBefore) {
        if (checkIfAddressConfidentialityHasChanged(partyDetails, partyDetailsBefore)
            || checkIfPhoneConfidentialityHasChanged(partyDetails, partyDetailsBefore)
            || checkIfEmailConfidentialityHasChanged(partyDetails, partyDetailsBefore)) {

            if (null != partyDetails.getResponse()) {
                return partyDetails
                    .toBuilder()
                    .response(partyDetails
                        .getResponse()
                        .toBuilder()
                        .keepDetailsPrivate(updateRespondentKeepYourDetailsPrivateInformation(partyDetails))
                        .build())
                    .build();
            } else {
                return partyDetails
                    .toBuilder()
                    .response(Response
                        .builder()
                        .keepDetailsPrivate(updateRespondentKeepYourDetailsPrivateInformation(partyDetails))
                        .build())
                    .build();
            }
        }

        return partyDetails;
    }

    private static boolean indexExists(final List<?> list, final int index) {
        return list != null && index >= 0 && index < list.size();
    }

    private static boolean checkIfAddressConfidentialityHasChanged(PartyDetails partyDetails, PartyDetails partyDetailsBefore) {
        return isNotEmpty(partyDetails.getIsAddressConfidential())
            && isNotEmpty(partyDetailsBefore.getIsAddressConfidential())
            && !partyDetailsBefore.getIsAddressConfidential()
            .equals(partyDetails.getIsAddressConfidential());
    }

    private static boolean checkIfEmailConfidentialityHasChanged(PartyDetails partyDetails, PartyDetails partyDetailsBefore) {
        return isNotEmpty(partyDetails.getIsEmailAddressConfidential())
            && isNotEmpty(partyDetailsBefore.getIsEmailAddressConfidential())
            && !partyDetailsBefore.getIsEmailAddressConfidential()
            .equals(partyDetails.getIsEmailAddressConfidential());
    }

    private static boolean checkIfPhoneConfidentialityHasChanged(PartyDetails partyDetails, PartyDetails partyDetailsBefore) {
        return isNotEmpty(partyDetails.getIsPhoneNumberConfidential())
            && isNotEmpty(partyDetailsBefore.getIsPhoneNumberConfidential())
            && !partyDetailsBefore.getIsPhoneNumberConfidential()
            .equals(partyDetails.getIsPhoneNumberConfidential());
    }

    private static void setC100ApplicantPartyName(Optional<List<Element<PartyDetails>>> applicantsWrapped, Map<String, Object> updatedCaseData) {
        if (applicantsWrapped.isPresent() && !applicantsWrapped.get().isEmpty()) {
            List<PartyDetails> applicants = applicantsWrapped.get()
                .stream()
                .map(Element::getValue)
                .toList();
            PartyDetails applicant1 = applicants.get(0);
            if (Objects.nonNull(applicant1)) {
                updatedCaseData.put("applicantName", applicant1.getFirstName() + " " + applicant1.getLastName());
            }
        }
    }

    private static void setFl401PartyNames(PartyDetails fl401Applicant,
                                           CaseData caseData,
                                           Map<String, Object> updatedCaseData,
                                           PartyDetails fl401respondent) {
        if (Objects.nonNull(fl401Applicant)) {
            CommonUtils.generatePartyUuidForFL401(caseData);
            updatedCaseData.put("applicantName", fl401Applicant.getLabelForDynamicList());
        }

        if (Objects.nonNull(fl401respondent)) {
            CommonUtils.generatePartyUuidForFL401(caseData);
            updatedCaseData.put("respondentName", fl401respondent.getLabelForDynamicList());
        }
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
            if (CollectionUtils.isNotEmpty(caseData.getOtherPartyInTheCaseRevised())) {
                List<Element<PartyDetails>> updatedOtherParties = new ArrayList<>();
                caseData.getOtherPartyInTheCaseRevised().forEach(otherParties ->
                                                                     updatedOtherParties.add(element(
                                                                         otherParties.getId(),
                                                                         resetOtherParties(otherParties.getValue())
                                                                     ))
                );
                updatedCaseData.put(OTHER_PARTY, updatedOtherParties);
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
            .refugeConfidentialityC8Form(YesOrNo.Yes.equals(partyDetails.getLiveInRefuge())
                                             ? partyDetails.getRefugeConfidentialityC8Form() : null)
            .build();

        return partyDetails;
    }

    private PartyDetails resetRespondent(PartyDetails partyDetails) {
        boolean isRepresented = YesNoDontKnow.yes.equals(partyDetails.getDoTheyHaveLegalRepresentation());
        partyDetails = partyDetails.toBuilder()
            .dateOfBirth(YesOrNo.Yes.equals(partyDetails.getIsDateOfBirthKnown()) ? partyDetails.getDateOfBirth() : null)
            .placeOfBirth(YesOrNo.Yes.equals(partyDetails.getIsPlaceOfBirthKnown()) ? partyDetails.getPlaceOfBirth() : null)
            .address(YesOrNo.Yes.equals(partyDetails.getIsCurrentAddressKnown()) ? partyDetails.getAddress() : null)
            .liveInRefuge(YesOrNo.Yes.equals(partyDetails.getIsCurrentAddressKnown()) ? partyDetails.getLiveInRefuge() : null)
            .refugeConfidentialityC8Form(YesOrNo.Yes.equals(partyDetails.getIsCurrentAddressKnown())
                                             && YesOrNo.Yes.equals(partyDetails.getLiveInRefuge())
                                             ? partyDetails.getRefugeConfidentialityC8Form() : null)
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
            .response(getPartyResponse(partyDetails).toBuilder()
                          .keepDetailsPrivate(updateRespondentKeepYourDetailsPrivateInformation(partyDetails))
                          .build())
            .build();

        return partyDetails;
    }

    private PartyDetails resetOtherParties(PartyDetails partyDetails) {
        partyDetails = partyDetails.toBuilder()
            .dateOfBirth(YesOrNo.Yes.equals(partyDetails.getIsDateOfBirthKnown()) ? partyDetails.getDateOfBirth() : null)
            .placeOfBirth(YesOrNo.Yes.equals(partyDetails.getIsPlaceOfBirthKnown()) ? partyDetails.getPlaceOfBirth() : null)
            .address(YesOrNo.Yes.equals(partyDetails.getIsCurrentAddressKnown()) ? partyDetails.getAddress() : null)
            .liveInRefuge(YesOrNo.Yes.equals(partyDetails.getIsCurrentAddressKnown()) ? partyDetails.getLiveInRefuge() : null)
            .refugeConfidentialityC8Form(YesOrNo.Yes.equals(partyDetails.getIsCurrentAddressKnown())
                                             && YesOrNo.Yes.equals(partyDetails.getLiveInRefuge())
                                             ? partyDetails.getRefugeConfidentialityC8Form() : null)
            .isAddressConfidential(YesOrNo.Yes.equals(partyDetails.getIsCurrentAddressKnown())
                                       ? partyDetails.getIsAddressConfidential() : null)
            .email(YesOrNo.Yes.equals(partyDetails.getCanYouProvideEmailAddress()) ? partyDetails.getEmail() : null)
            .isEmailAddressConfidential(YesOrNo.Yes.equals(partyDetails.getCanYouProvideEmailAddress())
                                            ? partyDetails.getIsEmailAddressConfidential() : null)
            .phoneNumber(YesOrNo.Yes.equals(partyDetails.getCanYouProvidePhoneNumber()) ? partyDetails.getPhoneNumber() : null)
            .isPhoneNumberConfidential(YesOrNo.Yes.equals(partyDetails.getCanYouProvidePhoneNumber())
                                           ? partyDetails.getIsPhoneNumberConfidential() : null)
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
            caseDetails.put(APPLICANTS, applicantsWrapped);
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
            caseDetails.put(RESPONDENTS, respondentsWrapped);
        }
    }

    private void generateC8DocumentsForRespondents(Map<String, Object> updatedCaseData, CallbackRequest callbackRequest, String authorisation,
                                                       CaseData caseData, List<Element<PartyDetails>> currentRespondents)
        throws Exception {
        int respondentIndex = 0;
        Map<String, Object> casDataMap = callbackRequest.getCaseDetailsBefore().getData();
        CaseData caseDataBefore = objectMapper.convertValue(casDataMap, CaseData.class);
        for (Element<PartyDetails> respondent: currentRespondents) {
            PartyDetails updatedPartyDetails = respondent.getValue().toBuilder().response(getPartyResponse(respondent.getValue()).toBuilder()
                                                                                              .keepDetailsPrivate(
                                                                                                  updateRespondentKeepYourDetailsPrivateInformation(
                                                                                                      respondent.getValue()))
                                                                                              .build()).build();
            respondent = element(respondent.getId(), updatedPartyDetails);
            Map<String, Object> dataMap = c100RespondentSolicitorService.populateDataMap(
                callbackRequest,
                respondent,
                SOLICITOR
            );
            //PRL-6790 - Add updated respondent details to dataMap for C8 document generation
            dataMap.put(RESPONDENT, respondent.getValue());
            populateC8Documents(authorisation,
                                updatedCaseData,
                                caseData,
                                dataMap, checkIfConfidentialityDetailsChangedRespondent(caseDataBefore, respondent),
                                respondentIndex, respondent
            );
            respondentIndex++;
        }
    }

    private KeepDetailsPrivate updateRespondentKeepYourDetailsPrivateInformation(PartyDetails respondent) {
        KeepDetailsPrivate keepDetailsPrivate;
        if (null != respondent.getResponse() && null != respondent.getResponse().getKeepDetailsPrivate()) {
            keepDetailsPrivate = respondent.getResponse().getKeepDetailsPrivate();
        } else {
            keepDetailsPrivate = KeepDetailsPrivate.builder().build();
        }
        List<ConfidentialityListEnum> confidentialityList = new ArrayList<>();
        if ((YesOrNo.Yes.equals(respondent.getIsCurrentAddressKnown()) && YesOrNo.Yes.equals(respondent.getIsAddressConfidential()))
            || (null != respondent.getAddress() && YesOrNo.Yes.equals(respondent.getIsAddressConfidential()))) {
            confidentialityList.add(ConfidentialityListEnum.address);
        }
        if (YesOrNo.Yes.equals(respondent.getCanYouProvidePhoneNumber()) && YesOrNo.Yes.equals(respondent.getIsPhoneNumberConfidential())
            || (null != respondent.getPhoneNumber() && YesOrNo.Yes.equals(respondent.getIsPhoneNumberConfidential()))) {
            confidentialityList.add(ConfidentialityListEnum.phoneNumber);
        }
        if (YesOrNo.Yes.equals(respondent.getCanYouProvideEmailAddress()) && YesOrNo.Yes.equals(respondent.getIsEmailAddressConfidential())) {
            confidentialityList.add(ConfidentialityListEnum.email);
        }
        return keepDetailsPrivate.toBuilder()
            .confidentiality(CollectionUtils.isEmpty(confidentialityList) ? YesOrNo.No : YesOrNo.Yes)
            .confidentialityList(confidentialityList)
            .build();
    }

    public Boolean checkIfConfidentialityDetailsChangedRespondent(CaseData caseDataBefore, Element<PartyDetails> respondent) {
        List<Element<PartyDetails>> respondentList = null;
        if (caseDataBefore.getCaseTypeOfApplication().equals(C100_CASE_TYPE)) {
            respondentList = caseDataBefore.getRespondents().stream()
                .filter(resp1 -> resp1.getId().equals(respondent.getId())
                    && (CaseUtils.isEmailAddressChanged(respondent.getValue(), resp1.getValue())
                    || CaseUtils.checkIfAddressIsChanged(respondent.getValue(), resp1.getValue())
                    || CaseUtils.isPhoneNumberChanged(respondent.getValue(), resp1.getValue())
                    || !StringUtils.equals(resp1.getValue().getLabelForDynamicList(), respondent.getValue()
                    .getLabelForDynamicList()))).toList();
        } else {
            PartyDetails respondentDetailsFL401 = caseDataBefore.getRespondentsFL401();
            if ((CaseUtils.isEmailAddressChanged(respondent.getValue(), respondentDetailsFL401))
                || CaseUtils.checkIfAddressIsChanged(respondent.getValue(), respondentDetailsFL401)
                || (CaseUtils.isPhoneNumberChanged(respondent.getValue(), respondentDetailsFL401))
                || !StringUtils.equals(respondent.getValue().getLabelForDynamicList(), respondentDetailsFL401
                .getLabelForDynamicList())) {
                return true;
            }
        }
        if (respondentList != null && !respondentList.isEmpty()) {
            return true;
        }
        return false;
    }



    public void populateC8Documents(String authorisation, Map<String, Object> updatedCaseData, CaseData caseData,
                                      Map<String, Object> dataMap, Boolean isDetailsChanged, int partyIndex,
                                      Element<PartyDetails> respondent) throws Exception {
        //prl-6790 - getting user-role and adding to datamap
        dataMap.put("loggedInUserRole", manageOrderService.getLoggedInUserType(authorisation));

        log.info("inside populateC8Documents for partyIndex " + partyIndex);
        if (partyIndex >= 0) {
            switch (partyIndex) {
                case 0:
                    updatedCaseData
                        .put("respondentAc8Documents",getOrCreateC8DocumentList(authorisation, caseData, dataMap,
                                                                                caseData.getRespondentC8Document()
                                                                                    .getRespondentAc8Documents(),
                                                                                isDetailsChanged,
                                                                                respondent));
                    break;
                case 1:
                    updatedCaseData
                        .put("respondentBc8Documents",getOrCreateC8DocumentList(authorisation, caseData,
                                                                                dataMap,
                                                                                caseData.getRespondentC8Document()
                                                                                    .getRespondentBc8Documents(),
                                                                                isDetailsChanged,
                                                                                respondent));
                    break;
                case 2:
                    updatedCaseData
                        .put("respondentCc8Documents",getOrCreateC8DocumentList(authorisation, caseData,
                                                                                dataMap,
                                                                                caseData.getRespondentC8Document()
                                                                                    .getRespondentCc8Documents(),
                                                                                isDetailsChanged,
                                                                                respondent));
                    break;
                case 3:
                    updatedCaseData
                        .put("respondentDc8Documents",getOrCreateC8DocumentList(authorisation, caseData,
                                                                                dataMap,
                                                                                caseData.getRespondentC8Document()
                                                                                    .getRespondentDc8Documents(),
                                                                                isDetailsChanged,
                                                                                respondent));
                    break;
                case 4:
                    updatedCaseData
                        .put("respondentEc8Documents",getOrCreateC8DocumentList(authorisation, caseData,
                                                                                dataMap,
                                                                                caseData.getRespondentC8Document()
                                                                                    .getRespondentEc8Documents(),
                                                                                isDetailsChanged,
                                                                                respondent));
                    break;
                default:
                    break;
            }
        }
    }

    private List<Element<ResponseDocuments>> getOrCreateC8DocumentList(String authorisation, CaseData caseData,
                                                                       Map<String, Object> dataMap,
                                                                       List<Element<ResponseDocuments>> c8Documents,
                                                                       boolean isDetailsChanged,
                                                                       Element<PartyDetails> respondent)
        throws  Exception {
        Document c8FinalDocument;
        Document c8FinalWelshDocument = null;
        String partyName = respondent.getValue().getLabelForDynamicList();
        if (dataMap.containsKey(IS_CONFIDENTIAL_DATA_PRESENT)) {
            if (isDetailsChanged) {
                String fileName = C_8_OF + partyName
                    + " " + LocalDateTime.now(ZoneId.of(LONDON_TIME_ZONE)).format(dateTimeFormatter);
                dataMap.put("dynamic_fileName", fileName + ".pdf");
                c8FinalDocument = documentGenService.generateSingleDocument(
                        authorisation,
                        caseData,
                        caseData.getCaseTypeOfApplication()
                                .equals(C100_CASE_TYPE) ? C8_RESP_FINAL_HINT
                                : C8_RESP_FL401_FINAL_HINT,
                        false,
                        dataMap
                );
                dataMap.put("dynamic_fileName", fileName + " welsh" + ".pdf");
                DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
                if (documentLanguage.isGenWelsh()) {
                    c8FinalWelshDocument = documentGenService.generateSingleDocument(
                            authorisation,
                            caseData,
                            caseData.getCaseTypeOfApplication()
                                    .equals(C100_CASE_TYPE) ? C8_RESP_FINAL_HINT
                                    : C8_RESP_FL401_FINAL_HINT,
                            true,
                            dataMap
                    );
                }
                Element<ResponseDocuments> newC8Document = ElementUtils.element(ResponseDocuments.builder()
                                                                                    .dateTimeCreated(LocalDateTime.now())
                                                                                    .respondentC8Document(
                                                                                        c8FinalDocument)
                                                                                    .respondentC8DocumentWelsh(
                                                                                        c8FinalWelshDocument)
                                                                                    .build());
                return getC8DocumentReverseOrderList(c8Documents, newC8Document);
            } else {
                return  c8Documents;
            }
        } else {
            return Collections.emptyList();
        }
    }

    private List<Element<ResponseDocuments>> getC8DocumentReverseOrderList(List<Element<ResponseDocuments>> c8Documents,
                                                                           Element<ResponseDocuments> newC8Document) {
        List<Element<ResponseDocuments>> newC8Documents = new ArrayList<>();
        if (null != c8Documents) {
            c8Documents.add(newC8Document);
            c8Documents.sort(Comparator.comparing(
                m -> m.getValue().getDateTimeCreated(),
                Comparator.reverseOrder()
            ));
            return c8Documents;
        } else {
            newC8Documents.add(newC8Document);
            return newC8Documents;
        }
    }

    public Map<String, Object> setDefaultEmptyApplicantForC100(CaseData caseData) {

        Map<String, Object> caseDataUpdated = new HashMap<>();
        List<Element<PartyDetails>> applicants = caseData.getApplicants();
        if (CollectionUtils.isEmpty(applicants) || CollectionUtils.size(applicants) < 1) {
            applicants = new ArrayList<>();
            Element<PartyDetails> partyDetails = element(PartyDetails.builder().build());
            applicants.add(partyDetails);
            caseDataUpdated.put(APPLICANTS, applicants);
            return caseDataUpdated;
        }
        caseDataUpdated.put(APPLICANTS, caseData.getApplicants());
        return caseDataUpdated;

    }

    public Map<String, Object> setDefaultEmptyRespondentForC100(CaseData caseData) {

        Map<String, Object> caseDataUpdated = new HashMap<>();
        List<Element<PartyDetails>> respondents = caseData.getRespondents();
        if (CollectionUtils.isEmpty(respondents) || CollectionUtils.size(respondents) < 1) {
            respondents = new ArrayList<>();
            Element<PartyDetails> partyDetails = element(PartyDetails.builder().build());
            respondents.add(partyDetails);
            caseDataUpdated.put(RESPONDENTS, respondents);
            return caseDataUpdated;
        }
        caseDataUpdated.put(RESPONDENTS, caseData.getRespondents());
        return caseDataUpdated;

    }

    public Map<String, Object> setDefaultEmptyChildDetails(CaseData caseData) {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        if (TASK_LIST_VERSION_V2.equalsIgnoreCase(caseData.getTaskListVersion())
            || TASK_LIST_VERSION_V3.equalsIgnoreCase(caseData.getTaskListVersion())) {
            List<Element<ChildDetailsRevised>> children = caseData.getNewChildDetails();
            if (CollectionUtils.isEmpty(children) || CollectionUtils.size(children) < 1) {
                children = new ArrayList<>();
                Element<ChildDetailsRevised> childDetails = element(ChildDetailsRevised.builder()
                    .whoDoesTheChildLiveWith(populateWhoDoesTheChildLiveWith(caseData)).build());
                children.add(childDetails);
                caseDataUpdated.put(NEW_CHILDREN, children);
            } else {
                List<Element<ChildDetailsRevised>> listOfChildren = caseData.getNewChildDetails();
                List<Element<ChildDetailsRevised>> listOfChildrenRevised = new ArrayList<>();
                listOfChildren.forEach(child -> listOfChildrenRevised.add(element(
                    child.getValue().toBuilder()
                        .whoDoesTheChildLiveWith(
                            populateWhoDoesTheChildLiveWith(caseData)
                                .toBuilder()
                                .value(null != child.getValue().getWhoDoesTheChildLiveWith()
                                    ? child.getValue().getWhoDoesTheChildLiveWith().getValue() : DynamicListElement.EMPTY)
                                .build())
                        .build())));
                caseDataUpdated.put(NEW_CHILDREN, listOfChildrenRevised);
            }
        } else {
            List<Element<Child>> children = caseData.getChildren();
            if (CollectionUtils.isEmpty(children) || CollectionUtils.size(children) < 1) {
                children = new ArrayList<>();
                Element<Child> childDetails = element(Child.builder().build());
                children.add(childDetails);
                caseDataUpdated.put(CHILDREN, children);
            } else {
                caseDataUpdated.put(CHILDREN, caseData.getChildren());
            }
        }
        return caseDataUpdated;

    }

    private DynamicList populateWhoDoesTheChildLiveWith(CaseData caseData) {
        List<Element<PartyDetails>> listOfParties = new ArrayList<>();
        if (null != caseData.getApplicants()) {
            listOfParties.addAll(caseData.getApplicants());
        }
        if (null != caseData.getRespondents()) {
            listOfParties.addAll(caseData.getRespondents());
        }
        if (null != caseData.getOtherPartyInTheCaseRevised()) {
            listOfParties.addAll(caseData.getOtherPartyInTheCaseRevised());
        }
        List<DynamicListElement> whoDoesTheChildLiveWith = new ArrayList<>();
        if (!listOfParties.isEmpty()) {
            for (Element<PartyDetails> parties : listOfParties) {

                String address = populateAddressInDynamicList(parties);
                String name = populateNameInDynamicList(parties, address);

                if (null != name && null != address) {
                    whoDoesTheChildLiveWith.add(DynamicListElement
                        .builder()
                        .code(parties.getId())
                        .label(name + address)
                        .build());
                } else if (null != name) {
                    whoDoesTheChildLiveWith.add(DynamicListElement
                        .builder()
                        .code(parties.getId())
                        .label(name)
                        .build());
                }
            }
        }

        return DynamicList
            .builder()
            .listItems(whoDoesTheChildLiveWith)
            .build();
    }

    private  String populateNameInDynamicList(Element<PartyDetails> parties, String address) {
        String name = null;
        if (!StringUtils.isBlank(parties.getValue().getFirstName())
            && !StringUtils.isBlank(parties.getValue().getLastName())) {
            name = !StringUtils.isBlank(address)
                ? parties.getValue().getFirstName() + " " + parties.getValue().getLastName() + " - "
                : parties.getValue().getFirstName() + " " + parties.getValue().getLastName();
        }
        return name;
    }

    private String populateAddressInDynamicList(Element<PartyDetails> parties) {
        String address = null;
        if (null != parties.getValue().getAddress()
            && !StringUtils.isBlank(parties.getValue().getAddress().getAddressLine1())) {

            //Address line 2 is an optional field
            String addressLine2 = "";

            //Postcode is an optional field
            String postcode = !StringUtils.isBlank(parties.getValue().getAddress().getPostCode())
                ? parties.getValue().getAddress().getPostCode() : "";

            //Adding comma to address line 2 if the postcode is there
            if (!StringUtils.isBlank(parties.getValue().getAddress().getAddressLine2())) {
                addressLine2 = !StringUtils.isBlank(postcode)
                    ?  parties.getValue().getAddress().getAddressLine2().concat(", ")
                    : parties.getValue().getAddress().getAddressLine2();
            }

            //Comma is required if postcode or address line 2 is not blank
            String addressLine1 = !StringUtils.isBlank(postcode) || !StringUtils.isBlank(addressLine2)
                ? parties.getValue().getAddress().getAddressLine1().concat(", ")
                : parties.getValue().getAddress().getAddressLine1();

            address = addressLine1 + addressLine2 + postcode;
        }

        return address;
    }

    public Map<String, Object> updateOtherPeopleInTheCaseConfidentialityData(CallbackRequest callbackRequest) {
        Map<String, Object> updatedCaseData =  amendOtherPeopleInTheCase(callbackRequest);
        CaseData caseData = objectMapper.convertValue(updatedCaseData, CaseData.class);

        if (C100_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            confidentialityC8RefugeService.processForcePartiesConfidentialityIfLivesInRefugeForC100(
                ofNullable(caseData.getOtherPartyInTheCaseRevised()),
                updatedCaseData,
                OTHER_PARTY,
                false
            );

            findAndListRefugeDocsForC100(callbackRequest, caseData, updatedCaseData);
        }
        cleanUpCaseDataBasedOnYesNoSelection(updatedCaseData, caseData);
        return updatedCaseData;
    }

    private void findAndListRefugeDocsForC100(CallbackRequest callbackRequest, CaseData caseData, Map<String, Object> updatedCaseData) {
        CaseData caseDataBefore = CaseUtils.getCaseData(callbackRequest.getCaseDetailsBefore(), objectMapper);
        boolean eligibleForDocumentProcessing
            = Arrays.stream(HISTORICAL_DOC_TO_RETAIN_FOR_EVENTS).anyMatch(s -> s.equalsIgnoreCase(callbackRequest.getEventId()));
        if (eligibleForDocumentProcessing) {
            RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord
                = confidentialityC8RefugeService.processC8RefugeDocumentsOnAmendForC100(
                caseDataBefore,
                caseData,
                callbackRequest.getEventId()
            );
            if (refugeConfidentialDocumentsRecord != null) {
                updatedCaseData.put(REFUGE_DOCUMENTS, refugeConfidentialDocumentsRecord.refugeDocuments());
                updatedCaseData.put(
                    HISTORICAL_REFUGE_DOCUMENTS,
                    refugeConfidentialDocumentsRecord.historicalRefugeDocuments()
                );
            }
        }
    }

    private void findAndListRefugeDocsForFL401(CallbackRequest callbackRequest, CaseData caseData, Map<String, Object> updatedCaseData) {
        CaseData caseDataBefore = CaseUtils.getCaseData(callbackRequest.getCaseDetailsBefore(), objectMapper);
        boolean eligibleForDocumentProcessing
            = Arrays.stream(HISTORICAL_DOC_TO_RETAIN_FOR_EVENTS).anyMatch(s -> s.equalsIgnoreCase(callbackRequest.getEventId()));
        if (eligibleForDocumentProcessing) {
            RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord
                = confidentialityC8RefugeService.processC8RefugeDocumentsOnAmendForFL401(
                caseDataBefore,
                caseData,
                callbackRequest.getEventId()
            );
            if (refugeConfidentialDocumentsRecord != null) {
                updatedCaseData.put(REFUGE_DOCUMENTS, refugeConfidentialDocumentsRecord.refugeDocuments());
                updatedCaseData.put(
                    HISTORICAL_REFUGE_DOCUMENTS,
                    refugeConfidentialDocumentsRecord.historicalRefugeDocuments()
                );
            }
        }
    }
}
