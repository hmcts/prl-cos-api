package uk.gov.hmcts.reform.prl.services.document;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenLiveAtAddress;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.DocumentDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.documents.DocumentResponse;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.citizen.GenerateAndUploadDocumentRequest;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.DgsService;
import uk.gov.hmcts.reform.prl.services.DocumentLanguageService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.UploadDocumentService;
import uk.gov.hmcts.reform.prl.utils.NumberToWords;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C1A_DRAFT_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C1A_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C8_DRAFT_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C8_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_C1A_BLANK_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_C7_BLANK_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_C8_BLANK_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_COVER_SHEET_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C1A;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C1A_DRAFT_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C1A_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C8;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C8_DRAFT_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C8_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_DRAFT_C1A;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_DRAFT_C8;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_FINAL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_FINAL_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_PRIVACY_NOTICE_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRAFT_DOCUMENT_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRAFT_DOCUMENT_WELSH_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRAFT_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRUG_AND_ALCOHOL_TESTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FINAL_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LETTERS_FROM_SCHOOL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MAIL_SCREENSHOTS_MEDIA_FILES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MEDICAL_RECORDS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MEDICAL_REPORTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.OTHER_DOCUMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.OTHER_WITNESS_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PATERNITY_TEST_REPORTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.POLICE_REPORTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PREVIOUS_ORDERS_SUBMITTED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TENANCY_MORTGAGE_AGREEMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.YOUR_POSITION_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.YOUR_WITNESS_STATEMENTS;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentGenService {

    @Value("${document.templates.c100.c100_final_template}")
    protected String c100FinalTemplate;

    @Value("${document.templates.c100.c100_final_filename}")
    protected String c100FinalFilename;

    @Value("${document.templates.c100.c100_draft_template}")
    protected String c100DraftTemplate;

    @Value("${document.templates.c100.c100_draft_filename}")
    protected String c100DraftFilename;

    @Value("${document.templates.c100.c100_c8_template}")
    protected String c100C8Template;

    @Value("${document.templates.c100.c100_c8_draft_template}")
    protected String c100C8DraftTemplate;

    @Value("${document.templates.c100.c100_c8_filename}")
    protected String c100C8Filename;

    @Value("${document.templates.c100.c100_c8_draft_filename}")
    protected String c100C8DraftFilename;

    @Value("${document.templates.c100.c100_c1a_template}")
    protected String c100C1aTemplate;

    @Value("${document.templates.c100.c100_c1a_draft_template}")
    protected String c100C1aDraftTemplate;

    @Value("${document.templates.c100.c100_c1a_filename}")
    protected String c100C1aFilename;

    @Value("${document.templates.c100.c100_c1a_draft_filename}")
    protected String c100C1aDraftFilename;

    @Value("${document.templates.c100.c100_final_welsh_template}")
    protected String c100FinalWelshTemplate;

    @Value("${document.templates.c100.c100_final_welsh_filename}")
    protected String c100FinalWelshFilename;

    @Value("${document.templates.c100.c100_draft_welsh_template}")
    protected String c100DraftWelshTemplate;

    @Value("${document.templates.c100.c100_draft_welsh_filename}")
    protected String c100DraftWelshFilename;

    @Value("${document.templates.c100.c100_c8_welsh_template}")
    protected String c100C8WelshTemplate;

    @Value("${document.templates.c100.c100_c8_draft_welsh_template}")
    protected String c100C8DraftWelshTemplate;

    @Value("${document.templates.c100.c100_c8_welsh_filename}")
    protected String c100C8WelshFilename;

    @Value("${document.templates.c100.c100_c8_draft_welsh_filename}")
    protected String c100C8DraftWelshFilename;

    @Value("${document.templates.c100.c100_c1a_welsh_template}")
    protected String c100C1aWelshTemplate;

    @Value("${document.templates.c100.c100_c1a_welsh_filename}")
    protected String c100C1aWelshFilename;

    @Value("${document.templates.c100.c100_c1a_draft_welsh_template}")
    protected String c100C1aDraftWelshTemplate;

    @Value("${document.templates.c100.c100_c1a_draft_welsh_filename}")
    protected String c100C1aDraftWelshFilename;

    @Value("${document.templates.fl401.fl401_draft_filename}")
    protected String fl401DraftFilename;

    @Value("${document.templates.fl401.fl401_draft_template}")
    protected String fl401DraftTemplate;

    @Value("${document.templates.fl401.fl401_draft_welsh_template}")
    protected String fl401DraftWelshTemplate;

    @Value("${document.templates.fl401.fl401_draft_welsh_filename}")
    protected String fl401DraftWelshFileName;

    @Value("${document.templates.fl401.fl401_final_template}")
    protected String fl401FinalTemplate;

    @Value("${document.templates.fl401.fl401_final_filename}")
    protected String fl401FinalFilename;

    @Value("${document.templates.fl401.fl401_final_welsh_template}")
    protected String fl401FinalWelshTemplate;

    @Value("${document.templates.fl401.fl401_final_welsh_filename}")
    protected String fl401FinalWelshFilename;

    @Value("${document.templates.fl401.fl401_c8_template}")
    protected String fl401C8Template;

    @Value("${document.templates.fl401.fl401_c8_filename}")
    protected String fl401C8Filename;

    @Value("${document.templates.fl401.fl401_c8_welsh_template}")
    protected String fl401C8WelshTemplate;

    @Value("${document.templates.fl401.fl401_c8_welsh_filename}")
    protected String fl401C8WelshFilename;

    @Value("${document.templates.common.doc_cover_sheet_template}")
    protected String docCoverSheetTemplate;

    @Value("${document.templates.common.doc_cover_sheet_welsh_template}")
    protected String docCoverSheetWelshTemplate;

    @Value("${document.templates.common.doc_cover_sheet_filename}")
    protected String docCoverSheetFilename;

    @Value("${document.templates.common.doc_cover_sheet_welsh_filename}")
    protected String docCoverSheetWelshFilename;

    @Value("${document.templates.common.prl_c7_blank_template}")
    protected String docC7BlankTemplate;

    @Value("${document.templates.common.prl_c7_blank_filename}")
    protected String docC7BlankFilename;

    @Value("${document.templates.common.prl_c1a_blank_template}")
    protected String docC1aBlankTemplate;

    @Value("${document.templates.common.prl_c1a_blank_filename}")
    protected String docC1aBlankFilename;

    @Value("${document.templates.common.prl_c8_blank_template}")
    protected String docC8BlankTemplate;

    @Value("${document.templates.common.prl_c8_blank_filename}")
    protected String docC8BlankFilename;

    @Value("${document.templates.common.prl_privacy_notice_template}")
    protected String privacyNoticeTemplate;

    @Value("${document.templates.common.prl_privacy_notice_filename}")
    protected String privacyNoticeFilename;

    @Value("${document.templates.citizen.prl_citizen_upload_template}")
    protected String prlCitizenUploadTemplate;

    @Value("${document.templates.citizen.prl_citizen_upload_filename}")
    protected String prlCitizenUploadFileName;

    @Autowired
    private DgsService dgsService;

    @Autowired
    DocumentLanguageService documentLanguageService;

    @Autowired
    OrganisationService organisationService;

    @Autowired
    UploadDocumentService uploadService;

    private CaseData fillOrgDetails(CaseData caseData) {
        log.info("Calling org service to update the org address .. for case id {} ", caseData.getId());
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            caseData = organisationService.getApplicantOrganisationDetails(caseData);
            caseData = organisationService.getRespondentOrganisationDetails(caseData);
        } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            caseData = organisationService.getApplicantOrganisationDetailsForFL401(caseData);
        }
        log.info("Called org service to update the org address .. for case id {} ", caseData.getId());
        return caseData;
    }

    public Map<String, Object> generateDocuments(String authorisation, CaseData caseData) throws Exception {

        Map<String, Object> updatedCaseData = new HashMap<>();

        caseData = fillOrgDetails(caseData);
        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);

        if (documentLanguage.isGenEng()) {
            updatedCaseData.put("isEngDocGen", Yes.toString());
            if (isConfidentialInformationPresentForC100(caseData)) {
                if (State.CASE_ISSUE.equals(caseData.getState())) {
                    updatedCaseData.put(DOCUMENT_FIELD_C8, getDocument(authorisation, caseData, C8_HINT, false));
                } else {
                    updatedCaseData.put(
                        DOCUMENT_FIELD_DRAFT_C8,
                        getDocument(authorisation, caseData, C8_DRAFT_HINT, false)
                    );
                }
            } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
                && isApplicantOrChildDetailsConfidential(caseData)) {
                updatedCaseData.put(DOCUMENT_FIELD_C8, getDocument(authorisation, caseData, C8_HINT, false));
            } else {
                updatedCaseData.put(DOCUMENT_FIELD_C8, null);
            }
            if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
                && caseData.getAllegationOfHarm() != null
                && YesOrNo.Yes.equals(caseData.getAllegationOfHarm().getAllegationsOfHarmYesNo())) {
                if (State.CASE_ISSUE.equals(caseData.getState())) {
                    updatedCaseData.put(DOCUMENT_FIELD_C1A, getDocument(authorisation, caseData, C1A_HINT, false));
                } else {
                    updatedCaseData.put(
                        DOCUMENT_FIELD_DRAFT_C1A,
                        getDocument(authorisation, caseData, C1A_DRAFT_HINT, false)
                    );

                }
            } else {
                updatedCaseData.put(DOCUMENT_FIELD_C1A, null);
            }
            if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication()) || State.CASE_ISSUE.equals(
                caseData.getState())) {
                updatedCaseData.put(DOCUMENT_FIELD_FINAL, getDocument(authorisation, caseData, FINAL_HINT, false));
            }
        }
        if (documentLanguage.isGenWelsh()) {
            updatedCaseData.put("isWelshDocGen", Yes.toString());
            if (isConfidentialInformationPresentForC100(caseData)) {
                if (State.CASE_ISSUE.equals(caseData.getState())) {
                    updatedCaseData.put(DOCUMENT_FIELD_C8_WELSH, getDocument(authorisation, caseData, C8_HINT, true));
                } else {
                    updatedCaseData.put(
                        DOCUMENT_FIELD_C8_DRAFT_WELSH,
                        getDocument(authorisation, caseData, C8_DRAFT_HINT, true)
                    );
                }
            } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
                && isApplicantOrChildDetailsConfidential(caseData)) {
                updatedCaseData.put(DOCUMENT_FIELD_C8_WELSH, getDocument(authorisation, caseData, C8_HINT, true));
            } else {
                updatedCaseData.put(DOCUMENT_FIELD_C8_WELSH, null);
            }


            if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
                && caseData.getAllegationOfHarm() != null
                && YesOrNo.Yes.equals(caseData.getAllegationOfHarm().getAllegationsOfHarmYesNo())) {
                if (State.CASE_ISSUE.equals(caseData.getState())) {
                    updatedCaseData.put(DOCUMENT_FIELD_C1A_WELSH, getDocument(authorisation, caseData, C1A_HINT, true));
                } else {
                    updatedCaseData.put(
                        DOCUMENT_FIELD_C1A_DRAFT_WELSH,
                        getDocument(authorisation, caseData, C1A_DRAFT_HINT, true)
                    );
                }
            } else {
                updatedCaseData.put(DOCUMENT_FIELD_C1A_WELSH, null);
            }
            if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication()) || State.CASE_ISSUE.equals(
                caseData.getState())) {
                updatedCaseData.put(
                    DOCUMENT_FIELD_FINAL_WELSH,
                    getDocument(authorisation, caseData, FINAL_HINT, true)
                );
            }
        }
        if (documentLanguage.isGenEng() && !documentLanguage.isGenWelsh()) {
            updatedCaseData.put(DOCUMENT_FIELD_FINAL_WELSH, null);
            updatedCaseData.put(DOCUMENT_FIELD_C1A_WELSH, null);
            updatedCaseData.put(DOCUMENT_FIELD_C8_WELSH, null);
        } else if (!documentLanguage.isGenEng() && documentLanguage.isGenWelsh()) {
            updatedCaseData.put(DOCUMENT_FIELD_FINAL, null);
            updatedCaseData.put(DOCUMENT_FIELD_C8, null);
            updatedCaseData.put(DOCUMENT_FIELD_C1A, null);
        }
        return updatedCaseData;
    }


    private boolean isConfidentialInformationPresentForC100(CaseData caseData) {
        return C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
            && ofNullable(caseData.getApplicantsConfidentialDetails()).isPresent()
            && !caseData.getApplicantsConfidentialDetails().isEmpty()
            || ofNullable(caseData.getChildrenConfidentialDetails()).isPresent()
            && !caseData.getChildrenConfidentialDetails().isEmpty();
    }

    public Map<String, Object> generateDraftDocuments(String authorisation, CaseData caseData) throws Exception {

        Map<String, Object> updatedCaseData = new HashMap<>();

        caseData = fillOrgDetails(caseData);
        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
        log.info(
            "Selected Language for generating the docs English => {}, Welsh => {}",
            documentLanguage.isGenEng(),
            documentLanguage.isGenWelsh()
        );
        if (documentLanguage.isGenEng()) {
            updatedCaseData.put("isEngDocGen", Yes.toString());
            updatedCaseData.put(DRAFT_DOCUMENT_FIELD, getDocument(authorisation, caseData, DRAFT_HINT, false));
        }
        if (documentLanguage.isGenWelsh()) {
            updatedCaseData.put("isWelshDocGen", Yes.toString());
            updatedCaseData.put(DRAFT_DOCUMENT_WELSH_FIELD, getDocument(authorisation, caseData, DRAFT_HINT, true));
        }

        return updatedCaseData;
    }


    private Document getDocument(String authorisation, CaseData caseData, String hint, boolean isWelsh)
        throws Exception {
        return generateDocumentField(
            getFileName(caseData, hint, isWelsh),
            generateDocument(authorisation, getTemplate(caseData, hint, isWelsh), caseData, isWelsh)
        );
    }

    private UploadedDocuments getDocument(String authorisation, GenerateAndUploadDocumentRequest generateAndUploadDocumentRequest, String fileName)
        throws Exception {
        return generateCitizenUploadDocument(
            fileName,
            generateCitizenUploadedDocument(authorisation, prlCitizenUploadTemplate, generateAndUploadDocumentRequest),
            generateAndUploadDocumentRequest
        );
    }

    private String getCitizenUploadedStatementFileName(GenerateAndUploadDocumentRequest generateAndUploadDocumentRequest,
                                                       Integer fileIndex) {
        String fileName = "";

        if (generateAndUploadDocumentRequest.getValues() != null
            && generateAndUploadDocumentRequest.getValues().containsKey("partyName")
            && generateAndUploadDocumentRequest.getValues().containsKey("documentType")) {
            fileName = generateAndUploadDocumentRequest.getValues().get("partyName").replace(" ", "_");
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
            switch (generateAndUploadDocumentRequest.getValues().get("documentType")) {
                case YOUR_POSITION_STATEMENTS:
                    fileName = fileName + "_" + NumberToWords.convertNumberToWords(fileIndex)
                        + "_position_satement_" + currentDate + "_submitted.pdf";
                    break;
                case YOUR_WITNESS_STATEMENTS:
                    fileName = fileName + "_" + NumberToWords.convertNumberToWords(fileIndex)
                        + "_witness_satement_" + currentDate + "_submitted.pdf";
                    break;
                case OTHER_WITNESS_STATEMENTS:
                    fileName = fileName + "_" + NumberToWords.convertNumberToWords(fileIndex)
                        + "_other_witness_satement_" + currentDate + "_submitted.pdf";
                    break;
                case MEDICAL_RECORDS:
                    fileName = fileName + "_" + NumberToWords.convertNumberToWords(fileIndex)
                        + "_medical_records_" + currentDate + "_submitted.pdf";
                    break;
                case MAIL_SCREENSHOTS_MEDIA_FILES:
                    fileName = fileName + "_" + NumberToWords.convertNumberToWords(fileIndex)
                        + "_media_files_" + currentDate + "_submitted.pdf";
                    break;
                case LETTERS_FROM_SCHOOL:
                    fileName = fileName + "_" + NumberToWords.convertNumberToWords(fileIndex)
                        + "_letter_from_school_" + currentDate + "_submitted.pdf";
                    break;
                case TENANCY_MORTGAGE_AGREEMENTS:
                    fileName = fileName + "_" + NumberToWords.convertNumberToWords(fileIndex)
                        + "_tenancy_mortgage_agreements_" + currentDate + "_submitted.pdf";
                    break;
                case PREVIOUS_ORDERS_SUBMITTED:
                    fileName = fileName + "_" + NumberToWords.convertNumberToWords(fileIndex)
                        + "_previous_orders_submitted_" + currentDate + "_submitted.pdf";
                    break;
                case MEDICAL_REPORTS:
                    fileName = fileName + "_" + NumberToWords.convertNumberToWords(fileIndex)
                        + "_medical_reports_" + currentDate + "_submitted.pdf";
                    break;
                case PATERNITY_TEST_REPORTS:
                    fileName = fileName + "_" + NumberToWords.convertNumberToWords(fileIndex)
                        + "_paternity_test_reports_" + currentDate + "_submitted.pdf";
                    break;
                case DRUG_AND_ALCOHOL_TESTS:
                    fileName = fileName + "_" + NumberToWords.convertNumberToWords(fileIndex)
                        + "_drug_and_alcohol_tests_" + currentDate + "_submitted.pdf";
                    break;
                case POLICE_REPORTS:
                    fileName = fileName + "_" + NumberToWords.convertNumberToWords(fileIndex)
                        + "_police_reports_" + currentDate + "_submitted.pdf";
                    break;
                case OTHER_DOCUMENTS:
                    fileName = fileName + "_" + NumberToWords.convertNumberToWords(fileIndex)
                        + "_other_documents_" + currentDate + "_submitted.pdf";
                    break;
                default:
                    fileName = "";
            }
        }
        return fileName.toLowerCase();
    }

    private GeneratedDocumentInfo generateCitizenUploadedDocument(String authorisation,
                                                                  String template,
                                                                  GenerateAndUploadDocumentRequest generateAndUploadDocumentRequest)
        throws Exception {
        String caseId = generateAndUploadDocumentRequest.getValues().get("caseId");
        log.info("Generating the {} statement document from the text box for case id {} ", template, caseId);
        GeneratedDocumentInfo generatedDocumentInfo = null;

        generatedDocumentInfo = dgsService.generateCitizenDocument(
            authorisation,
            generateAndUploadDocumentRequest,
            template
        );
        boolean isDocumentGenerated = generatedDocumentInfo.getUrl() != null;
        log.info("Is the document generated for the template {} : {} ", template, isDocumentGenerated);
        log.info("Generated the {} document for case id {} ", template, caseId);
        return generatedDocumentInfo;
    }


    private Document generateDocumentField(String fileName, GeneratedDocumentInfo generatedDocumentInfo) {
        if (null == generatedDocumentInfo) {
            return null;
        }
        return Document.builder()
            .documentUrl(generatedDocumentInfo.getUrl())
            .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
            .documentHash(generatedDocumentInfo.getHashToken())
            .documentFileName(fileName).build();
    }

    private GeneratedDocumentInfo generateDocument(String authorisation, String template, CaseData caseData,
                                                   boolean isWelsh)
        throws Exception {
        log.info("Generating the {} document for case id {} ", template, caseData.getId());
        GeneratedDocumentInfo generatedDocumentInfo = null;
        caseData = caseData.toBuilder().isDocumentGenerated("No").build();
        if (isWelsh) {
            generatedDocumentInfo = dgsService.generateWelshDocument(
                authorisation,
                uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder().caseData(caseData).build(),
                template
            );
        } else {
            log.info("Generating document for {} ",template);
            generatedDocumentInfo = dgsService.generateDocument(
                authorisation,
                uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder().caseData(caseData).build(),
                template
            );
        }
        if (null != generatedDocumentInfo) {
            caseData = caseData.toBuilder().isDocumentGenerated("Yes").build();
        }
        log.info("Is the document generated for the template {} : {} ", template, caseData.getIsDocumentGenerated());
        log.info("Generated the {} document for case id {} ", template, caseData.getId());
        return generatedDocumentInfo;
    }

    private String getFileName(CaseData caseData, String docGenFor, boolean isWelsh) {
        String caseTypeOfApp = caseData.getCaseTypeOfApplication();
        String fileName = "";

        switch (docGenFor) {
            case C8_HINT:
                fileName = findC8Filename(isWelsh, caseTypeOfApp);
                break;
            case C8_DRAFT_HINT:
                fileName = !isWelsh ? c100C8DraftFilename : c100C8DraftWelshFilename;
                break;
            case C1A_HINT:
                fileName = !isWelsh ? c100C1aFilename : c100C1aWelshFilename;
                break;
            case C1A_DRAFT_HINT:
                fileName =  !isWelsh ? c100C1aDraftFilename : c100C1aDraftWelshFilename;
                break;
            case FINAL_HINT:
                fileName = findFinalFilename(isWelsh, caseTypeOfApp);
                break;
            case DRAFT_HINT:
                fileName = findDraftFilename(isWelsh, caseTypeOfApp);
                break;
            case DOCUMENT_COVER_SHEET_HINT:
                fileName = findDocCoversheetFileName(isWelsh);
                break;
            case DOCUMENT_C7_BLANK_HINT:
                fileName = docC7BlankFilename;
                break;
            case DOCUMENT_C1A_BLANK_HINT:
                fileName = docC1aBlankFilename;
                break;
            case DOCUMENT_C8_BLANK_HINT:
                fileName = docC8BlankFilename;
                break;
            case DOCUMENT_PRIVACY_NOTICE_HINT:
                fileName = privacyNoticeFilename;
                break;
            default:
                fileName = "";
        }
        return fileName;
    }

    private String findDraftFilename(boolean isWelsh, String caseTypeOfApp) {
        String fileName;
        if (C100_CASE_TYPE.equalsIgnoreCase(caseTypeOfApp)) {
            fileName = !isWelsh ? c100DraftFilename : c100DraftWelshFilename;
        } else {
            fileName = !isWelsh ? fl401DraftFilename : fl401DraftWelshFileName;
        }
        return fileName;
    }

    private String findFinalFilename(boolean isWelsh, String caseTypeOfApp) {
        String fileName;
        if (C100_CASE_TYPE.equalsIgnoreCase(caseTypeOfApp)) {
            fileName = !isWelsh ? c100FinalFilename : c100FinalWelshFilename;
        } else {
            fileName = !isWelsh ? fl401FinalFilename : fl401FinalWelshFilename;
        }
        return fileName;
    }

    private String findC8Filename(boolean isWelsh, String caseTypeOfApp) {
        String fileName;
        if (C100_CASE_TYPE.equalsIgnoreCase(caseTypeOfApp)) {
            fileName = !isWelsh ? c100C8Filename : c100C8WelshFilename;
        } else {
            fileName = !isWelsh ? fl401C8Filename : fl401C8WelshFilename;
        }
        return fileName;
    }

    private String findDocCoversheetFileName(boolean isWelsh) {

        return !isWelsh ? docCoverSheetFilename : docCoverSheetWelshFilename;

    }

    private String getTemplate(CaseData caseData, String docGenFor, boolean isWelsh) {
        String caseTypeOfApp = caseData.getCaseTypeOfApplication();
        String template = "";

        switch (docGenFor) {
            case C8_HINT:
                template = findC8Template(isWelsh, caseTypeOfApp);
                break;
            case C8_DRAFT_HINT:
                template = !isWelsh ? c100C8DraftTemplate : c100C8DraftWelshTemplate;
                break;
            case C1A_HINT:
                template = !isWelsh ? c100C1aTemplate : c100C1aWelshTemplate;
                break;
            case C1A_DRAFT_HINT:
                template = !isWelsh ? c100C1aDraftTemplate : c100C1aDraftWelshTemplate;
                break;
            case FINAL_HINT:
                template = findFinalTemplate(isWelsh, caseTypeOfApp);
                break;
            case DRAFT_HINT:
                template = findDraftTemplate(isWelsh, caseTypeOfApp);
                break;
            case DOCUMENT_COVER_SHEET_HINT:
                template = findDocCoverSheetTemplate(isWelsh);
                break;
            case DOCUMENT_C7_BLANK_HINT:
                template = docC7BlankTemplate;
                break;
            case DOCUMENT_C1A_BLANK_HINT:
                template = docC1aBlankTemplate;
                break;
            case DOCUMENT_C8_BLANK_HINT:
                template = docC8BlankTemplate;
                break;
            case DOCUMENT_PRIVACY_NOTICE_HINT:
                template = privacyNoticeTemplate;
                break;
            case CITIZEN_HINT:
                template = prlCitizenUploadTemplate;
                break;
            default:
                template = "";
        }
        return template;
    }

    private String findDraftTemplate(boolean isWelsh, String caseTypeOfApp) {
        String template;
        if (C100_CASE_TYPE.equalsIgnoreCase(caseTypeOfApp)) {
            template = !isWelsh ? c100DraftTemplate : c100DraftWelshTemplate;
        } else {
            template = !isWelsh ? fl401DraftTemplate : fl401DraftWelshTemplate;
        }
        return template;
    }

    private String findFinalTemplate(boolean isWelsh, String caseTypeOfApp) {
        String template;
        if (C100_CASE_TYPE.equalsIgnoreCase(caseTypeOfApp)) {
            template = !isWelsh ? c100FinalTemplate : c100FinalWelshTemplate;
        } else {
            template = !isWelsh ? fl401FinalTemplate : fl401FinalWelshTemplate;
        }
        return template;
    }

    private String findC8Template(boolean isWelsh, String caseTypeOfApp) {
        String template;
        if (C100_CASE_TYPE.equalsIgnoreCase(caseTypeOfApp)) {
            template = !isWelsh ? c100C8Template : c100C8WelshTemplate;
        } else {
            template = !isWelsh ? fl401C8Template : fl401C8WelshTemplate;
        }
        return template;
    }

    private String findDocCoverSheetTemplate(boolean isWelsh) {
        return !isWelsh ? docCoverSheetTemplate : docCoverSheetWelshTemplate;
    }

    private boolean isApplicantOrChildDetailsConfidential(CaseData caseData) {
        PartyDetails partyDetails = caseData.getApplicantsFL401();
        Optional<TypeOfApplicationOrders> typeOfApplicationOrders = ofNullable(caseData.getTypeOfApplicationOrders());

        boolean isChildrenConfidential = isChildrenDetailsConfidentiality(caseData, typeOfApplicationOrders);

        return isApplicantDetailsConfidential(partyDetails) || isChildrenConfidential;

    }

    private boolean isChildrenDetailsConfidentiality(CaseData caseData, Optional<TypeOfApplicationOrders> typeOfApplicationOrders) {
        boolean childrenConfidentiality = false;

        if (typeOfApplicationOrders.isPresent() && typeOfApplicationOrders.get().getOrderType().contains(
            FL401OrderTypeEnum.occupationOrder)
            && Objects.nonNull(caseData.getHome())
            && YesOrNo.Yes.equals(caseData.getHome().getDoAnyChildrenLiveAtAddress())) {
            List<ChildrenLiveAtAddress> childrenLiveAtAddresses = caseData.getHome().getChildren().stream().map(Element::getValue).collect(
                Collectors.toList());

            for (ChildrenLiveAtAddress address : childrenLiveAtAddresses) {
                if (YesOrNo.Yes.equals(address.getKeepChildrenInfoConfidential())) {
                    childrenConfidentiality = true;
                }

            }
        }
        return childrenConfidentiality;
    }

    private boolean isApplicantDetailsConfidential(PartyDetails applicant) {

        boolean isApplicantInformationConfidential = false;
        if ((YesOrNo.Yes).equals(applicant.getIsAddressConfidential())) {
            isApplicantInformationConfidential = true;
        }
        if ((YesOrNo.Yes).equals(applicant.getIsEmailAddressConfidential())) {
            isApplicantInformationConfidential = true;
        }
        if ((YesOrNo.Yes).equals(applicant.getIsPhoneNumberConfidential())) {
            isApplicantInformationConfidential = true;
        }
        return isApplicantInformationConfidential;
    }

    public Document generateSingleDocument(String authorisation,
                                           CaseData caseData,
                                           String hint,
                                           boolean isWelsh) throws Exception {
        log.info(" *** Document generation initiated for {} *** ", hint);
        return getDocument(authorisation, caseData, hint, isWelsh);
    }

    public UploadedDocuments generateCitizenStatementDocument(String authorisation,
                                                              GenerateAndUploadDocumentRequest generateAndUploadDocumentRequest,
                                                              Integer fileIndex) throws Exception {
        String fileName = getCitizenUploadedStatementFileName(generateAndUploadDocumentRequest, fileIndex);
        return getDocument(authorisation, generateAndUploadDocumentRequest, fileName);
    }

    private UploadedDocuments generateCitizenUploadDocument(String fileName, GeneratedDocumentInfo generatedDocumentInfo,
                                                            GenerateAndUploadDocumentRequest generateAndUploadDocumentRequest) {
        if (null == generatedDocumentInfo) {
            return null;
        }
        String parentDocumentType = "";
        String documentType = "";
        String partyName = "";
        String documentName = "";
        String partyId = "";
        LocalDate today = LocalDate.now();
        String formattedCurrentDate = today.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
        String isApplicant = "";

        if (generateAndUploadDocumentRequest.getValues() != null) {
            if (generateAndUploadDocumentRequest.getValues().containsKey("parentDocumentType")) {
                parentDocumentType = generateAndUploadDocumentRequest.getValues().get("parentDocumentType");
            }
            if (generateAndUploadDocumentRequest.getValues().containsKey("partyId")) {
                partyId = generateAndUploadDocumentRequest.getValues().get("partyId");
            }
            if (generateAndUploadDocumentRequest.getValues().containsKey("documentType")) {
                documentType = generateAndUploadDocumentRequest.getValues().get("documentType");
                if (generateAndUploadDocumentRequest.getValues().containsKey("partyName")) {
                    partyName = generateAndUploadDocumentRequest.getValues().get("partyName");
                    documentName = documentType.replace("Your", partyName + "'s");
                }
            }
            if (generateAndUploadDocumentRequest.getValues().containsKey("isApplicant")) {
                isApplicant = generateAndUploadDocumentRequest.getValues().get("isApplicant");
            }

        }

        return UploadedDocuments.builder()
            .parentDocumentType(parentDocumentType)
            .documentType(documentType)
            .partyName(partyName)
            .isApplicant(isApplicant)
            .uploadedBy(partyId)
            .dateCreated(LocalDate.now())
            .documentDetails(DocumentDetails.builder()
                                 .documentName(documentName)
                                 .documentUploadedDate(formattedCurrentDate)
                                 .build()).citizenDocument(generateDocumentField(
                fileName,
                generatedDocumentInfo
            )).build();
    }

    public DocumentResponse uploadDocument(String authorization, MultipartFile file) {
        try {
            uk.gov.hmcts.reform.ccd.document.am.model.Document stampedDocument
                = uploadService.uploadDocument(file, file.getOriginalFilename(), file.getContentType(), authorization);
            log.info("Stored Doc Detail: " + stampedDocument.toString());
            return DocumentResponse.builder().status("Success").document(Document.builder()
                                                                             .documentBinaryUrl(stampedDocument.links.binary.href)
                                                                             .documentUrl(stampedDocument.links.self.href)
                                                                             .documentFileName(stampedDocument.originalDocumentName)
                                                                             .build()).build();

        } catch (Exception e) {
            log.error("Error while uploading document ." + e.getMessage());
            throw e;
        }
    }


}
