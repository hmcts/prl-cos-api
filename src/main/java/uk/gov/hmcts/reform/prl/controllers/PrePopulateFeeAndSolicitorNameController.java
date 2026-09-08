package uk.gov.hmcts.reform.prl.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.framework.exceptions.DocumentGenerationException;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.models.user.UserRoles;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.DgsService;
import uk.gov.hmcts.reform.prl.services.DocumentLanguageService;
import uk.gov.hmcts.reform.prl.services.MiamPolicyUpgradeService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.document.C100DocumentTemplateFinderService;
import uk.gov.hmcts.reform.prl.services.payment.FeeService;
import uk.gov.hmcts.reform.prl.services.validators.SubmitAndPayChecker;
import uk.gov.hmcts.reform.prl.utils.DocumentUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.config.DocumentGenerationExecutorVirtualConfig.DOCUMENT_EXECUTOR_SERVICE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CURRENCY_SIGN_POUND;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V3;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.wrapElements;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
public class PrePopulateFeeAndSolicitorNameController {
    private final FeeService feeService;
    private final UserService userService;
    private final CourtFinderService courtLocatorService;
    private final SubmitAndPayChecker submitAndPayChecker;
    private final DgsService dgsService;
    private final C100DocumentTemplateFinderService c100DocumentTemplateFinderService;
    private final OrganisationService organisationService;
    private final DocumentLanguageService documentLanguageService;
    private final AuthorisationService authorisationService;
    @Qualifier(DOCUMENT_EXECUTOR_SERVICE)
    private final ExecutorService documentVirtualThreadExecutorService;

    @Value("${document.templates.c100.c100_draft_filename}")
    protected String c100DraftFilename;

    @Value("${document.templates.c100.c100_draft_welsh_filename}")
    protected String c100DraftWelshFilename;

    @Value("${southampton.court.email-address}")
    protected String southamptonCourtEmailAddress;

    private final MiamPolicyUpgradeService miamPolicyUpgradeService;

    @PostMapping(path = "/getSolicitorAndFeeDetails", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to get Solicitor name and fee amount. ")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User name received."),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public CallbackResponse prePopulateSolicitorAndFees(
        @RequestHeader("Authorization") @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) throws Exception {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            List<String> errorList = new ArrayList<>();
            CaseData caseData = null;
            boolean mandatoryEventStatus = submitAndPayChecker.hasMandatoryCompleted(callbackRequest
                                                                                         .getCaseDetails().getCaseData());

            if (!mandatoryEventStatus) {
                errorList.add(
                    "Submit and pay is not allowed for this case unless you finish all the mandatory events");
            } else {
                FeeResponse feeResponse;
                try {
                    feeResponse = feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE);
                } catch (Exception e) {
                    errorList.add(e.getMessage());
                    return CallbackResponse.builder()
                        .errors(errorList)
                        .build();
                }
                Court closestChildArrangementsCourt = courtLocatorService
                    .getNearestFamilyCourt(callbackRequest.getCaseDetails()
                                               .getCaseData());

                CaseData baseCaseData = callbackRequest.getCaseDetails().getCaseData();

                UserDetails userDetails = userService.getUserDetails(authorisation);

                caseData = baseCaseData.toBuilder()
                    .caseSolicitorName(userDetails.getFullName()) //adding caseSolicitorName for SOT
                    .solicitorName(userDetails.getFullName())
                    .userInfo(wrapElements(userService.getUserInfo(authorisation, UserRoles.SOLICITOR)))
                    .applicantSolicitorEmailAddress(userDetails.getEmail())
                    .caseworkerEmailAddress(southamptonCourtEmailAddress)
                    .feeAmount(CURRENCY_SIGN_POUND + feeResponse.getAmount().toString())
                    .courtName((closestChildArrangementsCourt != null) ? closestChildArrangementsCourt.getCourtName() : "No Court Fetched")
                    .build();
                if (TASK_LIST_VERSION_V3.equalsIgnoreCase(caseData.getTaskListVersion())
                    && isNotEmpty(caseData.getMiamPolicyUpgradeDetails())) {
                    caseData = miamPolicyUpgradeService.updateMiamPolicyUpgradeDetails(
                        caseData,
                        new HashMap<>()
                    );
                }
                // Pass only the merged caseData to the document generation so new solicitor name is not lost
                caseData = buildGeneratedDocumentCaseData(
                    authorisation,
                    callbackRequest,
                    caseData
                );
            }

            return CallbackResponse.builder()
                .data(caseData)
                .errors(errorList)
                .build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    /**
     * Helper method to cancel Future tasks.
     * @param future the future task to be cancelled
     */
    private void cancelFuture(Future<?> future) {
        if (future != null && !future.isDone()) {
            future.cancel(true);
        }
    }

    private CaseData buildGeneratedDocumentCaseData(
        @RequestHeader("Authorization") String authorisation,
        @RequestBody CallbackRequest callbackRequest,
        CaseData caseData) {

        // Clone for templating (do NOT mutate the original as we don't want transient orgs)
        CaseData caseDataForDocs = caseData;

        // Enrich with transient org info
        try {
            if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
                caseDataForDocs = organisationService.getApplicantOrganisationDetails(caseDataForDocs);
                caseDataForDocs = organisationService.getRespondentOrganisationDetails(caseDataForDocs);
            } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
                caseDataForDocs = organisationService.getApplicantOrganisationDetailsForFL401(caseDataForDocs);
                caseDataForDocs = organisationService.getRespondentOrganisationDetailsForFL401(caseDataForDocs);
            }
            // caseDataForDocs now has PartyDetails.organisations populated for doc merge only
        } catch (Exception e) {
            log.warn("Unable to enrich organisation details for doc generation: {}", e.getMessage());
        }

        //Copy case data into an effectively final variable to be able to use it in lambdas and multithreading
        CaseData documentCaseData = caseDataForDocs;
        CaseDetails documentCaseDetails = CaseDetails.builder()
            .caseData(documentCaseData)
            .build();

        DocumentLanguage documentLanguage =
            documentLanguageService.docGenerateLang(callbackRequest.getCaseDetails().getCaseData());

        //Tasks
        Future<GeneratedDocumentInfo> englishFuture = null;
        Future<GeneratedDocumentInfo> welshFuture = null;

        //Keep template selection outside the virtual-thread lambdas so only the remote Docmosis calls
        //are submitted to the Executor Service
        //Submit both tasks (remote Docmosis calls) before waiting for a result
        if (documentLanguage.isGenEng()) {
            String englishTemplate = c100DocumentTemplateFinderService
                .findFinalDraftDocumentTemplate(documentCaseData, false);
            englishFuture = documentVirtualThreadExecutorService.submit(
                () -> dgsService.generateDocument(
                    authorisation,
                    documentCaseDetails,
                    englishTemplate
                )
            );
        }

        if (documentLanguage.isGenWelsh()) {
            String welshTemplate = c100DocumentTemplateFinderService
                .findFinalDraftDocumentTemplate(documentCaseData, true);
            welshFuture = documentVirtualThreadExecutorService.submit(
                () -> dgsService.generateWelshDocument(
                    authorisation,
                    documentCaseDetails,
                    welshTemplate
                )
            );
        }

        //Results
        GeneratedDocumentInfo englishDocumentInfo = null;
        GeneratedDocumentInfo welshDocumentInfo = null;
        //Obtain the results from the Virtual Threads
        try {
            if (englishFuture != null) {
                englishDocumentInfo = englishFuture.get();
            }

            if (welshFuture != null) {
                welshDocumentInfo = welshFuture.get();
            }
        } catch (InterruptedException e) {
            cancelFuture(englishFuture);
            cancelFuture(welshFuture);
            Thread.currentThread().interrupt();
            throw new DocumentGenerationException(
                "C100 Draft document generation interrupted",
                e
            );
        } catch (ExecutionException e) {
            cancelFuture(englishFuture);
            cancelFuture(welshFuture);
            throw new DocumentGenerationException(
                "C100 Draft document generation failed",
                e.getCause()
            );
        }

        if (englishDocumentInfo != null) {
            caseData = caseData.toBuilder()
                .isEngDocGen(Yes.toString())
                .submitAndPayDownloadApplicationLink(
                    DocumentUtils.toDocumentWithFilename(englishDocumentInfo, c100DraftFilename)
                )
                .build();
        }

        if (welshDocumentInfo != null) {
            caseData = caseData.toBuilder()
                .isWelshDocGen(Yes.toString())
                .submitAndPayDownloadApplicationWelshLink(
                    DocumentUtils.toDocumentWithFilename(welshDocumentInfo, c100DraftWelshFilename)
                )
                .build();
        }

        return caseData;
    }
}
