package uk.gov.hmcts.reform.prl.services.citizen;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.records.CitizenUpdatePartyDataContent;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.mapper.citizen.CitizenPartyDetailsMapper;
import uk.gov.hmcts.reform.prl.models.UpdateCaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CitizenCaseUpdateService {

    private final AllTabServiceImpl allTabService;
    private final CitizenPartyDetailsMapper citizenPartyDetailsMapper;

    protected static final List<CaseEvent> EVENT_IDS_FOR_ALL_TAB_REFRESHED = Arrays.asList(
        CaseEvent.CONFIRM_YOUR_DETAILS,
        CaseEvent.KEEP_DETAILS_PRIVATE);

    public CaseDetails updateCitizenPartyDetails(String authorisation,
                                                 String caseId,
                                                 String eventId,
                                                 UpdateCaseData citizenUpdatedCaseData) {
        CaseDetails caseDetails = null;
        CaseEvent caseEvent = CaseEvent.fromValue(eventId);
        log.info("*************** eventId received from " + caseEvent.getValue());

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent
            = allTabService.getStartUpdateForSpecificUserEvent(caseId, eventId, authorisation);
        CaseData dbCaseData = startAllTabsUpdateDataContent.caseData();

        Optional<CitizenUpdatePartyDataContent> citizenUpdatePartyDataContent = Optional.ofNullable(
            citizenPartyDetailsMapper.mapUpdatedPartyDetails(
            dbCaseData, citizenUpdatedCaseData,
            caseEvent,
            startAllTabsUpdateDataContent.authorisation()
        ));

        if (citizenUpdatePartyDataContent.isPresent()) {
            log.info("*************** Going to update party details received from Citizen");
            caseDetails = allTabService.submitUpdateForSpecificUserEvent(
                startAllTabsUpdateDataContent.authorisation(),
                caseId,
                startAllTabsUpdateDataContent.startEventResponse(),
                startAllTabsUpdateDataContent.eventRequestData(),
                citizenUpdatePartyDataContent.get().updatedCaseDataMap(),
                startAllTabsUpdateDataContent.userDetails()
            );

            if (EVENT_IDS_FOR_ALL_TAB_REFRESHED.contains(caseEvent)) {
                log.info("*************** Going to refresh all tabs after updating citizen party details");
                return allTabService.updateAllTabsIncludingConfTab(caseId);
            }
        }

        return caseDetails;
    }
}
