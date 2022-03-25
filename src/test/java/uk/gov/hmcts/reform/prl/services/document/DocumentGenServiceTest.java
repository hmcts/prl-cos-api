package uk.gov.hmcts.reform.prl.services.document;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.DgsService;
import uk.gov.hmcts.reform.prl.services.DocumentLanguageService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;

import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C1A;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C1A_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C8;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C8_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_FINAL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_FINAL_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.english;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.class)
public class DocumentGenServiceTest {

    @Mock
    private DgsService dgsService;

    @Mock
    DocumentLanguageService documentLanguageService;

    @Mock
    OrganisationService organisationService;

    @InjectMocks
    DocumentGenService documentGenService;

    private GeneratedDocumentInfo generatedDocumentInfo;

    public static final String authToken = "Bearer TestAuthToken";

    CaseData c100CaseData;
    CaseData fl401CaseData;

    @Before
    public void setUp() {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        c100CaseData = CaseData.builder()
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .allegationsOfHarmYesNo(Yes)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();

        fl401CaseData = CaseData.builder()
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();
    }

    @Test
    public void generateDocsForC100Test() throws Exception {
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        doReturn(generatedDocumentInfo).when(dgsService).generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any());
        doReturn(generatedDocumentInfo).when(dgsService).generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any());
        when(organisationService.getApplicantOrganisationDetails(Mockito.any(CaseData.class))).thenReturn(c100CaseData);
        when(organisationService.getRespondentOrganisationDetails(Mockito.any(CaseData.class))).thenReturn(c100CaseData);

        Map<String, Object> stringObjectMap = documentGenService.generateDocuments(authToken, c100CaseData);

        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_FINAL_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C1A_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_FINAL));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C1A));

        verify(dgsService, times(3)).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verify(dgsService, times(3)).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);
    }

    @Test
    public void generateDocsForFL401Test() throws Exception {
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        doReturn(generatedDocumentInfo).when(dgsService).generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any());
        doReturn(generatedDocumentInfo).when(dgsService).generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any());
        when(organisationService.getApplicantOrganisationDetailsForFL401(Mockito.any(CaseData.class))).thenReturn(fl401CaseData);

        Map<String, Object> stringObjectMap = documentGenService.generateDocuments(authToken, fl401CaseData);

        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_FINAL_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_FINAL));

        verify(dgsService, times(2)).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verify(dgsService, times(2)).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);
    }

}


