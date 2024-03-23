package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.citizen.LinkCitizenCaseService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/citizen")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LinkCitizenCaseController {
    private final ObjectMapper objectMapper;
    private final LinkCitizenCaseService linkCitizenCaseService;
    private final AuthorisationService authorisationService;
    private static final String INVALID_CLIENT = "Invalid Client";
    private static final String CASE_LINKING_FAILED = "Case Linking has failed";

    @PostMapping(value = "/link-case-to-account", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Linking case to citizen account with access code")
    public CaseData linkCitizenToCase(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                      @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
                                      @RequestHeader("caseId") String caseId,
                                      @RequestHeader("accessCode") String accessCode) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            Optional<CaseDetails> caseDetails = linkCitizenCaseService.linkCitizenToCase(
                authorisation,
                caseId,
                accessCode
            );
            if (caseDetails.isPresent()) {
                return CaseUtils.getCaseData(caseDetails.get(), objectMapper);
            } else {
                throw (new RuntimeException(CASE_LINKING_FAILED));
            }
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }
}
