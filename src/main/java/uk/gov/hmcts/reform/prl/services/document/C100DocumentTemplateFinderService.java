package uk.gov.hmcts.reform.prl.services.document;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V3;


@Service
@Slf4j
public class C100DocumentTemplateFinderService {



    @Value("${document.templates.c100.c100_final_template}")
    protected String c100FinalTemplate;

    @Value("${document.templates.c100.c100_final_welsh_template}")
    protected String c100FinalWelshTemplate;

    @Value("${document.templates.c100.c100_final_template_v2}")
    protected String c100FinalTemplateV2;

    @Value("${document.templates.c100.c100_final_welsh_template_v2}")
    protected String c100FinalWelshTemplateV2;

    @Value("${document.templates.c100.c100_final_template_v3}")
    protected String c100FinalTemplateV3;

    @Value("${document.templates.c100.c100_final_welsh_template_v3}")
    protected String c100FinalWelshTemplateV3;


    @Value("${document.templates.c100.c100_draft_template}")
    protected String c100DraftTemplate;


    @Value("${document.templates.c100.c100_draft_welsh_template}")
    protected String c100DraftWelshTemplate;

    @Value("${document.templates.c100.c100_draft_template_v2}")
    protected String c100DraftTemplateV2;


    @Value("${document.templates.c100.c100_draft_welsh_template_v2}")
    protected String c100DraftWelshTemplateV2;

    @Value("${document.templates.c100.c100_draft_template_v3}")
    protected String c100DraftTemplateV3;


    @Value("${document.templates.c100.c100_draft_welsh_template_v3}")
    protected String c100DraftWelshTemplateV3;

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

    @Value("${document.templates.c100.c100_c1a_template}")
    protected String c100C1aTemplate;

    @Value("${document.templates.c100.c100_c1a_draft_template}")
    protected String c100C1aDraftTemplate;

    @Value("${document.templates.c100.c100_c1a_template_v2}")
    protected String c100C1aTemplateV2;

    @Value("${document.templates.c100.c100_c1a_draft_template_v2}")
    protected String c100C1aDraftTemplateV2;

    @Value("${document.templates.c100.c100_c1a_welsh_template}")
    protected String c100C1aWelshTemplate;

    @Value("${document.templates.c100.c100_c1a_welsh_template_v2}")
    protected String c100C1aWelshTemplateV2;

    @Value("${document.templates.c100.c100_c1a_draft_welsh_template}")
    protected String c100C1aDraftWelshTemplate;

    @Value("${document.templates.c100.c100_c1a_draft_welsh_template_v2}")
    protected String c100C1aDraftWelshTemplateV2;


    public String findFinalDocumentTemplate(CaseData caseData,boolean isWelsh) {

        if (TASK_LIST_VERSION_V3.equalsIgnoreCase(caseData.getTaskListVersion())) {
            log.info("generate v3 {}", !isWelsh ? c100FinalTemplateV3 : c100FinalWelshTemplateV3);
            return !isWelsh ? c100FinalTemplateV3 : c100FinalWelshTemplateV3;
        } else if (TASK_LIST_VERSION_V2.equalsIgnoreCase(caseData.getTaskListVersion())) {
            log.info("generate v2 {}", !isWelsh ? c100FinalTemplateV2 : c100FinalWelshTemplateV2);
            return !isWelsh ? c100FinalTemplateV2 : c100FinalWelshTemplateV2;
        }
        log.info("generate v1 {}",!isWelsh ? c100FinalTemplate : c100FinalWelshTemplate);
        return !isWelsh ? c100FinalTemplate : c100FinalWelshTemplate;

    }

    public String findFinalDraftDocumentTemplate(CaseData caseData,boolean isWelsh) {

        if (TASK_LIST_VERSION_V3.equalsIgnoreCase(caseData.getTaskListVersion())) {
            log.info("generate v3 {}", !isWelsh ? c100DraftTemplateV3 : c100DraftWelshTemplateV3);
            return !isWelsh ? c100DraftTemplateV3 : c100DraftWelshTemplateV3;
        } else if (TASK_LIST_VERSION_V2.equalsIgnoreCase(caseData.getTaskListVersion())) {
            log.info("generate v2 {}", !isWelsh ? c100DraftTemplateV2 : c100DraftWelshTemplateV2);
            return !isWelsh ? c100DraftTemplateV2 : c100DraftWelshTemplateV2;
        }
        log.info("generate v1 {}",!isWelsh  ? c100DraftTemplate : c100DraftWelshTemplate);
        return !isWelsh  ? c100DraftTemplate :  c100DraftWelshTemplate;

    }

    public String findC8DocumentTemplate(CaseData caseData,boolean isWelsh) {
        if (TASK_LIST_VERSION_V2.equalsIgnoreCase(caseData.getTaskListVersion())
            || TASK_LIST_VERSION_V3.equalsIgnoreCase(caseData.getTaskListVersion())) {
            return !isWelsh ? c100C8TemplateV2 : c100C8WelshTemplateV2;
        }
        return !isWelsh ? c100C8Template : c100C8WelshTemplate;

    }

    public String findC8DraftDocumentTemplate(CaseData caseData,boolean isWelsh) {
        if (TASK_LIST_VERSION_V2.equalsIgnoreCase(caseData.getTaskListVersion())
            || TASK_LIST_VERSION_V3.equalsIgnoreCase(caseData.getTaskListVersion())) {
            return !isWelsh ? c100C8DraftTemplateV2 : c100C8DraftWelshTemplateV2;
        }
        return !isWelsh  ? c100C8DraftTemplate : c100C8DraftWelshTemplate;

    }

    public String findC1ATemplate(CaseData caseData,boolean isWelsh) {
        if (TASK_LIST_VERSION_V2.equalsIgnoreCase(caseData.getTaskListVersion())
            || TASK_LIST_VERSION_V3.equalsIgnoreCase(caseData.getTaskListVersion())) {
            return !isWelsh ? c100C1aTemplateV2 : c100C1aWelshTemplateV2;
        }
        return !isWelsh ? c100C1aTemplate : c100C1aWelshTemplate;

    }

    public String findDraftC1ATemplate(CaseData caseData,boolean isWelsh) {
        if (TASK_LIST_VERSION_V2.equalsIgnoreCase(caseData.getTaskListVersion())
            || TASK_LIST_VERSION_V3.equalsIgnoreCase(caseData.getTaskListVersion())) {
            return !isWelsh ? c100C1aDraftTemplateV2 : c100C1aDraftWelshTemplateV2;
        }
        return !isWelsh ? c100C1aDraftTemplate : c100C1aDraftWelshTemplate;

    }
}
