package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.events.TransferToAnotherCourtEvent;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.LocalCourtAdminEmail;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.court.CourtEmailAddress;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AmendCourtService;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.LocationRefDataService;
import uk.gov.hmcts.reform.prl.services.cafcass.CafcassDateTimeService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_ID_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_LIST;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TransferCourtController {

    private static final String CONFIRMATION_HEADER = "# Case transferred to another court ";
    private static final String CONFIRMATION_BODY_PREFIX = "The case has been transferred to ";
    private static final String CONFIRMATION_BODY_SUFFIX = " \n\n Local court admin have been notified ";

    private final ObjectMapper objectMapper;
    private final LocationRefDataService locationRefDataService;
    private final AuthorisationService authorisationService;
    private final AmendCourtService amendCourtService;
    private final CafcassDateTimeService cafcassDateTimeService;
    private final AllTabServiceImpl allTabsService;
    private final CourtFinderService courtLocatorService;
    private final EventService eventPublisher;

    @PostMapping(path = "/transfer-court/about-to-start", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to Issue and send to local court")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse amendCourtAboutToStart(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest) {

        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            CaseData caseData = objectMapper.convertValue(
                callbackRequest.getCaseDetails().getData(),
                CaseData.class
            );
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            List<DynamicListElement> courtList;
            if (Event.TRANSFER_TO_ANOTHER_COURT.getId().equalsIgnoreCase(callbackRequest.getEventId())) {
                courtList = C100_CASE_TYPE.equals(CaseUtils.getCaseTypeOfApplication(caseData))
                    ? locationRefDataService.getFilteredCourtLocations(authorisation) :
                    locationRefDataService.getDaFilteredCourtLocations(authorisation);
            } else {
                courtList = locationRefDataService.getCourtLocations(authorisation);
            }
            addAndSelectCourtList(caseDataUpdated, courtList, authorisation);
            caseDataUpdated.put(CASE_TYPE_OF_APPLICATION, CaseUtils.getCaseTypeOfApplication(caseData));
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    private void addAndSelectCourtList(Map<String, Object> caseDataUpdated, List<DynamicListElement> courtList,
                                       String authorisation) {
        String selectedCourtId = String.valueOf(caseDataUpdated.get(COURT_ID_FIELD));
        DynamicListElement selectedCourtElement = locationRefDataService
            .getDisplayEntryFromEpimmsId(selectedCourtId, authorisation);
        caseDataUpdated.put(COURT_LIST, DynamicList.builder().value(selectedCourtElement).listItems(courtList)
            .build());
    }

    @PostMapping(path = "/transfer-court/validate-court-fields", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to validate court fields")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Child details are fetched"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse validateCourtFields(
        @RequestBody CallbackRequest callbackRequest
    ) {
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );
        List<String> errorList = new ArrayList<>();
        if (amendCourtService.validateCourtFields(caseData, errorList)) {
            return uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse.builder()
                .errors(errorList)
                .build();
        }
        return uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse.builder()
            .data(caseData)
            .build();
    }

    @PostMapping(path = "/transfer-court/validate-court-email", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to validate court email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Court email is validated"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse validateTransferCourtEmail(
        @RequestBody CallbackRequest callbackRequest) {
        List<String> errorList = amendCourtService.validateCourtEmailAddress(callbackRequest);
        if (CollectionUtils.isNotEmpty(errorList)) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errorList)
                .build();
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(callbackRequest.getCaseDetails().getData())
            .build();
    }

    @PostMapping(path = "/transfer-court/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to Issue and send to local court")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse amendCourtAboutToSubmit(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            amendCourtService.handleAmendCourtSubmission(authorisation, callbackRequest, caseDataUpdated);
            cafcassDateTimeService.updateCafcassDateTime(callbackRequest);
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/update-application", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to refresh the tabs")
    @SecurityRequirement(name = "Bearer Authentication")
    public void updateApplication(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            allTabsService.updateAllTabsIncludingConfTab(String.valueOf(callbackRequest.getCaseDetails().getId()));
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/pre-populate-court-details", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to Generate document after submit application")
    public AboutToStartOrSubmitCallbackResponse prePopulateCourtDetails(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) throws NotFoundException {

        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            Court closestChildArrangementsCourt = courtLocatorService
                .getNearestFamilyCourt(caseData);
            Optional<CourtEmailAddress> courtEmailAddress = closestChildArrangementsCourt == null ? Optional.empty() : courtLocatorService
                .getEmailAddress(closestChildArrangementsCourt);
            if (courtEmailAddress.isPresent()) {
                log.info("Found court email for case id {}", caseData.getId());
                caseDataUpdated.put("localCourtAdmin", List.of(
                    Element.<LocalCourtAdminEmail>builder().value(LocalCourtAdminEmail.builder().email(courtEmailAddress.get().getAddress()).build())
                        .build()));
            } else {
                log.info("Court email not found for case id {}", caseData.getId());
            }
            List<DynamicListElement> courtList = locationRefDataService.getCourtLocations(authorisation);
            addAndSelectCourtList(caseDataUpdated, courtList, authorisation);
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/transfer-court/transfer-court-confirmation",
        consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to create confirmation of transfer court ")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = uk.gov.hmcts.reform.ccd.client.model.CallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public ResponseEntity<SubmittedCallbackResponse> transferCourtConfirmation(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        TransferToAnotherCourtEvent event =
            prepareTransferToAnotherCourtEvent(authorisation, caseData,
                                               Event.TRANSFER_TO_ANOTHER_COURT.getName()
            );
        eventPublisher.publishEvent(event);
        return ok(SubmittedCallbackResponse.builder().confirmationHeader(
            CONFIRMATION_HEADER).confirmationBody(
            CONFIRMATION_BODY_PREFIX + caseData.getCourtName()
                + CONFIRMATION_BODY_SUFFIX
        ).build());
    }

    private TransferToAnotherCourtEvent prepareTransferToAnotherCourtEvent(String authorisation, CaseData newCaseData,
                                                                           String typeOfEvent) {
        return TransferToAnotherCourtEvent.builder()
            .authorisation(authorisation)
            .caseData(newCaseData)
            .typeOfEvent(typeOfEvent)
            .build();
    }


}

