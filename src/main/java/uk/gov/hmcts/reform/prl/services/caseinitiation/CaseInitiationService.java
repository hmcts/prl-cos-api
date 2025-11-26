package uk.gov.hmcts.reform.prl.services.caseinitiation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.CaseRole;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.LocationRefDataService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.caseaccess.AssignCaseAccessService;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.getCaseData;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseInitiationService {

    private final ObjectMapper objectMapper;
    private final EventService eventPublisher;
    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi coreCaseDataApi;
    private final AssignCaseAccessService assignCaseAccessService;
    private final LocationRefDataService locationRefDataService;
    private final OrganisationService organisationService;
    public static final String COURT_LIST = "submitCountyCourtSelection";

    public void handleCaseInitiation(String authorisation, CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        String caseId = String.valueOf(caseData.getId());
        assignCaseAccessService.assignCaseAccess(caseId, authorisation);

        // respondents access I shold put this behind a launchdarkly flag
        assignRespondentSolicitorsAccess(caseId, authorisation, caseData);

        // setting supplementary data updates to enable global search
        Map<String, Map<String, Map<String, Object>>> supplementaryData = new HashMap<>();
        supplementaryData.put(
            "supplementary_data_updates",
            Map.of("$set", Map.of("HMCTSServiceId", "ABA5"))
        );
        coreCaseDataApi.submitSupplementaryData(
            authorisation, authTokenGenerator.generate(), caseId,
            supplementaryData
        );

        eventPublisher.publishEvent(new CaseDataChanged(caseData));
    }

    public Map<String, Object> prePopulateCourtDetails(String authorisation, Map<String, Object> caseDataUpdated) {

        if (C100_CASE_TYPE.equalsIgnoreCase(String.valueOf(caseDataUpdated.get("caseTypeOfApplication")))) {
            List<DynamicListElement> courtList = locationRefDataService.getCourtLocations(authorisation);
            caseDataUpdated.put(
                COURT_LIST, DynamicList.builder().value(DynamicListElement.EMPTY).listItems(courtList)
                    .build()
            );
        } else {
            caseDataUpdated.put(
                COURT_LIST, DynamicList.builder()
                    .listItems(locationRefDataService.getDaCourtLocations(authorisation).stream()
                                   .sorted(Comparator.comparing(
                                       DynamicListElement::getLabel,
                                       Comparator.naturalOrder()
                                   ))
                                   .toList())
                    .build()
            );
        }
        return caseDataUpdated;
    }

    // Java
    private void assignRespondentSolicitorsAccess(String caseId,
                                                  String invokingAuth,
                                                  CaseData caseData) {

        if (caseData.getRespondents() == null) {
            return;
        }

        List<Element<PartyDetails>> respondents = caseData.getRespondents();

        for (int i = 0; i < respondents.size(); i++) {
            PartyDetails party = respondents.get(i).getValue();

            if (!YesNoDontKnow.yes.equals(party.getDoTheyHaveLegalRepresentation())) {
                continue;
            }

            var solicitorOrgObj = party.getSolicitorOrg();
            String solicitorOrgId = solicitorOrgObj != null ? solicitorOrgObj.getOrganisationID() : null;
            String solicitorEmail = party.getSolicitorEmail();

            Optional<String> userIdOpt = organisationService.findUserByEmail(solicitorEmail);

            if (isSolicitorEmailValid(solicitorEmail, caseId, i)
                && isSolicitorOrgIdValid(solicitorOrgId, caseId)
                && isSolicitorUserResolvable(userIdOpt, solicitorEmail)) {

                String assigneeUserId = userIdOpt.get();
                CaseRole role = CaseRole.respondentSolicitors().get(i);

                assignCaseAccessService.assignCaseAccessToUserWithRole(
                    caseId,
                    assigneeUserId,
                    role.formattedName(),
                    invokingAuth
                );
            }
        }
    }

    private boolean isSolicitorEmailValid(String email, String caseId, int index) {
        if (email == null || email.isBlank()) {
            log.warn("Respondent solicitor email missing on case {} for respondent index {}; skipping", caseId, index);
            return false;
        }
        return true;
    }

    private boolean isSolicitorOrgIdValid(String orgId, String caseId) {
        if (orgId == null || orgId.isBlank()) {
            log.warn("Respondent solicitor org missing on case {}; skipping", caseId);
            return false;
        }
        return true;
    }

    private boolean isSolicitorUserResolvable(Optional<String> userIdOpt, String email) {
        if (userIdOpt.isEmpty()) {
            log.warn("Unable to resolve IDAM user for respondent sol {}", email);
            return false;
        }
        return true;
    }

}
