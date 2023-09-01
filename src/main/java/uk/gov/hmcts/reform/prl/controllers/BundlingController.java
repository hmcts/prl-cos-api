package uk.gov.hmcts.reform.prl.controllers;

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
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.bundle.BundlingService;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping("/bundle")
public class BundlingController extends AbstractCallbackController {
    @Autowired
    private BundlingService bundlingService;

    @Autowired
    private AuthorisationService authorisationService;

    @PostMapping(path = "/createBundle", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Creating bundle. ")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bundle Created Successfully ."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})

    public AboutToStartOrSubmitCallbackResponse createBundle(@RequestHeader("Authorization") @Parameter(hidden = true) String authorization,
                                                             @RequestHeader("ServiceAuthorization") @Parameter(hidden = true)
                                                             String serviceAuthorization,
                                                             @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorization,serviceAuthorization)) {
            CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            moveExistingCaseBundlesToHistoricalBundles(caseData);
            log.info("*** Creating Bundle for the case id : {}", caseData.getId());
            BundleCreateResponse bundleCreateResponse = bundlingService.createBundleServiceRequest(caseData,
                                                                                                   callbackRequest.getEventId(),
                                                                                                   authorization
            );
            if (null != bundleCreateResponse && null != bundleCreateResponse.getData() && null != bundleCreateResponse.getData().getCaseBundles()) {
                caseDataUpdated.put(
                    "bundleInformation",
                    BundlingInformation.builder().caseBundles(removeEmptyFolders(bundleCreateResponse.getData().getCaseBundles()))
                        .historicalBundles(caseData.getBundleInformation().getHistoricalBundles())
                        .bundleConfiguration(bundleCreateResponse.data.getBundleConfiguration())
                        .bundleCreationDateAndTime(DateTimeFormatter.ISO_OFFSET_DATE_TIME
                                                       .format(ZonedDateTime.now(ZoneId.of("Europe/London"))))
                        .bundleHearingDateAndTime(null != bundleCreateResponse.getData().getData()
                                                      && null != bundleCreateResponse.getData().getData().getHearingDetails().getHearingDateAndTime()
                                                      ? bundleCreateResponse.getData().getData().getHearingDetails().getHearingDateAndTime() : "")
                        .build()
                );
                log.info(
                    "*** Bundle created successfully.. Updating bundle Information in case data for the case id: {}",
                    caseData.getId()
                );
            }
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }


    private List<Bundle> removeEmptyFolders(List<Bundle> caseBundles) {
        List<Bundle> caseBundlesPostEmptyFoldersRemoval = new ArrayList<>();
        if (!caseBundles.isEmpty()) {
            caseBundles.forEach(bundle -> {
                List<BundleFolder> folders = bundle.getValue().getFolders();
                if (null != folders) {
                    List<BundleFolder> foldersAfterEmptyRemoval = new ArrayList<>();
                    folders.forEach(rootFolder -> {
                        if (null != rootFolder.getValue().getFolders()) {
                            List<BundleSubfolder> bundleSubfoldersAfterEmptyRemoval = new ArrayList<>();
                            rootFolder.getValue().getFolders().forEach(rootSubFolder -> {
                                List<BundleNestedSubfolder1> bundleNestedSubfolder1AfterRemoval = new ArrayList<>();
                                checkAndGetNonEmptyBundleNestedSubfolders(rootSubFolder,bundleNestedSubfolder1AfterRemoval);
                                updateBundleSubFoldersWithNonEmptyNestedSubFolders(rootSubFolder,
                                    bundleSubfoldersAfterEmptyRemoval,bundleNestedSubfolder1AfterRemoval);
                            });
                            updateBundleFoldersWithNonEmptySubfolders(rootFolder,foldersAfterEmptyRemoval,bundleSubfoldersAfterEmptyRemoval);
                        }
                    });
                    updateBundleWithNonEmptyfolders(bundle,foldersAfterEmptyRemoval,caseBundlesPostEmptyFoldersRemoval);
                } else {
                    caseBundlesPostEmptyFoldersRemoval.add(bundle);
                }
            });

        }
        return caseBundlesPostEmptyFoldersRemoval;
    }

    private void updateBundleWithNonEmptyfolders(Bundle bundle, List<BundleFolder> foldersAfterEmptyRemoval,
                                                 List<Bundle> caseBundlesPostEmptyfoldersRemoval) {
        if (!foldersAfterEmptyRemoval.isEmpty()) {
            bundle.getValue().setFolders(foldersAfterEmptyRemoval);
            caseBundlesPostEmptyfoldersRemoval.add(bundle);
        }
    }

    private void updateBundleFoldersWithNonEmptySubfolders(BundleFolder rootFolder, List<BundleFolder> foldersAfterEmptyRemoval,
                                                           List<BundleSubfolder> bundleSubfoldersAfterEmptyRemoval) {
        if (!bundleSubfoldersAfterEmptyRemoval.isEmpty()) {
            foldersAfterEmptyRemoval.add(BundleFolder.builder()
                .value(BundleFolderDetails.builder().name(rootFolder.getValue().getName())
                    .folders(bundleSubfoldersAfterEmptyRemoval).build()).build());
        }
    }

    private void updateBundleSubFoldersWithNonEmptyNestedSubFolders(BundleSubfolder rootSubFolder,
                                                                    List<BundleSubfolder> bundleSubfoldersAfterEmptyRemoval,
                                                                    List<BundleNestedSubfolder1> bundleNestedSubfolder1AfterRemoval) {
        if (!bundleNestedSubfolder1AfterRemoval.isEmpty()) {
            bundleSubfoldersAfterEmptyRemoval.add(BundleSubfolder.builder()
                .value(BundleSubfolderDetails.builder().name(rootSubFolder.getValue().getName())
                    .folders(bundleNestedSubfolder1AfterRemoval).build()).build());
        }
    }

    private void checkAndGetNonEmptyBundleNestedSubfolders(BundleSubfolder rootSubFolder,
                                                           List<BundleNestedSubfolder1> bundleNestedSubfolder1AfterRemoval) {
        if (null != rootSubFolder.getValue().getFolders()) {
            rootSubFolder.getValue().getFolders().forEach(bundleNestedSubfolder1 -> {
                List<BundleDocument> bundleDocumentsPostEmptyRemoval = new ArrayList<>();
                if (null != bundleNestedSubfolder1.getValue().getDocuments()) {
                    bundleDocumentsPostEmptyRemoval = bundleNestedSubfolder1.getValue().getDocuments().stream()
                        .filter(bundleDocument -> nonNull(bundleDocument.getValue().getSourceDocument()))
                        .toList();
                    if (!bundleDocumentsPostEmptyRemoval.isEmpty()) {
                        bundleNestedSubfolder1AfterRemoval.add(BundleNestedSubfolder1.builder()
                            .value(BundleNestedSubfolder1Details.builder().name(bundleNestedSubfolder1.getValue().getName())
                                .documents(bundleDocumentsPostEmptyRemoval).build()).build());
                    }
                }
            });
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
                historicalBundles.addAll(existingBundleInformation.getCaseBundles());
            }
            existingBundleInformation.setHistoricalBundles(historicalBundles);
            existingBundleInformation.setCaseBundles(null);
        } else {
            caseData.setBundleInformation(BundlingInformation.builder().build());
        }
    }
}
