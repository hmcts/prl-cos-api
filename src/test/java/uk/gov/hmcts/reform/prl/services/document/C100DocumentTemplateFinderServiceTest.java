package uk.gov.hmcts.reform.prl.services.document;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V3;


@RunWith(MockitoJUnitRunner.class)
public class C100DocumentTemplateFinderServiceTest {


    @InjectMocks
    C100DocumentTemplateFinderService c100DocumentTemplateFinderService;

    CaseData caseData;


    @Before
    public void setUp() {
        ReflectionTestUtils.setField(c100DocumentTemplateFinderService, "c100FinalTemplate", "c100FinalTemplate");
        ReflectionTestUtils.setField(c100DocumentTemplateFinderService, "c100FinalWelshTemplate", "c100FinalWelshTemplate");
        ReflectionTestUtils.setField(c100DocumentTemplateFinderService, "c100FinalTemplateV2", "c100FinalTemplateV2");
        ReflectionTestUtils.setField(c100DocumentTemplateFinderService, "c100FinalWelshTemplateV2", "c100FinalWelshTemplateV2");
        ReflectionTestUtils.setField(c100DocumentTemplateFinderService, "c100FinalTemplateV3", "c100FinalTemplateV3");
        ReflectionTestUtils.setField(c100DocumentTemplateFinderService, "c100FinalWelshTemplateV3", "c100FinalWelshTemplateV3");

        ReflectionTestUtils.setField(c100DocumentTemplateFinderService, "c100DraftTemplate", "c100DraftTemplate");
        ReflectionTestUtils.setField(c100DocumentTemplateFinderService, "c100DraftWelshTemplate", "c100DraftWelshTemplate");
        ReflectionTestUtils.setField(c100DocumentTemplateFinderService, "c100DraftTemplateV2", "c100DraftTemplateV2");
        ReflectionTestUtils.setField(c100DocumentTemplateFinderService, "c100DraftWelshTemplateV2", "c100DraftWelshTemplateV2");
        ReflectionTestUtils.setField(c100DocumentTemplateFinderService, "c100DraftTemplateV3", "c100DraftTemplateV3");
        ReflectionTestUtils.setField(c100DocumentTemplateFinderService, "c100DraftWelshTemplateV3", "c100DraftWelshTemplateV3");

        ReflectionTestUtils.setField(c100DocumentTemplateFinderService, "c100C8Template", "c100C8Template");
        ReflectionTestUtils.setField(c100DocumentTemplateFinderService, "c100C8DraftTemplate", "c100C8DraftTemplate");
        ReflectionTestUtils.setField(c100DocumentTemplateFinderService, "c100C8TemplateV2", "c100C8TemplateV2");
        ReflectionTestUtils.setField(c100DocumentTemplateFinderService, "c100C8DraftTemplateV2", "c100C8DraftTemplateV2");

        ReflectionTestUtils.setField(c100DocumentTemplateFinderService, "c100C8WelshTemplate", "c100C8WelshTemplate");
        ReflectionTestUtils.setField(c100DocumentTemplateFinderService, "c100C8WelshTemplateV2", "c100C8WelshTemplateV2");
        ReflectionTestUtils.setField(c100DocumentTemplateFinderService, "c100C8DraftWelshTemplate", "c100C8DraftWelshTemplate");
        ReflectionTestUtils.setField(c100DocumentTemplateFinderService, "c100C8DraftWelshTemplateV2", "c100C8DraftWelshTemplateV2");

        ReflectionTestUtils.setField(c100DocumentTemplateFinderService, "c100C1aWelshTemplate", "c100C1aWelshTemplate");
        ReflectionTestUtils.setField(c100DocumentTemplateFinderService, "c100C1aWelshTemplateV2", "c100C1aWelshTemplateV2");
        ReflectionTestUtils.setField(c100DocumentTemplateFinderService, "c100C1aDraftWelshTemplate", "c100C1aDraftWelshTemplate");
        ReflectionTestUtils.setField(c100DocumentTemplateFinderService, "c100C1aDraftWelshTemplateV2", "c100C1aDraftWelshTemplateV2");

        ReflectionTestUtils.setField(c100DocumentTemplateFinderService, "c100C1aTemplate", "c100C1aTemplate");
        ReflectionTestUtils.setField(c100DocumentTemplateFinderService, "c100C1aTemplateV2", "c100C1aTemplateV2");
        ReflectionTestUtils.setField(c100DocumentTemplateFinderService, "c100C1aDraftTemplate", "c100C1aDraftTemplate");
        ReflectionTestUtils.setField(c100DocumentTemplateFinderService, "c100C1aDraftTemplateV2", "c100C1aDraftTemplateV2");

    }


    @Test
    public void findFinalDocumentTemplateV2True() {
        caseData = CaseData.builder().taskListVersion(TASK_LIST_VERSION_V2).build();
        Assert.assertNotNull(c100DocumentTemplateFinderService.findFinalDocumentTemplate(caseData,Boolean.TRUE));
    }

    @Test
    public void findFinalDocumentTemplateV3True() {
        caseData = CaseData.builder().taskListVersion(TASK_LIST_VERSION_V3).build();
        Assert.assertNotNull(c100DocumentTemplateFinderService.findFinalDocumentTemplate(caseData,Boolean.TRUE));
    }

    @Test
    public void findFinalDocumentTemplateV2False() {
        caseData = CaseData.builder().taskListVersion(TASK_LIST_VERSION_V2).build();
        Assert.assertNotNull(c100DocumentTemplateFinderService.findFinalDocumentTemplate(caseData,Boolean.FALSE));
    }

    @Test
    public void findFinalDocumentTemplateV3False() {
        caseData = CaseData.builder().taskListVersion(TASK_LIST_VERSION_V3).build();
        Assert.assertNotNull(c100DocumentTemplateFinderService.findFinalDocumentTemplate(caseData,Boolean.FALSE));
    }

    @Test
    public void findFinalDocumentTemplateTrue() {
        caseData = CaseData.builder().build();
        Assert.assertNotNull(c100DocumentTemplateFinderService.findFinalDocumentTemplate(caseData,Boolean.TRUE));
    }

    @Test
    public void findFinalDocumentTemplateFalse() {
        caseData = CaseData.builder().build();
        Assert.assertNotNull(c100DocumentTemplateFinderService.findFinalDocumentTemplate(caseData,Boolean.TRUE));
    }

    @Test
    public void findFinalDraftDocumentTemplateV2True() {
        caseData = CaseData.builder().taskListVersion(TASK_LIST_VERSION_V2).build();
        Assert.assertNotNull(c100DocumentTemplateFinderService.findFinalDraftDocumentTemplate(caseData,Boolean.TRUE));
    }

    @Test
    public void findFinalDraftDocumentTemplateV3True() {
        caseData = CaseData.builder().taskListVersion(TASK_LIST_VERSION_V3).build();
        Assert.assertNotNull(c100DocumentTemplateFinderService.findFinalDraftDocumentTemplate(caseData,Boolean.TRUE));
    }

    @Test
    public void findFinalDraftDocumentTemplateV2False() {
        caseData = CaseData.builder().taskListVersion(TASK_LIST_VERSION_V2).build();
        Assert.assertNotNull(c100DocumentTemplateFinderService.findFinalDraftDocumentTemplate(caseData,Boolean.FALSE));
    }

    @Test
    public void findFinalDraftDocumentTemplateV3False() {
        caseData = CaseData.builder().taskListVersion(TASK_LIST_VERSION_V3).build();
        Assert.assertNotNull(c100DocumentTemplateFinderService.findFinalDraftDocumentTemplate(caseData,Boolean.FALSE));
    }

    @Test
    public void findFinalDraftDocumentTemplateTrue() {
        caseData = CaseData.builder().build();
        Assert.assertNotNull(c100DocumentTemplateFinderService.findFinalDraftDocumentTemplate(caseData,Boolean.TRUE));
    }

    @Test
    public void findFinalDraftDocumentTemplateFalse() {
        caseData = CaseData.builder().build();
        Assert.assertNotNull(c100DocumentTemplateFinderService.findFinalDraftDocumentTemplate(caseData,Boolean.FALSE));
    }

    @Test
    public void findC8DocumentTemplateV2True() {
        caseData = CaseData.builder().taskListVersion(TASK_LIST_VERSION_V2).build();
        Assert.assertNotNull(c100DocumentTemplateFinderService.findC8DocumentTemplate(caseData,Boolean.TRUE));
    }

    @Test
    public void findC8DocumentTemplateV2False() {
        caseData = CaseData.builder().taskListVersion(TASK_LIST_VERSION_V2).build();
        Assert.assertNotNull(c100DocumentTemplateFinderService.findC8DocumentTemplate(caseData,Boolean.FALSE));
    }

    @Test
    public void findC8DocumentTemplateTrue() {
        caseData = CaseData.builder().build();
        Assert.assertNotNull(c100DocumentTemplateFinderService.findC8DocumentTemplate(caseData,Boolean.TRUE));
    }

    @Test
    public void findC8DocumentTemplateFalse() {
        caseData = CaseData.builder().build();
        Assert.assertNotNull(c100DocumentTemplateFinderService.findC8DocumentTemplate(caseData,Boolean.FALSE));
    }

    @Test
    public void findC8DraftDocumentTemplateV2True() {
        caseData = CaseData.builder().taskListVersion(TASK_LIST_VERSION_V2).build();
        Assert.assertNotNull(c100DocumentTemplateFinderService.findC8DraftDocumentTemplate(caseData,Boolean.TRUE));
    }

    @Test
    public void findC8DraftDocumentTemplateV2False() {
        caseData = CaseData.builder().taskListVersion(TASK_LIST_VERSION_V2).build();
        Assert.assertNotNull(c100DocumentTemplateFinderService.findC8DraftDocumentTemplate(caseData,Boolean.FALSE));
    }

    @Test
    public void findC8DraftDocumentTemplateTrue() {
        caseData = CaseData.builder().build();
        Assert.assertNotNull(c100DocumentTemplateFinderService.findC8DraftDocumentTemplate(caseData,Boolean.TRUE));
    }

    @Test
    public void findC8DraftDocumentTemplateFalse() {
        caseData = CaseData.builder().build();
        Assert.assertNotNull(c100DocumentTemplateFinderService.findC8DraftDocumentTemplate(caseData,Boolean.FALSE));
    }

    @Test
    public void findC1ADraftDocumentTemplateV2True() {
        caseData = CaseData.builder().taskListVersion(TASK_LIST_VERSION_V2).build();
        Assert.assertNotNull(c100DocumentTemplateFinderService.findDraftC1ATemplate(caseData,Boolean.TRUE));
    }

    @Test
    public void findC1ADraftDocumentTemplateV2False() {
        caseData = CaseData.builder().taskListVersion(TASK_LIST_VERSION_V2).build();
        Assert.assertNotNull(c100DocumentTemplateFinderService.findDraftC1ATemplate(caseData,Boolean.FALSE));
    }

    @Test
    public void findC1ADraftDocumentTemplateTrue() {
        caseData = CaseData.builder().build();
        Assert.assertNotNull(c100DocumentTemplateFinderService.findDraftC1ATemplate(caseData,Boolean.TRUE));
    }

    @Test
    public void findC1ADraftDocumentTemplateFalse() {
        caseData = CaseData.builder().build();
        Assert.assertNotNull(c100DocumentTemplateFinderService.findDraftC1ATemplate(caseData,Boolean.FALSE));
    }

    @Test
    public void findC1ADocumentTemplateV2True() {
        caseData = CaseData.builder().taskListVersion(TASK_LIST_VERSION_V2).build();
        Assert.assertNotNull(c100DocumentTemplateFinderService.findC1ATemplate(caseData,Boolean.TRUE));
    }

    @Test
    public void findC1ADocumentTemplateV2False() {
        caseData = CaseData.builder().taskListVersion(TASK_LIST_VERSION_V2).build();
        Assert.assertNotNull(c100DocumentTemplateFinderService.findC1ATemplate(caseData,Boolean.FALSE));
    }

    @Test
    public void findC1ADocumentTemplateTrue() {
        caseData = CaseData.builder().build();;
        Assert.assertNotNull(c100DocumentTemplateFinderService.findC1ATemplate(caseData,Boolean.TRUE));
    }

    @Test
    public void findC1ADocumentTemplateFalse() {
        caseData = CaseData.builder().build();
        Assert.assertNotNull(c100DocumentTemplateFinderService.findC1ATemplate(caseData,Boolean.FALSE));
    }

}
