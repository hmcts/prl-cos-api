package uk.gov.hmcts.reform.prl.handlers;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CategoriesAndDocuments;
import uk.gov.hmcts.reform.ccd.client.model.Category;
import uk.gov.hmcts.reform.prl.enums.CantFindCourtEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.events.TransferToAnotherCourtEvent;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.managedocuments.ManageDocuments;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DocumentManagementDetails;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.SendgridService;
import uk.gov.hmcts.reform.prl.services.transfercase.TransferCaseContentProvider;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
@Slf4j
public class TransferToAnotherCourtEventHandlerTest {

    @Mock
    private TransferCaseContentProvider transferCaseContentProvider;

    @Mock
    private EmailService emailService;

    @Mock
    private SendgridService sendgridService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CoreCaseDataApi coreCaseDataApi;
    private final String serviceAuthToken = "Bearer testServiceAuth";

    @InjectMocks
    private TransferToAnotherCourtEventHandler transferToAnotherCourtEventHandler;

    private TransferToAnotherCourtEvent transferToAnotherCourtEvent;

    private TransferToAnotherCourtEvent transferToAnotherCourtEventWithDocs;

    private TransferToAnotherCourtEvent transferToAnotherCourtEventWithDocsFL401;

    @Before
    public void init() {
        PartyDetails applicant1 = PartyDetails.builder()
            .firstName("af1").lastName("al1")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("afl11@test.com")
            .build();
        PartyDetails applicant2 = PartyDetails.builder()
            .firstName("af2").lastName("al2")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("asf2").representativeLastName("asl2")
            .solicitorEmail("asl22@test.com")
            .build();
        PartyDetails respondent1 = PartyDetails.builder()
            .firstName("rf1").lastName("rl1")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("rfl11@test.com")
            .build();
        PartyDetails respondent2 = PartyDetails.builder()
            .firstName("rf2").lastName("rl2")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .email("rfl11@test.com")
            .representativeFirstName("rsf2").representativeLastName("rsl2")
            .solicitorEmail("rsl22@test.com")
            .build();
        PartyDetails otherPerson = PartyDetails.builder()
            .firstName("of").lastName("ol")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("ofl@test.com")
            .build();
        final CaseData caseData = CaseData.builder()
            .id(nextLong())
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(Arrays.asList(element(applicant1), element(applicant2)))
            .respondents(Arrays.asList(element(respondent1), element(respondent2)))
            .othersToNotify(Collections.singletonList(element(otherPerson)))
            .courtEmailAddress("test@test.com")
            .courtName("old court")
            .anotherCourt("new court")
            .transferredCourtFrom("old court")
            .cantFindCourtCheck(List.of(CantFindCourtEnum.cantFindCourt))
            .build();

        Document document = Document.builder().build();
        final CaseData caseDataWithDocs = CaseData.builder()
            .id(nextLong())
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(Arrays.asList(element(applicant1), element(applicant2)))
            .respondents(Arrays.asList(element(respondent1), element(respondent2)))
            .othersToNotify(Collections.singletonList(element(otherPerson)))
            .courtEmailAddress("test@test.com")
            .courtName("old court")
            .anotherCourt("new court")
            .transferredCourtFrom("old court")
            .finalDocument(document)
            .finalWelshDocument(document)
            .c8Document(document)
            .c8WelshDocument(document)
            .c1ADocument(document)
            .c1AWelshDocument(document)
            .otherDocuments(List.of(element(OtherDocuments.builder().build())))
            .orderCollection(List.of(element(OrderDetails.builder()
                                                              .orderDocument(document)
                                                 .orderDocumentWelsh(document).build())))
            .otherDocumentsUploaded(List.of(document))
            .cantFindCourtCheck(List.of(CantFindCourtEnum.cantFindCourt))
            .build();

        final CaseData caseDataWithDocsFl401 = CaseData.builder()
            .id(nextLong())
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicants(Arrays.asList(element(applicant1), element(applicant2)))
            .respondents(Arrays.asList(element(respondent1), element(respondent2)))
            .othersToNotify(Collections.singletonList(element(otherPerson)))
            .courtEmailAddress("test@test.com")
            .courtName("old court")
            .anotherCourt("new court")
            .transferredCourtFrom("old court")
            .finalDocument(document)
            .finalWelshDocument(document)
            .c8Document(document)
            .c8WelshDocument(document)
            .c1ADocument(document)
            .c1AWelshDocument(document)
            .otherDocuments(List.of(element(OtherDocuments.builder().build())))
            .documentManagementDetails(
                DocumentManagementDetails
                    .builder()
                    .manageDocuments(List.of(element(ManageDocuments.builder()
                                                         .document(document).build())))
                    .build())

            .citizenUploadedDocumentList(List.of(element(UploadedDocuments.builder()
                                                             .citizenDocument(document).build())))
            .citizenResponseC7DocumentList(List.of(element(ResponseDocuments.builder()
                                                               .citizenDocument(document).build())))
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .courtStaffQuarantineDocsList(List.of(element(QuarantineLegalDoc.builder()
                                                                                             .document(document).build())))
                                           .build())
            .orderCollection(List.of(element(OrderDetails.builder()
                                                 .orderDocument(document)
                                                 .orderDocumentWelsh(document).build())))
            .otherDocumentsUploaded(List.of(document))
            .cantFindCourtCheck(List.of(CantFindCourtEnum.cantFindCourt))
            .build();

        transferToAnotherCourtEvent = TransferToAnotherCourtEvent.builder()
            .caseData(caseDataWithDocs)
            .typeOfEvent("transferCourt")
            .authorisation("test")
            .build();

        transferToAnotherCourtEventWithDocs = TransferToAnotherCourtEvent.builder()
            .caseData(caseData)
            .typeOfEvent("transferCourt")
            .authorisation("test")
            .build();

        transferToAnotherCourtEventWithDocsFL401 = TransferToAnotherCourtEvent.builder()
            .caseData(caseDataWithDocsFl401)
            .typeOfEvent("transferCourt")
            .authorisation("test")
            .build();
        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
        uk.gov.hmcts.reform.ccd.client.model.Document documents =
            new uk.gov.hmcts.reform.ccd.client.model
                .Document("documentURL", "fileName", "binaryUrl", "attributePath", LocalDateTime.now());
        Category category = new Category("categoryId", "categoryName", 2, List.of(documents), null);

        CategoriesAndDocuments categoriesAndDocuments = new CategoriesAndDocuments(1, List.of(category), List.of(documents));
        when(coreCaseDataApi.getCategoriesAndDocuments(
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        )).thenReturn(categoriesAndDocuments);
    }

    @Test
    public void shouldNotifyCourtForTransfer() throws Exception {

        doNothing().when(emailService)
                .send(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any());

        doNothing().when(sendgridService)
            .sendTransferCourtEmailWithAttachments(Mockito.any(),
                                                   Mockito.any(),
                                                   Mockito.any(),
                                                   Mockito.any());
        transferToAnotherCourtEventHandler.transferCourtEmail(transferToAnotherCourtEvent);

        verify(emailService,times(1)).send(Mockito.anyString(),
                                           Mockito.any(),
                                           Mockito.any(), Mockito.any());
        verify(sendgridService,times(1))
            .sendTransferCourtEmailWithAttachments(Mockito.any(),
                                                   Mockito.any(),
                                                   Mockito.any(),
                                                   Mockito.any());

    }

    @Test
    public void shouldGiveErrorWhenFailedToSendEmail() throws Exception {

        doNothing().when(emailService)
            .send(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any());

        doThrow(IOException.class).when(sendgridService)
            .sendTransferCourtEmailWithAttachments(Mockito.any(),
                                                   Mockito.any(),
                                                   Mockito.any(),
                                                   Mockito.any());
        transferToAnotherCourtEventHandler.transferCourtEmail(transferToAnotherCourtEventWithDocs);
        verify(emailService,times(1)).send(Mockito.anyString(),
                                           Mockito.any(),
                                           Mockito.any(), Mockito.any());
        verify(sendgridService,times(1))
            .sendTransferCourtEmailWithAttachments(Mockito.any(),
                                                   Mockito.any(),
                                                   Mockito.any(),
                                                   Mockito.any());

    }

    @Test
    public void shouldGiveErrorWhenFailedToSendEmailForFL401() throws Exception {

        doNothing().when(emailService)
            .send(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any());

        doThrow(IOException.class).when(sendgridService)
            .sendTransferCourtEmailWithAttachments(Mockito.any(),
                                                   Mockito.any(),
                                                   Mockito.any(),
                                                   Mockito.any());

        transferToAnotherCourtEventHandler.transferCourtEmail(transferToAnotherCourtEventWithDocsFL401);
        verify(emailService,times(1)).send(Mockito.anyString(),
                                           Mockito.any(),
                                           Mockito.any(), Mockito.any());
        verify(sendgridService,times(1))
            .sendTransferCourtEmailWithAttachments(Mockito.any(),
                                                   Mockito.any(),
                                                   Mockito.any(),
                                                   Mockito.any());

    }


}
