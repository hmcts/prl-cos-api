package uk.gov.hmcts.reform.prl.mapper.citizen;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.clients.ccd.records.CitizenUpdatePartyDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.citizen.ConfidentialityListEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.UpdateCaseData;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_APPLICANTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_RESPONDENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_APPLICANTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_RESPONDENTS;
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

    public CitizenUpdatePartyDataContent mapUpdatedPartyDetails(UpdateCaseData citizenUpdatedCaseData,
                                                                CaseData dbCaseData,
                                                                PartyEnum partyType,
                                                                CaseEvent caseEvent) {
        log.info("Start CitizenPartyDetailsMapper:mapUpdatedPartyDetails() for event " + caseEvent.getValue());

        Map<String, Object> caseDataMapToBeUpdated = new HashMap<>();
        if (C100_CASE_TYPE.equalsIgnoreCase(citizenUpdatedCaseData.getCaseTypeOfApplication())) {
            dbCaseData = updatingPartyDetailsCa(dbCaseData, citizenUpdatedCaseData.getPartyDetails(), partyType, caseEvent);
        } else {
            dbCaseData = updatingPartyDetailsDa(dbCaseData, citizenUpdatedCaseData.getPartyDetails(), partyType, caseEvent);
        }

        if (CaseEvent.CONFIRM_YOUR_DETAILS.equals(caseEvent)) {
            generateAnswersForNoc(dbCaseData, caseDataMapToBeUpdated);
            //check if anything needs to do for citizen flags
        }

        putUpdatedApplicantRespondentDetailsInMap(dbCaseData, caseDataMapToBeUpdated);
        log.info("Updated caseDataMap =>" + caseDataMapToBeUpdated);
        log.info("Exit CitizenPartyDetailsMapper:mapUpdatedPartyDetails() for event " + caseEvent.getValue());
        return new CitizenUpdatePartyDataContent(caseDataMapToBeUpdated, dbCaseData);
    }

    private void generateAnswersForNoc(CaseData caseData, Map<String, Object> caseDataMapToBeUpdated) {
        if (caseDataMapToBeUpdated != null) {
            if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
                caseDataMapToBeUpdated.putAll(noticeOfChangePartiesService.generate(caseData, CARESPONDENT));
                caseDataMapToBeUpdated.putAll(noticeOfChangePartiesService.generate(caseData, CAAPPLICANT));
            } else {
                caseDataMapToBeUpdated.putAll(noticeOfChangePartiesService.generate(caseData, DARESPONDENT));
                caseDataMapToBeUpdated.putAll(noticeOfChangePartiesService.generate(caseData, DAAPPLICANT));
            }
        }
    }

    private void putUpdatedApplicantRespondentDetailsInMap(CaseData caseData, Map<String, Object> caseDataMapToBeUpdated) {
        if (caseDataMapToBeUpdated != null) {
            if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
                caseDataMapToBeUpdated.put(C100_APPLICANTS, caseData.getApplicants());
                caseDataMapToBeUpdated.put(C100_RESPONDENTS, caseData.getRespondents());
            } else {
                caseDataMapToBeUpdated.put(FL401_APPLICANTS, caseData.getApplicantsFL401());
                caseDataMapToBeUpdated.put(FL401_RESPONDENTS, caseData.getRespondentsFL401());
            }
        }
    }

    private CaseData updatingPartyDetailsCa(CaseData caseData, PartyDetails citizenProvidedPartyDetails,
                                                   PartyEnum partyType, CaseEvent caseEvent) {
        log.info("Inside updatingPartyDetailsCa");
        if (PartyEnum.applicant.equals(partyType)) {
            List<Element<PartyDetails>> applicants = new ArrayList<>(caseData.getApplicants());
            applicants.stream()
                .filter(party -> Objects.equals(
                    party.getValue().getUser().getIdamId(),
                    citizenProvidedPartyDetails.getUser().getIdamId()
                ))
                .findFirst()
                .ifPresent(party -> {
                    PartyDetails updatedPartyDetails = getUpdatedPartyDetailsBasedOnEvent(citizenProvidedPartyDetails,
                                                                                          party.getValue(),
                                                                                          caseEvent);
                    applicants.set(applicants.indexOf(party), element(party.getId(), updatedPartyDetails));
                });
            caseData = caseData.toBuilder().applicants(applicants).build();
        } else if (PartyEnum.respondent.equals(partyType)) {
            List<Element<PartyDetails>> respondents = new ArrayList<>(caseData.getRespondents());
            respondents.stream()
                .filter(party -> Objects.equals(
                    party.getValue().getUser().getIdamId(),
                    citizenProvidedPartyDetails.getUser().getIdamId()
                ))
                .findFirst()
                .ifPresent(party -> {
                    PartyDetails updatedPartyDetails = getUpdatedPartyDetailsBasedOnEvent(citizenProvidedPartyDetails,
                                                                                          party.getValue(),
                                                                                          caseEvent);
                    respondents.set(respondents.indexOf(party), element(party.getId(), updatedPartyDetails));
                });
            caseData = caseData.toBuilder().respondents(respondents).build();
        }
        return caseData;
    }

    private CaseData updatingPartyDetailsDa(CaseData caseData,
                                            PartyDetails citizenProvidedPartyDetails,
                                            PartyEnum partyType,
                                            CaseEvent caseEvent) {
        log.info("Inside updatingPartyDetailsDa");
        PartyDetails partyDetails;
        if (PartyEnum.applicant.equals(partyType)) {
            if (citizenProvidedPartyDetails.getUser().getIdamId().equalsIgnoreCase(caseData.getApplicantsFL401().getUser().getIdamId())) {
                partyDetails = getUpdatedPartyDetailsBasedOnEvent(citizenProvidedPartyDetails,
                                                                  caseData.getApplicantsFL401(),
                                                                  caseEvent);
                caseData = caseData.toBuilder().applicantsFL401(partyDetails).build();
            }
        } else {
            if (citizenProvidedPartyDetails.getUser().getIdamId().equalsIgnoreCase(caseData.getRespondentsFL401().getUser().getIdamId())) {
                partyDetails = getUpdatedPartyDetailsBasedOnEvent(citizenProvidedPartyDetails,
                                                                  caseData.getRespondentsFL401(),
                                                                  caseEvent);
                caseData = caseData.toBuilder().respondentsFL401(partyDetails).build();
            }
        }
        return caseData;
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
                return updateCitizenLegalRepresentaionDetails(
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
            default -> {
                //For citizen-case-update
                return updateCitizenResponseDataForOtherEvents(
                    existingPartyDetails,
                    citizenProvidedPartyDetails
                );
            }
        }
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

    private PartyDetails updateCitizenLegalRepresentaionDetails(PartyDetails existingPartyDetails, PartyDetails citizenProvidedPartyDetails) {
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
        return existingPartyDetails.toBuilder()
            .response(existingPartyDetails.getResponse()
                          .toBuilder()
                          .currentOrPreviousProceedings(isNotEmpty(citizenProvidedPartyDetails.getResponse().getCurrentOrPreviousProceedings())
                                                            ? citizenProvidedPartyDetails.getResponse().getCurrentOrPreviousProceedings()
                                                            : existingPartyDetails.getResponse().getCurrentOrPreviousProceedings())
                          .citizenFlags(isNotEmpty(citizenProvidedPartyDetails.getResponse().getCitizenFlags())
                          ? citizenProvidedPartyDetails.getResponse().getCitizenFlags() : existingPartyDetails.getResponse().getCitizenFlags())
                          .build())
            .build();
    }

    private PartyDetails updateCitizenPersonalDetails(PartyDetails existingPartyDetails, PartyDetails citizenProvidedPartyDetails) {
        log.info("Updating parties personal details");
        boolean isAddressNeedsToUpdate = isNotEmpty(citizenProvidedPartyDetails.getAddress())
            && StringUtils.isNotEmpty(citizenProvidedPartyDetails.getAddress().getAddressLine1());

        boolean isEmailNeedsToUpdate = StringUtils.isNotEmpty(citizenProvidedPartyDetails.getEmail());

        boolean isPhoneNoNeedsToUpdate = StringUtils.isNotEmpty(citizenProvidedPartyDetails.getPhoneNumber());

        return existingPartyDetails.toBuilder()
            .canYouProvideEmailAddress(isEmailNeedsToUpdate ? YesOrNo.Yes : YesOrNo.No)
            .email(isEmailNeedsToUpdate
                       ? citizenProvidedPartyDetails.getEmail() : existingPartyDetails.getEmail())
            .canYouProvidePhoneNumber(isPhoneNoNeedsToUpdate ? YesOrNo.Yes : YesOrNo.No)
            .phoneNumber(isPhoneNoNeedsToUpdate
                             ? citizenProvidedPartyDetails.getPhoneNumber() : existingPartyDetails.getPhoneNumber())
            //.isAtAddressLessThan5Years(partyDetails.getIsAtAddressLessThan5Years() != null ? YesOrNo.Yes : YesOrNo.No)
            .isCurrentAddressKnown(isAddressNeedsToUpdate ? YesOrNo.Yes : YesOrNo.No)
            .address(isAddressNeedsToUpdate ? citizenProvidedPartyDetails.getAddress() : existingPartyDetails.getAddress())
            .addressLivedLessThan5YearsDetails(StringUtils.isNotEmpty(citizenProvidedPartyDetails.getAddressLivedLessThan5YearsDetails())
                                                   ? citizenProvidedPartyDetails.getAddressLivedLessThan5YearsDetails()
                                                   : existingPartyDetails.getAddressLivedLessThan5YearsDetails())
            .firstName(citizenProvidedPartyDetails.getFirstName())
            .lastName(citizenProvidedPartyDetails.getLastName())
            .previousName(StringUtils.isNotEmpty(citizenProvidedPartyDetails.getPreviousName())
                              ? citizenProvidedPartyDetails.getPreviousName() : existingPartyDetails.getPreviousName())
            .response(existingPartyDetails.getResponse().toBuilder()
                          .citizenDetails(citizenProvidedPartyDetails.getResponse().getCitizenDetails())
                          .build())
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
