package uk.gov.hmcts.reform.prl.services.document;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.DocumentLanguageService;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;


@Service
@Slf4j
public class C100DocumentTemplateFinderService {


    @Autowired
    DocumentLanguageService documentLanguageService;

    @Value("${document.templates.c100.c100_final_template}")
    protected String c100FinalTemplate;

    @Value("${document.templates.c100.c100_final_welsh_template}")
    protected String c100FinalWelshTemplate;

    @Value("${document.templates.c100.c100_final_template_v2}")
    protected String c100FinalTemplateV2;

    @Value("${document.templates.c100.c100_final_welsh_template_v2}")
    protected String c100FinalWelshTemplateV2;


    @Value("${document.templates.c100.c100_draft_template}")
    protected String c100DraftTemplate;


    @Value("${document.templates.c100.c100_draft_welsh_template}")
    protected String c100DraftWelshTemplate;

    @Value("${document.templates.c100.c100_draft_template_v2}")
    protected String c100DraftTemplateV2;


    @Value("${document.templates.c100.c100_draft_welsh_template_v2}")
    protected String c100DraftWelshTemplateV2;

    @Value("${document.templates.c100.c100_c8_template}")
    protected String c100C8Template;

    @Value("${document.templates.c100.c100_c8_template_v2}")
    protected String c100C8TemplateV2;

    @Value("${document.templates.c100.c100_c8_draft_template}")
    protected String c100C8DraftTemplate;

    @Value("${document.templates.c100.c100_c8_draft_template_v2}")
    protected String c100C8DraftTemplateV2;

    @Value("${document.templates.c100.c100_c8_welsh_template}")
    protected String c100C8WelshTemplate;

    @Value("${document.templates.c100.c100_c8_welsh_template_v2}")
    protected String c100C8WelshTemplateV2;

    @Value("${document.templates.c100.c100_c8_draft_welsh_template}")
    protected String c100C8DraftWelshTemplate;

    @Value("${document.templates.c100.c100_c8_draft_welsh_template_v2}")
    protected String c100C8DraftWelshTemplateV2;


    public String findFinalDocumentTemplate(CaseData caseData) {
        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
        if (TASK_LIST_VERSION_V2.equalsIgnoreCase(caseData.getTaskListVersion())) {
            log.info("generate v2 {}" ,!documentLanguage.isGenWelsh() ? c100FinalTemplateV2 : c100FinalWelshTemplateV2);
            return !documentLanguage.isGenWelsh() ? c100FinalTemplateV2 : c100FinalWelshTemplateV2;
        }
        log.info("generate v1 {}" ,!documentLanguage.isGenWelsh() ? c100FinalTemplate : c100FinalWelshTemplate);
        return !documentLanguage.isGenWelsh() ? c100FinalTemplate : c100FinalWelshTemplate;

    }

    public String findFinalDraftDocumentTemplate(CaseData caseData) {
        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
        if (TASK_LIST_VERSION_V2.equalsIgnoreCase(caseData.getTaskListVersion())) {
            log.info("generate v2 {}" ,!documentLanguage.isGenWelsh() ? c100DraftTemplateV2 : c100DraftWelshTemplateV2);
            return !documentLanguage.isGenWelsh() ? c100DraftTemplateV2 : c100DraftWelshTemplateV2;
        }
        log.info("generate v1 {}" ,!documentLanguage.isGenWelsh()  ? c100DraftTemplate : c100DraftWelshTemplate);
        return !documentLanguage.isGenWelsh()  ? c100DraftTemplate :  c100DraftWelshTemplate;

    }

    public String findC8DocumentTemplate(CaseData caseData) {
        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
        if (TASK_LIST_VERSION_V2.equalsIgnoreCase(caseData.getTaskListVersion())) {
            return !documentLanguage.isGenWelsh() ? c100C8TemplateV2 : c100C8WelshTemplateV2;
        }
        return !documentLanguage.isGenWelsh() ? c100C8Template : c100C8WelshTemplate;

    }

    public String findC8DraftDocumentTemplate(CaseData caseData) {
        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
        if (TASK_LIST_VERSION_V2.equalsIgnoreCase(caseData.getTaskListVersion())) {
            return !documentLanguage.isGenWelsh() ? c100C8DraftTemplateV2 : c100C8DraftWelshTemplateV2;
        }
        return !documentLanguage.isGenWelsh()  ? c100C8DraftTemplate : c100C8DraftWelshTemplate;

    }
}
