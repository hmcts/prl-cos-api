package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.respondentsolicitor.documents.RespondentDocs;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CitizenResponseDocuments;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.C100RespondentSolicitorService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.citizen.CitizenResponseNotificationEmailService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C1A_FINAL_RESPONSE_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C7_FINAL_ENGLISH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C8_RESP_FINAL_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_C7_DRAFT_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.REVIEW_AND_SUBMIT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_C1A_FINAL_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.UNDERSCORE;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseApplicationResponseController {
    public static final String C_1_ARESPONSE = "C1Aresponse";
    public static final String DYNAMIC_FILE_NAME = "dynamic_fileName";
    private final DocumentGenService documentGenService;
    private final CoreCaseDataApi coreCaseDataApi;
    private final ObjectMapper objectMapper;
    private final CaseService caseService;
    private final CitizenResponseNotificationEmailService citizenResponseNotificationEmailService;
    private final C100RespondentSolicitorService c100RespondentSolicitorService;
    private final IdamClient idamClient;

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("ddmmyyyyy");


    @PostMapping(path = "/{caseId}/{partyId}/generate-c7document", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @Operation(description = "Generate a PDF for citizen as part of Respond to the Application")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document generated"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")})
    public Document generateC7DraftDocument(
        @PathVariable("caseId") String caseId,
        @PathVariable("partyId") String partyId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader("serviceAuthorization") String s2sToken) throws Exception {

        CaseDetails caseDetails = coreCaseDataApi.getCase(authorisation, s2sToken, caseId);
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        updateCurrentRespondent(caseData, YesOrNo.Yes, partyId);
        log.info(" Generating C7 draft document for respondent ");

        Document document = documentGenService.generateSingleDocument(
            authorisation,
            caseData,
            DOCUMENT_C7_DRAFT_HINT,
            false
        );
        log.info("C7 draft document generated successfully for respondent ");
        return document;
    }

    @PostMapping(path = "/{caseId}/{partyId}/generate-c7document-final", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @Operation(description = "Generate a PDF for citizen as part of Respond to the Application")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document generated"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")})
    public CaseData generateC7FinalDocument(
        @PathVariable("caseId") String caseId,
        @PathVariable("partyId") String partyId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader("serviceAuthorization") String s2sToken) throws Exception {

        CaseDetails caseDetails = coreCaseDataApi.getCase(authorisation, s2sToken, caseId);
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        updateCurrentRespondent(caseData, YesOrNo.Yes, partyId);
        log.info(" Generating C7 Final document for respondent ");
        Optional<Element<PartyDetails>> currentRespondent
            = caseData.getRespondents()
            .stream()
            .filter(
                respondent -> YesOrNo.Yes.equals(
                    respondent.getValue().getCurrentRespondent()))
            .findFirst();

        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        CaseDetails caseDetailsReturn = null;

        Document document = documentGenService.generateSingleDocument(
            authorisation,
            caseData,
            C7_FINAL_ENGLISH,
            false
        );

        log.info("C7 Final document generated successfully for respondent ");
        updateCurrentRespondent(caseData, null, partyId);
        if (document != null) {
            String partyName = caseData.getRespondents()
                .stream()
                .filter(element -> element.getId()
                    .toString()
                    .equalsIgnoreCase(partyId))
                .map(Element::getValue)
                .findFirst()
                .map(PartyDetails::getLabelForDynamicList)
                .orElse("");

            UserDetails userDetails = idamClient.getUserDetails(authorisation);
            caseData = generateOtherC1aAndC8Documents(
                authorisation,
                caseData,
                currentRespondent,
                callbackRequest,
                document,
                partyName,
                userDetails
            );
            log.info("C1A and C8 Final document generated successfully for respondent ");

            caseDetailsReturn = caseService.updateCase(
                caseData,
                authorisation,
                caseId,
                REVIEW_AND_SUBMIT
            );
        }

        if (caseDetailsReturn != null) {
            /**
             * send notification to Applicant solicitor for respondent's response
             */
            log.info("generateC7FinalDocument:: sending notification to applicant solicitor");
            citizenResponseNotificationEmailService.sendC100ApplicantSolicitorNotification(caseDetails);
            return objectMapper.convertValue(
                caseDetailsReturn.getData(),
                CaseData.class
            );
        }

        return objectMapper.convertValue(
            caseData,
            CaseData.class
        );
    }

    private CaseData generateOtherC1aAndC8Documents(String authorisation, CaseData caseData, Optional<Element<PartyDetails>> currentRespondent,
                                                    CallbackRequest callbackRequest, Document document, String partyName,
                                                    UserDetails userDetails) throws Exception {
        Document c1aFinalDocument = null;
        Document c8FinalDocument = null;
        if (currentRespondent.isPresent()) {
            Map<String, Object> dataMap = c100RespondentSolicitorService.populateDataMap(
                callbackRequest,
                currentRespondent.get()
            );

            if (isNotEmpty(currentRespondent.get().getValue().getResponse())
                && isNotEmpty(currentRespondent.get().getValue().getResponse().getSafetyConcerns())
                && Yes.equals(currentRespondent.get().getValue().getResponse().getSafetyConcerns().getHaveSafetyConcerns())) {
                c1aFinalDocument = documentGenService.generateSingleDocument(
                    authorisation,
                    caseData,
                    SOLICITOR_C1A_FINAL_DOCUMENT,
                    false,
                    dataMap
                );
            }

            caseData = generateRespondentC1aResponseDocuments(authorisation, caseData, currentRespondent, partyName, dataMap);

            RespondentDocs respondentDocs = RespondentDocs.builder().build();

            if (dataMap.containsKey("isConfidentialDataPresent")) {
                c8FinalDocument = documentGenService.generateSingleDocument(
                    authorisation,
                    caseData,
                    C8_RESP_FINAL_HINT,
                    false,
                    dataMap
                );
            }

            if (null != c1aFinalDocument) {
                respondentDocs = respondentDocs
                    .toBuilder()
                    .c1aDocument(ResponseDocuments
                                     .builder()
                                     .partyName(partyName)
                                     .createdBy(userDetails.getFullName())
                                     .dateCreated(LocalDate.now())
                                     .citizenDocument(c1aFinalDocument)
                                     .build()
                    )
                    .build();
            }

            if (null != document) {
                respondentDocs = respondentDocs
                    .toBuilder()
                    .c7Document(ResponseDocuments
                                    .builder()
                                    .partyName(partyName)
                                    .createdBy(userDetails.getFullName())
                                    .dateCreated(LocalDate.now())
                                    .citizenDocument(document)
                                    .build()
                    )
                    .build();
            }

            if (null != caseData.getRespondentDocsList()) {
                caseData.getRespondentDocsList().add(element(respondentDocs));
            } else {
                caseData.setRespondentDocsList(List.of(element(respondentDocs)));
            }

            populateC8Documents(caseData, currentRespondent.get(), partyName, userDetails, c8FinalDocument);
        }
        return caseData;
    }

    private CaseData generateRespondentC1aResponseDocuments(String authorisation,
                                             CaseData caseData,
                                             Optional<Element<PartyDetails>> currentRespondent,
                                             String partyName,
                                             Map<String, Object> dataMap) throws Exception {
        Document c1aEngFinalResponseDocument;
        log.info("inside generateRespondentC1aResponseDocuments()");
        if (isNotEmpty(currentRespondent.get().getValue().getResponse())
            && isNotEmpty(currentRespondent.get().getValue().getResponse().getResponseToAllegationsOfHarm())
            && Yes.equals(currentRespondent.get().getValue().getResponse().getResponseToAllegationsOfHarm()
                              .getResponseToAllegationsOfHarmYesOrNoResponse())) {
            String fileName = partyName + UNDERSCORE + C_1_ARESPONSE + UNDERSCORE + LocalDateTime.now(ZoneId.of(
                LONDON_TIME_ZONE)).format(dateTimeFormatter);
            dataMap.put(DYNAMIC_FILE_NAME, fileName + ".pdf");
            log.info("generating respondent C1A response documents");
            c1aEngFinalResponseDocument = documentGenService.generateSingleDocument(
                authorisation,
                caseData,
                C1A_FINAL_RESPONSE_DOCUMENT,
                false,
                dataMap
            );
            if (CollectionUtils.isNotEmpty(caseData.getRespondents()) && isNotEmpty(c1aEngFinalResponseDocument)) {
                log.info("generated respondent C1A response documents");
                Optional<Element<PartyDetails>> respondentElement = caseData.getRespondents().stream().filter(
                        element -> element.getId()
                            .toString()
                            .equalsIgnoreCase(currentRespondent.get().getId().toString()))
                    .findFirst();
                if (respondentElement.isPresent()) {
                    log.info("setting respondent C1A response documents");
                    caseData.getRespondents().set(
                        caseData.getRespondents().indexOf(respondentElement.get()),
                        ElementUtils.element(
                            respondentElement.get().getId(),
                            respondentElement.get().getValue().toBuilder()
                                .response(respondentElement.get().getValue().getResponse()
                                              .toBuilder()
                                              .responseToAllegationsOfHarm(respondentElement.get().getValue().getResponse()
                                                                               .getResponseToAllegationsOfHarm()
                                                                               .toBuilder()
                                                                               .responseToAllegationsOfHarmDocument(
                                                                                   c1aEngFinalResponseDocument)
                                                                               .build())
                                              .build())
                                .build()
                        )
                    );
                }
            }
            dataMap.remove(DYNAMIC_FILE_NAME);
        }
        return caseData;
    }

    private static void populateC8Documents(CaseData caseData, Element<PartyDetails> currentRespondent, String partyName,
                                            UserDetails userDetails, Document c8FinalDocument) {
        int partyIndex = caseData.getRespondents().indexOf(currentRespondent);
        if (null != c8FinalDocument && partyIndex >= 0) {
            ResponseDocuments c8ResponseDocuments = ResponseDocuments.builder()
                .partyName(partyName)
                .createdBy(userDetails.getFullName())
                .dateCreated(LocalDate.now())
                .citizenDocument(c8FinalDocument)
                .build();
            if (caseData.getCitizenResponseDocuments() == null) {
                caseData.setCitizenResponseDocuments(CitizenResponseDocuments.builder().build());
            }
            switch (partyIndex) {
                case 0:
                    caseData.toBuilder().citizenResponseDocuments(CitizenResponseDocuments.builder()
                                                                      .respondentAc8(c8ResponseDocuments).build());
                    break;
                case 1:
                    caseData.toBuilder().citizenResponseDocuments(CitizenResponseDocuments.builder()
                                                                      .respondentBc8(c8ResponseDocuments).build());
                    break;
                case 2:
                    caseData.toBuilder().citizenResponseDocuments(CitizenResponseDocuments.builder()
                                                                      .respondentCc8(c8ResponseDocuments).build());
                    break;
                case 3:
                    caseData.toBuilder().citizenResponseDocuments(CitizenResponseDocuments.builder()
                                                                      .respondentDc8(c8ResponseDocuments).build());
                    break;
                case 4:
                    caseData.toBuilder().citizenResponseDocuments(CitizenResponseDocuments.builder()
                                                                      .respondentEc8(c8ResponseDocuments).build());
                    break;
                default:
                    break;
            }
        }
    }

    private CaseData updateCurrentRespondent(CaseData caseData, YesOrNo currentRespondent, String partyId) {

        for (Element<PartyDetails> partyElement : caseData.getRespondents()) {
            if (partyElement.getId().toString().equalsIgnoreCase(partyId)) {
                PartyDetails respondent = partyElement.getValue();
                respondent.setCurrentRespondent(currentRespondent);
            }
        }
        return caseData;
    }
}

