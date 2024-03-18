package uk.gov.hmcts.reform.prl.mapper.citizen;

import com.google.common.collect.Iterables;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.clients.ccd.records.CitizenUpdatePartyDataContent;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.CitizenUpdatedCaseData;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_APPLICANTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_RESPONDENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_APPLICANTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_RESPONDENTS;
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
                                                                 CitizenUpdatePartyDataContent citizenUpdatePartyDataContent,
                                                                 PartyDetails newPartyDetailsFromCitizen,
                                                                 PartyEnum partyType) {
        log.info("Updating parties personal details from citizen");
        Map<String, Object> caseDataMapToBeUpdated = citizenUpdatePartyDataContent.updatedCaseDataMap();
        CaseData caseData = citizenUpdatePartyDataContent.updatedCaseData();

        if (C100_CASE_TYPE.equalsIgnoreCase(citizenUpdatedCaseData.getCaseTypeOfApplication())) {
            caseData = updatingPartyDetailsCa(caseData, newPartyDetailsFromCitizen, partyType);
        } else {
            caseData = updatingPartyDetailsDa(caseData, newPartyDetailsFromCitizen, partyType);
        }
        generateAnswersForNoc(caseData, caseDataMapToBeUpdated);
        //check if anything needs to do for citizen flags
        putUpdatedApplicantRespondentDetailsInMap(caseData, caseDataMapToBeUpdated);
        Iterables.removeIf(caseDataMapToBeUpdated.values(), Objects::isNull);
        log.info("Updated caseDataMap =>" + caseDataMapToBeUpdated);
        return new CitizenUpdatePartyDataContent(caseDataMapToBeUpdated, caseData);
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

    private static CaseData updatingPartyDetailsCa(CaseData caseData, PartyDetails newPartyDetailsFromCitizen,
                                                   PartyEnum partyType) {
        if (PartyEnum.applicant.equals(partyType)) {
            List<Element<PartyDetails>> applicants = new ArrayList<>(caseData.getApplicants());
            applicants.stream()
                .filter(party -> Objects.equals(
                    party.getValue().getUser().getIdamId(),
                    newPartyDetailsFromCitizen.getUser().getIdamId()
                ))
                .findFirst()
                .ifPresent(party -> {
                               PartyDetails updatedPartyDetails = getUpdatedPartyDetails(party.getValue(), newPartyDetailsFromCitizen);
                               applicants.set(applicants.indexOf(party), element(party.getId(), updatedPartyDetails));
                           }
                );
            caseData = caseData.toBuilder().applicants(applicants).build();
        } else if (PartyEnum.respondent.equals(partyType)) {
            List<Element<PartyDetails>> respondents = new ArrayList<>(caseData.getRespondents());
            respondents.stream()
                .filter(party -> Objects.equals(
                    party.getValue().getUser().getIdamId(),
                    newPartyDetailsFromCitizen.getUser().getIdamId()
                ))
                .findFirst()
                .ifPresent(party -> {
                               PartyDetails updatedPartyDetails = getUpdatedPartyDetails(party.getValue(), newPartyDetailsFromCitizen);
                               respondents.set(respondents.indexOf(party), element(party.getId(), updatedPartyDetails));
                           }
                );
            caseData = caseData.toBuilder().respondents(respondents).build();
        }
        return caseData;
    }

    private static CaseData updatingPartyDetailsDa(CaseData caseData, PartyDetails newPartyDetailsFromCitizen, PartyEnum partyType) {
        PartyDetails partyDetails;
        if (PartyEnum.applicant.equals(partyType)) {
            if (newPartyDetailsFromCitizen.getUser().getIdamId().equalsIgnoreCase(caseData.getApplicantsFL401().getUser().getIdamId())) {
                partyDetails = getUpdatedPartyDetails(caseData.getApplicantsFL401(), newPartyDetailsFromCitizen);
                caseData = caseData.toBuilder().applicantsFL401(partyDetails).build();
            }
        } else {
            if (newPartyDetailsFromCitizen.getUser().getIdamId().equalsIgnoreCase(caseData.getRespondentsFL401().getUser().getIdamId())) {
                partyDetails = getUpdatedPartyDetails(caseData.getRespondentsFL401(), newPartyDetailsFromCitizen);
                caseData = caseData.toBuilder().respondentsFL401(partyDetails).build();
            }
        }
        return caseData;
    }

    private static PartyDetails getUpdatedPartyDetails(PartyDetails existingPartyDetails, PartyDetails newPartyDetailsFromCitizen) {
        return existingPartyDetails.toBuilder()
            .canYouProvideEmailAddress(StringUtils.isNotEmpty(newPartyDetailsFromCitizen.getEmail()) ? YesOrNo.Yes : YesOrNo.No)
            .email(newPartyDetailsFromCitizen.getEmail())
            .canYouProvidePhoneNumber(StringUtils.isNotEmpty(newPartyDetailsFromCitizen.getPhoneNumber()) ? YesOrNo.Yes :
                                          YesOrNo.No)
            .phoneNumber(newPartyDetailsFromCitizen.getPhoneNumber())
            //.isAtAddressLessThan5Years(partyDetails.getIsAtAddressLessThan5Years() != null ? YesOrNo.Yes : YesOrNo.No)
            .isCurrentAddressKnown(newPartyDetailsFromCitizen.getAddress() != null ? YesOrNo.Yes : YesOrNo.No)
            .address(newPartyDetailsFromCitizen.getAddress())
            .addressLivedLessThan5YearsDetails(newPartyDetailsFromCitizen.getAddressLivedLessThan5YearsDetails())
            .firstName(newPartyDetailsFromCitizen.getFirstName())
            .lastName(newPartyDetailsFromCitizen.getLastName())
            .previousName(newPartyDetailsFromCitizen.getPreviousName())
            .response(existingPartyDetails.getResponse().toBuilder()
                          .citizenDetails(newPartyDetailsFromCitizen.getResponse().getCitizenDetails())
                          .build())
            .build();
    }
}
