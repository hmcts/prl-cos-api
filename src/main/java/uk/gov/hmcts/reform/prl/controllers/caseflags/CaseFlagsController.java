package uk.gov.hmcts.reform.prl.controllers.caseflags;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.caseflags.AllPartyFlags;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.datamigration.caseflag.CaseFlag;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.caseflags.CaseFlagsWaService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@RestController
@RequiredArgsConstructor
@RequestMapping("/caseflags")
public class CaseFlagsController {
    private static final String FLAG_TYPE = "PARTY";
    public static final String REQUESTED = "Requested";

    private final AuthorisationService authorisationService;
    private final CaseFlagsWaService caseFlagsWaService;
    private final ObjectMapper objectMapper;
    private final RefDataUserService refDataUserService;
    private final SystemUserService systemUserService;

    @PostMapping(path = "/setup-wa-task", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to validate case creator to decide on the WA task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public void setUpWaTaskForCaseFlags2(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            caseFlagsWaService.setUpWaTaskForCaseFlagsEventHandler(
                authorisation,
                callbackRequest
            );
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestHeader("Authorization")
                                                                   @Parameter(hidden = true) String authorisation,
                                                                   @RequestBody CallbackRequest callbackRequest) {
        CaseFlag caseFlag = refDataUserService.retrieveCaseFlags(systemUserService.getSysUserToken(), FLAG_TYPE);
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        AllPartyFlags allPartyFlags = caseData.getAllPartyFlags();
        List<String> errors = new ArrayList<>();
        try {
            if (allPartyFlags != null) {
                Field[] fields = allPartyFlags.getClass().getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    Object value = field.get(allPartyFlags);
                    Flags typeValue = (Flags) value;
                    if (typeValue != null && typeValue.getDetails() != null
                        && CollectionUtils.isNotEmpty(typeValue.getDetails())) {
                        if (!REQUESTED.equals(typeValue.getDetails().getFirst().getValue().getStatus())) {
                            field.set(allPartyFlags, Flags.builder().build());
                        }
                    }
                }
            }
        } catch (IllegalAccessException e) {
            errors.add(e.getMessage());
        }
        Map<String, Object> caseDataMap = caseData.toMap(CcdObjectMapper.getObjectMapper());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .data(caseDataMap)
            .build();
    }

    @PostMapping(path = "/check-wa-task-status", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to check the work allocation status to decide if the task should be closed or not")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse checkWorkAllocationTaskStatus(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {

        CaseData caseDataBefore = CaseUtils.getCaseData(callbackRequest.getCaseDetailsBefore(), objectMapper);
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        AllPartyFlags allPartyFlagsBefore = caseDataBefore.getAllPartyFlags();
        AllPartyFlags allPartyFlags = caseData.getAllPartyFlags();

        List<String> errors = new ArrayList<>();
        try {
            if (allPartyFlags != null & allPartyFlagsBefore != null) {
                Field[] fieldsBefore = allPartyFlagsBefore.getClass().getDeclaredFields();
                Field[] fields = allPartyFlags.getClass().getDeclaredFields();
                for (int i = 0; i < fieldsBefore.length; i++) {
                    fieldsBefore[i].setAccessible(true);
                    fields[i].setAccessible(true);
                    Flags fieldValueBefore = (Flags) fieldsBefore[i].get(allPartyFlagsBefore);
                    Flags fieldValue = (Flags) fields[i].get(allPartyFlags);
                    if (fieldValue != null && CollectionUtils.isNotEmpty(fieldValue.getDetails())) {
                        if (!fieldValueBefore.getDetails().getFirst().getValue().getStatus()
                            .equals(fieldValue.getDetails().getFirst().getValue().getStatus())) {
                            if (REQUESTED.equals(fieldValue.getDetails().getFirst().getValue().getStatus())) {
                                errors.add("Please select the status of flag other than Requested");
                                break;
                            }
                        }
                    }
                }
            }
        } catch (IllegalAccessException e) {
            errors.add(e.getMessage());
        }


        Map<String, Object> caseDataMap = caseData.toMap(CcdObjectMapper.getObjectMapper());
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .data(caseDataMap)
            .build();
    }

}
