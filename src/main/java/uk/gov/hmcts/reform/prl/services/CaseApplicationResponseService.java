package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.respondentsolicitor.documents.RespondentDocs;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CitizenResponseDocuments;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DocumentManagementDetails;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.C100RespondentSolicitorService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C7_FINAL_ENGLISH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C8_RESP_FINAL_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_C7_DRAFT_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.REVIEW_AND_SUBMIT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_C1A_FINAL_DOCUMENT;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseApplicationResponseService {

    private final DocumentGenService documentGenService;
    private final C100RespondentSolicitorService c100RespondentSolicitorService;
    private final CaseService caseService;
    private final IdamClient idamClient;

    public CaseData updateCurrentRespondent(CaseData caseData, YesOrNo currentRespondent, String partyId) {

        for (Element<PartyDetails> partyElement : caseData.getRespondents()) {
            if (partyElement.getId().toString().equalsIgnoreCase(partyId)) {
                PartyDetails respondent = partyElement.getValue();
                respondent.setCurrentRespondent(currentRespondent);
            }
        }
        return caseData;
    }

    public CaseDetails generateCitizenResponseFinalDocuments(CaseData caseData, CaseDetails caseDetails, String authorisation, String partyId,
                                                             String caseId, String s2sToken) throws Exception {
        Optional<Element<PartyDetails>> currentRespondent
            = caseData.getRespondents()
            .stream()
            .filter(
                respondent -> YesOrNo.Yes.equals(
                    respondent.getValue().getCurrentRespondent()))
            .findFirst();

        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        CaseDetails caseDetailsReturn;

        log.info(" Generating C7 Final document for respondent ");
        Document document = generateFinalC7(caseData, authorisation);
        log.info("C7 Final document generated successfully for respondent ");

        if (document != null && null != caseDetails) {
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
                s2sToken,
                caseId,
                REVIEW_AND_SUBMIT,
                null
            );
            return caseDetailsReturn;
        }
        return caseDetails;
    }

    private Document generateFinalC7(CaseData caseData, String authorisation) throws Exception {

        return documentGenService.generateSingleDocument(
            authorisation,
            caseData,
            C7_FINAL_ENGLISH,
            false
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

            List<Element<Document>> responseDocs = new ArrayList<>();

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
                responseDocs.add(element(c1aFinalDocument));
            }

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
                responseDocs.add(element(document));
            }

            if (null != caseData.getRespondentDocsList()) {
                caseData.getRespondentDocsList().add(element(respondentDocs));
            } else {
                caseData.setRespondentDocsList(List.of(element(respondentDocs)));
            }

            List<Element<QuarantineLegalDoc>> quarantineDocs = new ArrayList<>();
            if (null != caseData.getDocumentManagementDetails() && null != caseData
                .getDocumentManagementDetails().getCitizenQuarantineDocsList()) {
                quarantineDocs = caseData.getDocumentManagementDetails().getCitizenQuarantineDocsList();
            }

            quarantineDocs.addAll(responseDocs.stream().map(element -> Element.<QuarantineLegalDoc>builder()
                    .value(QuarantineLegalDoc
                        .builder()
                        .citizenQuarantineDocument(element.getValue())
                        .build())
                    .id(element.getId()).build())
                .toList());

            log.info("quarantineDocs is {}", quarantineDocs);
            if (null != caseData.getDocumentManagementDetails()) {
                caseData.getDocumentManagementDetails().setCitizenQuarantineDocsList(quarantineDocs);
            } else {
                caseData.setDocumentManagementDetails(DocumentManagementDetails
                    .builder()
                    .citizenQuarantineDocsList(quarantineDocs)
                    .build());
            }
            log.info("quarantineDocs is {}", caseData.getDocumentManagementDetails().getCitizenQuarantineDocsList());

            populateC8Documents(caseData, currentRespondent.get(), partyName, userDetails, c8FinalDocument);
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
                case 0 -> caseData.toBuilder().citizenResponseDocuments(CitizenResponseDocuments.builder()
                    .respondentAc8(c8ResponseDocuments).build());
                case 1 -> caseData.toBuilder().citizenResponseDocuments(CitizenResponseDocuments.builder()
                    .respondentBc8(c8ResponseDocuments).build());
                case 2 -> caseData.toBuilder().citizenResponseDocuments(CitizenResponseDocuments.builder()
                    .respondentCc8(c8ResponseDocuments).build());
                case 3 -> caseData.toBuilder().citizenResponseDocuments(CitizenResponseDocuments.builder()
                    .respondentDc8(c8ResponseDocuments).build());
                case 4 -> caseData.toBuilder().citizenResponseDocuments(CitizenResponseDocuments.builder()
                    .respondentEc8(c8ResponseDocuments).build());
                default -> caseData.toBuilder().build();
            }
        }
    }

    public Document generateC7DraftDocument(String authorisation, CaseData caseData) throws Exception {

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
}
