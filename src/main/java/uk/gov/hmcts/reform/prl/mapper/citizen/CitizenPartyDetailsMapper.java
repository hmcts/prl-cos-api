package uk.gov.hmcts.reform.prl.mapper.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.clients.ccd.records.CitizenUpdatePartyDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.citizen.ConfidentialityListEnum;
import uk.gov.hmcts.reform.prl.exception.CoreCaseDataStoreException;
import uk.gov.hmcts.reform.prl.models.CitizenUpdatedCaseData;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildApplicantDetailsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildChildDetailsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildConsentOrderDetails;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildCourtOrderElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildData;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildHearingWithoutNoticeElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildInternationalElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildMiamElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildOtherChildrenDetailsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildOtherPersonDetailsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildOtherProceedingsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildReasonableAdjustmentsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildRespondentDetailsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildSafetyConcernsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildUrgencyElements;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.CitizenDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.CitizenFlags;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.Contact;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.refuge.RefugeConfidentialDocumentsRecord;
import uk.gov.hmcts.reform.prl.services.ConfidentialityC8RefugeService;
import uk.gov.hmcts.reform.prl.services.ConfidentialityTabService;
import uk.gov.hmcts.reform.prl.services.UpdatePartyDetailsService;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.C100RespondentSolicitorService;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_APPLICANTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_RESPONDENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_DATA_ID;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CHILDREN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_NAME_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_SEAL_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_APPLICANTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_RESPONDENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HISTORICAL_REFUGE_DOCUMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ISSUE_DATE_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.REFUGE_DOCUMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RESPONDENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V3;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CONFIRM_YOUR_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.KEEP_DETAILS_PRIVATE;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.CAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.CARESPONDENT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.DAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.DARESPONDENT;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataApplicantElementsMapper.updateApplicantElementsForCaseData;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataChildDetailsElementsMapper.updateChildDetailsElementsForCaseData;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataConsentOrderDetailsElementsMapper.updateConsentOrderDetailsForCaseData;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataHelpWithFeesElementsMapper.updateHelpWithFeesDetailsForCaseData;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataHwnElementsMapper.updateHearingWithoutNoticeElementsForCaseData;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataInternationalElementsMapper.updateInternationalElementsForCaseData;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataMiamElementsMapper.updateMiamElementsForCaseData;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataOtherChildrenDetailsElementsMapper.updateOtherChildDetailsElementsForCaseData;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataOtherPersonsElementsMapper.updateOtherPersonDetailsElementsForCaseData;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataOtherProceedingsElementsMapper.updateOtherProceedingsElementsForCaseData;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataReasonableAdjustmentsElementsMapper.updateReasonableAdjustmentsElementsForCaseData;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataRespondentDetailsElementsMapper.updateRespondentDetailsElementsForCaseData;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataSafetyConcernsElementsMapper.updateSafetyConcernsElementsForCaseData;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataTypeOfOrderElementsMapper.updateTypeOfOrderElementsForCaseData;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataUrgencyElementsMapper.updateUrgencyElementsForCaseData;
import static uk.gov.hmcts.reform.prl.utils.CommonUtils.getPartyResponse;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeList;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CitizenPartyDetailsMapper {
    private final NoticeOfChangePartiesService noticeOfChangePartiesService;
    private final C100RespondentSolicitorService c100RespondentSolicitorService;
    private final UpdatePartyDetailsService updatePartyDetailsService;
    private final ObjectMapper objectMapper;
    private final CitizenRespondentAohElementsMapper citizenAllegationOfHarmMapper;
    private final ConfidentialityTabService confidentialityTabService;
    private final ConfidentialityC8RefugeService confidentialityC8RefugeService;

    public CitizenUpdatePartyDataContent mapUpdatedPartyDetails(CaseData dbCaseData,
                                                                CitizenUpdatedCaseData citizenUpdatedCaseData,
                                                                CaseEvent caseEvent,
                                                                String authorisation) {
        Optional<CitizenUpdatePartyDataContent> citizenUpdatePartyDataContent;
        if (C100_CASE_TYPE.equalsIgnoreCase(citizenUpdatedCaseData.getCaseTypeOfApplication())) {
            citizenUpdatePartyDataContent = Optional.ofNullable(updatingPartyDetailsCa(
                dbCaseData,
                citizenUpdatedCaseData,
                caseEvent,
                authorisation
            ));
        } else {
            citizenUpdatePartyDataContent = Optional.ofNullable(updatingPartyDetailsDa(
                dbCaseData,
                citizenUpdatedCaseData,
                caseEvent,
                authorisation
            ));
        }

        if (citizenUpdatePartyDataContent.isPresent()) {
            if (CONFIRM_YOUR_DETAILS.equals(caseEvent)) {
                generateAnswersForNoc(citizenUpdatePartyDataContent.get(), citizenUpdatedCaseData.getPartyType());
                //check if anything needs to do for citizen flags like RA amend journey
                findAndProcessRefugeFiles(dbCaseData, citizenUpdatedCaseData, citizenUpdatePartyDataContent.get());
            }
        } else {
            throw new CoreCaseDataStoreException("Citizen party update failed for this transaction");
        }
        return citizenUpdatePartyDataContent.get();
    }

    private void findAndProcessRefugeFiles(CaseData dbCaseData,
                                           CitizenUpdatedCaseData citizenUpdatedCaseData,
                                           CitizenUpdatePartyDataContent citizenUpdatePartyDataContent) {
        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord = null;
        if (C100_CASE_TYPE.equalsIgnoreCase(citizenUpdatedCaseData.getCaseTypeOfApplication())
            && PartyEnum.applicant.equals(citizenUpdatedCaseData.getPartyType())) {
            refugeConfidentialDocumentsRecord
                = confidentialityC8RefugeService.processC8RefugeDocumentsOnAmendForC100(
                dbCaseData,
                citizenUpdatePartyDataContent.updatedCaseData(),
                CaseEvent.AMEND_APPLICANTS_DETAILS.getValue()
            );
        } else if (C100_CASE_TYPE.equalsIgnoreCase(citizenUpdatedCaseData.getCaseTypeOfApplication())
            && PartyEnum.respondent.equals(citizenUpdatedCaseData.getPartyType())) {
            refugeConfidentialDocumentsRecord
                = confidentialityC8RefugeService.processC8RefugeDocumentsOnAmendForC100(
                dbCaseData,
                citizenUpdatePartyDataContent.updatedCaseData(),
                CaseEvent.AMEND_RESPONDENTS_DETAILS.getValue()
            );
        } else if (FL401_CASE_TYPE.equalsIgnoreCase(citizenUpdatedCaseData.getCaseTypeOfApplication())
            && PartyEnum.applicant.equals(citizenUpdatedCaseData.getPartyType())) {
            refugeConfidentialDocumentsRecord
                = confidentialityC8RefugeService.processC8RefugeDocumentsOnAmendForFL401(
                dbCaseData,
                citizenUpdatePartyDataContent.updatedCaseData(),
                CaseEvent.AMEND_APPLICANTS_DETAILS.getValue()
            );
        } else if (FL401_CASE_TYPE.equalsIgnoreCase(citizenUpdatedCaseData.getCaseTypeOfApplication())
            && PartyEnum.respondent.equals(citizenUpdatedCaseData.getPartyType())) {
            refugeConfidentialDocumentsRecord
                = confidentialityC8RefugeService.processC8RefugeDocumentsOnAmendForFL401(
                dbCaseData,
                citizenUpdatePartyDataContent.updatedCaseData(),
                CaseEvent.AMEND_RESPONDENTS_DETAILS.getValue()
            );
        }
        if (refugeConfidentialDocumentsRecord != null) {
            citizenUpdatePartyDataContent.updatedCaseDataMap().put(
                REFUGE_DOCUMENTS,
                refugeConfidentialDocumentsRecord.refugeDocuments()
            );
            citizenUpdatePartyDataContent.updatedCaseDataMap().put(
                HISTORICAL_REFUGE_DOCUMENTS,
                refugeConfidentialDocumentsRecord.historicalRefugeDocuments()
            );
        }
    }

    private void generateAnswersForNoc(CitizenUpdatePartyDataContent citizenUpdatePartyDataContent, PartyEnum partyType) {
        CaseData caseData = citizenUpdatePartyDataContent.updatedCaseData();
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            if (PartyEnum.respondent.equals(partyType)) {
                citizenUpdatePartyDataContent.updatedCaseDataMap().putAll(noticeOfChangePartiesService.generate(
                    caseData,
                    CARESPONDENT
                ));
            } else {
                citizenUpdatePartyDataContent.updatedCaseDataMap().putAll(noticeOfChangePartiesService.generate(
                    caseData,
                    CAAPPLICANT
                ));
            }
        } else {
            if (PartyEnum.respondent.equals(partyType)) {
                citizenUpdatePartyDataContent.updatedCaseDataMap().putAll(noticeOfChangePartiesService.generate(
                    caseData,
                    DARESPONDENT
                ));
            } else {
                citizenUpdatePartyDataContent.updatedCaseDataMap().putAll(noticeOfChangePartiesService.generate(
                    caseData,
                    DAAPPLICANT
                ));
            }
        }
    }

    private CitizenUpdatePartyDataContent updatingPartyDetailsCa(CaseData caseData,
                                                                 CitizenUpdatedCaseData citizenUpdatedCaseData,
                                                                 CaseEvent caseEvent,
                                                                 String authorisation) {
        Map<String, Object> caseDataMapToBeUpdated = new HashMap<>();
        if (PartyEnum.applicant.equals(citizenUpdatedCaseData.getPartyType())) {
            List<Element<ChildDetailsRevised>> childDetails = caseData.getNewChildDetails();// child details only
            List<Element<PartyDetails>> applicants = new ArrayList<>(caseData.getApplicants());
            applicants.stream()
                .filter(party -> Objects.equals(
                    party.getValue().getUser().getIdamId(),
                    citizenUpdatedCaseData.getPartyDetails().getUser().getIdamId()
                ))
                .findFirst()
                .ifPresent(party -> {
                    PartyDetails updatedPartyDetails = getUpdatedPartyDetailsBasedOnEvent(citizenUpdatedCaseData.getPartyDetails(),
                                                                                          party.getValue(),
                                                                                          caseEvent, childDetails);

                    applicants.set(applicants.indexOf(party), element(party.getId(), updatedPartyDetails));
                });
            caseData = caseData.toBuilder().applicants(applicants).build();
            caseDataMapToBeUpdated.put(C100_APPLICANTS, caseData.getApplicants());
            return new CitizenUpdatePartyDataContent(caseDataMapToBeUpdated, caseData);
        } else if (PartyEnum.respondent.equals(citizenUpdatedCaseData.getPartyType())) {
            List<Element<PartyDetails>> respondents = new ArrayList<>(caseData.getRespondents());
            CaseData oldCaseData = caseData;
            List<Element<ChildDetailsRevised>> childDetails = caseData.getNewChildDetails();
            respondents.stream()
                .filter(party -> Objects.equals(
                    party.getValue().getUser().getIdamId(),
                    citizenUpdatedCaseData.getPartyDetails().getUser().getIdamId()
                ))
                .findFirst()
                .ifPresent(party -> {
                    PartyDetails updatedPartyDetails = getUpdatedPartyDetailsBasedOnEvent(citizenUpdatedCaseData.getPartyDetails(),
                                                                                          party.getValue(),
                                                                                          caseEvent, childDetails);
                    Element<PartyDetails> updatedPartyElement = element(party.getId(), updatedPartyDetails);
                    int updatedRespondentPartyIndex = respondents.indexOf(party);
                    respondents.set(updatedRespondentPartyIndex, updatedPartyElement);

                    if (CONFIRM_YOUR_DETAILS.equals(caseEvent) || KEEP_DETAILS_PRIVATE.equals(caseEvent)) {
                        reGenerateRespondentC8Documents(caseDataMapToBeUpdated, updatedPartyElement,
                                                        oldCaseData, updatedRespondentPartyIndex, authorisation);
                    }
                });
            caseData = caseData.toBuilder().respondents(respondents).build();
            caseDataMapToBeUpdated.put(C100_RESPONDENTS, caseData.getRespondents());

            return new CitizenUpdatePartyDataContent(caseDataMapToBeUpdated, caseData);
        }
        return null;
    }

    private void reGenerateRespondentC8Documents(Map<String, Object> caseDataMapToBeUpdated,
                                                 Element<PartyDetails> updatedPartyElement,
                                                 CaseData oldCaseData,
                                                 int respondentIndex,
                                                 String authorisation) {
        Map<String, Object> dataMapForC8Document = new HashMap<>();
        dataMapForC8Document.put(COURT_NAME_FIELD, oldCaseData.getCourtName());
        dataMapForC8Document.put(CASE_DATA_ID, oldCaseData.getId());
        dataMapForC8Document.put(ISSUE_DATE_FIELD, oldCaseData.getIssueDate());
        dataMapForC8Document.put(COURT_SEAL_FIELD,
                                 oldCaseData.getCourtSeal() == null ? "[userImage:familycourtseal.png]"
                                     : oldCaseData.getCourtSeal());
        dataMapForC8Document.put(RESPONDENT, updatedPartyElement.getValue());
        if (oldCaseData.getTaskListVersion() != null
            && (TASK_LIST_VERSION_V2.equalsIgnoreCase(oldCaseData.getTaskListVersion())
                || TASK_LIST_VERSION_V3.equalsIgnoreCase(oldCaseData.getTaskListVersion()))) {
            List<Element<ChildDetailsRevised>> listOfChildren = oldCaseData.getNewChildDetails();
            dataMapForC8Document.put(CHILDREN, listOfChildren);

        } else {
            List<Element<Child>> listOfChildren = oldCaseData.getChildren();
            dataMapForC8Document.put(CHILDREN, listOfChildren);

        }
        c100RespondentSolicitorService.populateConfidentialAndMiscDataMap(updatedPartyElement, dataMapForC8Document,
                                                                          CITIZEN
        );

        try {
            updatePartyDetailsService.populateC8Documents(authorisation,
                                                          caseDataMapToBeUpdated, oldCaseData, dataMapForC8Document,
                                                          updatePartyDetailsService
                                                              .checkIfConfidentialityDetailsChangedRespondent(
                                                                  oldCaseData, updatedPartyElement
                                                              ),
                                                          respondentIndex, updatedPartyElement
            );
        } catch (Exception e) {
            log.error("Failed to generate C8 document for Case id - {}",
                      oldCaseData.getId()
            );
            throw new CoreCaseDataStoreException(e.getMessage(), e);
        }
    }

    private CitizenUpdatePartyDataContent updatingPartyDetailsDa(CaseData caseData,
                                                                 CitizenUpdatedCaseData citizenUpdatedCaseData,
                                                                 CaseEvent caseEvent,
                                                                 String authorisation) {
        PartyDetails partyDetails;
        Map<String, Object> caseDataMapToBeUpdated = new HashMap<>();
        if (PartyEnum.applicant.equals(citizenUpdatedCaseData.getPartyType())) {
            if (citizenUpdatedCaseData.getPartyDetails().getUser().getIdamId()
                .equalsIgnoreCase(caseData.getApplicantsFL401().getUser().getIdamId())) {
                partyDetails = getUpdatedPartyDetailsBasedOnEvent(
                    citizenUpdatedCaseData.getPartyDetails(),
                    caseData.getApplicantsFL401(),
                    caseEvent, caseData.getNewChildDetails()
                );
                caseData = caseData.toBuilder().applicantsFL401(partyDetails).build();
                caseDataMapToBeUpdated.put(FL401_APPLICANTS, caseData.getApplicantsFL401());
                return new CitizenUpdatePartyDataContent(caseDataMapToBeUpdated, caseData);
            }
        } else {
            if (citizenUpdatedCaseData.getPartyDetails().getUser().getIdamId()
                .equalsIgnoreCase(caseData.getRespondentsFL401().getUser().getIdamId())) {
                partyDetails = getUpdatedPartyDetailsBasedOnEvent(
                    citizenUpdatedCaseData.getPartyDetails(),
                    caseData.getRespondentsFL401(),
                    caseEvent, caseData.getNewChildDetails()
                );
                //PRL-6790 - create C8 for DA respondent
                if (CONFIRM_YOUR_DETAILS.equals(caseEvent) || KEEP_DETAILS_PRIVATE.equals(caseEvent)) {
                    reGenerateRespondentC8Documents(caseDataMapToBeUpdated,
                                                    element(partyDetails.getPartyId(), partyDetails),
                                                    caseData, 0, authorisation);
                }
                caseData = caseData.toBuilder().respondentsFL401(partyDetails).build();
                caseDataMapToBeUpdated.put(FL401_RESPONDENTS, caseData.getRespondentsFL401());
                return new CitizenUpdatePartyDataContent(caseDataMapToBeUpdated, caseData);
            }
        }
        return null;
    }

    public PartyDetails getUpdatedPartyDetailsBasedOnEvent(PartyDetails citizenProvidedPartyDetails,
                                                                   PartyDetails existingPartyDetails,
                                                                   CaseEvent caseEvent,
                                                           List<Element<ChildDetailsRevised>> childDetails) {
        switch (caseEvent) {
            case CONFIRM_YOUR_DETAILS -> {
                return updateCitizenPersonalDetails(
                    existingPartyDetails,
                    citizenProvidedPartyDetails
                );
            }
            case KEEP_DETAILS_PRIVATE -> {
                return updateCitizenConfidentialData(
                    existingPartyDetails,
                    citizenProvidedPartyDetails
                );
            }
            case CONSENT_TO_APPLICATION -> {
                return updateCitizenConsentDetails(
                    existingPartyDetails,
                    citizenProvidedPartyDetails
                );
            }
            case EVENT_RESPONDENT_MIAM -> {
                return updateCitizenMiamDetails(
                    existingPartyDetails,
                    citizenProvidedPartyDetails
                );
            }
            case LEGAL_REPRESENTATION -> {
                return updateCitizenLegalRepresentationDetails(
                    existingPartyDetails,
                    citizenProvidedPartyDetails
                );
            }
            case EVENT_RESPONDENT_AOH -> {
                return updateCitizenSafetyConcernDetails(
                    existingPartyDetails,
                    citizenProvidedPartyDetails, childDetails
                );
            }
            case EVENT_INTERNATIONAL_ELEMENT -> {
                return updateCitizenInternationalElementDetails(
                    existingPartyDetails,
                    citizenProvidedPartyDetails
                );
            }
            case CITIZEN_REMOVE_LEGAL_REPRESENTATIVE -> {
                return updateCitizenRemoveLegalRepresentativeFlag(
                    existingPartyDetails,
                    citizenProvidedPartyDetails
                );
            }
            case SUPPORT_YOU_DURING_CASE -> {
                return updateCitizenHearingNeedsDetails(
                    existingPartyDetails,
                    citizenProvidedPartyDetails
                );
            }
            case CITIZEN_CONTACT_PREFERENCE -> {
                return updateCitizenContactPreferenceDetails(
                    existingPartyDetails,
                    citizenProvidedPartyDetails
                );
            }
            case CITIZEN_INTERNAL_FLAG_UPDATES -> {
                return updateCitizenResponseDataForFlagUpdates(
                    existingPartyDetails,
                    citizenProvidedPartyDetails
                );
            }
            case CITIZEN_PCQ_UPDATE -> {
                return updatePcqIdForParty(
                    existingPartyDetails,
                    citizenProvidedPartyDetails
                );
            }
            case CITIZEN_CURRENT_OR_PREVIOUS_PROCCEDINGS -> {
                // For citizen-case-update - currentOrPreviousProceedings
                return updateCitizenResponseForProceedings(
                    existingPartyDetails,
                    citizenProvidedPartyDetails
                );
            }
            case REVIEW_AND_SUBMIT -> {
                return updateCitizenC7Response(existingPartyDetails, citizenProvidedPartyDetails);
            }
            case CITIZEN_RESPONSE_TO_AOH -> {
                return updateCitizenResponseToAohDetails(
                    existingPartyDetails,
                    citizenProvidedPartyDetails
                );
            }
            default -> {
                //return existing party details - no event
                return existingPartyDetails;
            }
        }
    }

    private PartyDetails updateCitizenResponseToAohDetails(PartyDetails existingPartyDetails, PartyDetails citizenProvidedPartyDetails) {
        return existingPartyDetails.toBuilder()
            .response(getPartyResponse(existingPartyDetails)
                          .toBuilder()
                          .responseToAllegationsOfHarm(citizenProvidedPartyDetails.getResponse()
                                                           .getResponseToAllegationsOfHarm().toBuilder()
                                                           .responseToAllegationsOfHarmYesOrNoResponse(
                                                               citizenProvidedPartyDetails.getResponse()
                                                                   .getResponseToAllegationsOfHarm().getResponseToAllegationsOfHarmYesOrNoResponse())
                                                           .respondentResponseToAllegationOfHarm(YesOrNo.Yes.equals(
                                                               citizenProvidedPartyDetails.getResponse()
                                                                   .getResponseToAllegationsOfHarm().getResponseToAllegationsOfHarmYesOrNoResponse())
                                                                                                     ? citizenProvidedPartyDetails.getResponse()
                                                               .getResponseToAllegationsOfHarm().getRespondentResponseToAllegationOfHarm() : null)
                                                           .build())
                          .build())
            .build();
    }

    private PartyDetails updateCitizenContactPreferenceDetails(PartyDetails existingPartyDetails, PartyDetails citizenProvidedPartyDetails) {
        //Need to revisit later
        return existingPartyDetails.toBuilder()
            .contactPreferences(isNotEmpty(citizenProvidedPartyDetails.getContactPreferences())
            ? citizenProvidedPartyDetails.getContactPreferences() : existingPartyDetails.getContactPreferences())
            .build();
    }

    private PartyDetails updateCitizenHearingNeedsDetails(PartyDetails existingPartyDetails, PartyDetails citizenProvidedPartyDetails) {
        return existingPartyDetails.toBuilder()
            .response(getPartyResponse(existingPartyDetails)
                          .toBuilder()
                          .supportYouNeed(citizenProvidedPartyDetails.getResponse().getSupportYouNeed())
                          .build())
            .build();
    }

    private PartyDetails updateCitizenRemoveLegalRepresentativeFlag(PartyDetails existingPartyDetails, PartyDetails citizenProvidedPartyDetails) {
        return existingPartyDetails.toBuilder()
            .isRemoveLegalRepresentativeRequested(citizenProvidedPartyDetails.getIsRemoveLegalRepresentativeRequested())
            .build();
    }

    private PartyDetails updatePcqIdForParty(PartyDetails existingPartyDetails, PartyDetails citizenProvidedPartyDetails) {
        User user = ObjectUtils.isNotEmpty(existingPartyDetails.getUser())
            ? existingPartyDetails.getUser().toBuilder().pcqId(citizenProvidedPartyDetails.getUser().getPcqId()).build()
            : User.builder().pcqId(citizenProvidedPartyDetails.getUser().getPcqId()).build();
        return existingPartyDetails.toBuilder()
            .user(user)
            .build();
    }

    private PartyDetails updateCitizenInternationalElementDetails(PartyDetails existingPartyDetails, PartyDetails citizenProvidedPartyDetails) {
        return existingPartyDetails.toBuilder()
            .response(getPartyResponse(existingPartyDetails)
                          .toBuilder()
                          .citizenInternationalElements(citizenProvidedPartyDetails.getResponse().getCitizenInternationalElements())
                          .build())
            .build();
    }

    private PartyDetails updateCitizenSafetyConcernDetails(PartyDetails existingPartyDetails, PartyDetails citizenProvidedPartyDetails,
                                                           List<Element<ChildDetailsRevised>> childDetails) {
        return existingPartyDetails.toBuilder()
            .response(getPartyResponse(existingPartyDetails)
                          .toBuilder()
                          .respondingCitizenAoH(citizenProvidedPartyDetails.getResponse().getRespondingCitizenAoH())
                          .respondentAllegationsOfHarmData(citizenAllegationOfHarmMapper
                                                               .map(citizenProvidedPartyDetails.getResponse()
                                                                        .getRespondingCitizenAoH(), childDetails))
                          .build())
            .build();
    }

    private PartyDetails updateCitizenLegalRepresentationDetails(PartyDetails existingPartyDetails, PartyDetails citizenProvidedPartyDetails) {
        return existingPartyDetails.toBuilder()
            .response(getPartyResponse(existingPartyDetails)
                          .toBuilder()
                          .legalRepresentation(citizenProvidedPartyDetails.getResponse().getLegalRepresentation())
                          .build())
            .build();
    }

    private PartyDetails updateCitizenMiamDetails(PartyDetails existingPartyDetails, PartyDetails citizenProvidedPartyDetails) {
        return existingPartyDetails.toBuilder()
            .response(getPartyResponse(existingPartyDetails)
                          .toBuilder()
                          .miam(citizenProvidedPartyDetails.getResponse().getMiam())
                          .build())
            .build();
    }

    private PartyDetails updateCitizenConsentDetails(PartyDetails existingPartyDetails, PartyDetails citizenProvidedPartyDetails) {
        return existingPartyDetails.toBuilder()
            .response(getPartyResponse(existingPartyDetails)
                          .toBuilder()
                          .consent(citizenProvidedPartyDetails.getResponse().getConsent())
                          .build())
            .build();
    }

    private PartyDetails updateCitizenResponseForProceedings(PartyDetails existingPartyDetails, PartyDetails citizenProvidedPartyDetails) {
        return existingPartyDetails.toBuilder()
            .response(getPartyResponse(existingPartyDetails)
                          .toBuilder()
                          .currentOrPreviousProceedings(isNotEmpty(citizenProvidedPartyDetails.getResponse().getCurrentOrPreviousProceedings())
                                                            ? citizenProvidedPartyDetails.getResponse().getCurrentOrPreviousProceedings()
                                                            : existingPartyDetails.getResponse().getCurrentOrPreviousProceedings())
                          .build())
            .build();
    }

    private PartyDetails updateCitizenResponseDataForFlagUpdates(PartyDetails existingPartyDetails, PartyDetails citizenProvidedPartyDetails) {
        boolean isCitizenFlagsPresent = isNotEmpty(citizenProvidedPartyDetails.getResponse().getCitizenFlags());
        return existingPartyDetails.toBuilder()
            .response(getPartyResponse(existingPartyDetails)
                          .toBuilder()
                          .citizenFlags(isCitizenFlagsPresent
                                            ? updateCitizenFlags(
                                            existingPartyDetails.getResponse().getCitizenFlags(),
                                            citizenProvidedPartyDetails.getResponse().getCitizenFlags()
                                        )
                                            : existingPartyDetails.getResponse().getCitizenFlags()
                          )
                          .build())
            .build();
    }

    private CitizenFlags updateCitizenFlags(CitizenFlags existingCitizenFlags, CitizenFlags updatedCitizenFlags) {
        return existingCitizenFlags.toBuilder()
            .isApplicationViewed(isNotEmpty(updatedCitizenFlags.getIsApplicationViewed())
                                     ? updatedCitizenFlags.getIsApplicationViewed()
                                     : existingCitizenFlags.getIsApplicationViewed())
            .isAllDocumentsViewed(isNotEmpty(updatedCitizenFlags.getIsAllDocumentsViewed())
                                      ? updatedCitizenFlags.getIsAllDocumentsViewed()
                                      : existingCitizenFlags.getIsAllDocumentsViewed())
            .isAllegationOfHarmViewed(isNotEmpty(updatedCitizenFlags.getIsAllegationOfHarmViewed())
                                          ? updatedCitizenFlags.getIsAllegationOfHarmViewed()
                                          : existingCitizenFlags.getIsAllegationOfHarmViewed())
            .isResponseInitiated(isNotEmpty(updatedCitizenFlags.getIsResponseInitiated())
                                     ? updatedCitizenFlags.getIsResponseInitiated()
                                     : existingCitizenFlags.getIsResponseInitiated())
            .isApplicationToBeServed(isNotEmpty(updatedCitizenFlags.getIsApplicationToBeServed())
                                         ? updatedCitizenFlags.getIsApplicationToBeServed()
                                         : existingCitizenFlags.getIsApplicationToBeServed())
            .isStatementOfServiceProvided(isNotEmpty(updatedCitizenFlags.getIsStatementOfServiceProvided())
                                              ? updatedCitizenFlags.getIsStatementOfServiceProvided()
                                              : existingCitizenFlags.getIsStatementOfServiceProvided())
            .build();
    }

    private PartyDetails updateCitizenPersonalDetails(PartyDetails existingPartyDetails, PartyDetails citizenProvidedPartyDetails) {
        boolean isAddressNeedsToUpdate = isNotEmpty(citizenProvidedPartyDetails.getAddress())
            && StringUtils.isNotEmpty(citizenProvidedPartyDetails.getAddress().getAddressLine1());

        boolean isEmailNeedsToUpdate = StringUtils.isNotEmpty(citizenProvidedPartyDetails.getEmail());

        boolean isPhoneNoNeedsToUpdate = StringUtils.isNotEmpty(citizenProvidedPartyDetails.getPhoneNumber());

        boolean isDateOfBirthNeedsToUpdate = isNotEmpty(citizenProvidedPartyDetails.getDateOfBirth());

        boolean isPlaceOfBirthNeedsToUpdate = StringUtils.isNotEmpty(citizenProvidedPartyDetails.getPlaceOfBirth());

        boolean livingInRefuge = isNotEmpty(citizenProvidedPartyDetails.getLiveInRefuge());

        existingPartyDetails = forceConfidentiality(existingPartyDetails, citizenProvidedPartyDetails);

        return existingPartyDetails.toBuilder()
            .canYouProvideEmailAddress(isEmailNeedsToUpdate ? YesOrNo.Yes : existingPartyDetails.getCanYouProvideEmailAddress())
            .email(isEmailNeedsToUpdate
                       ? citizenProvidedPartyDetails.getEmail() : existingPartyDetails.getEmail())
            .canYouProvidePhoneNumber(isPhoneNoNeedsToUpdate ? YesOrNo.Yes : existingPartyDetails.getCanYouProvidePhoneNumber())
            .phoneNumber(isPhoneNoNeedsToUpdate
                             ? citizenProvidedPartyDetails.getPhoneNumber() : existingPartyDetails.getPhoneNumber())
            .isAtAddressLessThan5Years(null != citizenProvidedPartyDetails.getIsAtAddressLessThan5Years()
                ? mapApplicantHaveYouLivedAtThisAddressForLessThanFiveYears(citizenProvidedPartyDetails)
                : existingPartyDetails.getIsAtAddressLessThan5Years())
            .isAtAddressLessThan5YearsWithDontKnow(null != citizenProvidedPartyDetails.getIsAtAddressLessThan5YearsWithDontKnow()
                ? mapRespondentHaveYouLivedAtThisAddressForLessThanFiveYears(citizenProvidedPartyDetails)
                : existingPartyDetails.getIsAtAddressLessThan5YearsWithDontKnow())
            .isCurrentAddressKnown(isAddressNeedsToUpdate ? citizenProvidedPartyDetails.getIsCurrentAddressKnown()
                                       : existingPartyDetails.getIsCurrentAddressKnown())
            .address(isAddressNeedsToUpdate ? citizenProvidedPartyDetails.getAddress() : existingPartyDetails.getAddress())
            .addressLivedLessThan5YearsDetails(StringUtils.isNotEmpty(citizenProvidedPartyDetails.getAddressLivedLessThan5YearsDetails())
                                                   ? citizenProvidedPartyDetails.getAddressLivedLessThan5YearsDetails()
                                                   : existingPartyDetails.getAddressLivedLessThan5YearsDetails())
            .firstName(citizenProvidedPartyDetails.getFirstName())
            .lastName(citizenProvidedPartyDetails.getLastName())
            .previousName(StringUtils.isNotEmpty(citizenProvidedPartyDetails.getPreviousName())
                              ? citizenProvidedPartyDetails.getPreviousName() : existingPartyDetails.getPreviousName())
            .dateOfBirth(isDateOfBirthNeedsToUpdate
                             ? citizenProvidedPartyDetails.getDateOfBirth() : existingPartyDetails.getDateOfBirth())
            .isDateOfBirthKnown(isDateOfBirthNeedsToUpdate
                                    ? YesOrNo.Yes : existingPartyDetails.getIsDateOfBirthKnown())
            .placeOfBirth(isNotEmpty(citizenProvidedPartyDetails.getPlaceOfBirth())
                              ? citizenProvidedPartyDetails.getPlaceOfBirth() : existingPartyDetails.getPlaceOfBirth())
            .liveInRefuge(livingInRefuge ? citizenProvidedPartyDetails.getLiveInRefuge() : existingPartyDetails.getLiveInRefuge())
            .refugeConfidentialityC8Form(citizenProvidedPartyDetails.getRefugeConfidentialityC8Form())
            .isPlaceOfBirthKnown(isPlaceOfBirthNeedsToUpdate
                                     ? YesOrNo.Yes : existingPartyDetails.getIsPlaceOfBirthKnown())
            .response(getPartyResponse(existingPartyDetails).toBuilder()
                          .citizenDetails(mapResponseCitizenDetails(citizenProvidedPartyDetails))
                          .safeToCallOption(fetchSafeToCallOption(citizenProvidedPartyDetails))
                          .build())
            .build();
    }

    private PartyDetails forceConfidentiality(PartyDetails existingPartyDetails, PartyDetails citizenProvidedPartyDetails) {
        if (Yes.equals(citizenProvidedPartyDetails.getLiveInRefuge())) {
            existingPartyDetails = existingPartyDetails.toBuilder()
                .response(getPartyResponse(existingPartyDetails).toBuilder()
                              .keepDetailsPrivate(getPartyResponse(existingPartyDetails)
                                                      .getKeepDetailsPrivate()
                                                      .toBuilder()
                                                      .confidentiality(Yes)
                                                      .confidentialityList(
                                                          Arrays.asList(
                                                              ConfidentialityListEnum.email,
                                                              ConfidentialityListEnum.address,
                                                              ConfidentialityListEnum.phoneNumber
                                                          ))
                                                      .build())
                              .build())
                .isPhoneNumberConfidential(Yes)
                .isAddressConfidential(Yes)
                .isEmailAddressConfidential(Yes)
                .build();
        }
        return existingPartyDetails;
    }

    private String fetchSafeToCallOption(PartyDetails citizenProvidedPartyDetails) {
        return null != citizenProvidedPartyDetails.getResponse()
            ? citizenProvidedPartyDetails.getResponse().getSafeToCallOption()
            : null;
    }

    private YesOrNo mapApplicantHaveYouLivedAtThisAddressForLessThanFiveYears(PartyDetails citizenProvidedPartyDetails) {
        if (Yes.equals(citizenProvidedPartyDetails.getIsAtAddressLessThan5Years())) {
            return Yes;
        } else {
            return No;
        }
    }

    private YesNoDontKnow mapRespondentHaveYouLivedAtThisAddressForLessThanFiveYears(PartyDetails citizenProvidedPartyDetails) {
        if (Yes.equals(citizenProvidedPartyDetails.getIsAtAddressLessThan5Years())) {
            return YesNoDontKnow.yes;
        } else {
            return YesNoDontKnow.no;
        }
    }

    private CitizenDetails mapResponseCitizenDetails(PartyDetails citizenProvidedPartyDetails) {
        return CitizenDetails.builder()
            .firstName(citizenProvidedPartyDetails.getFirstName())
            .lastName(citizenProvidedPartyDetails.getLastName())
            .dateOfBirth(isNotEmpty(citizenProvidedPartyDetails.getDateOfBirth())
                             ? citizenProvidedPartyDetails.getDateOfBirth() : null)
            .placeOfBirth(StringUtils.isNotEmpty(citizenProvidedPartyDetails.getPlaceOfBirth())
                              ? citizenProvidedPartyDetails.getPlaceOfBirth() : null)
            .previousName(StringUtils.isNotEmpty(citizenProvidedPartyDetails.getPreviousName())
                              ? citizenProvidedPartyDetails.getPreviousName() : null)
            .contact(Contact.builder()
                         .email(StringUtils.isNotEmpty(citizenProvidedPartyDetails.getEmail())
                             ? citizenProvidedPartyDetails.getEmail() : null)
                         .phoneNumber(StringUtils.isNotEmpty(citizenProvidedPartyDetails.getPhoneNumber())
                                          ? citizenProvidedPartyDetails.getPhoneNumber() : null)
                         .build())
            .address(isNotEmpty(citizenProvidedPartyDetails.getAddress())
                         && StringUtils.isNotEmpty(citizenProvidedPartyDetails.getAddress().getAddressLine1())
                         ? citizenProvidedPartyDetails.getAddress() : null)
            //.addressHistory(AddressHistory.builder().build())
            .build();
    }

    private PartyDetails updateCitizenConfidentialData(PartyDetails existingPartyDetails, PartyDetails citizenProvidedPartyDetails) {
        if (YesOrNo.Yes.equals(citizenProvidedPartyDetails.getLiveInRefuge())
            && null != citizenProvidedPartyDetails.getResponse()
            && null != citizenProvidedPartyDetails.getResponse().getKeepDetailsPrivate()) {
            return existingPartyDetails.toBuilder()
                .response(getPartyResponse(existingPartyDetails).toBuilder()
                              .keepDetailsPrivate(getPartyResponse(existingPartyDetails)
                                                      .getKeepDetailsPrivate()
                                                      .toBuilder()
                                                      .otherPeopleKnowYourContactDetails(citizenProvidedPartyDetails
                                                                                             .getResponse()
                                                                                             .getKeepDetailsPrivate()
                                                                                             .getOtherPeopleKnowYourContactDetails())
                                                      .confidentiality(Yes)
                                                      .confidentialityList(
                                                          Arrays.asList(
                                                              ConfidentialityListEnum.email,
                                                              ConfidentialityListEnum.address,
                                                              ConfidentialityListEnum.phoneNumber
                                                          ))
                                                      .build())
                              .build())
                .build();
        }

        if (null != citizenProvidedPartyDetails.getResponse()
            && null != citizenProvidedPartyDetails.getResponse().getKeepDetailsPrivate()
            && Yes.equals(citizenProvidedPartyDetails.getResponse().getKeepDetailsPrivate().getConfidentiality())
            && null != citizenProvidedPartyDetails.getResponse().getKeepDetailsPrivate().getConfidentialityList()) {
            return existingPartyDetails.toBuilder()
                .response(getPartyResponse(existingPartyDetails).toBuilder()
                              .keepDetailsPrivate(citizenProvidedPartyDetails.getResponse().getKeepDetailsPrivate())
                              .build())
                .isPhoneNumberConfidential(
                    citizenProvidedPartyDetails.getResponse().getKeepDetailsPrivate().getConfidentialityList().contains(
                        ConfidentialityListEnum.phoneNumber) ? Yes : No)
                .isAddressConfidential(citizenProvidedPartyDetails.getResponse().getKeepDetailsPrivate().getConfidentialityList().contains(
                    ConfidentialityListEnum.address) ? Yes : No)
                .isEmailAddressConfidential(citizenProvidedPartyDetails.getResponse().getKeepDetailsPrivate().getConfidentialityList().contains(
                    ConfidentialityListEnum.email) ? Yes : No).build();
        } else {
            return existingPartyDetails.toBuilder()
                .response(getPartyResponse(existingPartyDetails).toBuilder()
                              .keepDetailsPrivate(citizenProvidedPartyDetails.getResponse().getKeepDetailsPrivate())
                              .build())
                .isPhoneNumberConfidential(No)
                .isAddressConfidential(No)
                .isEmailAddressConfidential(No).build();
        }
    }

    public Map<String, Object> getC100RebuildCaseDataMap(CaseData citizenUpdatedCaseData) throws JsonProcessingException {
        Map<String, Object> caseDataMapToBeUpdated = new HashMap<>();
        if (citizenUpdatedCaseData != null) {
            caseDataMapToBeUpdated.put(
                "c100RebuildInternationalElements",
                citizenUpdatedCaseData.getC100RebuildData().getC100RebuildInternationalElements()
            );
            caseDataMapToBeUpdated.put(
                "c100RebuildReasonableAdjustments",
                citizenUpdatedCaseData.getC100RebuildData().getC100RebuildReasonableAdjustments()
            );
            caseDataMapToBeUpdated.put(
                "c100RebuildTypeOfOrder",
                citizenUpdatedCaseData.getC100RebuildData().getC100RebuildTypeOfOrder()
            );
            caseDataMapToBeUpdated.put(
                "c100RebuildHearingWithoutNotice",
                citizenUpdatedCaseData.getC100RebuildData().getC100RebuildHearingWithoutNotice()
            );
            caseDataMapToBeUpdated.put(
                "c100RebuildHearingUrgency",
                citizenUpdatedCaseData.getC100RebuildData().getC100RebuildHearingUrgency()
            );
            caseDataMapToBeUpdated.put(
                "c100RebuildOtherProceedings",
                citizenUpdatedCaseData.getC100RebuildData().getC100RebuildOtherProceedings()
            );
            caseDataMapToBeUpdated.put(
                "c100RebuildReturnUrl",
                citizenUpdatedCaseData.getC100RebuildData().getC100RebuildReturnUrl()
            );
            caseDataMapToBeUpdated.put(
                "c100RebuildMaim",
                citizenUpdatedCaseData.getC100RebuildData().getC100RebuildMaim()
            );
            caseDataMapToBeUpdated.put(
                "c100RebuildChildDetails",
                citizenUpdatedCaseData.getC100RebuildData().getC100RebuildChildDetails()
            );
            caseDataMapToBeUpdated.put(
                "c100RebuildApplicantDetails",
                citizenUpdatedCaseData.getC100RebuildData().getC100RebuildApplicantDetails()
            );
            caseDataMapToBeUpdated.put(
                "c100RebuildOtherChildrenDetails",
                citizenUpdatedCaseData.getC100RebuildData().getC100RebuildOtherChildrenDetails()
            );
            caseDataMapToBeUpdated.put(
                "c100RebuildRespondentDetails",
                citizenUpdatedCaseData.getC100RebuildData().getC100RebuildRespondentDetails()
            );
            caseDataMapToBeUpdated.put(
                "c100RebuildOtherPersonsDetails",
                citizenUpdatedCaseData.getC100RebuildData().getC100RebuildOtherPersonsDetails()
            );
            caseDataMapToBeUpdated.put(
                "c100RebuildSafetyConcerns",
                citizenUpdatedCaseData.getC100RebuildData().getC100RebuildSafetyConcerns()
            );
            caseDataMapToBeUpdated.put(
                "c100RebuildScreeningQuestions",
                citizenUpdatedCaseData.getC100RebuildData().getC100RebuildScreeningQuestions()
            );
            caseDataMapToBeUpdated.put(
                "c100RebuildHelpWithFeesDetails",
                citizenUpdatedCaseData.getC100RebuildData().getC100RebuildHelpWithFeesDetails()
            );
            caseDataMapToBeUpdated.put(
                "c100RebuildStatementOfTruth",
                citizenUpdatedCaseData.getC100RebuildData().getC100RebuildStatementOfTruth()
            );
            caseDataMapToBeUpdated.put(
                "helpWithFeesReferenceNumber",
                citizenUpdatedCaseData.getC100RebuildData().getHelpWithFeesReferenceNumber()
            );
            caseDataMapToBeUpdated.put(
                "c100RebuildChildPostCode",
                citizenUpdatedCaseData.getC100RebuildData().getC100RebuildChildPostCode()
            );
            caseDataMapToBeUpdated.put(
                "c100RebuildConsentOrderDetails",
                citizenUpdatedCaseData.getC100RebuildData().getC100RebuildConsentOrderDetails()
            );
            caseDataMapToBeUpdated.put(
                "applicantPcqId",
                citizenUpdatedCaseData.getC100RebuildData().getApplicantPcqId()
            );
            caseDataMapToBeUpdated.put(
                "applicantCaseName",
                buildApplicantAndRespondentForCaseName(citizenUpdatedCaseData.getC100RebuildData())
            );
        }
        return caseDataMapToBeUpdated;
    }

    public CaseData buildUpdatedCaseData(CaseData caseData, C100RebuildData c100RebuildData) throws JsonProcessingException {
        C100RebuildChildDetailsElements c100RebuildChildDetailsElements = null;
        ObjectMapper mapper = new ObjectMapper();
        CaseData.CaseDataBuilder<?,?> caseDataBuilder = caseData.toBuilder();

        if (StringUtils.isNotEmpty(c100RebuildData.getC100RebuildInternationalElements())) {
            C100RebuildInternationalElements c100RebuildInternationalElements = mapper
                .readValue(c100RebuildData.getC100RebuildInternationalElements(), C100RebuildInternationalElements.class);
            updateInternationalElementsForCaseData(caseDataBuilder, c100RebuildInternationalElements);
        }

        if (StringUtils.isNotEmpty(c100RebuildData.getC100RebuildHearingWithoutNotice())) {
            C100RebuildHearingWithoutNoticeElements c100RebuildHearingWithoutNoticeElements = mapper
                .readValue(c100RebuildData.getC100RebuildHearingWithoutNotice(), C100RebuildHearingWithoutNoticeElements.class);
            updateHearingWithoutNoticeElementsForCaseData(caseDataBuilder, c100RebuildHearingWithoutNoticeElements);
        }

        if (StringUtils.isNotEmpty(c100RebuildData.getC100RebuildTypeOfOrder())) {
            C100RebuildCourtOrderElements c100RebuildCourtOrderElements = mapper
                .readValue(c100RebuildData.getC100RebuildTypeOfOrder(), C100RebuildCourtOrderElements.class);
            updateTypeOfOrderElementsForCaseData(caseDataBuilder, c100RebuildCourtOrderElements);
        }

        if (StringUtils.isNotEmpty(c100RebuildData.getC100RebuildOtherProceedings())) {
            C100RebuildOtherProceedingsElements c100RebuildOtherProceedingsElements = mapper
                .readValue(c100RebuildData.getC100RebuildOtherProceedings(), C100RebuildOtherProceedingsElements.class);
            updateOtherProceedingsElementsForCaseData(caseDataBuilder, c100RebuildOtherProceedingsElements);
        }

        if (StringUtils.isNotEmpty(c100RebuildData.getC100RebuildHearingUrgency())) {
            C100RebuildUrgencyElements c100RebuildUrgencyElements = mapper
                .readValue(c100RebuildData.getC100RebuildHearingUrgency(), C100RebuildUrgencyElements.class);
            updateUrgencyElementsForCaseData(caseDataBuilder, c100RebuildUrgencyElements);
        }

        if (StringUtils.isNotEmpty(c100RebuildData.getC100RebuildMaim())) {
            C100RebuildMiamElements c100RebuildMiamElements = mapper
                .readValue(c100RebuildData.getC100RebuildMaim(), C100RebuildMiamElements.class);
            updateMiamElementsForCaseData(caseDataBuilder, c100RebuildMiamElements);
        }

        if (StringUtils.isNotEmpty(c100RebuildData.getC100RebuildChildDetails())) {
            c100RebuildChildDetailsElements = mapper
                .readValue(c100RebuildData.getC100RebuildChildDetails(), C100RebuildChildDetailsElements.class);

            updateChildDetailsElementsForCaseData(caseDataBuilder, c100RebuildChildDetailsElements);
        }

        if (StringUtils.isNotEmpty(c100RebuildData.getC100RebuildApplicantDetails())) {
            C100RebuildApplicantDetailsElements c100RebuildApplicantDetailsElements = mapper
                .readValue(c100RebuildData.getC100RebuildApplicantDetails(), C100RebuildApplicantDetailsElements.class);
            updateApplicantElementsForCaseData(caseDataBuilder, c100RebuildApplicantDetailsElements, c100RebuildChildDetailsElements,
                                               c100RebuildData.getApplicantPcqId());
        }

        if (StringUtils.isNotEmpty(c100RebuildData.getC100RebuildRespondentDetails())) {
            C100RebuildRespondentDetailsElements c100RebuildRespondentDetailsElements = mapper
                .readValue(c100RebuildData.getC100RebuildRespondentDetails(), C100RebuildRespondentDetailsElements.class);
            updateRespondentDetailsElementsForCaseData(caseDataBuilder, c100RebuildRespondentDetailsElements, c100RebuildChildDetailsElements);
        }

        if (StringUtils.isNotEmpty(c100RebuildData.getC100RebuildOtherPersonsDetails())) {
            C100RebuildOtherPersonDetailsElements c100RebuildOtherPersonDetailsElements = mapper
                .readValue(c100RebuildData.getC100RebuildOtherPersonsDetails(), C100RebuildOtherPersonDetailsElements.class);
            updateOtherPersonDetailsElementsForCaseData(caseDataBuilder,
                                                        c100RebuildOtherPersonDetailsElements, c100RebuildChildDetailsElements);
            caseDataBuilder.otherPartyInTheCaseRevised(confidentialityTabService.updateOtherPeopleConfidentiality(
                caseDataBuilder.build().getRelations().getChildAndOtherPeopleRelations(),
                caseDataBuilder.build().getOtherPartyInTheCaseRevised()
            ));
        }

        if (StringUtils.isNotEmpty(c100RebuildData.getC100RebuildOtherChildrenDetails())) {
            C100RebuildOtherChildrenDetailsElements c100RebuildOtherChildrenDetailsElements = mapper
                .readValue(c100RebuildData.getC100RebuildOtherChildrenDetails(), C100RebuildOtherChildrenDetailsElements.class);
            updateOtherChildDetailsElementsForCaseData(caseDataBuilder, c100RebuildOtherChildrenDetailsElements);
        }

        if (StringUtils.isNotEmpty(c100RebuildData.getC100RebuildReasonableAdjustments())) {
            C100RebuildReasonableAdjustmentsElements c100RebuildReasonableAdjustmentsElements = mapper
                .readValue(c100RebuildData.getC100RebuildReasonableAdjustments(), C100RebuildReasonableAdjustmentsElements.class);
            updateReasonableAdjustmentsElementsForCaseData(caseDataBuilder, c100RebuildReasonableAdjustmentsElements);
        }

        if (StringUtils.isNotEmpty(c100RebuildData.getC100RebuildConsentOrderDetails())) {
            C100RebuildConsentOrderDetails c100RebuildConsentOrderDetails = mapper
                .readValue(c100RebuildData.getC100RebuildConsentOrderDetails(), C100RebuildConsentOrderDetails.class);
            updateConsentOrderDetailsForCaseData(caseDataBuilder, c100RebuildConsentOrderDetails);
        }

        if (StringUtils.isNotEmpty(c100RebuildData.getC100RebuildSafetyConcerns())) {
            mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
            C100RebuildSafetyConcernsElements c100C100RebuildSafetyConcernsElements = mapper
                .readValue(c100RebuildData.getC100RebuildSafetyConcerns(), C100RebuildSafetyConcernsElements.class);
            updateSafetyConcernsElementsForCaseData(caseDataBuilder,
                                                    c100C100RebuildSafetyConcernsElements,
                                                    c100RebuildChildDetailsElements);
        }

        updateHelpWithFeesDetailsForCaseData(caseDataBuilder, c100RebuildData);

        caseDataBuilder.applicantCaseName(buildApplicantAndRespondentForCaseName(c100RebuildData));
        caseDataBuilder.caseAccessCategory("C100");
        //Set case type, applicant name & respondent names for case list table
        caseDataBuilder.selectedCaseTypeID(caseData.getCaseTypeOfApplication());
        caseDataBuilder.applicantName(getPartyName(caseDataBuilder.build().getApplicants()));
        caseDataBuilder.respondentName(getPartyName(caseDataBuilder.build().getRespondents()));

        return caseDataBuilder.build();
    }

    private String getPartyName(List<Element<PartyDetails>> parties) {
        return nullSafeList(parties).get(0).getValue().getLabelForDynamicList();
    }

    public String buildApplicantAndRespondentForCaseName(C100RebuildData c100RebuildData) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        C100RebuildApplicantDetailsElements c100RebuildApplicantDetailsElements = null;
        C100RebuildRespondentDetailsElements c100RebuildRespondentDetailsElements = null;
        if (null != c100RebuildData) {
            if (StringUtils.isNotEmpty(c100RebuildData.getC100RebuildApplicantDetails())) {
                c100RebuildApplicantDetailsElements = mapper
                    .readValue(c100RebuildData.getC100RebuildApplicantDetails(), C100RebuildApplicantDetailsElements.class);
            }

            if (StringUtils.isNotEmpty(c100RebuildData.getC100RebuildRespondentDetails())) {
                c100RebuildRespondentDetailsElements = mapper
                    .readValue(c100RebuildData.getC100RebuildRespondentDetails(), C100RebuildRespondentDetailsElements.class);
            }
        }
        return buildCaseName(c100RebuildApplicantDetailsElements, c100RebuildRespondentDetailsElements);
    }


    private String buildCaseName(C100RebuildApplicantDetailsElements c100RebuildApplicantDetailsElements,
                                 C100RebuildRespondentDetailsElements c100RebuildRespondentDetailsElements) {
        String caseName = null;
        if (null != c100RebuildApplicantDetailsElements
            && null != c100RebuildApplicantDetailsElements.getApplicants()
            && !c100RebuildApplicantDetailsElements.getApplicants().isEmpty()
            && null != c100RebuildApplicantDetailsElements.getApplicants().get(0)
            && null != c100RebuildRespondentDetailsElements
            && null != c100RebuildRespondentDetailsElements.getRespondentDetails()
            && !c100RebuildRespondentDetailsElements.getRespondentDetails().isEmpty()
            && null != c100RebuildRespondentDetailsElements.getRespondentDetails().get(0)) {
            caseName = c100RebuildApplicantDetailsElements.getApplicants().get(0).getApplicantLastName() + " V "
                + c100RebuildRespondentDetailsElements.getRespondentDetails().get(0).getLastName();
        }

        return caseName;
    }

    private PartyDetails updateCitizenC7Response(PartyDetails existingPartyDetails, PartyDetails citizenProvidedPartyDetails) {
        if (null != citizenProvidedPartyDetails.getResponse()) {
            return existingPartyDetails.toBuilder()
                    .response(getPartyResponse(existingPartyDetails).toBuilder()
                            .c7ResponseSubmitted(Yes)
                            .build())
                    .currentRespondent(null)
                    .build();
        }

        return existingPartyDetails;
    }
}
