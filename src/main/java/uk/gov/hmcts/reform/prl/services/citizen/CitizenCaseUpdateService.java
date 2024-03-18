package uk.gov.hmcts.reform.prl.services.citizen;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.records.CitizenUpdatePartyDataContent;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.mapper.citizen.CitizenPartyDetailsMapper;
import uk.gov.hmcts.reform.prl.models.CitizenUpdatedCaseData;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.HashMap;


@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CitizenCaseUpdateService {

    private final AllTabServiceImpl allTabService;
    private final CitizenPartyDetailsMapper citizenPartyDetailsMapper;

    public CaseDetails updateCitizenPartyDetails(String authorisation,
                                                 String caseId,
                                                 String eventId,
                                                 CitizenUpdatedCaseData citizenUpdatedCaseData) {
        CaseEvent caseEvent = CaseEvent.fromValue(eventId);
        log.info("*************** eventId received from " + caseEvent.getValue());

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent
            = allTabService.getStartUpdateForSpecificUserEvent(caseId, eventId, authorisation);
        CaseData dbCaseData = startAllTabsUpdateDataContent.caseData();

        CitizenUpdatePartyDataContent citizenUpdatePartyDataContent = new CitizenUpdatePartyDataContent(
            new HashMap<>(),
            dbCaseData
        );
        PartyDetails newPartyDetailsFromCitizen = citizenUpdatedCaseData.getPartyDetails();
        PartyEnum partyType = citizenUpdatedCaseData.getPartyType();

        switch (caseEvent) {
            case KEEP_DETAILS_PRIVATE -> {
                // return citizenConfidentialDetailsMapper.mapConfidentialData(caseId, eventId, citizenUpdatedCaseData);
            }
            case CONFIRM_YOUR_DETAILS ->
                citizenUpdatePartyDataContent = citizenPartyDetailsMapper.mapUpdatedPartyDetails(
                    citizenUpdatedCaseData,
                    citizenUpdatePartyDataContent,
                    newPartyDetailsFromCitizen,
                    partyType
                );
            default -> {
                //in default case
            }
        }

        allTabService.submitUpdateForSpecificUserEvent(
            startAllTabsUpdateDataContent.systemAuthorisation(),
            caseId,
            startAllTabsUpdateDataContent.startEventResponse(),
            startAllTabsUpdateDataContent.eventRequestData(),
            citizenUpdatePartyDataContent.updatedCaseDataMap()
        );

        return allTabService.updateAllTabsIncludingConfTab(String.valueOf(caseId));
    }
}
