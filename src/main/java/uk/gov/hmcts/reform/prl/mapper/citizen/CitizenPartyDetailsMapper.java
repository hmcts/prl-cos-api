package uk.gov.hmcts.reform.prl.mapper.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.records.CitizenUpdatePartyDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.citizen.ConfidentialityListEnum;
import uk.gov.hmcts.reform.prl.exception.CoreCaseDataStoreException;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.UpdateCaseData;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.CitizenDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.CitizenFlags;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.Contact;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.UpdatePartyDetailsService;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.C100RespondentSolicitorService;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_APPLICANTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_RESPONDENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_APPLICANTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_RESPONDENTS;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CONFIRM_YOUR_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.KEEP_DETAILS_PRIVATE;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.CAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.CARESPONDENT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.DAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.DARESPONDENT;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CitizenPartyDetailsMapper {
    private final NoticeOfChangePartiesService noticeOfChangePartiesService;
    private final ObjectMapper objectMapper;
    private final C100RespondentSolicitorService c100RespondentSolicitorService;

    private final UpdatePartyDetailsService updatePartyDetailsService;


    public CitizenUpdatePartyDataContent mapUpdatedPartyDetails(CaseData dbCaseData,
                                                                UpdateCaseData citizenUpdatedCaseData,
                                                                CaseEvent caseEvent,
                                                                String authorisation) {
        log.info("Start CitizenPartyDetailsMapper:mapUpdatedPartyDetails() for event " + caseEvent.getValue());
        log.info("Start CitizenPartyDetailsMapper:mapUpdatedPartyDetails() for party type:: " + citizenUpdatedCaseData.getPartyType());
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
                caseEvent
            ));
        }

        if (citizenUpdatePartyDataContent.isPresent()) {
            if (CONFIRM_YOUR_DETAILS.equals(caseEvent)) {
                generateAnswersForNoc(citizenUpdatePartyDataContent.get(), citizenUpdatedCaseData.getPartyType());
                //check if anything needs to do for citizen flags like RA amend journey
            }
            log.info("Updated caseDataMap =>" + citizenUpdatePartyDataContent.get().updatedCaseDataMap());
            log.info("Exit CitizenPartyDetailsMapper:mapUpdatedPartyDetails() for event " + caseEvent.getValue());
        } else {
            log.error("{} is not successful for the case {}", caseEvent.getValue(), dbCaseData.getId());
            throw new CoreCaseDataStoreException("Citizen party update failed for this transaction");
        }
        return citizenUpdatePartyDataContent.get();
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
                                                                 UpdateCaseData citizenUpdatedCaseData,
                                                                 CaseEvent caseEvent,
                                                                 String authorisation) {
        log.info("Inside updatingPartyDetailsCa");
        Map<String, Object> caseDataMapToBeUpdated = new HashMap<>();
        if (PartyEnum.applicant.equals(citizenUpdatedCaseData.getPartyType())) {
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
                                                                                          caseEvent);
                    applicants.set(applicants.indexOf(party), element(party.getId(), updatedPartyDetails));
                });
            caseDataMapToBeUpdated.put(C100_APPLICANTS, caseData.getApplicants());
            caseData = caseData.toBuilder().applicants(applicants).build();
            return new CitizenUpdatePartyDataContent(caseDataMapToBeUpdated, caseData);
        } else if (PartyEnum.respondent.equals(citizenUpdatedCaseData.getPartyType())) {
            List<Element<PartyDetails>> respondents = new ArrayList<>(caseData.getRespondents());
            CaseData oldCaseData = caseData;
            respondents.stream()
                .filter(party -> Objects.equals(
                    party.getValue().getUser().getIdamId(),
                    citizenUpdatedCaseData.getPartyDetails().getUser().getIdamId()
                ))
                .findFirst()
                .ifPresent(party -> {
                    PartyDetails updatedPartyDetails = getUpdatedPartyDetailsBasedOnEvent(citizenUpdatedCaseData.getPartyDetails(),
                                                                                          party.getValue(),
                                                                                          caseEvent);
                    Element<PartyDetails> updatedPartyElement = element(party.getId(), updatedPartyDetails);
                    respondents.set(respondents.indexOf(party), updatedPartyElement);

                    if (CONFIRM_YOUR_DETAILS.equals(caseEvent) || KEEP_DETAILS_PRIVATE.equals(caseEvent)) {
                        reGenerateRespondentC8Documents(caseDataMapToBeUpdated, updatedPartyElement,
                                                        oldCaseData, respondents.indexOf(party), respondents, authorisation);
                    }
                });

            caseDataMapToBeUpdated.put(C100_RESPONDENTS, caseData.getRespondents());
            caseData = caseData.toBuilder().respondents(respondents).build();

            return new CitizenUpdatePartyDataContent(caseDataMapToBeUpdated, caseData);
        }
        return null;
    }

    private void reGenerateRespondentC8Documents(Map<String, Object> caseDataMapToBeUpdated,
                                                 Element<PartyDetails> updatedPartyElement,
                                                 CaseData caseData,
                                                 int respondentIndex,
                                                 List<Element<PartyDetails>> updatedRespondents,
                                                 String authorisation) {
        CaseData updatedCaseData = caseData.toBuilder()
            .respondents(updatedRespondents)
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetailsBefore(CaseDetails.builder()
                             .data(caseData.toMap(objectMapper))
                             .build())
            .caseDetails(CaseDetails.builder()
                             .data(updatedCaseData.toMap(objectMapper))
                             .build())
            .build();
        Map<String, Object> dataMap = c100RespondentSolicitorService.populateDataMap(
            callbackRequest,
            updatedPartyElement
        );
        try {
            updatePartyDetailsService.populateC8Documents(authorisation,
                                                          caseDataMapToBeUpdated, caseData, dataMap,
                                                          updatePartyDetailsService
                                                              .checkIfConfidentialityDetailsChangedRespondent(
                                                                  callbackRequest,updatedPartyElement
                                                              ),
                                respondentIndex,updatedPartyElement);
        } catch (Exception e) {
            log.error("Failed to generate C8 document for Case id - {} & Party name - {}",
                      caseData.getId(), updatedPartyElement.getValue().getLabelForDynamicList());
            throw new CoreCaseDataStoreException(e.getMessage(), e);
        }
    }

    private CitizenUpdatePartyDataContent updatingPartyDetailsDa(CaseData caseData,
                                                                 UpdateCaseData citizenUpdatedCaseData,
                                            CaseEvent caseEvent) {
        log.info("Inside updatingPartyDetailsDa");
        PartyDetails partyDetails;
        Map<String, Object> caseDataMapToBeUpdated = new HashMap<>();
        if (PartyEnum.applicant.equals(citizenUpdatedCaseData.getPartyType())) {
            if (citizenUpdatedCaseData.getPartyDetails().getUser().getIdamId()
                .equalsIgnoreCase(caseData.getApplicantsFL401().getUser().getIdamId())) {
                partyDetails = getUpdatedPartyDetailsBasedOnEvent(
                    citizenUpdatedCaseData.getPartyDetails(),
                    caseData.getApplicantsFL401(),
                    caseEvent
                );
                caseDataMapToBeUpdated.put(FL401_APPLICANTS, caseData.getApplicantsFL401());
                caseData = caseData.toBuilder().applicantsFL401(partyDetails).build();
                return new CitizenUpdatePartyDataContent(caseDataMapToBeUpdated, caseData);
            }
        } else {
            if (citizenUpdatedCaseData.getPartyDetails().getUser().getIdamId()
                .equalsIgnoreCase(caseData.getRespondentsFL401().getUser().getIdamId())) {
                partyDetails = getUpdatedPartyDetailsBasedOnEvent(
                    citizenUpdatedCaseData.getPartyDetails(),
                    caseData.getRespondentsFL401(),
                    caseEvent
                );
                caseDataMapToBeUpdated.put(FL401_RESPONDENTS, caseData.getRespondentsFL401());
                caseData = caseData.toBuilder().respondentsFL401(partyDetails).build();
                return new CitizenUpdatePartyDataContent(caseDataMapToBeUpdated, caseData);
            }
        }
        return null;
    }

    private PartyDetails getUpdatedPartyDetailsBasedOnEvent(PartyDetails citizenProvidedPartyDetails,
                                                                   PartyDetails existingPartyDetails,
                                                                   CaseEvent caseEvent) {
        log.info("Inside getUpdatedPartyDetailsBasedOnEvent for event " + caseEvent.getValue());

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
            case EVENT_RESPONDENT_SAFETY_CONCERNS -> {
                return updateCitizenSafetyConcernDetails(
                    existingPartyDetails,
                    citizenProvidedPartyDetails
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
            default -> {
                //For citizen-case-update - currentOrPreviousProceedings
                return updateCitizenResponseDataForOtherEvents(
                    existingPartyDetails,
                    citizenProvidedPartyDetails
                );
            }
        }
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
            .response(existingPartyDetails.getResponse()
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

    private PartyDetails updateCitizenInternationalElementDetails(PartyDetails existingPartyDetails, PartyDetails citizenProvidedPartyDetails) {
        return existingPartyDetails.toBuilder()
            .response(existingPartyDetails.getResponse()
                          .toBuilder()
                          .citizenInternationalElements(citizenProvidedPartyDetails.getResponse().getCitizenInternationalElements())
                          .build())
            .build();
    }

    private PartyDetails updateCitizenSafetyConcernDetails(PartyDetails existingPartyDetails, PartyDetails citizenProvidedPartyDetails) {
        return existingPartyDetails.toBuilder()
            .response(existingPartyDetails.getResponse()
                          .toBuilder()
                          .safetyConcerns(citizenProvidedPartyDetails.getResponse().getSafetyConcerns())
                          .build())
            .build();
    }

    private PartyDetails updateCitizenLegalRepresentationDetails(PartyDetails existingPartyDetails, PartyDetails citizenProvidedPartyDetails) {
        return existingPartyDetails.toBuilder()
            .response(existingPartyDetails.getResponse()
                          .toBuilder()
                          .legalRepresentation(citizenProvidedPartyDetails.getResponse().getLegalRepresentation())
                          .build())
            .build();
    }

    private PartyDetails updateCitizenMiamDetails(PartyDetails existingPartyDetails, PartyDetails citizenProvidedPartyDetails) {
        return existingPartyDetails.toBuilder()
            .response(existingPartyDetails.getResponse()
                          .toBuilder()
                          .miam(citizenProvidedPartyDetails.getResponse().getMiam())
                          .build())
            .build();
    }

    private PartyDetails updateCitizenConsentDetails(PartyDetails existingPartyDetails, PartyDetails citizenProvidedPartyDetails) {
        return existingPartyDetails.toBuilder()
            .response(existingPartyDetails.getResponse()
                          .toBuilder()
                          .consent(citizenProvidedPartyDetails.getResponse().getConsent())
                          .build())
            .build();
    }

    private PartyDetails updateCitizenResponseDataForOtherEvents(PartyDetails existingPartyDetails, PartyDetails citizenProvidedPartyDetails) {
        boolean isCitizenFlagsPresent = isNotEmpty(citizenProvidedPartyDetails.getResponse().getCitizenFlags());
        return existingPartyDetails.toBuilder()
            .response(existingPartyDetails.getResponse()
                          .toBuilder()
                          .currentOrPreviousProceedings(isNotEmpty(citizenProvidedPartyDetails.getResponse().getCurrentOrPreviousProceedings())
                                                            ? citizenProvidedPartyDetails.getResponse().getCurrentOrPreviousProceedings()
                                                            : existingPartyDetails.getResponse().getCurrentOrPreviousProceedings())
                          .citizenFlags(isCitizenFlagsPresent
                                            ? updateCitizenFlags(existingPartyDetails.getResponse().getCitizenFlags(),
                                                                 citizenProvidedPartyDetails.getResponse().getCitizenFlags())
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
        log.info("Updating parties personal details");
        boolean isAddressNeedsToUpdate = isNotEmpty(citizenProvidedPartyDetails.getAddress())
            && StringUtils.isNotEmpty(citizenProvidedPartyDetails.getAddress().getAddressLine1());

        boolean isEmailNeedsToUpdate = StringUtils.isNotEmpty(citizenProvidedPartyDetails.getEmail());

        boolean isPhoneNoNeedsToUpdate = StringUtils.isNotEmpty(citizenProvidedPartyDetails.getPhoneNumber());

        boolean isDateOfBirthNeedsToUpdate = isNotEmpty(citizenProvidedPartyDetails.getDateOfBirth());

        boolean isPlaceOfBirthNeedsToUpdate = StringUtils.isNotEmpty(citizenProvidedPartyDetails.getPlaceOfBirth());

        return existingPartyDetails.toBuilder()
            .canYouProvideEmailAddress(isEmailNeedsToUpdate ? YesOrNo.Yes : existingPartyDetails.getCanYouProvideEmailAddress())
            .email(isEmailNeedsToUpdate
                       ? citizenProvidedPartyDetails.getEmail() : existingPartyDetails.getEmail())
            .canYouProvidePhoneNumber(isPhoneNoNeedsToUpdate ? YesOrNo.Yes : existingPartyDetails.getCanYouProvidePhoneNumber())
            .phoneNumber(isPhoneNoNeedsToUpdate
                             ? citizenProvidedPartyDetails.getPhoneNumber() : existingPartyDetails.getPhoneNumber())
            //.isAtAddressLessThan5Years(partyDetails.getIsAtAddressLessThan5Years() != null ? YesOrNo.Yes : YesOrNo.No)
            .isCurrentAddressKnown(isAddressNeedsToUpdate ? YesOrNo.Yes : existingPartyDetails.getIsCurrentAddressKnown())
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
            .isPlaceOfBirthKnown(isPlaceOfBirthNeedsToUpdate
                                     ? YesOrNo.Yes : existingPartyDetails.getIsPlaceOfBirthKnown())
            .response(existingPartyDetails.getResponse().toBuilder()
                          .citizenDetails(mapResponseCitizenDetails(citizenProvidedPartyDetails))
                          .build())
            .build();
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
        log.info("Updating parties confidential details");
        if (null != citizenProvidedPartyDetails.getResponse()
            && null != citizenProvidedPartyDetails.getResponse().getKeepDetailsPrivate()
            && Yes.equals(citizenProvidedPartyDetails.getResponse().getKeepDetailsPrivate().getConfidentiality())
            && null != citizenProvidedPartyDetails.getResponse().getKeepDetailsPrivate().getConfidentialityList()) {
            return existingPartyDetails.toBuilder()
                .response(existingPartyDetails.getResponse().toBuilder()
                              .keepDetailsPrivate(citizenProvidedPartyDetails.getResponse().getKeepDetailsPrivate())
                              .build())
                .isPhoneNumberConfidential(
                    citizenProvidedPartyDetails.getResponse().getKeepDetailsPrivate().getConfidentialityList().contains(
                        ConfidentialityListEnum.phoneNumber) ? Yes : existingPartyDetails.getIsPhoneNumberConfidential())
                .isAddressConfidential(citizenProvidedPartyDetails.getResponse().getKeepDetailsPrivate().getConfidentialityList().contains(
                    ConfidentialityListEnum.address) ? Yes : existingPartyDetails.getIsAddressConfidential())
                .isEmailAddressConfidential(citizenProvidedPartyDetails.getResponse().getKeepDetailsPrivate().getConfidentialityList().contains(
                    ConfidentialityListEnum.email) ? Yes : existingPartyDetails.getIsEmailAddressConfidential()).build();
        }
        return existingPartyDetails;
    }
}
