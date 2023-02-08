package uk.gov.hmcts.reform.prl.services.document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.DocumentLanguageService;


@Service
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


    public String findC100FinalDocumentTemplate(CaseData caseData) {
        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
        if ("v2".equalsIgnoreCase(caseData.getTaskListVersion())) {
            return !documentLanguage.isGenWelsh() ? c100FinalTemplateV2 : c100FinalWelshTemplateV2;
        }
        return !documentLanguage.isGenWelsh() ? c100FinalTemplate : c100FinalWelshTemplate;

    }

    public String findC100FinalDraftDocumentTemplate(CaseData caseData) {
        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
        if ("v2".equalsIgnoreCase(caseData.getTaskListVersion())) {
            return !documentLanguage.isGenWelsh() ? c100DraftTemplateV2 : c100DraftWelshTemplateV2;
        }
        return !documentLanguage.isGenWelsh()  ? c100DraftTemplate : c100DraftWelshTemplate;

    }
}
