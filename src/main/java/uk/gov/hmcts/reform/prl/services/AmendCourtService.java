package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.EmailUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COLON_SEPERATOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_CODE_FROM_FACT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_LIST;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_NAME_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_SEAL_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STATE_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TRANSFERRED_COURT_FROM;
import static uk.gov.hmcts.reform.prl.utils.CommonUtils.isEmpty;
import static uk.gov.hmcts.reform.prl.utils.CommonUtils.isNotEmpty;

@Service
@Slf4j
@RequiredArgsConstructor
public class AmendCourtService {

    private final C100IssueCaseService c100IssueCaseService;
    private final LocationRefDataService locationRefDataService;
    private final CourtSealFinderService courtSealFinderService;
    private final ObjectMapper objectMapper;
    private final CaseSummaryTabService caseSummaryTab;
    private final DfjLookupService dfjLookupService;

    public Map<String, Object> handleAmendCourtSubmission(String authorisation, CallbackRequest callbackRequest,
                                                          Map<String, Object> caseDataUpdated) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        if (!CollectionUtils.isEmpty(caseData.getCantFindCourtCheck())) {
            caseDataUpdated.put(COURT_NAME_FIELD, caseData.getAnotherCourt());
            caseDataUpdated.put(STATE_FIELD, State.PROCEEDS_IN_HERITAGE_SYSTEM);
        } else {

            String baseLocationId = caseData.getCourtList().getValue().getCode().split(COLON_SEPERATOR)[0];
            Optional<CourtVenue> courtVenue = locationRefDataService.getCourtDetailsFromEpimmsId(
                baseLocationId,
                authorisation
            );
            caseDataUpdated.putAll(CaseUtils.getCourtDetails(courtVenue, baseLocationId));
            courtVenue.ifPresent(venue -> caseDataUpdated.put(
                COURT_CODE_FROM_FACT,
                c100IssueCaseService.getFactCourtId(
                    venue
                )
            ));
            caseDataUpdated.put(COURT_LIST, DynamicList.builder().value(caseData.getCourtList().getValue()).build());
            if (courtVenue.isPresent()) {
                String courtSeal = courtSealFinderService.getCourtSeal(courtVenue.get().getRegionId());
                caseDataUpdated.put(COURT_SEAL_FIELD, courtSeal);
                caseDataUpdated.keySet().removeAll(dfjLookupService.getAllCourtFields());
                caseDataUpdated.putAll(dfjLookupService.getDfjAreaFieldsByCourtId(baseLocationId));
            }
            caseDataUpdated.put(STATE_FIELD, caseData.getState());
        }
        caseDataUpdated.put(TRANSFERRED_COURT_FROM, caseData.getCourtName());
        caseDataUpdated.putAll(caseSummaryTab.updateTab(objectMapper.convertValue(caseDataUpdated, CaseData.class)));
        return caseDataUpdated;
    }

    public List<String> validateCourtFields(CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        if (!CollectionUtils.isEmpty(caseData.getCantFindCourtCheck()) && caseData.getCourtList() != null) {
            return List.of("Please select one of the option for court name.");
        } else if (CollectionUtils.isNotEmpty(caseData.getCantFindCourtCheck())) {
            if (!isNotEmpty(caseData.getAnotherCourt())) {
                return List.of("Please enter court name.");
            } else if (!isNotEmpty(caseData.getCourtEmailAddress())) {
                return List.of("Please enter court email address.");
            } else if (!EmailUtils.isValidEmailAddress(caseData.getCourtEmailAddress())) {
                return List.of("Please enter valid court email address.");
            }
        } else if (CollectionUtils.isEmpty(caseData.getCantFindCourtCheck())
            && caseData.getCourtList() == null) {
            return List.of("Please select court name from list.");
        }
        return Collections.emptyList();
    }
}

