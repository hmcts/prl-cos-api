package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.launchdarkly.shaded.org.jetbrains.annotations.NotNull;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.exception.CaseActivationNotAllowedException;
import uk.gov.hmcts.reform.prl.models.citizen.AccessCodeRequest;
import uk.gov.hmcts.reform.prl.models.citizen.CaseActivationRequest;
import uk.gov.hmcts.reform.prl.models.citizen.CaseActivationResponse;
import uk.gov.hmcts.reform.prl.models.citizen.CaseDataWithHearingResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.citizen.UiCitizenCaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.cafcass.HearingService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.citizen.LinkCitizenCaseService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.Optional;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/citizen")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LinkCitizenCaseController {
    private final ObjectMapper objectMapper;
    private final LinkCitizenCaseService linkCitizenCaseService;
    private final AuthorisationService authorisationService;
    private final HearingService hearingService;
    private final CaseService caseService;
    private static final String INVALID_CLIENT = "Invalid Client";
    private static final String CASE_LINKING_FAILED = "Case Linking has failed";

    @PostMapping(value = "/check-case-activation")
    @Operation(description = "Validate an access code and check whether the case can be activated without granting access")
    public CaseActivationResponse checkCaseActivation(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody @Valid CaseActivationRequest request) {
        if (!authorisationService.isAuthorized(authorisation, s2sToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, INVALID_CLIENT);
        }
        return linkCitizenCaseService.checkCaseActivation(request.caseId(), request.accessCode(), authorisation);
    }

    @ExceptionHandler(CaseActivationNotAllowedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public CaseActivationResponse handleCaseActivationNotAllowed() {
        return new CaseActivationResponse(LinkCitizenCaseService.NOT_READY);
    }

    @PostMapping(value = "/link-case-to-account")
    @Operation(description = "Linking case to citizen account with access code")
    public CaseData linkCitizenToCase(@RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
                                       @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
                                       @RequestBody @NotNull @Valid AccessCodeRequest accessCodeRequest) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            Optional<CaseDetails> caseDetails = linkCitizenCaseService.linkCitizenToCase(
                authorisation,
                accessCodeRequest.getCaseId(),
                accessCodeRequest.getAccessCode()
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

    @PostMapping(value = "/link-case-to-account-with-hearing")
    @Operation(description = "Linking case to citizen account with access code")
    public CaseDataWithHearingResponse linkCitizenToCaseWithHearing(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody @NotNull @Valid AccessCodeRequest accessCodeRequest) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            CaseDataWithHearingResponse caseDataWithHearingResponse = CaseDataWithHearingResponse.builder().build();
            Optional<CaseDetails> caseDetails = linkCitizenCaseService.linkCitizenToCase(
                authorisation,
                accessCodeRequest.getCaseId(),
                accessCodeRequest.getAccessCode()
            );
            if (caseDetails.isPresent()) {
                CaseData caseData = CaseUtils.getCaseData(caseDetails.get(), objectMapper);
                caseDataWithHearingResponse = caseDataWithHearingResponse
                    .toBuilder()
                    .caseData(UiCitizenCaseData.builder()
                                  .caseData(caseData)
                                  //This is a non-persistent view, list of citizen documents, orders & packs
                                  .citizenDocumentsManagement(
                                      caseService.getAllCitizenDocumentsOrders(authorisation, caseData))
                                  .build())
                    .build();

                if ("Yes".equalsIgnoreCase(accessCodeRequest.getHearingNeeded())) {
                    caseDataWithHearingResponse = caseDataWithHearingResponse.toBuilder()
                        .hearings(hearingService.getHearings(authorisation, accessCodeRequest.getCaseId()))
                        .build();
                }
                return caseDataWithHearingResponse;
            } else {
                throw (new RuntimeException(CASE_LINKING_FAILED));
            }
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(value = "/validate-access-code")
    @Operation(description = "Frontend to fetch the data")
    public String validateAccessCode(@RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
                                      @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
                                      @RequestBody @NotNull @Valid AccessCodeRequest accessCodeRequest) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            return linkCitizenCaseService.validateAccessCode(
                accessCodeRequest.getCaseId(),
                accessCodeRequest.getAccessCode(),
                authorisation
            );
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }
}
