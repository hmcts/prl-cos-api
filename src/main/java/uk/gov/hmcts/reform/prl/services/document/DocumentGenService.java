package uk.gov.hmcts.reform.prl.services.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.DgsApiClient;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.exception.InvalidResourceException;
import uk.gov.hmcts.reform.prl.framework.exceptions.DocumentGenerationException;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenLiveAtAddress;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.DocumentDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.documents.DocumentResponse;
import uk.gov.hmcts.reform.prl.models.dto.GenerateDocumentRequest;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.citizen.DocumentCategory;
import uk.gov.hmcts.reform.prl.models.dto.citizen.DocumentRequest;
import uk.gov.hmcts.reform.prl.models.dto.citizen.GenerateAndUploadDocumentRequest;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.AllegationOfHarmRevisedService;
import uk.gov.hmcts.reform.prl.services.DgsService;
import uk.gov.hmcts.reform.prl.services.DocumentLanguageService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.UploadDocumentService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.services.time.Time;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.NumberToWords;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.apache.logging.log4j.util.Strings.isNotBlank;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C1A_DRAFT_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C1A_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C7_FINAL_RESPONDENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C8_DRAFT_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C8_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C8_RESP_DRAFT_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C8_RESP_FINAL_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C8_RESP_FL401_FINAL_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_ID;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DA_LIST_ON_NOTICE_FL404B_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_C1A_BLANK_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_C7_DRAFT_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_C8_BLANK_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_COVER_SHEET_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_COVER_SHEET_SERVE_ORDER_HINT;
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
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_REQUEST;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRAFT_APPLICATION_DOCUMENT_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRAFT_APPLICATION_DOCUMENT_WELSH_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRAFT_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRUG_AND_ALCOHOL_TESTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DUMMY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_SPACE_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ENGDOCGEN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FINAL_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_APPLICANT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_WELSH_DOC_GEN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LETTERS_FROM_SCHOOL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MAIL_SCREENSHOTS_MEDIA_FILES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MEDICAL_RECORDS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MEDICAL_REPORTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.OTHER_DOCUMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.OTHER_WITNESS_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PARENT_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PARTY_ID;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PARTY_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PATERNITY_TEST_REPORTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.POLICE_REPORTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PREVIOUS_ORDERS_SUBMITTED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_C1A_DRAFT_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_C1A_FINAL_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_C1A_WELSH_DRAFT_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_C1A_WELSH_FINAL_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_C7_DRAFT_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_C7_FINAL_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SUBMITTED_PDF;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SUCCESS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TENANCY_MORTGAGE_AGREEMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.UNDERSCORE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.YOUR_POSITION_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.YOUR_WITNESS_STATEMENTS;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_UPDATE;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings({"java:S6204"})
public class DocumentGenService {
    public static final String GENERATED_THE_DOCUMENT_FOR_CASE_ID = "Generated the {} document for case id {} ";
    @Value("${document.templates.c100.c100_final_template}")
    protected String c100FinalTemplate;
    @Value("${document.templates.c100.c100_final_filename}")
    protected String c100FinalFilename;
    @Value("${document.templates.c100.c100_draft_filename}")
    protected String c100DraftFilename;
    @Value("${document.templates.c100.c100_c8_filename}")
    protected String c100C8Filename;
    @Value("${document.templates.c100.c100_c8_draft_filename}")
    protected String c100C8DraftFilename;
    @Value("${document.templates.c100.c100_resp_c8_template}")
    protected String c100RespC8Template;

    @Value("${document.templates.fl401.fl401_resp_c8_template}")
    protected String fl401RespC8Template;

    @Value("${document.templates.fl401.fl401_resp_c8_template_welsh}")
    protected String fl401RespC8TemplateWelsh;

    @Value("${document.templates.c100.c100_resp_c8_draft_template}")
    protected String c100RespC8DraftTemplate;
    @Value("${document.templates.c100.c100_resp_c8_filename}")
    protected String c100RespC8Filename;
    @Value("${document.templates.c100.c100_resp_c8_draft_filename}")
    protected String c100RespC8DraftFilename;
    @Value("${document.templates.c100.c100_c1a_template}")
    protected String c100C1aTemplate;
    @Value("${document.templates.c100.c100_c1a_draft_template}")
    protected String c100C1aDraftTemplate;
    @Value("${document.templates.c100.c100_c1a_filename}")
    protected String c100C1aFilename;
    @Value("${document.templates.c100.c100_c1a_draft_filename}")
    protected String c100C1aDraftFilename;
    @Value("${document.templates.c100.c100_final_welsh_filename}")
    protected String c100FinalWelshFilename;
    @Value("${document.templates.c100.c100_draft_welsh_filename}")
    protected String c100DraftWelshFilename;
    @Value("${document.templates.c100.c100_c8_welsh_filename}")
    protected String c100C8WelshFilename;
    @Value("${document.templates.c100.c100_c8_draft_welsh_filename}")
    protected String c100C8DraftWelshFilename;
    @Value("${document.templates.c100.c100_c1a_welsh_filename}")
    protected String c100C1aWelshFilename;
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
    @Value("${document.templates.common.prl_c7_draft_template}")
    protected String docC7DraftTemplate;
    @Value("${document.templates.common.prl_c7_draft_template_wel}")
    protected String docC7DraftWelshTemplate;
    @Value("${document.templates.common.prl_c7_final_template_eng}")
    protected String docC7FinalEngTemplate;
    @Value("${document.templates.common.prl_c7_final_template_wel}")
    protected String docC7FinalWelshTemplate;
    @Value("${document.templates.common.prl_c7_draft_filename}")
    protected String docC7DraftFilename;
    @Value("${document.templates.common.prl_c7_draft_filename_wel}")
    protected String docC7DraftWelshFilename;
    @Value("${document.templates.common.prl_c7_final_filename_eng}")
    protected String docC7FinalEngFilename;
    @Value("${document.templates.common.prl_c7_final_filename_wel}")
    protected String docC7FinalWelshFilename;
    @Value("${document.templates.common.prl_solicitor_c7_draft_template}")
    protected String solicitorC7DraftTemplate;
    @Value("${document.templates.common.prl_solicitor_c7_draft_filename}")
    protected String solicitorC7DraftFilename;
    @Value("${document.templates.common.prl_solicitor_c7_final_template}")
    protected String solicitorC7FinalTemplate;
    @Value("${document.templates.common.prl_solicitor_c7_final_filename}")
    protected String solicitorC7FinalFilename;
    @Value("${document.templates.common.prl_solicitor_c7_welsh_draft_template}")
    protected String solicitorC7WelshDraftTemplate;
    @Value("${document.templates.common.prl_solicitor_c7_welsh_draft_filename}")
    protected String solicitorC7WelshDraftFilename;
    @Value("${document.templates.common.prl_solicitor_c7_welsh_final_template}")
    protected String solicitorC7WelshFinalTemplate;
    @Value("${document.templates.common.prl_solicitor_c7_welsh_final_filename}")
    protected String solicitorC7WelshFinalFilename;
    @Value("${document.templates.common.prl_solicitor_c1a_draft_template}")
    protected String solicitorC1ADraftTemplate;
    @Value("${document.templates.common.prl_solicitor_c1a_draft_filename}")
    protected String solicitorC1ADraftFilename;
    @Value("${document.templates.common.prl_solicitor_c1a_final_template}")
    protected String solicitorC1AFinalTemplate;
    @Value("${document.templates.common.prl_solicitor_c1a_final_filename}")
    protected String solicitorC1AFinalFilename;
    @Value("${document.templates.common.prl_solicitor_c1a_welsh_draft_template}")
    protected String solicitorC1ADraftWelshTemplate;
    @Value("${document.templates.common.prl_solicitor_c1a_welsh_draft_filename}")
    protected String solicitorC1ADraftWelshFilename;
    @Value("${document.templates.common.prl_solicitor_c1a_welsh_final_template}")
    protected String solicitorC1AFinalWelshTemplate;
    @Value("${document.templates.common.prl_solicitor_c1a_welsh_final_filename}")
    protected String solicitorC1AFinalWelshFilename;
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
    @Value("${document.templates.fl401listonnotice.prl_fl404b_for_da_list_on_notice_template}")
    protected String daListOnNoticeFl404bTemplate;
    @Value("${document.templates.fl401listonnotice.prl_fl404b_for_da_list_on_notice_filename}")
    protected String daListOnNoticeFl404bFile;
    @Value("${document.templates.c100.c100_resp_c8_welsh_template}")
    protected String respC8TemplateWelsh;
    @Value("${document.templates.c100.c100_resp_c8_welsh_filename}")
    protected String respC8FilenameWelsh;

    @Value("${document.templates.common.doc_cover_sheet_serve_order_template}")
    protected String docCoverSheetServeOrderTemplate;
    @Value("${document.templates.common.doc_cover_sheet_welsh_serve_order_template}")
    protected String docCoverSheetWelshServeOrderTemplate;

    private final DgsService dgsService;
    private final DocumentLanguageService documentLanguageService;
    private final OrganisationService organisationService;
    private final UploadDocumentService uploadService;
    private final CaseDocumentClient caseDocumentClient;
    private final IdamClient idamClient;
    private final C100DocumentTemplateFinderService c100DocumentTemplateFinderService;
    private final AllegationOfHarmRevisedService allegationOfHarmRevisedService;
    private final AllTabServiceImpl allTabService;

    private final DgsApiClient dgsApiClient;

    private final AuthTokenGenerator authTokenGenerator;
    private final UserService userService;
    private final ManageDocumentsService manageDocumentsService;
    private final CaseService caseService;
    private final ObjectMapper objectMapper;

    private final Time dateTime;

    protected static final String[] ALLOWED_FILE_TYPES = {"jpeg", "jpg", "doc", "docx", "png", "txt"};

    public CaseData fillOrgDetails(CaseData caseData) {
        log.info("Calling org service to update the org address .. for case id {} ", caseData.getId());
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            caseData = organisationService.getApplicantOrganisationDetails(caseData);
            caseData = organisationService.getRespondentOrganisationDetails(caseData);
        } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            caseData = organisationService.getApplicantOrganisationDetailsForFL401(caseData);
            caseData = organisationService.getRespondentOrganisationDetailsForFL401(caseData);
        }
        log.info("Called org service to update the org address .. for case id {} ", caseData.getId());
        return caseData;
    }

    /*
    Need to remove this method once we have clarity on document generation for citizen
     */
    public Map<String, Object> generateDocumentsForCitizenSubmission(String authorisation, CaseData caseData) throws Exception {

        Map<String, Object> updatedCaseData = new HashMap<>();

        caseData = fillOrgDetails(caseData);
        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
        updatedCaseData.put(ENGDOCGEN, Yes.toString());
        updatedCaseData.put(DOCUMENT_FIELD_FINAL, getDocument(authorisation, caseData, FINAL_HINT, false));
        if (documentLanguage.isGenEng() && !documentLanguage.isGenWelsh()) {
            updatedCaseData.put(DOCUMENT_FIELD_FINAL_WELSH, null);
        }
        return updatedCaseData;

    }

    public Map<String, Object> generateDocuments(String authorisation, CaseData caseData) throws Exception {
        caseData = fillOrgDetails(caseData);
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            caseData = allegationOfHarmRevisedService.updateChildAbusesForDocmosis(caseData);
        }

        Map<String, Object> updatedCaseData = new HashMap<>();
        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
        documentLanguageIsEng(authorisation, caseData, updatedCaseData, documentLanguage);
        documentLanguageIsWelsh(authorisation, caseData, updatedCaseData, documentLanguage);
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

    private void documentLanguageIsWelsh(String authorisation, CaseData caseData, Map<String, Object> updatedCaseData,
                                         DocumentLanguage documentLanguage) throws Exception {
        if (documentLanguage.isGenWelsh()) {
            updatedCaseData.put("isWelshDocGen", Yes.toString());
            isConfidentialInformationPresentForC100Welsh(authorisation, caseData, updatedCaseData);
            isC100CaseTypeWelsh(authorisation, caseData, updatedCaseData);
            if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication()) || State.CASE_ISSUED.equals(
                caseData.getState()) || State.JUDICIAL_REVIEW.equals(caseData.getState())) {
                updatedCaseData.put(
                    DOCUMENT_FIELD_FINAL_WELSH,
                    getDocument(authorisation, caseData, FINAL_HINT, true)
                );
            }
        }
    }

    private void isC100CaseTypeWelsh(String authorisation, CaseData caseData, Map<String, Object> updatedCaseData) throws Exception {
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
            && (caseData.getAllegationOfHarm() != null
            && YesOrNo.Yes.equals(caseData.getAllegationOfHarm().getAllegationsOfHarmYesNo()))
            || (caseData.getAllegationOfHarmRevised() != null
            && YesOrNo.Yes.equals(caseData.getAllegationOfHarmRevised().getNewAllegationsOfHarmYesNo()))) {
            if (State.CASE_ISSUED.equals(caseData.getState()) || State.JUDICIAL_REVIEW.equals(caseData.getState())) {
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
    }

    private void isConfidentialInformationPresentForC100Welsh(String authorisation, CaseData caseData,
                                                              Map<String, Object> updatedCaseData) throws Exception {
        if (isConfidentialInformationPresentForC100(caseData)) {
            if (State.CASE_ISSUED.equals(caseData.getState()) || State.JUDICIAL_REVIEW.equals(caseData.getState())) {
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
    }

    private void documentLanguageIsEng(String authorisation, CaseData caseData, Map<String, Object> updatedCaseData,
                                       DocumentLanguage documentLanguage) throws Exception {
        if (documentLanguage.isGenEng()) {
            updatedCaseData.put(ENGDOCGEN, Yes.toString());
            isConfidentialInformationPresentForC100Eng(authorisation, caseData, updatedCaseData);
            isC100CaseTypeEng(authorisation, caseData, updatedCaseData);
            if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication()) || State.CASE_ISSUED.equals(
                caseData.getState()) || State.JUDICIAL_REVIEW.equals(caseData.getState())) {
                updatedCaseData.put(DOCUMENT_FIELD_FINAL, getDocument(authorisation, caseData, FINAL_HINT, false));
            }
        }
    }

    private void isC100CaseTypeEng(String authorisation, CaseData caseData, Map<String, Object> updatedCaseData) throws Exception {
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
            && (caseData.getAllegationOfHarm() != null
            && YesOrNo.Yes.equals(caseData.getAllegationOfHarm().getAllegationsOfHarmYesNo()))
            || (caseData.getAllegationOfHarmRevised() != null
            && YesOrNo.Yes.equals(caseData.getAllegationOfHarmRevised().getNewAllegationsOfHarmYesNo()))) {
            if (State.CASE_ISSUED.equals(caseData.getState()) || State.JUDICIAL_REVIEW.equals(caseData.getState())) {
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
    }

    private void isConfidentialInformationPresentForC100Eng(String authorisation, CaseData caseData,
                                                            Map<String, Object> updatedCaseData) throws Exception {
        if (isConfidentialInformationPresentForC100(caseData)) {
            if (State.CASE_ISSUED.equals(caseData.getState()) || State.JUDICIAL_REVIEW.equals(caseData.getState())) {
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
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            caseData = allegationOfHarmRevisedService.updateChildAbusesForDocmosis(caseData);
        }

        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
        log.info(
            "Selected Language for generating the docs English => {}, Welsh => {}",
            documentLanguage.isGenEng(),
            documentLanguage.isGenWelsh()
        );
        if (documentLanguage.isGenEng()) {
            updatedCaseData.put(ENGDOCGEN, Yes.toString());
            updatedCaseData.put(DRAFT_APPLICATION_DOCUMENT_FIELD, getDocument(authorisation, caseData, DRAFT_HINT, false));
        }
        if (documentLanguage.isGenWelsh()) {
            updatedCaseData.put(IS_WELSH_DOC_GEN, Yes.toString());
            updatedCaseData.put(DRAFT_APPLICATION_DOCUMENT_WELSH_FIELD, getDocument(authorisation, caseData, DRAFT_HINT, true));
        }

        return updatedCaseData;
    }

    public Map<String, Object> generateDraftDocumentsForC100CaseResubmission(String authorisation, CaseData caseData) throws Exception {
        Map<String, Object> updatedCaseData = new HashMap<>();
        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
        boolean isConfidentialInformationPresentForC100 = isConfidentialInformationPresentForC100(caseData);
        boolean isC1aPresentForC100 = C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
            && (caseData.getAllegationOfHarm() != null
            && YesOrNo.Yes.equals(caseData.getAllegationOfHarm().getAllegationsOfHarmYesNo()))
            || (caseData.getAllegationOfHarmRevised() != null
            && YesOrNo.Yes.equals(caseData.getAllegationOfHarmRevised().getNewAllegationsOfHarmYesNo()));

        updatedCaseData.putAll(generateDraftDocuments(authorisation, caseData));
        generateDraftEngC1aAndC8DocumentsForResubmission(
            authorisation,
            caseData,
            updatedCaseData,
            documentLanguage,
            isConfidentialInformationPresentForC100,
            isC1aPresentForC100
        );
        generateDraftWelshC1aAndC8DocumentsForResubmission(
            authorisation,
            caseData,
            updatedCaseData,
            documentLanguage,
            isConfidentialInformationPresentForC100,
            isC1aPresentForC100
        );
        return updatedCaseData;
    }

    private void generateDraftWelshC1aAndC8DocumentsForResubmission(String authorisation,
                                                                    CaseData caseData,
                                                                    Map<String, Object> updatedCaseData,
                                                                    DocumentLanguage documentLanguage,
                                                                    boolean isConfidentialInformationPresentForC100,
                                                                    boolean isC1aPresentForC100) throws Exception {
        if (documentLanguage.isGenWelsh()) {
            if (isConfidentialInformationPresentForC100) {
                updatedCaseData.put(
                    DOCUMENT_FIELD_C8_DRAFT_WELSH,
                    getDocument(authorisation, caseData, C8_DRAFT_HINT, true)
                );
            } else {
                updatedCaseData.put(
                    DOCUMENT_FIELD_C8_DRAFT_WELSH, null);
            }
            if (isC1aPresentForC100) {
                updatedCaseData.put(
                    DOCUMENT_FIELD_C1A_DRAFT_WELSH,
                    getDocument(authorisation, caseData, C1A_DRAFT_HINT, true)
                );
            } else {
                updatedCaseData.put(DOCUMENT_FIELD_C1A_DRAFT_WELSH, null);
            }
        }
    }

    private void generateDraftEngC1aAndC8DocumentsForResubmission(String authorisation,
                                                                  CaseData caseData,
                                                                  Map<String, Object> updatedCaseData,
                                                                  DocumentLanguage documentLanguage,
                                                                  boolean isConfidentialInformationPresentForC100,
                                                                  boolean isC1aPresentForC100) throws Exception {
        if (documentLanguage.isGenEng()) {
            if (isConfidentialInformationPresentForC100) {
                updatedCaseData.put(
                    DOCUMENT_FIELD_DRAFT_C8,
                    getDocument(authorisation, caseData, C8_DRAFT_HINT, false)
                );
            } else {
                updatedCaseData.put(
                    DOCUMENT_FIELD_DRAFT_C8, null);
            }
            if (isC1aPresentForC100) {
                updatedCaseData.put(
                    DOCUMENT_FIELD_DRAFT_C1A,
                    getDocument(authorisation, caseData, C1A_DRAFT_HINT, false)
                );

            } else {
                updatedCaseData.put(DOCUMENT_FIELD_DRAFT_C1A, null);
            }
        }
    }

    private Document getDocument(String authorisation, CaseData caseData, String hint, boolean isWelsh, Map<String, Object> respondentDetails)
        throws Exception {
        String filename = "";
        if (respondentDetails.containsKey("dynamic_fileName")) {
            filename = String.valueOf(respondentDetails.get("dynamic_fileName"));
        } else {
            filename = getFileName(caseData, hint, isWelsh);
        }
        return generateDocumentField(
            filename,
            generateDocument(authorisation, getTemplate(caseData, hint, isWelsh), caseData, isWelsh, respondentDetails)
        );
    }

    private Document getDocument(String authorisation, CaseData caseData, String hint, boolean isWelsh)
        throws Exception {
        return generateDocumentField(
            getFileName(caseData, hint, isWelsh),
            generateDocument(authorisation, getTemplate(caseData, hint, isWelsh), caseData, isWelsh)
        );
    }

    private UploadedDocuments getDocument(String authorisation,
                                          GenerateAndUploadDocumentRequest generateAndUploadDocumentRequest,
                                          String fileName)
        throws Exception {
        return generateCitizenUploadDocument(
            fileName,
            generateCitizenUploadedDocument(authorisation, prlCitizenUploadTemplate, generateAndUploadDocumentRequest),
            generateAndUploadDocumentRequest
        );
    }

    public Map<String, Object> generateC7DraftDocuments(String authorisation, CaseData caseData) throws Exception {

        Map<String, Object> updatedCaseData = new HashMap<>();

        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
        if (documentLanguage.isGenEng()) {
            updatedCaseData.put("draftC7ResponseDoc", getDocument(authorisation, caseData, DRAFT_HINT, false));
        }

        return updatedCaseData;
    }

    private String getCitizenUploadedStatementFileName(DocumentRequest documentRequest) {
        StringBuilder fileNameBuilder = new StringBuilder();

        if (null != documentRequest.getPartyName()) {
            fileNameBuilder.append(documentRequest.getPartyName().replace(EMPTY_SPACE_STRING, UNDERSCORE));
            fileNameBuilder.append(UNDERSCORE);
        }
        if (null != documentRequest.getCategoryId()) {
            fileNameBuilder.append(DocumentCategory.getValue(documentRequest.getCategoryId()).getFileNamePrefix());
            fileNameBuilder.append(UNDERSCORE);
        }
        fileNameBuilder.append(dateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy-hh-mm-ss-a", Locale.UK)));
        fileNameBuilder.append(UNDERSCORE);
        fileNameBuilder.append(SUBMITTED_PDF);

        return fileNameBuilder.toString().toLowerCase();
    }

    private String getCitizenUploadedStatementFileName(GenerateAndUploadDocumentRequest generateAndUploadDocumentRequest,
                                                       Integer fileIndex) {
        String fileName = "";

        if (generateAndUploadDocumentRequest.getValues() != null
            && generateAndUploadDocumentRequest.getValues().containsKey(PARTY_NAME)
            && generateAndUploadDocumentRequest.getValues().containsKey(DOCUMENT_TYPE)) {
            fileName = generateAndUploadDocumentRequest.getValues().get(PARTY_NAME).replace(" ", "_");
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
            switch (generateAndUploadDocumentRequest.getValues().get(DOCUMENT_TYPE)) {
                case YOUR_POSITION_STATEMENTS:
                    fileName = fileName + "_" + NumberToWords.convertNumberToWords(fileIndex)
                        + "_position_satement_" + currentDate + SUBMITTED_PDF;
                    break;
                case YOUR_WITNESS_STATEMENTS:
                    fileName = fileName + "_" + NumberToWords.convertNumberToWords(fileIndex)
                        + "_witness_satement_" + currentDate + SUBMITTED_PDF;
                    break;
                case OTHER_WITNESS_STATEMENTS:
                    fileName = fileName + "_" + NumberToWords.convertNumberToWords(fileIndex)
                        + "_other_witness_satement_" + currentDate + SUBMITTED_PDF;
                    break;
                case MEDICAL_RECORDS:
                    fileName = fileName + "_" + NumberToWords.convertNumberToWords(fileIndex)
                        + "_medical_records_" + currentDate + SUBMITTED_PDF;
                    break;
                case MAIL_SCREENSHOTS_MEDIA_FILES:
                    fileName = fileName + "_" + NumberToWords.convertNumberToWords(fileIndex)
                        + "_media_files_" + currentDate + SUBMITTED_PDF;
                    break;
                case LETTERS_FROM_SCHOOL:
                    fileName = fileName + "_" + NumberToWords.convertNumberToWords(fileIndex)
                        + "_letter_from_school_" + currentDate + SUBMITTED_PDF;
                    break;
                case TENANCY_MORTGAGE_AGREEMENTS:
                    fileName = fileName + "_" + NumberToWords.convertNumberToWords(fileIndex)
                        + "_tenancy_mortgage_agreements_" + currentDate + SUBMITTED_PDF;
                    break;
                case PREVIOUS_ORDERS_SUBMITTED:
                    fileName = fileName + "_" + NumberToWords.convertNumberToWords(fileIndex)
                        + "_previous_orders_submitted_" + currentDate + SUBMITTED_PDF;
                    break;
                case MEDICAL_REPORTS:
                    fileName = fileName + "_" + NumberToWords.convertNumberToWords(fileIndex)
                        + "_medical_reports_" + currentDate + SUBMITTED_PDF;
                    break;
                case PATERNITY_TEST_REPORTS:
                    fileName = fileName + "_" + NumberToWords.convertNumberToWords(fileIndex)
                        + "_paternity_test_reports_" + currentDate + SUBMITTED_PDF;
                    break;
                case DRUG_AND_ALCOHOL_TESTS:
                    fileName = fileName + "_" + NumberToWords.convertNumberToWords(fileIndex)
                        + "_drug_and_alcohol_tests_" + currentDate + SUBMITTED_PDF;
                    break;
                case POLICE_REPORTS:
                    fileName = fileName + "_" + NumberToWords.convertNumberToWords(fileIndex)
                        + "_police_reports_" + currentDate + SUBMITTED_PDF;
                    break;
                case OTHER_DOCUMENTS:
                    fileName = fileName + "_" + NumberToWords.convertNumberToWords(fileIndex)
                        + "_other_documents_" + currentDate + SUBMITTED_PDF;
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
        log.info("=========generate Citizen Uploaded Document=========");
        String caseId = generateAndUploadDocumentRequest.getValues().get(CASE_ID);
        log.info("Generating the {} statement document from the text box for case id {} ", template, caseId);
        GeneratedDocumentInfo generatedDocumentInfo = null;

        generatedDocumentInfo = dgsService.generateCitizenDocument(
            authorisation,
            generateAndUploadDocumentRequest,
            template
        );

        log.info(GENERATED_THE_DOCUMENT_FOR_CASE_ID, template, caseId);
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
                                                   boolean isWelsh, Map<String, Object> dataMap)
        throws Exception {
        log.info(GENERATED_THE_DOCUMENT_FOR_CASE_ID, template, caseData.getId());
        GeneratedDocumentInfo generatedDocumentInfo = null;
        caseData = caseData.toBuilder().isDocumentGenerated("No").build();
        if (isWelsh) {
            generatedDocumentInfo = dgsService.generateWelshDocument(
                authorisation,
                String.valueOf(caseData.getId()),
                caseData
                    .getCaseTypeOfApplication(),
                template,
                dataMap
            );
        } else {
            log.info("Generating document for {} ", template);
            generatedDocumentInfo = dgsService.generateDocument(
                authorisation,
                String.valueOf(caseData.getId()),
                template,
                dataMap
            );
        }
        if (null != generatedDocumentInfo) {
            caseData = caseData.toBuilder().isDocumentGenerated("Yes").build();
        }
        log.info(GENERATED_THE_DOCUMENT_FOR_CASE_ID, template, caseData.getId());
        return generatedDocumentInfo;
    }

    private GeneratedDocumentInfo generateDocument(String authorisation, String template, CaseData caseData,
                                                   boolean isWelsh)
        throws Exception {
        log.info(GENERATED_THE_DOCUMENT_FOR_CASE_ID, template, caseData.getId());
        GeneratedDocumentInfo generatedDocumentInfo = null;
        caseData = caseData.toBuilder().isDocumentGenerated("No").build();
        if (isWelsh) {
            generatedDocumentInfo = dgsService.generateWelshDocument(
                authorisation,
                uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder().caseData(caseData).build(),
                template
            );
        } else {
            log.info("Generating document for {} ", template);
            generatedDocumentInfo = dgsService.generateDocument(
                authorisation,
                uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder().caseData(caseData).build(),
                template
            );
        }
        if (null != generatedDocumentInfo) {
            caseData = caseData.toBuilder().isDocumentGenerated("Yes").build();
        }
        log.info(GENERATED_THE_DOCUMENT_FOR_CASE_ID, template, caseData.getId());
        return generatedDocumentInfo;
    }

    private String getFileName(CaseData caseData, String docGenFor, boolean isWelsh) {
        String caseTypeOfApp = CaseUtils.getCaseTypeOfApplication(caseData);
        String fileName = "";

        switch (docGenFor) {
            case C8_HINT:
                fileName = findC8Filename(isWelsh, caseTypeOfApp);
                break;
            case C8_DRAFT_HINT:
                fileName = !isWelsh ? c100C8DraftFilename : c100C8DraftWelshFilename;
                break;
            case C8_RESP_DRAFT_HINT:
                fileName = c100RespC8DraftFilename;
                break;
            case C8_RESP_FINAL_HINT:
                fileName = findFinalRespondentC8FileName(isWelsh);
                break;
            case C1A_HINT:
                fileName = !isWelsh ? c100C1aFilename : c100C1aWelshFilename;
                break;
            case C1A_DRAFT_HINT:
                fileName = !isWelsh ? c100C1aDraftFilename : c100C1aDraftWelshFilename;
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
            case DOCUMENT_C7_DRAFT_HINT:
                fileName = getC7DraftFileName(isWelsh);
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
            case C7_FINAL_RESPONDENT:
                fileName = getC7FinalFileName(isWelsh);
                break;
            case SOLICITOR_C7_DRAFT_DOCUMENT:
                fileName = findDocCoverSheetC7DraftFileName(isWelsh);
                break;
            case SOLICITOR_C7_FINAL_DOCUMENT:
                fileName = findDocCoverSheetC7FinalFileName(isWelsh);
                break;
            case SOLICITOR_C1A_FINAL_DOCUMENT:
                fileName = solicitorC1AFinalFilename;
                break;
            case SOLICITOR_C1A_DRAFT_DOCUMENT:
                fileName = solicitorC1ADraftFilename;
                break;
            case SOLICITOR_C1A_WELSH_FINAL_DOCUMENT:
                fileName = solicitorC1AFinalWelshFilename;
                break;
            case SOLICITOR_C1A_WELSH_DRAFT_DOCUMENT:
                fileName = solicitorC1ADraftWelshFilename;
                break;
            case DA_LIST_ON_NOTICE_FL404B_DOCUMENT:
                fileName = daListOnNoticeFl404bFile;
                break;
            default:
                fileName = "";
        }
        return fileName;
    }

    private String getC7FinalFileName(boolean isWelsh) {
        return !isWelsh ? docC7FinalEngFilename : docC7FinalWelshFilename;
    }

    private String getC7DraftFileName(boolean isWelsh) {
        return !isWelsh ? docC7DraftFilename : docC7DraftWelshFilename;
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

    public String getTemplate(CaseData caseData, String docGenFor, boolean isWelsh) {
        String template = "";

        switch (docGenFor) {
            case C8_HINT:
                template = findC8Template(isWelsh, caseData);
                break;
            case C8_DRAFT_HINT:
                template = c100DocumentTemplateFinderService.findC8DraftDocumentTemplate(caseData,isWelsh);
                break;
            case C8_RESP_DRAFT_HINT:
                template = c100RespC8DraftTemplate;
                break;
            case C8_RESP_FINAL_HINT:
                template = findFinalRespondentC8Template(isWelsh);
                break;
            case C8_RESP_FL401_FINAL_HINT:
                template = findFinalDaRespondentC8Template(isWelsh);
                break;
            case C1A_HINT:
                template = c100DocumentTemplateFinderService.findC1ATemplate(caseData,isWelsh);
                break;
            case C1A_DRAFT_HINT:
                template = c100DocumentTemplateFinderService.findDraftC1ATemplate(caseData,isWelsh);
                break;
            case FINAL_HINT:
                template = findFinalTemplate(isWelsh, caseData);
                break;
            case DRAFT_HINT:
                template = findDraftTemplate(isWelsh, caseData);
                break;
            case DOCUMENT_COVER_SHEET_HINT:
                template = findDocCoverSheetTemplate(isWelsh);
                break;
            case DOCUMENT_C7_DRAFT_HINT:
                template = getC7CitizenDraftTemplate(isWelsh);
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
            case C7_FINAL_RESPONDENT:
                template = getC7FinalTemplate(isWelsh);
                break;
            case SOLICITOR_C7_DRAFT_DOCUMENT:
                template = findDocCoverSheetC7DraftTemplate(isWelsh);
                break;
            case SOLICITOR_C7_FINAL_DOCUMENT:
                template = findDocCoverSheetC7FinalTemplate(isWelsh);
                break;
            case SOLICITOR_C1A_FINAL_DOCUMENT:
                template = solicitorC1AFinalTemplate;
                break;
            case SOLICITOR_C1A_DRAFT_DOCUMENT:
                template = solicitorC1ADraftTemplate;
                break;
            case SOLICITOR_C1A_WELSH_FINAL_DOCUMENT:
                template = solicitorC1AFinalWelshTemplate;
                break;
            case SOLICITOR_C1A_WELSH_DRAFT_DOCUMENT:
                template = solicitorC1ADraftWelshTemplate;
                break;
            case DA_LIST_ON_NOTICE_FL404B_DOCUMENT:
                template = daListOnNoticeFl404bTemplate;
                break;
            case DOCUMENT_COVER_SHEET_SERVE_ORDER_HINT:
                template = findDocCoverSheetTemplateForServeOrder(isWelsh);
                break;
            default:
                template = "";
        }
        return template;
    }

    private String getC7FinalTemplate(boolean isWelsh) {
        return !isWelsh ? docC7FinalEngTemplate : docC7FinalWelshTemplate;
    }

    private String getC7CitizenDraftTemplate(boolean isWelsh) {
        return !isWelsh ? docC7DraftTemplate : docC7DraftWelshTemplate;
    }

    private String findDraftTemplate(boolean isWelsh, CaseData caseData) {

        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            return c100DocumentTemplateFinderService.findFinalDraftDocumentTemplate(caseData,isWelsh);
        }
        return !isWelsh ? fl401DraftTemplate : fl401DraftWelshTemplate;

    }

    private String findFinalTemplate(boolean isWelsh, CaseData caseData) {

        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            return c100DocumentTemplateFinderService.findFinalDocumentTemplate(caseData,isWelsh);
        }
        return !isWelsh ? fl401FinalTemplate : fl401FinalWelshTemplate;

    }

    private String findC8Template(boolean isWelsh, CaseData caseData) {
        String template;
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            return c100DocumentTemplateFinderService.findC8DocumentTemplate(caseData,isWelsh);
        } else {
            template = !isWelsh ? fl401C8Template : fl401C8WelshTemplate;
        }
        return template;
    }

    private String findFinalRespondentC8Template(boolean isWelsh) {
        return !isWelsh ? c100RespC8Template  : respC8TemplateWelsh;

    }

    private String findFinalDaRespondentC8Template(boolean isWelsh) {
        return !isWelsh ? fl401RespC8Template  : fl401RespC8TemplateWelsh;
    }

    private String findFinalRespondentC8FileName(boolean isWelsh) {
        return !isWelsh ? c100RespC8Filename  : respC8FilenameWelsh;

    }

    private String findDocCoverSheetTemplate(boolean isWelsh) {
        return !isWelsh ? docCoverSheetTemplate : docCoverSheetWelshTemplate;
    }

    private String findDocCoverSheetTemplateForServeOrder(boolean isWelsh) {
        //Need to replace EMPTY_STRING with received welsh template
        return !isWelsh ? docCoverSheetServeOrderTemplate : docCoverSheetWelshServeOrderTemplate;
    }

    private String findDocCoverSheetC7DraftTemplate(boolean isWelsh) {
        return !isWelsh ? solicitorC7DraftTemplate : solicitorC7WelshDraftTemplate;
    }

    private String findDocCoverSheetC7FinalTemplate(boolean isWelsh) {
        return !isWelsh ? solicitorC7FinalTemplate : solicitorC7WelshFinalTemplate;
    }

    private String findDocCoverSheetC7DraftFileName(boolean isWelsh) {
        return !isWelsh ? solicitorC7DraftFilename : solicitorC7WelshDraftFilename;
    }

    private String findDocCoverSheetC7FinalFileName(boolean isWelsh) {
        return !isWelsh ? solicitorC7FinalFilename : solicitorC7WelshFinalFilename;
    }

    private boolean isApplicantOrChildDetailsConfidential(CaseData caseData) {
        PartyDetails partyDetails = caseData.getApplicantsFL401();
        Optional<TypeOfApplicationOrders> typeOfApplicationOrders = ofNullable(caseData.getTypeOfApplicationOrders());

        boolean isChildrenConfidential = isChildrenDetailsConfidentiality(caseData, typeOfApplicationOrders);

        return isPartyDetailsConfidential(partyDetails)
            || isChildrenConfidential;
    }

    private boolean isChildrenDetailsConfidentiality(CaseData caseData,
                                                     Optional<TypeOfApplicationOrders> typeOfApplicationOrders) {
        boolean childrenConfidentiality = false;

        if (typeOfApplicationOrders.isPresent() && typeOfApplicationOrders.get().getOrderType().contains(
            FL401OrderTypeEnum.occupationOrder)
            && Objects.nonNull(caseData.getHome())
            && YesOrNo.Yes.equals(caseData.getHome().getDoAnyChildrenLiveAtAddress())) {
            List<ChildrenLiveAtAddress> childrenLiveAtAddresses =
                caseData.getHome().getChildren().stream().map(Element::getValue).collect(
                    Collectors.toList());

            for (ChildrenLiveAtAddress address : childrenLiveAtAddresses) {
                if (YesOrNo.Yes.equals(address.getKeepChildrenInfoConfidential())) {
                    childrenConfidentiality = true;
                }

            }
        }
        return childrenConfidentiality;
    }

    private boolean isPartyDetailsConfidential(PartyDetails partyDetails) {

        boolean isPartyInformationConfidential = false;
        if ((YesOrNo.Yes).equals(partyDetails.getIsAddressConfidential())) {
            isPartyInformationConfidential = true;
        }
        if ((YesOrNo.Yes).equals(partyDetails.getIsEmailAddressConfidential())) {
            isPartyInformationConfidential = true;
        }
        if ((YesOrNo.Yes).equals(partyDetails.getIsPhoneNumberConfidential())) {
            isPartyInformationConfidential = true;
        }
        return isPartyInformationConfidential;
    }

    public Document generateSingleDocument(String authorisation,
                                           CaseData caseData,
                                           String hint,
                                           boolean isWelsh, Map<String, Object> respondentDetails) throws Exception {
        return getDocument(authorisation, caseData, hint, isWelsh, respondentDetails);
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

    private UploadedDocuments generateCitizenUploadDocument(String fileName,
                                                            GeneratedDocumentInfo generatedDocumentInfo,
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
        YesOrNo documentRequest = null;

        if (generateAndUploadDocumentRequest.getValues() != null) {
            if (generateAndUploadDocumentRequest.getValues().containsKey(PARENT_DOCUMENT_TYPE)) {
                parentDocumentType = generateAndUploadDocumentRequest.getValues().get(PARENT_DOCUMENT_TYPE);
            }
            if (generateAndUploadDocumentRequest.getValues().containsKey(PARTY_ID)) {
                partyId = generateAndUploadDocumentRequest.getValues().get(PARTY_ID);
            }
            if (generateAndUploadDocumentRequest.getValues().containsKey(DOCUMENT_TYPE)) {
                documentType = generateAndUploadDocumentRequest.getValues().get(DOCUMENT_TYPE);
                if (generateAndUploadDocumentRequest.getValues().containsKey(PARTY_NAME)) {
                    partyName = generateAndUploadDocumentRequest.getValues().get(PARTY_NAME);
                    documentName = documentType.replace("Your", partyName + "'s");
                }
            }
            if (generateAndUploadDocumentRequest.getValues().containsKey(IS_APPLICANT)) {
                isApplicant = generateAndUploadDocumentRequest.getValues().get(IS_APPLICANT);
            }

            if (generateAndUploadDocumentRequest.getValues().containsKey(DOCUMENT_REQUEST)) {
                documentRequest = YesOrNo.valueOf(generateAndUploadDocumentRequest.getValues().get(DOCUMENT_REQUEST));
            }

        }

        return UploadedDocuments.builder()
            .parentDocumentType(parentDocumentType)
            .documentType(documentType)
            .partyName(partyName)
            .isApplicant(isApplicant)
            .uploadedBy(partyId)
            .dateCreated(LocalDate.now())
            .documentRequestedByCourt(documentRequest)
            .documentDetails(DocumentDetails.builder()
                                 .documentName(documentName)
                                 .documentUploadedDate(formattedCurrentDate)
                                 .build()).citizenDocument(generateDocumentField(
                fileName,
                generatedDocumentInfo
            )).build();
    }

    public DocumentResponse uploadDocument(String authorization, MultipartFile file) throws IOException {
        try {
            uk.gov.hmcts.reform.ccd.document.am.model.Document stampedDocument
                = uploadService.uploadDocument(
                file.getBytes(),
                file.getOriginalFilename(),
                file.getContentType(),
                authorization
            );
            log.info("Stored Doc Detail: " + stampedDocument.toString());
            return DocumentResponse.builder().status("Success").document(Document.builder()
                                                                             .documentBinaryUrl(stampedDocument.links.binary.href)
                                                                             .documentUrl(stampedDocument.links.self.href)
                                                                             .documentFileName(stampedDocument.originalDocumentName)
                                                                             .documentCreatedOn(stampedDocument.createdOn)
                                                                             .build()).build();

        } catch (Exception e) {
            log.error("Error while uploading document .", e);
            throw e;
        }
    }

    public DocumentResponse deleteDocument(String authorization, String documentId) {
        try {
            uploadService.deleteDocument(
                authorization,
                documentId
            );
            log.info("document deleted successfully..");
            return DocumentResponse.builder().status("Success").build();

        } catch (Exception e) {
            log.error("Error while deleting  document .", e);
            throw e;
        }
    }

    public ResponseEntity<Resource> downloadDocument(String authorization, String documentId) {
        try {
            return uploadService.downloadDocument(
                authorization,
                documentId
            );

        } catch (Exception e) {
            log.error("Error while downloading  document .", e);
            throw e;
        }
    }

    public Map<String, Object> generateDocumentsForTestingSupport(String authorisation, CaseData caseData) throws Exception {

        Map<String, Object> updatedCaseData = new HashMap<>();

        caseData = fillOrgDetails(caseData);
        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);

        documentLanguageIsEngForTestingSupport(authorisation, caseData, updatedCaseData, documentLanguage);
        documentLanguageIsWelshForTestingSupport(authorisation, caseData, updatedCaseData, documentLanguage);
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

    private void documentLanguageIsEngForTestingSupport(String authorisation, CaseData caseData, Map<String, Object> updatedCaseData,
                                                        DocumentLanguage documentLanguage) throws Exception {
        if (documentLanguage.isGenEng()) {
            updatedCaseData.put(ENGDOCGEN, Yes.toString());
            isConfidentialInformationPresentForC100EngForTestingSupport(authorisation, caseData, updatedCaseData);
            isC100CaseTypeEngForTestingSupport(authorisation, caseData, updatedCaseData);
            updatedCaseData.put(DOCUMENT_FIELD_FINAL, getDocument(authorisation, caseData, FINAL_HINT, false));
            updatedCaseData.put(DRAFT_APPLICATION_DOCUMENT_FIELD, getDocument(authorisation, caseData, DRAFT_HINT, false));
        }
    }

    private void isConfidentialInformationPresentForC100EngForTestingSupport(String authorisation, CaseData caseData,
                                                                             Map<String, Object> updatedCaseData) throws Exception {
        if (isConfidentialInformationPresentForC100(caseData)) {
            updatedCaseData.put(DOCUMENT_FIELD_C8, getDocument(authorisation, caseData, C8_HINT, false));
            updatedCaseData.put(
                DOCUMENT_FIELD_DRAFT_C8,
                getDocument(authorisation, caseData, C8_DRAFT_HINT, false)
            );
        } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
            && isApplicantOrChildDetailsConfidential(caseData)) {
            updatedCaseData.put(DOCUMENT_FIELD_C8, getDocument(authorisation, caseData, C8_HINT, false));
        } else {
            updatedCaseData.put(DOCUMENT_FIELD_C8, null);
        }
    }

    private void isC100CaseTypeEngForTestingSupport(String authorisation, CaseData caseData, Map<String, Object> updatedCaseData) throws Exception {
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
            && caseData.getAllegationOfHarm() != null
            && YesOrNo.Yes.equals(caseData.getAllegationOfHarm().getAllegationsOfHarmYesNo())) {
            updatedCaseData.put(DOCUMENT_FIELD_C1A, getDocument(authorisation, caseData, C1A_HINT, false));
            updatedCaseData.put(
                DOCUMENT_FIELD_DRAFT_C1A,
                getDocument(authorisation, caseData, C1A_DRAFT_HINT, false)
            );
        } else {
            updatedCaseData.put(DOCUMENT_FIELD_C1A, null);
        }
    }

    private void documentLanguageIsWelshForTestingSupport(String authorisation, CaseData caseData, Map<String, Object> updatedCaseData,
                                                          DocumentLanguage documentLanguage) throws Exception {
        if (documentLanguage.isGenWelsh()) {
            updatedCaseData.put("isWelshDocGen", Yes.toString());
            isConfidentialInformationPresentForC100WelshForTestingSupport(authorisation, caseData, updatedCaseData);
            isC100CaseTypeWelshForTestingSupport(authorisation, caseData, updatedCaseData);
            updatedCaseData.put(
                DOCUMENT_FIELD_FINAL_WELSH,
                getDocument(authorisation, caseData, FINAL_HINT, true)
            );
            updatedCaseData.put(DRAFT_APPLICATION_DOCUMENT_WELSH_FIELD, getDocument(authorisation, caseData, DRAFT_HINT, true));
        }
    }

    private void isConfidentialInformationPresentForC100WelshForTestingSupport(String authorisation, CaseData caseData,
                                                                               Map<String, Object> updatedCaseData) throws Exception {
        if (isConfidentialInformationPresentForC100(caseData)) {
            updatedCaseData.put(DOCUMENT_FIELD_C8_WELSH, getDocument(authorisation, caseData, C8_HINT, true));
            updatedCaseData.put(
                DOCUMENT_FIELD_C8_DRAFT_WELSH,
                getDocument(authorisation, caseData, C8_DRAFT_HINT, true)
            );
        } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
            && isApplicantOrChildDetailsConfidential(caseData)) {
            updatedCaseData.put(DOCUMENT_FIELD_C8_WELSH, getDocument(authorisation, caseData, C8_HINT, true));
        } else {
            updatedCaseData.put(DOCUMENT_FIELD_C8_WELSH, null);
        }
    }

    private void isC100CaseTypeWelshForTestingSupport(String authorisation, CaseData caseData, Map<String, Object> updatedCaseData) throws Exception {
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
            && caseData.getAllegationOfHarm() != null
            && YesOrNo.Yes.equals(caseData.getAllegationOfHarm().getAllegationsOfHarmYesNo())) {
            updatedCaseData.put(DOCUMENT_FIELD_C1A_WELSH, getDocument(authorisation, caseData, C1A_HINT, true));
            updatedCaseData.put(
                DOCUMENT_FIELD_C1A_DRAFT_WELSH,
                getDocument(authorisation, caseData, C1A_DRAFT_HINT, true)
            );
        } else {
            updatedCaseData.put(DOCUMENT_FIELD_C1A_WELSH, null);
        }
    }

    public byte[] getDocumentBytes(String docUrl, String authToken, String s2sToken) {

        String fileName = FilenameUtils.getName(docUrl);
        ResponseEntity<Resource> resourceResponseEntity = caseDocumentClient.getDocumentBinary(
            authToken,
            s2sToken,
            docUrl
        );

        return Optional.ofNullable(resourceResponseEntity)
            .map(ResponseEntity::getBody)
            .map(resource -> {
                try {
                    return resource.getInputStream().readAllBytes();
                } catch (IOException e) {
                    throw new InvalidResourceException("Doc name " + fileName, e);
                }
            })
            .orElseThrow(() -> new InvalidResourceException("Resource is invalid " + fileName));
    }

    private boolean checkFileFormat(String fileName) {
        String format = "";
        if (null != fileName) {
            int i = fileName.lastIndexOf('.');
            if (i >= 0) {
                format = fileName.substring(i + 1);
            }
            String finalFormat = format;
            return Arrays.stream(ALLOWED_FILE_TYPES).anyMatch(s -> s.equalsIgnoreCase(finalFormat));
        } else {
            return false;
        }
    }

    public Document convertToPdf(String authorisation, Document document) {
        String filename = document.getDocumentFileName();
        if (checkFileFormat(document.getDocumentFileName())) {
            ResponseEntity<Resource> responseEntity = caseDocumentClient.getDocumentBinary(
                authorisation,
                authTokenGenerator.generate(),
                document.getDocumentBinaryUrl()
            );
            byte[] docInBytes = Optional.ofNullable(responseEntity)
                .map(ResponseEntity::getBody)
                .map(resource -> {
                    try {
                        return resource.getInputStream().readAllBytes();
                    } catch (IOException e) {
                        throw new InvalidResourceException("Doc name " + filename, e);
                    }
                })
                .orElseThrow(() -> new InvalidResourceException("Resource is invalid " + filename));

            Map<String, Object> tempCaseDetails = new HashMap<>();
            tempCaseDetails.put("fileName", docInBytes);
            GeneratedDocumentInfo generatedDocumentInfo = dgsApiClient.convertDocToPdf(
                document.getDocumentFileName(),
                authorisation, GenerateDocumentRequest
                    .builder().template(DUMMY).values(tempCaseDetails).build()
            );
            return Document.builder()
                .documentUrl(generatedDocumentInfo.getUrl())
                .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                .documentFileName(generatedDocumentInfo.getDocName())
                .build();


        }
        return document;
    }

    public DocumentResponse generateAndUploadDocument(String authorisation,
                                                      DocumentRequest documentRequest) throws DocumentGenerationException {
        //generate file name
        String fileName = getCitizenUploadedStatementFileName(documentRequest);
        log.info("fileName {}", fileName);

        GeneratedDocumentInfo generatedDocumentInfo = dgsService.generateCitizenDocument(
            authorisation,
            documentRequest,
            prlCitizenUploadTemplate
        );
        log.info("generatedDocumentInfo {}", generatedDocumentInfo);
        if (null != generatedDocumentInfo) {
            return DocumentResponse.builder()
                .status(SUCCESS)
                .document(generateDocumentField(fileName, generatedDocumentInfo))
                .build();
        }

        return null;
    }

    public CaseDetails citizenSubmitDocuments(String authorisation, DocumentRequest documentRequest) {

        String caseId = documentRequest.getCaseId();

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent
            = allTabService.getStartUpdateForSpecificEvent(String.valueOf(caseId), CITIZEN_CASE_UPDATE.getValue());
        Map<String, Object> updatedCaseDataMap = startAllTabsUpdateDataContent.caseDataMap();
        CaseData updatedCaseData = startAllTabsUpdateDataContent.caseData();

        UserDetails userDetails = userService.getUserDetails(authorisation);

        if (isNotBlank(documentRequest.getCategoryId())
            && CollectionUtils.isNotEmpty(documentRequest.getDocuments())) {
            DocumentCategory category = DocumentCategory.getValue(documentRequest.getCategoryId());

            List<QuarantineLegalDoc> quarantineLegalDocs = documentRequest.getDocuments().stream()
                .map(document -> getCitizenQuarantineDocument(
                    document,
                    documentRequest,
                    category,
                    userDetails
                ))
                .toList();

            if (category.equals(DocumentCategory.FM5_STATEMENTS)) {
                for (QuarantineLegalDoc quarantineLegalDoc : quarantineLegalDocs) {
                    String userRole = CaseUtils.getUserRole(userDetails);
                    manageDocumentsService.moveDocumentsToRespectiveCategoriesNew(
                        quarantineLegalDoc,
                        userDetails,
                        updatedCaseData,
                        updatedCaseDataMap,
                        userRole
                    );
                }
            } else {
                //move all documents to citizen quarantine except fm5 documents
                manageDocumentsService.setFlagsForWaTask(
                    updatedCaseData,
                    updatedCaseDataMap,
                    CITIZEN,
                    quarantineLegalDocs.get(0)
                );

                moveCitizenDocumentsToQuarantineTab(
                    quarantineLegalDocs,
                    updatedCaseData,
                    updatedCaseDataMap
                );
            }

            //update all tabs
            return allTabService.submitAllTabsUpdate(
                startAllTabsUpdateDataContent.authorisation(),
                caseId,
                startAllTabsUpdateDataContent.startEventResponse(),
                startAllTabsUpdateDataContent.eventRequestData(),
                updatedCaseDataMap
            );

        }
        return null;
    }

    private Map<String, Object> moveCitizenDocumentsToQuarantineTab(List<QuarantineLegalDoc> quarantineLegalDocs,
                                                                    CaseData caseData,
                                                                    Map<String, Object> caseDataUpdated) {
        for (QuarantineLegalDoc quarantineLegalDoc : quarantineLegalDocs) {
            //invoke common manage docs
            manageDocumentsService.moveDocumentsToQuarantineTab(
                quarantineLegalDoc,
                caseData,
                caseDataUpdated,
                CITIZEN
            );
        }
        return caseDataUpdated;
    }

    private QuarantineLegalDoc getCitizenQuarantineDocument(Document document,
                                                            DocumentRequest documentRequest,
                                                            DocumentCategory category,
                                                            UserDetails userDetails) {
        return QuarantineLegalDoc.builder()
            .citizenQuarantineDocument(document.toBuilder()
                                           .documentCreatedOn(Date.from(ZonedDateTime.now(ZoneId.of(LONDON_TIME_ZONE)).toInstant()))
                                           .build())
            .documentParty(documentRequest.getPartyType())
            .documentUploadedDate(LocalDateTime.now(ZoneId.of(LONDON_TIME_ZONE)))
            .categoryId(category.getCategoryId())
            .categoryName(category.getDisplayedValue())
            .isConfidential(documentRequest.getIsConfidential())
            .isRestricted(documentRequest.getIsRestricted())
            .restrictedDetails(documentRequest.getRestrictDocumentDetails())
            .uploadedBy(documentRequest.getPartyName())
            .uploadedByIdamId(null != userDetails ? userDetails.getId() : null)
            .uploaderRole(CITIZEN)
            .build();
    }

}
