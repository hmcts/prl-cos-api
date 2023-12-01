package uk.gov.hmcts.reform.prl.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.caseaccess.AssignCaseAccessService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@Tag(name = "case-initiation-controller")
@RestController
@RequestMapping("/case-initiation")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
@SecurityRequirement(name = "Bearer Authentication")
public class CaseInitiationController extends AbstractCallbackController {


    private  final AssignCaseAccessService assignCaseAccessService;

    private final CoreCaseDataApi coreCaseDataApi;

    private final AuthTokenGenerator authTokenGenerator;

    private final AuthorisationService authorisationService;

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
                                @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
                                @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            final CaseDetails caseDetails = callbackRequest.getCaseDetails();
            final CaseData caseData = getCaseData(caseDetails).toBuilder().build();

            log.info("case detail received from xui {}",caseDetails);
            assignCaseAccessService.assignCaseAccess(caseDetails.getId().toString(), authorisation);

            // setting supplementary data updates to enable global search
            String caseId = String.valueOf(caseData.getId());
            Map<String, Map<String, Map<String, Object>>> supplementaryData = new HashMap<>();
            supplementaryData.put(
                "supplementary_data_updates",
                Map.of("$set", Map.of("HMCTSServiceId", "ABA5"))
            );
            coreCaseDataApi.submitSupplementaryData(authorisation, authTokenGenerator.generate(), caseId,
                                                    supplementaryData
            );

            publishEvent(new CaseDataChanged(caseData));
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }
}
