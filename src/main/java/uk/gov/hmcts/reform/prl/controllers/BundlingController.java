package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
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
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleDocument;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleFolder;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleFolderDetails;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleNestedSubfolder1;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleNestedSubfolder1Details;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleSubfolder;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleSubfolderDetails;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingInformation;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.bundle.BundlingService;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping("/bundle")
public class BundlingController extends AbstractCallbackController {
    @Autowired
    private BundlingService bundlingService;

    @PostMapping(path = "/createBundle", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Creating bundle. ")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bundle Created Successfully ."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})

    public AboutToStartOrSubmitCallbackResponse createBundle(@RequestHeader("Authorization") @Parameter(hidden = true) String authorization,
                                                             @RequestHeader("ServiceAuthorization") @Parameter(hidden = true)
                                                             String serviceAuthorization,
                                                             @RequestBody CallbackRequest callbackRequest)
        throws Exception {

        //log.info("*** callRecieved to createBundle api in prl-cos-api : {}", callbackRequest.toString());
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        moveExistingCaseBundlesToHistoricalBundles(caseData);
        log.info("*** Creating Bundle for the case id : {}", caseData.getId());
        BundleCreateResponse bundleCreateResponse = bundlingService.createBundleServiceRequest(caseData,
            callbackRequest.getEventId(), authorization);
        log.info("*** Bundle response from api : {}", new ObjectMapper().writeValueAsString(bundleCreateResponse));
        if (null != bundleCreateResponse && null != bundleCreateResponse.getData() && null != bundleCreateResponse.getData().getCaseBundles()) {
            caseDataUpdated.put("bundleInformation",
                BundlingInformation.builder().caseBundles(removeEmptyFolders(bundleCreateResponse.getData().getCaseBundles()))
                    .historicalBundles(caseData.getBundleInformation().getHistoricalBundles())
                    .bundleConfiguration(bundleCreateResponse.data.getBundleConfiguration())
                    .bundleCreationDateAndTime(DateTimeFormatter.ISO_OFFSET_DATE_TIME
                        .format(ZonedDateTime.now(ZoneId.of("Europe/London"))).toString())
                    .bundleHearingDateAndTime(null != bundleCreateResponse.getData().getData()
                        && null != bundleCreateResponse.getData().getData().getHearingDetails().getHearingDateAndTime()
                        ? bundleCreateResponse.getData().getData().getHearingDetails().getHearingDateAndTime() : "")
                    .build());
            log.info("*** Bundle information post emptyfolders removal from api : {}",
                new ObjectMapper().writeValueAsString(caseDataUpdated.get("bundleInformation")));
            log.info("*** Bundle created successfully.. Updating bundle Information in case data for the case id: {}", caseData.getId());
        }
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }


    private List<Bundle> removeEmptyFolders(List<Bundle> caseBundles) {
        List<Bundle> caseBundlesPostEmptyfoldersRemoval = new ArrayList<>();
        if (caseBundles.size() > 0) {
            caseBundles.stream().forEach(bundle -> {
                List<BundleFolder> folders = bundle.getValue().getFolders();
                if (null != folders) {
                    folders.stream().forEach(rootFolder -> {
                        List<BundleFolder> foldersAfterEmptyRemoval = new ArrayList<>();
                        if (null != rootFolder.getValue().getFolders()) {
                            List<BundleSubfolder> bundleSubfoldersAfterEmptyRemoval = new ArrayList<>();
                            rootFolder.getValue().getFolders().stream().forEach(rootSubFolder -> {

                                List<BundleNestedSubfolder1> bundleNestedSubfolder1AfterRemoval = new ArrayList<>();
                                if (null != rootSubFolder.getValue().getFolders()) {
                                    rootSubFolder.getValue().getFolders().stream().forEach(bundleNestedSubfolder1 -> {
                                        List<BundleDocument> bundleDocumentsPostEmptyRemoval = new ArrayList<>();
                                        if (null != bundleNestedSubfolder1.getValue().getDocuments()) {
                                            bundleDocumentsPostEmptyRemoval = bundleNestedSubfolder1.getValue().getDocuments().stream()
                                                .filter(bundleDocument -> nonNull(bundleDocument.getValue().getSourceDocument()))
                                                .collect(Collectors.toList());
                                            if (bundleDocumentsPostEmptyRemoval.size() > 0) {
                                                bundleNestedSubfolder1AfterRemoval.add(BundleNestedSubfolder1.builder()
                                                    .value(BundleNestedSubfolder1Details.builder().name(bundleNestedSubfolder1.getValue().getName())
                                                        .documents(bundleDocumentsPostEmptyRemoval).build()).build());
                                            }
                                        }
                                    });
                                }
                                if (bundleNestedSubfolder1AfterRemoval.size() > 0) {
                                    bundleSubfoldersAfterEmptyRemoval.add(BundleSubfolder.builder()
                                        .value(BundleSubfolderDetails.builder().name(rootSubFolder.getValue().getName())
                                            .folders(bundleNestedSubfolder1AfterRemoval).build()).build());
                                }
                            });
                            if (bundleSubfoldersAfterEmptyRemoval.size() > 0) {
                                foldersAfterEmptyRemoval.add(BundleFolder.builder()
                                    .value(BundleFolderDetails.builder().name(rootFolder.getValue().getName())
                                        .folders(bundleSubfoldersAfterEmptyRemoval).build()).build());
                            }
                        }
                        if (foldersAfterEmptyRemoval.size() > 0) {
                            bundle.getValue().setFolders(foldersAfterEmptyRemoval);
                            caseBundlesPostEmptyfoldersRemoval.add(bundle);
                        }
                    });

                }

            });

        }
        return caseBundlesPostEmptyfoldersRemoval;
    }

    private void moveExistingCaseBundlesToHistoricalBundles(CaseData caseData) {
        List<Bundle> historicalBundles = new ArrayList<>();
        BundlingInformation existingBundleInformation = caseData.getBundleInformation();
        if (nonNull(existingBundleInformation)) {
            if (nonNull(existingBundleInformation.getHistoricalBundles())) {
                historicalBundles.addAll(existingBundleInformation.getHistoricalBundles());
            }
            if (nonNull(existingBundleInformation.getCaseBundles())) {
                historicalBundles.addAll(existingBundleInformation.getCaseBundles());
            }
            existingBundleInformation.setHistoricalBundles(historicalBundles);
            existingBundleInformation.setCaseBundles(null);
        } else {
            caseData.setBundleInformation(BundlingInformation.builder().build());
        }
    }
}
