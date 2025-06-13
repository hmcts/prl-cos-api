package uk.gov.hmcts.reform.prl.controllers.citizen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.exception.CoreCaseDataStoreException;
import uk.gov.hmcts.reform.prl.models.CitizenUpdatedCaseData;
import uk.gov.hmcts.reform.prl.models.DocumentRequest;
import uk.gov.hmcts.reform.prl.models.citizen.CaseDataWithHearingResponse;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.citizen.CitizenResponseService;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class CitizenResponseControllerTest {

    @Mock
    private CitizenResponseService citizenResponseService;

    @Mock
    private AuthorisationService authorisationService;

    @InjectMocks
    private CitizenResponseController citizenResponseController;

    @Mock
    private CaseService caseService;
    CaseData caseData;


    private String authToken = "Bearer TestAuthToken";

    private String s2sToken = "s2s AuthToken";

    private String caseId = "1673970714366224";

    private String partyId = "a5dbc39d-6322-4abc-821a-45206b88253f";


    @Test
    void testGenerateC7DraftDocument() throws Exception {

        DocumentRequest documentRequest = DocumentRequest.builder().build();

        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);

        when(citizenResponseService.generateAndReturnDraftC7(anyString(), anyString(), anyString(), anyBoolean()))
                .thenReturn(Document.builder().build());

        assertNotNull(citizenResponseController.generateC7DraftDocument(caseId, partyId, authToken, s2sToken, documentRequest));

    }

    @Test
    void testGenerateC7DraftDocumentWithAuthorizationError() {

        DocumentRequest documentRequest = DocumentRequest.builder().build();

        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);

        assertThrows(RuntimeException.class, () ->
                citizenResponseController.generateC7DraftDocument(caseId, partyId, authToken, s2sToken, documentRequest));

    }

    @Test
    void testGenerateC1ADraftDocument() throws Exception {

        DocumentRequest documentRequest = DocumentRequest.builder().build();

        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);

        when(citizenResponseService.generateAndReturnDraftC1A(anyString(), anyString(), anyString(), anyBoolean()))
                .thenReturn(Document.builder().build());

        assertNotNull(citizenResponseController.generateC1ADraftDocument(caseId, partyId, authToken, s2sToken, documentRequest));

    }

    @Test
    void testGenerateC1ADraftDocumentWithAuthorizationError() {

        DocumentRequest documentRequest = DocumentRequest.builder().build();

        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);

        assertThrows(RuntimeException.class, () ->
                citizenResponseController.generateC1ADraftDocument(caseId, partyId, authToken, s2sToken, documentRequest));

    }

    @Test
    void testSubmitAndGenerateC7WithExcdption() throws Exception {

        CitizenUpdatedCaseData citizenUpdatedCaseData = CitizenUpdatedCaseData.builder().build();

        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);

        when(citizenResponseService.generateAndSubmitCitizenResponse(anyString(), anyString(), any(CitizenUpdatedCaseData.class)))
                .thenReturn(null);


        assertThrows(CoreCaseDataStoreException.class, () ->
                citizenResponseController.submitAndGenerateC7(citizenUpdatedCaseData, caseId, authToken, s2sToken));

    }

    @Test
    void testSubmitAndGenerateC7() throws Exception {

        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);

        when(citizenResponseService.generateAndSubmitCitizenResponse(anyString(), anyString(), any(CitizenUpdatedCaseData.class)))
                .thenReturn(CaseDetails.builder().build().builder().build());

        when(caseService
                .getCaseDataWithHearingResponse(anyString(), anyString(), any(CaseDetails.class)))
            .thenReturn(CaseDataWithHearingResponse.builder().build());

        CitizenUpdatedCaseData citizenUpdatedCaseData = CitizenUpdatedCaseData.builder().build();


        assertNotNull(citizenResponseController.submitAndGenerateC7(citizenUpdatedCaseData, caseId, authToken, s2sToken));

    }

    @Test
    void testSubmitAndGenerateC7WithAuthorizationError() {

        CitizenUpdatedCaseData citizenUpdatedCaseData = CitizenUpdatedCaseData.builder().build();

        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);

        assertThrows(RuntimeException.class, () ->
                citizenResponseController.submitAndGenerateC7(citizenUpdatedCaseData, caseId, authToken, s2sToken));

    }
}
