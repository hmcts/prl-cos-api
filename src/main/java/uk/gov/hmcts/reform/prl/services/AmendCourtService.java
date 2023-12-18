package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COLON_SEPERATOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_CODE_FROM_FACT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_LIST;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_NAME_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_SEAL_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STATE_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TRANSFERRED_COURT_FROM;

@Service
@Slf4j
@RequiredArgsConstructor
public class AmendCourtService {

    private final C100IssueCaseService c100IssueCaseService;
    private final LocationRefDataService locationRefDataService;
    private final CourtSealFinderService courtSealFinderService;
    private final ObjectMapper objectMapper;
    private final EmailService emailService;
    private final CaseWorkerEmailService caseWorkerEmailService;

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
            }
            if (caseData.getCourtEmailAddress() != null) {
                sendCourtAdminEmail(caseData, callbackRequest.getCaseDetails());
            }
        }
        caseDataUpdated.put(TRANSFERRED_COURT_FROM, caseData.getCourtName());
        return caseDataUpdated;
    }

    public boolean validateCourtFields(CaseData caseData, List<String> errorList) {
        if (!CollectionUtils.isEmpty(caseData.getCantFindCourtCheck())
            && (caseData.getAnotherCourt() == null
            || caseData.getCourtEmailAddress() == null)) {
            errorList.add("Please enter court name and email address.");
            return true;
        } else if (CollectionUtils.isEmpty(caseData.getCantFindCourtCheck()) && caseData.getCourtList() == null) {
            errorList.add("Please select court name from list.");
            return true;
        } else if (!CollectionUtils.isEmpty(caseData.getCantFindCourtCheck()) && caseData.getCourtList() != null) {
            errorList.add("Please select one of the option for court name.");
            return true;
        }
        return false;
    }

    private void sendCourtAdminEmail(CaseData caseData, CaseDetails caseDetails) {
        if (CaseUtils.getCaseTypeOfApplication(caseData).equalsIgnoreCase(C100_CASE_TYPE)) {
            emailService.send(
                caseData.getCourtEmailAddress(),
                EmailTemplateNames.COURTADMIN,
                caseWorkerEmailService.buildCourtAdminEmail(caseDetails),
                LanguagePreference.english
            );
        } else {
            caseWorkerEmailService.sendEmailToFl401LocalCourt(caseDetails, caseData.getCourtEmailAddress());
        }
    }
}
