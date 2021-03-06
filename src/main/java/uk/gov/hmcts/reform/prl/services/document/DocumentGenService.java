package uk.gov.hmcts.reform.prl.services.document;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenLiveAtAddress;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.DgsService;
import uk.gov.hmcts.reform.prl.services.DocumentLanguageService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C1A_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C8_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C1A;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C1A_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C8;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C8_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_FINAL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_FINAL_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRAFT_DOCUMENT_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRAFT_DOCUMENT_WELSH_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRAFT_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FINAL_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;


@Slf4j
@Service
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

    @Value("${document.templates.c100.c100_c8_filename}")
    protected String c100C8Filename;

    @Value("${document.templates.c100.c100_c1a_template}")
    protected String c100C1aTemplate;

    @Value("${document.templates.c100.c100_c1a_filename}")
    protected String c100C1aFilename;

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

    @Value("${document.templates.c100.c100_c8_welsh_filename}")
    protected String c100C8WelshFilename;

    @Value("${document.templates.c100.c100_c1a_welsh_template}")
    protected String c100C1aWelshTemplate;

    @Value("${document.templates.c100.c100_c1a_welsh_filename}")
    protected String c100C1aWelshFilename;

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

    @Autowired
    private DgsService dgsService;

    @Autowired
    DocumentLanguageService documentLanguageService;

    @Autowired
    OrganisationService organisationService;

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
                updatedCaseData.put(DOCUMENT_FIELD_C8, getDocument(authorisation, caseData, C8_HINT, false));
            } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
                    && isApplicantOrChildDetailsConfidential(caseData)) {
                updatedCaseData.put(DOCUMENT_FIELD_C8, getDocument(authorisation, caseData, C8_HINT, false));
            } else {
                updatedCaseData.put(DOCUMENT_FIELD_C8, null);
            }
            if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
                && caseData.getAllegationOfHarm() != null
                && YesOrNo.Yes.equals(caseData.getAllegationOfHarm().getAllegationsOfHarmYesNo())) {
                updatedCaseData.put(DOCUMENT_FIELD_C1A, getDocument(authorisation, caseData, C1A_HINT, false));
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
                updatedCaseData.put(DOCUMENT_FIELD_C8_WELSH, getDocument(authorisation, caseData, C8_HINT, true));
            } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
                && isApplicantOrChildDetailsConfidential(caseData)) {
                updatedCaseData.put(DOCUMENT_FIELD_C8_WELSH, getDocument(authorisation, caseData, C8_HINT, true));
            } else {
                updatedCaseData.put(DOCUMENT_FIELD_C8_WELSH, null);
            }

            if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
                && caseData.getAllegationOfHarm() != null
                && YesOrNo.Yes.equals(caseData.getAllegationOfHarm().getAllegationsOfHarmYesNo())) {
                updatedCaseData.put(DOCUMENT_FIELD_C1A_WELSH, getDocument(authorisation, caseData, C1A_HINT, true));
            } else {
                updatedCaseData.put(DOCUMENT_FIELD_C1A_WELSH, null);
            }
            if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication()) || State.CASE_ISSUE.equals(
                caseData.getState())) {
                updatedCaseData.put(DOCUMENT_FIELD_FINAL_WELSH, getDocument(authorisation, caseData, FINAL_HINT, true));
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
            &&  ofNullable(caseData.getApplicantsConfidentialDetails()).isPresent()
            && !caseData.getApplicantsConfidentialDetails().isEmpty()
            || ofNullable(caseData.getChildrenConfidentialDetails()).isPresent()
            && !caseData.getChildrenConfidentialDetails().isEmpty();
    }

    public Map<String, Object> generateDraftDocuments(String authorisation, CaseData caseData) throws Exception {

        Map<String, Object> updatedCaseData = new HashMap<>();

        caseData = fillOrgDetails(caseData);
        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
        log.info("Selected Language for generating the docs English => {}, Welsh => {}", documentLanguage.isGenEng(), documentLanguage.isGenWelsh());
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
            case C1A_HINT:
                fileName =  !isWelsh ? c100C1aFilename : c100C1aWelshFilename;
                break;
            case FINAL_HINT:
                fileName = findFinalFilename(isWelsh, caseTypeOfApp);
                break;
            case DRAFT_HINT:
                fileName = findDraftFilename(isWelsh, caseTypeOfApp);
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

    private String getTemplate(CaseData caseData, String docGenFor, boolean isWelsh) {
        String caseTypeOfApp = caseData.getCaseTypeOfApplication();
        String template = "";

        switch (docGenFor) {
            case C8_HINT:
                template = findC8Template(isWelsh, caseTypeOfApp);
                break;
            case C1A_HINT:
                template = !isWelsh ? c100C1aTemplate : c100C1aWelshTemplate;
                break;
            case FINAL_HINT:
                template = findFinalTemplate(isWelsh, caseTypeOfApp);
                break;
            case DRAFT_HINT:
                template = findDraftTemplate(isWelsh, caseTypeOfApp);
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
}
