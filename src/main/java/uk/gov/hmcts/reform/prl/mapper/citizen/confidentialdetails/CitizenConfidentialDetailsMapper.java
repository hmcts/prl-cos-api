package uk.gov.hmcts.reform.prl.mapper.citizen.confidentialdetails;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.citizen.ConfidentialityListEnum;
import uk.gov.hmcts.reform.prl.models.CitizenUpdatedCaseData;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import static java.util.Optional.ofNullable;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_APPLICANTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_RESPONDENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_APPLICANTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_RESPONDENTS;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CitizenConfidentialDetailsMapper {
    private final AllTabServiceImpl allTabService;
    private final ObjectMapper objectMapper;

    public CaseDetails mapConfidentialData(String caseId, String eventId, CitizenUpdatedCaseData citizenUpdatedCaseData) {
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent
            = allTabService.getStartUpdateForSpecificEvent(caseId, eventId);

        try {
            log.info("startAllTabsUpdateDataContent case data ===>" + objectMapper.writeValueAsString(
                startAllTabsUpdateDataContent.caseData()));
        } catch (JsonProcessingException e) {
            log.info("error");
        }

        try {
            log.info("startAllTabsUpdateDataContent case data map ===>" + objectMapper.writeValueAsString(
                startAllTabsUpdateDataContent.caseDataMap()));
        } catch (JsonProcessingException e) {
            log.info("error");
        }
        CaseData caseData = startAllTabsUpdateDataContent.caseData();
        PartyDetails partyDetails = citizenUpdatedCaseData.getPartyDetails();
        PartyEnum partyType = citizenUpdatedCaseData.getPartyType();
        Map<String, Object> caseDataToUpdate = new HashMap<>();
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            if (PartyEnum.applicant.equals(partyType)) {
                Optional<List<Element<PartyDetails>>> partyList = ofNullable(caseData.getApplicants());
                if (partyList.isPresent()) {
                    updatePartyList(partyList.get(), partyDetails);
                    caseData = caseData.toBuilder().applicants(partyList.get()).build();
                    caseDataToUpdate.put(C100_APPLICANTS, partyList.get());
                    caseDataToUpdate.putAll(allTabService.findCaseDataMap(caseData));
                }
            } else {
                Optional<List<Element<PartyDetails>>> partyList = ofNullable(caseData.getRespondents());
                if (partyList.isPresent()) {
                    updatePartyList(partyList.get(), partyDetails);
                    caseData = caseData.toBuilder().respondents(partyList.get()).build();
                    caseDataToUpdate.put(C100_RESPONDENTS, partyList.get());
                    caseDataToUpdate.putAll(allTabService.findCaseDataMap(caseData));
                }
            }
        } else {
            if (PartyEnum.applicant.equals(partyType)) {
                if (null != caseData.getApplicantsFL401()) {
                    PartyDetails updatedPartyDetails = updateCitizenConfidentialData(
                        caseData.getApplicantsFL401(),
                        partyDetails
                    );
                    caseData = caseData.toBuilder().applicantsFL401(updatedPartyDetails).build();
                    caseDataToUpdate.put(FL401_APPLICANTS, updatedPartyDetails);
                    caseDataToUpdate.putAll(allTabService.findCaseDataMap(caseData));
                }
            } else {
                if (null != caseData.getRespondentsFL401()) {
                    PartyDetails updatedPartyDetails = updateCitizenConfidentialData(
                        caseData.getRespondentsFL401(),
                        partyDetails
                    );
                    caseData = caseData.toBuilder().respondentsFL401(updatedPartyDetails).build();
                    caseDataToUpdate.put(FL401_RESPONDENTS, updatedPartyDetails);
                    caseDataToUpdate.putAll(allTabService.findCaseDataMap(caseData));
                }
            }
        }

        return allTabService.submitAllTabsUpdate(
            startAllTabsUpdateDataContent.systemAuthorisation(),
            caseId,
            startAllTabsUpdateDataContent.startEventResponse(),
            startAllTabsUpdateDataContent.eventRequestData(),
            caseDataToUpdate
        );
    }

    private void updatePartyList(List<Element<PartyDetails>> partyList, PartyDetails partyDetails) {
        partyList.stream()
            .filter(party -> Objects.equals(
                party.getValue().getUser().getIdamId(),
                partyDetails.getUser().getIdamId()
            ))
            .findFirst()
            .ifPresent(party -> {
                           PartyDetails updatedPartyDetails = updateCitizenConfidentialData(
                               party.getValue(),
                               partyDetails
                           );
                           partyList.set(
                               partyList.indexOf(party),
                               element(party.getId(), updatedPartyDetails)
                           );
                       }
            );
    }

    private PartyDetails updateCitizenConfidentialData(PartyDetails dbPartyDetails, PartyDetails citizenProvidedPartyDetails) {
        if (null != citizenProvidedPartyDetails.getResponse()
            && null != citizenProvidedPartyDetails.getResponse().getKeepDetailsPrivate()
            && Yes.equals(citizenProvidedPartyDetails.getResponse().getKeepDetailsPrivate().getConfidentiality())
            && null != citizenProvidedPartyDetails.getResponse().getKeepDetailsPrivate().getConfidentialityList()) {
            return dbPartyDetails.toBuilder()
                .isPhoneNumberConfidential(citizenProvidedPartyDetails.getResponse().getKeepDetailsPrivate().getConfidentialityList().contains(
                    ConfidentialityListEnum.phoneNumber) ? Yes : No)
                .isAddressConfidential(dbPartyDetails.getResponse().getKeepDetailsPrivate().getConfidentialityList().contains(
                    ConfidentialityListEnum.address) ? Yes : No)
                .isEmailAddressConfidential(dbPartyDetails.getResponse().getKeepDetailsPrivate().getConfidentialityList().contains(
                    ConfidentialityListEnum.email) ? Yes : No)
                .build();
        }
        return dbPartyDetails;
    }
}
