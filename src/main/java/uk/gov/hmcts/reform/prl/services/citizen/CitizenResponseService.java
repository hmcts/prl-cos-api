package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.mapper.citizen.CitizenPartyDetailsMapper;
import uk.gov.hmcts.reform.prl.models.CitizenUpdatedCaseData;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.proceedings.Proceedings;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.DocumentLanguageService;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.C100RespondentSolicitorService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_RESPONDENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C1A_FINAL_RESPONSE_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C8_RESP_FINAL_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_C7_DRAFT_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_C1A_FINAL_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_C1A_WELSH_FINAL_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.UNDERSCORE;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.C100RespondentSolicitorService.IS_CONFIDENTIAL_DATA_PRESENT;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CitizenResponseService {

    private final ObjectMapper objectMapper;
    private final CcdCoreCaseDataService ccdCoreCaseDataService;
    private final AllTabServiceImpl allTabService;
    private final C100RespondentSolicitorService c100RespondentSolicitorService;
    private final DocumentGenService documentGenService;
    private final DocumentLanguageService documentLanguageService;
    private final CitizenPartyDetailsMapper citizenPartyDetailsMapper;

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("ddmmyyyyy");
    public static final String C_1_ARESPONSE = "C1Aresponse";
    public static final String DYNAMIC_FILE_NAME = "dynamic_fileName";


    public Document generateAndReturnDraftC7(String caseId, String partyId, String authorisation) throws Exception {
        CaseDetails caseDetails = ccdCoreCaseDataService.findCaseById(authorisation, caseId);
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        updateCurrentRespondent(caseData, partyId);

        return documentGenService.generateSingleDocument(
                authorisation,
                caseData,
                DOCUMENT_C7_DRAFT_HINT,
                false
        );
    }

    private void updateCurrentRespondent(CaseData caseData, String partyId) {

        for (Element<PartyDetails> partyElement : caseData.getRespondents()) {
            if (partyElement.getId().toString().equalsIgnoreCase(partyId)) {
                PartyDetails respondent = partyElement.getValue();
                respondent.setCurrentRespondent(Yes);
            }
        }
    }

    public CaseDetails generateAndSubmitCitizenResponse(String authorisation,
                                                        String caseId,
                                                        CitizenUpdatedCaseData citizenUpdatedCaseData) throws Exception {
        if (C100_CASE_TYPE.equalsIgnoreCase(citizenUpdatedCaseData.getCaseTypeOfApplication())
                && PartyEnum.respondent.equals(citizenUpdatedCaseData.getPartyType())) {
            Map<String, Object> caseDataMapToBeUpdated = new HashMap<>();
            Map<Element<Document>, String> responseDocs = new HashMap<>();

            // initiate the event
            StartAllTabsUpdateDataContent startAllTabsUpdateDataContent
                    = allTabService.getStartUpdateForSpecificUserEvent(caseId, CaseEvent.REVIEW_AND_SUBMIT.getValue(), authorisation);
            CaseData dbCaseData = startAllTabsUpdateDataContent.caseData();
            CaseData caseDataToGenerateC7 = dbCaseData;
            caseDataToGenerateC7 = findAndSetCurrentRespondentForC7GenerationOnly(citizenUpdatedCaseData, caseDataToGenerateC7);

            try {
                log.info("******* dbCaseData json ===>" + objectMapper.writeValueAsString(dbCaseData));
            } catch (JsonProcessingException e) {
                log.info("error");
            }

            try {
                log.info("******* caseDataToGenerateC7 json ===>" + objectMapper.writeValueAsString(caseDataToGenerateC7));
            } catch (JsonProcessingException e) {
                log.info("error");
            }

            DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseDataToGenerateC7);
            try {
                log.info("******* documentLanguage json ===>" + objectMapper.writeValueAsString(documentLanguage));
            } catch (JsonProcessingException e) {
                log.info("error");
            }

            log.info(" Generating C7 Final document for respondent ");
            if (documentLanguage.isGenEng()) {
                log.info(" Generating C7 English Final document for respondent");
                responseDocs.put(element(generateFinalC7(caseDataToGenerateC7, authorisation, false)),"en");
            }
            if (documentLanguage.isGenWelsh()) {
                log.info(" Generating C7 Welsh Final document for respondent");
                responseDocs.put(element(generateFinalC7(caseDataToGenerateC7, authorisation, true)), "cy");
            }
            log.info("C7 Final document generated successfully for respondent ");

            Optional<Element<PartyDetails>> optionalCurrentRespondent
                    = dbCaseData.getRespondents()
                    .stream()
                    .filter(party -> Objects.equals(
                                    party.getValue().getUser().getIdamId(),
                                    citizenUpdatedCaseData.getPartyDetails().getUser().getIdamId()
                            )
                    )
                    .findFirst();

            if (optionalCurrentRespondent.isPresent()) {
                Element<PartyDetails> partyDetailsElement = optionalCurrentRespondent.get();

                CaseDetails caseDetails = CaseDetails.builder().id(Long.parseLong(caseId)).data(startAllTabsUpdateDataContent.caseDataMap()).build();
                CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

                Map<String, Object> dataMap = c100RespondentSolicitorService.populateDataMap(
                        callbackRequest,
                        partyDetailsElement
                );

                if (isNotEmpty(partyDetailsElement.getValue().getResponse())) {
                    Response response = partyDetailsElement.getValue().getResponse();
                    if (Yes != response.getC7ResponseSubmitted()) {
                        log.info("setting c7 response submitted");
                        List<Element<PartyDetails>> respondents = new ArrayList<>(dbCaseData.getRespondents());
                        respondents.stream()
                            .filter(party -> Objects.equals(
                                party.getValue().getUser().getIdamId(),
                                citizenUpdatedCaseData.getPartyDetails().getUser().getIdamId()
                            ))
                            .findFirst()
                            .ifPresent(party -> {

                                PartyDetails updatedPartyDetails = citizenPartyDetailsMapper.getUpdatedPartyDetailsBasedOnEvent(
                                    citizenUpdatedCaseData.getPartyDetails(),
                                    party.getValue(),
                                    CaseEvent.REVIEW_AND_SUBMIT,dbCaseData.getNewChildDetails());
                                updatedPartyDetails = generateRespondentC1aResponseDocument(
                                    updatedPartyDetails,
                                    documentLanguage,
                                    dataMap,
                                    dbCaseData,
                                    authorisation
                                );
                                Element<PartyDetails> updatedPartyElement = element(party.getId(), updatedPartyDetails);
                                int updatedRespondentPartyIndex = respondents.indexOf(party);
                                respondents.set(updatedRespondentPartyIndex, updatedPartyElement);
                            });
                        caseDataMapToBeUpdated.put(C100_RESPONDENTS, respondents);
                    }
                    responseDocs = checkPreviousProceedings(responseDocs, response);

                    //TODO: AoH to be revisited
                    if (isNotEmpty(response.getRespondentAllegationsOfHarmData())
                            && Yes.equals(response.getRespondentAllegationsOfHarmData().getRespAohYesOrNo())) {
                        log.info(" Generating C1A Final document for respondent ");
                        if (documentLanguage.isGenEng()) {
                            responseDocs.put(element(generateFinalC1A(dbCaseData, authorisation, dataMap)),"en");
                        }
                        if (documentLanguage.isGenWelsh()) {
                            responseDocs.put(element(generateFinalC1AWelsh(dbCaseData, authorisation, dataMap)),"cy");
                        }
                        log.info("C1A Final document generated successfully for respondent ");
                    }
                    try {
                        log.info("******* starting caseDataMapToBeUpdated json ===>" + objectMapper.writeValueAsString(caseDataMapToBeUpdated));
                    } catch (JsonProcessingException e) {
                        log.info("error");
                    }
                    log.info("document list {} ", responseDocs);
                    caseDataMapToBeUpdated.putAll(addCitizenDocumentsToTheQuarantineList(
                            dbCaseData,
                            responseDocs,
                            startAllTabsUpdateDataContent.userDetails()
                    ));
                    try {
                        log.info("******* after docs added caseDataMapToBeUpdated json ===>"
                                + objectMapper.writeValueAsString(caseDataMapToBeUpdated));
                    } catch (JsonProcessingException e) {
                        log.info("error");
                    }
                    caseDataMapToBeUpdated.putAll(generateC8Document(
                            authorisation,
                            dbCaseData,
                            partyDetailsElement,
                            dataMap,
                            partyDetailsElement.getValue().getLabelForDynamicList(),
                            startAllTabsUpdateDataContent.userDetails()
                    ));

                    try {
                        log.info("******* After C8 caseDataMapToBeUpdated json ===>" + objectMapper.writeValueAsString(caseDataMapToBeUpdated));
                    } catch (JsonProcessingException e) {
                        log.info("error");
                    }


                }
            }

            try {
                log.info("******* final caseDataMapToBeUpdated json ===>" + objectMapper.writeValueAsString(caseDataMapToBeUpdated));
            } catch (JsonProcessingException e) {
                log.info("error");
            }

            return allTabService.submitUpdateForSpecificUserEvent(
                    startAllTabsUpdateDataContent.authorisation(),
                    caseId,
                    startAllTabsUpdateDataContent.startEventResponse(),
                    startAllTabsUpdateDataContent.eventRequestData(),
                    caseDataMapToBeUpdated,
                    startAllTabsUpdateDataContent.userDetails()
            );
        } else {
            throw new RuntimeException("Invalid case type or party type for the event request "
                    + CaseEvent.REVIEW_AND_SUBMIT.getValue()
                    + " for the case id "
                    + caseId);
        }
    }

    private PartyDetails generateRespondentC1aResponseDocument(PartyDetails updatedPartyDetails,
                                                               DocumentLanguage documentLanguage,
                                                               Map<String, Object> dataMap,
                                                               CaseData caseData, String authorisation) {

        Document c1aFinalResponseEngDocument = null;
        log.info("inside generateRespondentC1aResponseDocuments()");
        if (isNotEmpty(updatedPartyDetails.getResponse())
            && isNotEmpty(updatedPartyDetails.getResponse().getResponseToAllegationsOfHarm())
            && Yes.equals(updatedPartyDetails.getResponse().getResponseToAllegationsOfHarm()
                              .getResponseToAllegationsOfHarmYesOrNoResponse())) {
            String fileName = updatedPartyDetails.getLabelForDynamicList()
                + UNDERSCORE + C_1_ARESPONSE + UNDERSCORE + LocalDateTime.now(ZoneId.of(
                LONDON_TIME_ZONE)).format(dateTimeFormatter);
            dataMap.put(DYNAMIC_FILE_NAME, fileName + ".pdf");
            log.info("generating respondent C1A response documents");
            try {
                if (documentLanguage.isGenEng()) {
                    c1aFinalResponseEngDocument = documentGenService.generateSingleDocument(
                        authorisation,
                        caseData,
                        C1A_FINAL_RESPONSE_DOCUMENT,
                        false,
                        dataMap
                    );
                }
                if (isNotEmpty(c1aFinalResponseEngDocument)) {
                    log.info("generated respondent C1A response documents");
                    updatedPartyDetails = updatedPartyDetails.toBuilder()
                        .response(updatedPartyDetails.getResponse().toBuilder()
                                      .responseToAllegationsOfHarm(updatedPartyDetails.getResponse().getResponseToAllegationsOfHarm()
                                                                       .toBuilder()
                                                                       .responseToAllegationsOfHarmDocument(
                                                                           c1aFinalResponseEngDocument)
                                                                       .build())
                                      .build())
                        .build();
                }
            } catch (Exception e) {
                log.info(
                    "Failed to generate Respondent C1A response document for party {}",
                    updatedPartyDetails.getLabelForDynamicList()
                );
            } finally {
                dataMap.remove(DYNAMIC_FILE_NAME);
            }
        }
        return updatedPartyDetails;
    }

    private CaseData findAndSetCurrentRespondentForC7GenerationOnly(CitizenUpdatedCaseData citizenUpdatedCaseData, CaseData caseData) {
        List<Element<PartyDetails>> respondents = new ArrayList<>(caseData.getRespondents());
        respondents.stream()
                .filter(party -> Objects.equals(
                        party.getValue().getUser().getIdamId(),
                        citizenUpdatedCaseData.getPartyDetails().getUser().getIdamId()
                ))
                .findFirst()
                .ifPresent(party -> {
                    PartyDetails updatedPartyDetails = party.getValue();
                    updatedPartyDetails.setCurrentRespondent(YesOrNo.Yes);
                    Element<PartyDetails> updatedPartyElement = element(party.getId(), updatedPartyDetails);
                    int updatedRespondentPartyIndex = respondents.indexOf(party);
                    respondents.set(updatedRespondentPartyIndex, updatedPartyElement);
                });
        caseData = caseData.toBuilder().respondents(respondents).build();
        return caseData;
    }

    private Document generateFinalC7(CaseData caseData, String authorisation, boolean isWelsh) throws Exception {

        return documentGenService.generateSingleDocument(
                authorisation,
                caseData,
                "c7FinalEng",
                isWelsh
        );
    }

    private Document generateFinalC1A(CaseData caseData, String authorisation, Map<String, Object> dataMap) throws Exception {

        return documentGenService.generateSingleDocument(
                authorisation,
                caseData,
                SOLICITOR_C1A_FINAL_DOCUMENT,
                false,
                dataMap
        );
    }

    private Document generateFinalC1AWelsh(CaseData caseData, String authorisation, Map<String, Object> dataMap) throws Exception {

        return documentGenService.generateSingleDocument(
            authorisation,
            caseData,
            SOLICITOR_C1A_WELSH_FINAL_DOCUMENT,
            true,
            dataMap
        );
    }

    private Map<String, Object> generateC8Document(String authorisation, CaseData caseData, Element<PartyDetails> partyDetailsElement,
                                                   Map<String, Object> dataMap, String partyName,
                                                   UserDetails userDetails) throws Exception {
        if (dataMap.containsKey(IS_CONFIDENTIAL_DATA_PRESENT)) {
            log.info(" Generating C8 Final document for respondent ");
            int partyIndex = caseData.getRespondents().indexOf(partyDetailsElement);
            Document c8FinalDocument = documentGenService.generateSingleDocument(
                    authorisation,
                    caseData,
                    C8_RESP_FINAL_HINT,
                    false,
                    dataMap
            );
            log.info("C8 Final document generated successfully for respondent");
            if (c8FinalDocument != null && partyIndex >= 0) {
                return populateC8Documents(partyName, userDetails, c8FinalDocument, partyIndex);
            }
        }

        return new HashMap<>();
    }

    private Map<String, Object> populateC8Documents(String partyName,
                                                    UserDetails userDetails,
                                                    Document c8FinalDocument,
                                                    int partyIndex) {
        Map<String, Object> caseDataMapToBeUpdated = new HashMap<>();
        ResponseDocuments c8ResponseDocuments = ResponseDocuments.builder()
                .partyName(partyName)
                .createdBy(userDetails.getFullName())
                .dateCreated(LocalDate.now())
                .citizenDocument(c8FinalDocument)
                .build();
        if (c8ResponseDocuments != null) {
            switch (partyIndex) {
                case 0 -> caseDataMapToBeUpdated.put("respondentAc8", c8ResponseDocuments);
                case 1 -> caseDataMapToBeUpdated.put("respondentBc8", c8ResponseDocuments);
                case 2 -> caseDataMapToBeUpdated.put("respondentCc8", c8ResponseDocuments);
                case 3 -> caseDataMapToBeUpdated.put("respondentDc8", c8ResponseDocuments);
                case 4 -> caseDataMapToBeUpdated.put("respondentEc8", c8ResponseDocuments);
                default -> log.error("Invalid party index while generating C8 as part of final C7 submission");
            }
        }
        return caseDataMapToBeUpdated;
    }

    private Map<Element<Document>,String> checkPreviousProceedings(Map<Element<Document>,String> responseDocs,
                                                             Response response) {
        if (isNotEmpty(response.getCurrentOrPreviousProceedings())
                && isNotEmpty(response.getCurrentOrPreviousProceedings().getProceedingsList())) {
            List<Proceedings> proceedingsList = new ArrayList<>();
            for (Element<Proceedings> elementProceedings : response.getCurrentOrPreviousProceedings()
                    .getProceedingsList()) {
                proceedingsList.add(elementProceedings.getValue());
            }

            return getOrderDocumentsFromProceedings(responseDocs, proceedingsList);
        }

        return responseDocs;
    }

    private Map<Element<Document>,String> getOrderDocumentsFromProceedings(Map<Element<Document>,String> responseDocs,
                                                                           List<Proceedings> proceedingsList) {
        proceedingsList.stream()
                .filter(proceedings -> null != proceedings.getProceedingDetails())
                .flatMap(proceedings -> proceedings.getProceedingDetails().stream())
                .filter(otherProceedingDetailsElement -> isNotEmpty(otherProceedingDetailsElement.getValue())
                        && null != otherProceedingDetailsElement.getValue().getOrderDocument())
                .map(otherProceedingDetailsElement ->
                    responseDocs.put(element(otherProceedingDetailsElement.getValue().getOrderDocument()),"en"));

        return responseDocs;
    }

    public Map<String, Object> addCitizenDocumentsToTheQuarantineList(CaseData caseData, Map<Element<Document>,String> responseDocs,
                                                            UserDetails userDetails) {

        List<Element<QuarantineLegalDoc>> quarantineDocs = new ArrayList<>();
        if (null != caseData.getDocumentManagementDetails() && null != caseData
                .getDocumentManagementDetails().getCitizenQuarantineDocsList()) {
            quarantineDocs = caseData.getDocumentManagementDetails().getCitizenQuarantineDocsList();
        }

        List<Element<QuarantineLegalDoc>> finalQuarantineDocs = quarantineDocs;
        responseDocs.forEach((element, language) -> {
            Element<QuarantineLegalDoc> quarantineLegalDoc = Element.<QuarantineLegalDoc>builder()
                .value(QuarantineLegalDoc
                           .builder()
                           .citizenQuarantineDocument(element.getValue()
                                                          .toBuilder()
                                                          .documentCreatedOn(Date.from(ZonedDateTime.now(ZoneId.of(
                                                                  LONDON_TIME_ZONE))
                                                                                           .toInstant()))
                                                          .build())
                           .categoryId(getCategoryId(element))
                           .documentUploadedDate(LocalDateTime.now(ZoneId.of(LONDON_TIME_ZONE)))
                           .uploadedBy(null != userDetails ? userDetails.getFullName() : null)
                           .uploadedByIdamId(null != userDetails ? userDetails.getId() : null)
                           .uploaderRole(PrlAppsConstants.CITIZEN)
                           .documentLanguage(language)
                           .build())
                .id(element.getId()).build();
            finalQuarantineDocs.add(quarantineLegalDoc);
        });

        Map<String, Object> caseDataMapToBeUpdated = new HashMap<>();
        caseDataMapToBeUpdated.put("citizenQuarantineDocsList", finalQuarantineDocs);

        return caseDataMapToBeUpdated;
    }

    private String getCategoryId(Element<Document> element) {

        if (null != element.getValue().getDocumentFileName()) {
            return switch (element.getValue().getDocumentFileName()) {
                case "C7_Document.pdf", "Final_C7_response_Welsh.pdf" -> "respondentApplication";
                case "C1A_allegation_of_harm.pdf",
                    "Final_C1A_allegation_of_harm_Welsh.pdf" -> "respondentC1AApplication";
                default -> "ordersFromOtherProceedings";
            };
        }

        return "";
    }
}
