package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.bundle.Bundle;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleCreateResponse;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleDetails;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingInformation;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.bundle.BundlingService;
import uk.gov.hmcts.reform.prl.services.cafcass.CafcassDateTimeService;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;
import static uk.gov.hmcts.reform.prl.mapper.bundle.BundleCreateRequestMapper.getBundleDateTime;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/bundle")
public class BundlingController extends AbstractCallbackController {
    private final BundlingService bundlingService;
    private final AuthorisationService authorisationService;
    private final CafcassDateTimeService cafcassDateTimeService;

    @Autowired
    protected BundlingController(ObjectMapper objectMapper,
                                 EventService eventPublisher,
                                 BundlingService bundlingService,
                                 AuthorisationService authorisationService,
                                 CafcassDateTimeService cafcassDateTimeService) {
        super(objectMapper, eventPublisher);
        this.bundlingService = bundlingService;
        this.authorisationService = authorisationService;
        this.cafcassDateTimeService = cafcassDateTimeService;
    }

    @PostMapping(path = "/createBundle", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Creating bundle. ")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bundle Created Successfully ."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})

    public AboutToStartOrSubmitCallbackResponse createBundle(@RequestHeader("Authorization") @Parameter(hidden = true) String authorization,
                                                             @RequestHeader("ServiceAuthorization") @Parameter(hidden = true)
                                                                 String serviceAuthorization,
                                                             @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorization, serviceAuthorization)) {
            CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            moveExistingCaseBundlesToHistoricalBundles(caseData);
            log.info("*** Creating Bundle for the case id : {}", caseData.getId());
            BundleCreateResponse bundleCreateResponse = bundlingService.createBundleServiceRequest(
                caseData,
                callbackRequest.getEventId(),
                authorization
            );
            if (null != bundleCreateResponse && null != bundleCreateResponse.getData() && null != bundleCreateResponse.getData().getCaseBundles()) {
                caseDataUpdated.put(
                    "bundleInformation",
                    BundlingInformation.builder().caseBundles(bundleCreateResponse.getData().getCaseBundles())
                        .historicalBundles(caseData.getBundleInformation().getHistoricalBundles())
                        .bundleConfiguration(bundleCreateResponse.data.getBundleConfiguration())
                        .bundleCreationDateAndTime(getBundleDateTime(ZonedDateTime.now(ZoneId.of("UTC")).toLocalDateTime()))
                        .bundleHearingDateAndTime(null != bundleCreateResponse.getData().getData()
                                                      && null != bundleCreateResponse.getData().getData().getHearingDetails().getHearingDateAndTime()
                                                      ? bundleCreateResponse.getData().getData().getHearingDetails().getHearingDateAndTime() : "")
                        .build()
                );
                log.info(
                    "*** Bundle created successfully.. Updating bundle Information in case data for the case id: {}",
                    caseData.getId()
                );
                cafcassDateTimeService.updateCafcassDateTime(callbackRequest);
            }
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    private void moveExistingCaseBundlesToHistoricalBundles(CaseData caseData) {
        List<Bundle> historicalBundles = new ArrayList<>();
        BundlingInformation existingBundleInformation = caseData.getBundleInformation();
        if (nonNull(existingBundleInformation)) {
            if (nonNull(existingBundleInformation.getHistoricalBundles())) {
                historicalBundles.addAll(existingBundleInformation.getHistoricalBundles());
            }
            if (nonNull(existingBundleInformation.getCaseBundles())) {
                List<Bundle> existingCaseBundles = existingBundleInformation.getCaseBundles();

                existingCaseBundles.stream().forEach(existingBundle -> {
                    BundleDetails bundleDetails = existingBundle.getValue().toBuilder()
                        .historicalStitchedDocument(existingBundle.getValue().getStitchedDocument())
                        .stitchedDocument(null).build();
                    existingCaseBundles.set(existingCaseBundles.indexOf(existingBundle),Bundle.builder().value(bundleDetails).build());
                }

                );
                historicalBundles.addAll(existingCaseBundles);
            }
            existingBundleInformation.setHistoricalBundles(historicalBundles);
            existingBundleInformation.setCaseBundles(null);
        } else {
            caseData.setBundleInformation(BundlingInformation.builder().build());
        }
    }
}
