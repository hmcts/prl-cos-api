package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.ContactInformation;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.DocumentDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarm;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.english;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.class)
public class RespondentSubmitResponseControllerTest {

    @Mock
    CoreCaseDataApi coreCaseDataApi;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    CaseService caseService;

    @Mock
    DocumentGenService documentGenService;

    @InjectMocks
    private RespondentSubmitResponseController respondentSubmitResponseController;

    CaseData c100CaseData;
    AllegationOfHarm allegationOfHarmYes;

    @Test
    public void checkNewDocumentIsCreatedToNewDocumentList() throws Exception {

        List<ContactInformation> contactInformationList = Collections.singletonList(ContactInformation.builder()
                                                                                        .addressLine1("29, SEATON DRIVE")
                                                                                        .addressLine2("test line")
                                                                                        .townCity("NORTHAMPTON")
                                                                                        .postCode("NN3 9SS")
                                                                                        .build());

        Organisations organisations = Organisations.builder()
            .organisationIdentifier("79ZRSOU")
            .name("Civil - Organisation 2")
            .contactInformation(contactInformationList)
            .build();

        PartyDetails partyDetailsWithOrganisations = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .isAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .isEmailAddressConfidential(Yes)
            .solicitorOrg(Organisation.builder()
                              .organisationID("79ZRSOU")
                              .organisationName("Civil - Organisation 2")
                              .build())
            .organisations(organisations)
            .build();


        Element<PartyDetails> applicants = Element.<PartyDetails>builder().value(partyDetailsWithOrganisations).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(applicants);

        allegationOfHarmYes = AllegationOfHarm.builder()
            .allegationsOfHarmYesNo(Yes).build();

        c100CaseData = CaseData.builder()
            .id(123456789123L)
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .allegationOfHarm(allegationOfHarmYes)
            .applicants(listOfApplicants)
            .state(State.CASE_ISSUE)
            //.allegationsOfHarmYesNo(No)
            .build();
        Map<String, Object> stringObjectMap = c100CaseData.toMap(new ObjectMapper());
        Mockito.when(documentGenService.generateC7FinalDocument("auth", c100CaseData))
            .thenReturn(UploadedDocuments.builder()
                            .parentDocumentType("test")
                            .documentType("test")
                            .partyName("test")
                            .isApplicant("test")
                            .dateCreated(LocalDate.now())
                            .documentDetails(DocumentDetails.builder()
                                                 .documentName("test")
                                                 .documentUploadedDate("test")
                                                 .build()).citizenDocument(Document.builder().build()).build()
            );

        Mockito.when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(c100CaseData);
        Mockito.when(caseService.updateCase(c100CaseData,"auth", "s2s","123456789123L","test-event"))
                .thenReturn(CaseDetails
                           .builder().data(stringObjectMap).build());
        respondentSubmitResponseController
            .submitRespondentResponse(c100CaseData,"123456789123L","test-event","auth","s2s");


        Mockito.verify(documentGenService, Mockito.times(1)).generateC7FinalDocument("auth", c100CaseData);
    }


    @Test
    public void checkNewDocumentIsAddedToList() throws Exception {

        List<ContactInformation> contactInformationList = Collections.singletonList(ContactInformation.builder()
                                                                                        .addressLine1("29, SEATON DRIVE")
                                                                                        .addressLine2("test line")
                                                                                        .townCity("NORTHAMPTON")
                                                                                        .postCode("NN3 9SS")
                                                                                        .build());

        Organisations organisations = Organisations.builder()
            .organisationIdentifier("79ZRSOU")
            .name("Civil - Organisation 2")
            .contactInformation(contactInformationList)
            .build();

        PartyDetails partyDetailsWithOrganisations = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .isAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .isEmailAddressConfidential(Yes)
            .solicitorOrg(Organisation.builder()
                              .organisationID("79ZRSOU")
                              .organisationName("Civil - Organisation 2")
                              .build())
            .organisations(organisations)
            .build();


        Element<PartyDetails> applicants = Element.<PartyDetails>builder().value(partyDetailsWithOrganisations).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(applicants);

        allegationOfHarmYes = AllegationOfHarm.builder()
            .allegationsOfHarmYesNo(Yes).build();

        List<Element<UploadedDocuments>> uploadedDocumentsList = new ArrayList<>();
        Element<UploadedDocuments> uploadDocumentElement = element(UploadedDocuments.builder().build());
        uploadedDocumentsList.add(uploadDocumentElement);

        c100CaseData = CaseData.builder()
            .id(123456789123L)
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .allegationOfHarm(allegationOfHarmYes)
            .citizenUploadedDocumentList(uploadedDocumentsList)
            .applicants(listOfApplicants)
            .state(State.CASE_ISSUE)
            //.allegationsOfHarmYesNo(No)
            .build();
        Map<String, Object> stringObjectMap = c100CaseData.toMap(new ObjectMapper());
        Mockito.when(documentGenService.generateC7FinalDocument("auth",c100CaseData))
            .thenReturn(UploadedDocuments.builder().build()
            );

        Mockito.when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(c100CaseData);
        Mockito.when(caseService.updateCase(c100CaseData,"auth", "s2s","123456789123L","test-event"))
                .thenReturn(CaseDetails
                           .builder().data(stringObjectMap).build());
        respondentSubmitResponseController
            .submitRespondentResponse(c100CaseData,"123456789123L","test-event","auth","s2s");


        Mockito.verify(documentGenService, Mockito.times(1)).generateC7FinalDocument("auth", c100CaseData);
    }
}


