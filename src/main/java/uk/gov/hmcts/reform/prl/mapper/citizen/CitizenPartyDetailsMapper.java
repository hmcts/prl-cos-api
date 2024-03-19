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
import uk.gov.hmcts.reform.prl.models.CitizenUpdatedCaseData;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_APPLICANTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_RESPONDENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_APPLICANTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_RESPONDENTS;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
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

    public CitizenUpdatePartyDataContent mapUpdatedPartyDetails(CitizenUpdatedCaseData citizenUpdatedCaseData,
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
        //Iterables.removeIf(caseDataMapToBeUpdated.values(), Objects::isNull);
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

    private PartyDetails getUpdatedPartyDetailsBasedOnEvent(PartyDetails citizenProvidedPartyDetails,
                                                                   PartyDetails existingPartyDetails,
                                                                   CaseEvent caseEvent) {
        log.info("Inside getUpdatedPartyDetailsBasedOnEvent for event " + caseEvent.getValue());
        if (CaseEvent.CONFIRM_YOUR_DETAILS.equals(caseEvent)) {
            return updateCitizenPersonalDetails(
                existingPartyDetails,
                citizenProvidedPartyDetails
            );
        } else if (CaseEvent.KEEP_DETAILS_PRIVATE.equals(caseEvent)) {
            return updateCitizenConfidentialData(
                existingPartyDetails,
                citizenProvidedPartyDetails
            );
        }
        return existingPartyDetails;
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

    private PartyDetails updateCitizenPersonalDetails(PartyDetails existingPartyDetails, PartyDetails citizenProvidedPartyDetails) {
        log.info("Updating parties personal details");
        return existingPartyDetails.toBuilder()
            .canYouProvideEmailAddress(StringUtils.isNotEmpty(citizenProvidedPartyDetails.getEmail()) ? YesOrNo.Yes : YesOrNo.No)
            .email(citizenProvidedPartyDetails.getEmail())
            .canYouProvidePhoneNumber(StringUtils.isNotEmpty(citizenProvidedPartyDetails.getPhoneNumber()) ? YesOrNo.Yes :
                                          YesOrNo.No)
            .phoneNumber(citizenProvidedPartyDetails.getPhoneNumber())
            //.isAtAddressLessThan5Years(partyDetails.getIsAtAddressLessThan5Years() != null ? YesOrNo.Yes : YesOrNo.No)
            .isCurrentAddressKnown(citizenProvidedPartyDetails.getAddress() != null ? YesOrNo.Yes : YesOrNo.No)
            .address(citizenProvidedPartyDetails.getAddress())
            .addressLivedLessThan5YearsDetails(citizenProvidedPartyDetails.getAddressLivedLessThan5YearsDetails())
            .firstName(citizenProvidedPartyDetails.getFirstName())
            .lastName(citizenProvidedPartyDetails.getLastName())
            .previousName(citizenProvidedPartyDetails.getPreviousName())
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
                .response(existingPartyDetails.getResponse().toBuilder().keepDetailsPrivate(
                    citizenProvidedPartyDetails.getResponse().getKeepDetailsPrivate()).build())
                .isPhoneNumberConfidential(
                    citizenProvidedPartyDetails.getResponse().getKeepDetailsPrivate().getConfidentialityList().contains(
                        ConfidentialityListEnum.phoneNumber) ? Yes : No)
                .isAddressConfidential(citizenProvidedPartyDetails.getResponse().getKeepDetailsPrivate().getConfidentialityList().contains(
                    ConfidentialityListEnum.address) ? Yes : No)
                .isEmailAddressConfidential(citizenProvidedPartyDetails.getResponse().getKeepDetailsPrivate().getConfidentialityList().contains(
                    ConfidentialityListEnum.email) ? Yes : No).build();
        }
        return existingPartyDetails;
    }
}
