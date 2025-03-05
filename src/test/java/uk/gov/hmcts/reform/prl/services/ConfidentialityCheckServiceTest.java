package uk.gov.hmcts.reform.prl.services;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.c100respondentsolicitor.RespondentC8;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RespondentC8Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;


@RunWith(MockitoJUnitRunner.class)
public class ConfidentialityCheckServiceTest {

    @InjectMocks
    private ConfidentialityCheckService confidentialityCheckService;

    @Test
    public void processRespondentsC8Documents() {
        List<Element<PartyDetails>> respondents = new ArrayList<>();
        Element<PartyDetails> partyDetailsElement = Element.<PartyDetails>builder().value(PartyDetails.builder().firstName("firstName")
                .lastName("lastName").refugeConfidentialityC8Form(Document.builder().build()).build()).build();
        Element<PartyDetails> partyDetailsElement1 = Element.<PartyDetails>builder().value(PartyDetails.builder().firstName("firstName1")
                .lastName("lastName").build()).build();
        Element<PartyDetails> partyDetailsElement2 = Element.<PartyDetails>builder().value(PartyDetails.builder().firstName("firstName2")
                .lastName("lastName").build()).build();
        Element<PartyDetails> partyDetailsElement3 = Element.<PartyDetails>builder().value(PartyDetails.builder().firstName("firstName3")
                .lastName("lastName").build()).build();
        Element<PartyDetails> partyDetailsElement4 = Element.<PartyDetails>builder().value(PartyDetails.builder().firstName("firstName4")
                .lastName("lastName").build()).build();
        respondents.add(partyDetailsElement);
        respondents.add(partyDetailsElement1);
        respondents.add(partyDetailsElement2);
        respondents.add(partyDetailsElement3);
        respondents.add(partyDetailsElement4);
        ResponseDocuments responseDocument1 = ResponseDocuments.builder().dateCreated(LocalDate.now())
                .dateTimeCreated(LocalDateTime.now()).respondentC8Document(Document.builder().documentUrl("with version").build())
                .respondentC8DocumentWelsh(Document.builder().documentUrl("with version").build()).build();
        Element<ResponseDocuments> responseDocumentsElement1 = Element.<ResponseDocuments>builder().value(responseDocument1).build();
        ResponseDocuments responseDocument2 = ResponseDocuments.builder().dateCreated(LocalDate.now())
                .dateTimeCreated(LocalDateTime.now().minusDays(1))
                .respondentC8Document(Document.builder().documentUrl("with version2").build()).build();
        Element<ResponseDocuments> responseDocumentsElement2 = Element.<ResponseDocuments>builder().value(responseDocument2).build();

        ResponseDocuments responseDocument3 = ResponseDocuments.builder().dateCreated(LocalDate.now())
                .dateTimeCreated(LocalDateTime.now().minusDays(1))
                .respondentC8Document(Document.builder().documentUrl("with version3").build()).build();
        Element<ResponseDocuments> responseDocumentsElement3 = Element.<ResponseDocuments>builder().value(responseDocument3).build();

        ResponseDocuments responseDocument4 = ResponseDocuments.builder().dateCreated(LocalDate.now())
                .dateTimeCreated(LocalDateTime.now().minusDays(1))
                .respondentC8Document(Document.builder().documentUrl("with version4").build()).build();
        Element<ResponseDocuments> responseDocumentsElement4 = Element.<ResponseDocuments>builder().value(responseDocument4).build();

        ResponseDocuments responseDocument5 = ResponseDocuments.builder().dateCreated(LocalDate.now())
                .dateTimeCreated(LocalDateTime.now().minusDays(1))
                .respondentC8Document(Document.builder().documentUrl("with version5").build()).build();
        Element<ResponseDocuments> responseDocumentsElement5 = Element.<ResponseDocuments>builder().value(responseDocument5).build();

        CaseData caseData = CaseData.builder().id(12345L).respondents(respondents).caseTypeOfApplication(C100_CASE_TYPE)
                .respondentC8Document(RespondentC8Document.builder()
                        .respondentAc8Documents(List.of(responseDocumentsElement1))
                        .respondentBc8Documents(List.of(responseDocumentsElement2))
                        .respondentCc8Documents(List.of(responseDocumentsElement3))
                        .respondentDc8Documents(List.of(responseDocumentsElement4))
                        .respondentEc8Documents(List.of(responseDocumentsElement5))
                        .build())
                .respondentC8(RespondentC8.builder()
                        .respondentAc8(responseDocument1)
                        .respondentBc8(responseDocument2)
                        .respondentCc8(responseDocument3)
                        .respondentDc8(responseDocument4)
                        .respondentEc8(responseDocument5)
                        .build()).build();

        Map<String, Object> caseDetails = new HashMap<>();
        confidentialityCheckService.processRespondentsC8Documents(caseDetails, caseData);

        Assert.assertTrue(caseDetails.containsKey("respAC8EngDocument"));
        Document responseDocuments = (Document) caseDetails.get("respAC8EngDocument");
        Assert.assertEquals("with version", responseDocuments.getDocumentUrl());
    }

    @Test
    public void processRespondentsC8DocumentsSceanrios_2() {
        List<Element<PartyDetails>> respondents = new ArrayList<>();
        Element<PartyDetails> partyDetailsElement = Element.<PartyDetails>builder().value(PartyDetails.builder().firstName("firstName")
                .lastName("lastName").build()).build();
        Element<PartyDetails> partyDetailsElement1 = Element.<PartyDetails>builder().value(PartyDetails.builder().firstName("firstName1")
                .lastName("lastName").build()).build();
        Element<PartyDetails> partyDetailsElement2 = Element.<PartyDetails>builder().value(PartyDetails.builder().firstName("firstName2")
                .lastName("lastName").build()).build();
        Element<PartyDetails> partyDetailsElement3 = Element.<PartyDetails>builder().value(PartyDetails.builder().firstName("firstName3")
                .lastName("lastName").build()).build();
        Element<PartyDetails> partyDetailsElement4 = Element.<PartyDetails>builder().value(PartyDetails.builder().firstName("firstName4")
                .lastName("lastName").build()).build();
        respondents.add(partyDetailsElement);
        respondents.add(partyDetailsElement1);
        respondents.add(partyDetailsElement2);
        respondents.add(partyDetailsElement3);
        respondents.add(partyDetailsElement4);
        ResponseDocuments responseDocument1 = ResponseDocuments.builder().dateCreated(LocalDate.now())
                .dateTimeCreated(LocalDateTime.now()).respondentC8Document(Document.builder().documentUrl("with version").build())
                .respondentC8DocumentWelsh(Document.builder().documentUrl("with version").build()).build();

        ResponseDocuments responseDocument2 = ResponseDocuments.builder().dateCreated(LocalDate.now())
                .dateTimeCreated(LocalDateTime.now().minusDays(1))
                .respondentC8Document(Document.builder().documentUrl("with version2").build()).build();


        ResponseDocuments responseDocument3 = ResponseDocuments.builder().dateCreated(LocalDate.now())
                .dateTimeCreated(LocalDateTime.now().minusDays(1))
                .respondentC8Document(Document.builder().documentUrl("with version3").build()).build();

        ResponseDocuments responseDocument4 = ResponseDocuments.builder().dateCreated(LocalDate.now())
                .dateTimeCreated(LocalDateTime.now().minusDays(1))
                .respondentC8Document(Document.builder().documentUrl("with version4").build()).build();


        ResponseDocuments responseDocument5 = ResponseDocuments.builder().dateCreated(LocalDate.now())
                .dateTimeCreated(LocalDateTime.now().minusDays(1))
                .respondentC8Document(Document.builder().documentUrl("with version5").build()).build();

        CaseData caseData = CaseData.builder().id(12345L).respondents(respondents).caseTypeOfApplication(C100_CASE_TYPE)
                .respondentC8Document(RespondentC8Document.builder()
                        .respondentAc8Documents(List.of())
                        .respondentBc8Documents(List.of())
                        .respondentCc8Documents(List.of())
                        .respondentDc8Documents(List.of())
                        .respondentEc8Documents(List.of())
                        .build())
                .respondentC8(RespondentC8.builder()
                        .respondentAc8(responseDocument1)
                        .respondentBc8(responseDocument2)
                        .respondentCc8(responseDocument3)
                        .respondentDc8(responseDocument4)
                        .respondentEc8(responseDocument5)
                        .build()).build();

        Map<String, Object> caseDetails = new HashMap<>();
        confidentialityCheckService.processRespondentsC8Documents(caseDetails, caseData);

        Assert.assertTrue(caseDetails.containsKey("respAC8EngDocument"));
        Document responseDocuments = (Document) caseDetails.get("respAC8EngDocument");
        Assert.assertEquals("with version", responseDocuments.getDocumentUrl());
    }



    @Test
    public void processRespondentsC8Documents_without_version() {
        List<Element<PartyDetails>> respondents = new ArrayList<>();
        respondents.add(Element.<PartyDetails>builder().value(PartyDetails.builder().firstName("firstName")
                .lastName("lastName").build()).build());
        ResponseDocuments responseDocument = ResponseDocuments.builder()
                .dateTimeCreated(LocalDateTime.now()).respondentC8Document(Document.builder().documentUrl("with version").build()).build();
        Element<ResponseDocuments> responseDocumentsElement = Element.<ResponseDocuments>builder().value(responseDocument).build();
        ResponseDocuments responseDocument1 = ResponseDocuments.builder()
                .dateTimeCreated(LocalDateTime.now().minusDays(1)).respondentC8Document(Document.builder().build()).build();
        Element<ResponseDocuments> responseDocumentsElement1 = Element.<ResponseDocuments>builder().value(responseDocument1).build();

        ResponseDocuments responseDocument2 = ResponseDocuments.builder()
                .dateTimeCreated(LocalDateTime.now().plusDays(2)).respondentC8Document(Document.builder()
                        .documentUrl("without version").build()).build();

        CaseData caseData = CaseData.builder().id(12345L).respondents(respondents).caseTypeOfApplication(C100_CASE_TYPE)
                .respondentC8Document(RespondentC8Document.builder()
                        .respondentAc8Documents(List.of(responseDocumentsElement, responseDocumentsElement1)).build())
                .respondentC8(RespondentC8.builder().respondentAc8(responseDocument2)
                        .build()).build();

        Map<String, Object> caseDetails = new HashMap<>();
        confidentialityCheckService.processRespondentsC8Documents(caseDetails, caseData);

        Assert.assertTrue(caseDetails.containsKey("respAC8EngDocument"));
        Document responseDocuments = (Document) caseDetails.get("respAC8EngDocument");
        Assert.assertEquals("without version", responseDocuments.getDocumentUrl());
    }


    @Test
    public void processRespondentsC8Documents_Without_version_docs() {
        List<Element<PartyDetails>> respondents = new ArrayList<>();
        Element<PartyDetails> partyDetailsElement = Element.<PartyDetails>builder().value(PartyDetails.builder().firstName("firstName")
                .lastName("lastName").build()).build();
        respondents.add(partyDetailsElement);
        ResponseDocuments responseDocument2 = ResponseDocuments.builder()
                .dateCreated(LocalDate.now().minusDays(2)).respondentC8Document(Document.builder().documentUrl("without version").build()).build();

        CaseData caseData = CaseData.builder().id(12345L).respondents(respondents).caseTypeOfApplication(C100_CASE_TYPE)
                .respondentC8(RespondentC8.builder().respondentAc8(responseDocument2)
                        .build()).build();

        Map<String, Object> caseDetails = new HashMap<>();
        confidentialityCheckService.processRespondentsC8Documents(caseDetails, caseData);
        Assert.assertTrue(caseDetails.containsKey("respAC8EngDocument"));
        Document responseDocuments = (Document) caseDetails.get("respAC8EngDocument");
        Assert.assertEquals("without version", responseDocuments.getDocumentUrl());
    }

    @Test
    public void clearRespondentsC8Documents() {
        Map<String, Object> caseDetails = new HashMap<>();
        caseDetails.put("respAC8EngDocument",Document.builder().build());
        confidentialityCheckService.clearRespondentsC8Documents(caseDetails);
        Assert.assertNull(caseDetails.get("respAC8EngDocument"));
    }

    @Test
    public void processRespondentsC8DocumentsFL401() {
        PartyDetails partyDetails = PartyDetails.builder().firstName("firstName")
                .lastName("lastName").build();
        ResponseDocuments responseDocument = ResponseDocuments.builder()
                .dateTimeCreated(LocalDateTime.now()).respondentC8Document(Document.builder().documentUrl("with version").build())
                .respondentC8DocumentWelsh(Document.builder().documentUrl("with version").build()).build();
        Element<ResponseDocuments> responseDocumentsElement = Element.<ResponseDocuments>builder().value(responseDocument).build();
        ResponseDocuments responseDocument1 = ResponseDocuments.builder()
                .dateTimeCreated(LocalDateTime.now().minusDays(1)).respondentC8Document(Document.builder().build()).build();
        Element<ResponseDocuments> responseDocumentsElement1 = Element.<ResponseDocuments>builder().value(responseDocument1).build();
        CaseData caseData = CaseData.builder().id(12345L).respondentsFL401(partyDetails).caseTypeOfApplication(FL401_CASE_TYPE)
                .respondentC8Document(RespondentC8Document.builder()
                        .respondentAc8Documents(List.of(responseDocumentsElement, responseDocumentsElement1))
                        .build())
                        .build();

        Map<String, Object> caseDetails = new HashMap<>();
        confidentialityCheckService.processRespondentsC8Documents(caseDetails, caseData);
        Assert.assertTrue(caseDetails.containsKey("respAC8EngDocument"));
        Document responseDocuments = (Document) caseDetails.get("respAC8EngDocument");
        Assert.assertEquals("with version", responseDocuments.getDocumentUrl());
    }

    @Test
    public void processApplicantC8DocumentsForC100() {
        List<Element<PartyDetails>> applicants = new ArrayList<>();
        Document applicant1RefugeDocument = Document.builder().documentFileName("applicant 1 C8 Refuge Form").build();
        Document applicant2RefugeDocument = Document.builder().documentFileName("applicant 2 C8 Refuge Form").build();
        Document applicant3RefugeDocument = Document.builder().documentFileName("applicant 3 C8 Refuge Form").build();
        Document applicant4RefugeDocument = Document.builder().documentFileName("applicant 4 C8 Refuge Form").build();
        Element<PartyDetails> partyDetailsElement = Element.<PartyDetails>builder()
            .value(PartyDetails.builder()
                       .firstName("firstName")
                       .lastName("lastName")
                       .liveInRefuge(YesOrNo.Yes)
                       .refugeConfidentialityC8Form(applicant1RefugeDocument)
                       .build())
            .build();
        Element<PartyDetails> partyDetailsElement1 = Element.<PartyDetails>builder()
            .value(PartyDetails.builder()
                       .firstName("firstName1")
                       .lastName("lastName")
                       .liveInRefuge(YesOrNo.Yes)
                       .refugeConfidentialityC8Form(applicant2RefugeDocument)
                       .build())
            .build();
        Element<PartyDetails> partyDetailsElement2 = Element.<PartyDetails>builder()
            .value(PartyDetails.builder()
                       .firstName("firstName2")
                       .lastName("lastName")
                       .liveInRefuge(YesOrNo.Yes)
                       .refugeConfidentialityC8Form(
                           applicant3RefugeDocument)
                       .build())
            .build();
        Element<PartyDetails> partyDetailsElement3 = Element.<PartyDetails>builder()
            .value(PartyDetails.builder()
                       .firstName("firstName3")
                       .lastName("lastName")
                       .liveInRefuge(YesOrNo.Yes)
                       .refugeConfidentialityC8Form(applicant4RefugeDocument)
                       .build())
            .build();
        Element<PartyDetails> partyDetailsElement4 = Element.<PartyDetails>builder()
            .value(PartyDetails.builder()
                       .firstName("firstName4")
                       .lastName("lastName")
                       .liveInRefuge(YesOrNo.Yes)
                       .refugeConfidentialityC8Form(null)
                       .build())
            .build();
        Element<PartyDetails> partyDetailsElement5 = Element.<PartyDetails>builder()
            .value(PartyDetails.builder()
                       .firstName("firstName5")
                       .lastName("lastName")
                       .liveInRefuge(YesOrNo.Yes)
                       .refugeConfidentialityC8Form(null)
                       .build())
            .build();

        applicants.add(partyDetailsElement);
        applicants.add(partyDetailsElement1);
        applicants.add(partyDetailsElement2);
        applicants.add(partyDetailsElement3);
        applicants.add(partyDetailsElement4);
        applicants.add(partyDetailsElement5);

        CaseData caseData = CaseData.builder().id(12345L).applicants(applicants).caseTypeOfApplication(C100_CASE_TYPE).build();
        Map<String, Object> caseDetails = new HashMap<>();
        confidentialityCheckService.processApplicantC8Documents(caseDetails, caseData);

        Assert.assertTrue(caseDetails.containsKey("appAC8RefugeDocument"));
        Assert.assertTrue(caseDetails.containsKey("appBC8RefugeDocument"));
        Assert.assertTrue(caseDetails.containsKey("appCC8RefugeDocument"));
        Assert.assertTrue(caseDetails.containsKey("appDC8RefugeDocument"));
        Assert.assertTrue(caseDetails.containsKey("appEC8RefugeDocument"));

        Assert.assertEquals(applicant1RefugeDocument, caseDetails.get("appAC8RefugeDocument"));
        Assert.assertEquals(applicant2RefugeDocument, caseDetails.get("appBC8RefugeDocument"));
        Assert.assertEquals(applicant3RefugeDocument, caseDetails.get("appCC8RefugeDocument"));
        Assert.assertEquals(applicant4RefugeDocument, caseDetails.get("appDC8RefugeDocument"));
        Assert.assertEquals(null, caseDetails.get("appEC8RefugeDocument"));
    }

    @Test
    public void processApplicantC8DocumentsForFL401() {
        Document applicant1RefugeDocument = Document.builder().documentFileName("applicant 1 C8 Refuge Form").build();
        PartyDetails partyDetails = PartyDetails.builder().firstName("firstName").lastName("lastName")
            .liveInRefuge(YesOrNo.Yes).refugeConfidentialityC8Form(applicant1RefugeDocument).build();

        CaseData caseData = CaseData.builder().id(12345L).applicantsFL401(partyDetails).caseTypeOfApplication(
            FL401_CASE_TYPE).build();
        Map<String, Object> caseDetails = new HashMap<>();
        confidentialityCheckService.processApplicantC8Documents(caseDetails, caseData);

        Assert.assertTrue(caseDetails.containsKey("appAC8RefugeDocument"));
        Assert.assertEquals(applicant1RefugeDocument, caseDetails.get("appAC8RefugeDocument"));
    }

    @Test
    public void processOtherPeopleC8Documents() {
        List<Element<PartyDetails>> otherPeople = new ArrayList<>();
        Document applicant1RefugeDocument = Document.builder().documentFileName("other person 1 C8 Refuge Form").build();
        Document applicant2RefugeDocument = Document.builder().documentFileName("other person 2 C8 Refuge Form").build();
        Document applicant3RefugeDocument = Document.builder().documentFileName("other person 3 C8 Refuge Form").build();
        Document applicant4RefugeDocument = Document.builder().documentFileName("other person 4 C8 Refuge Form").build();
        Element<PartyDetails> partyDetailsElement = Element.<PartyDetails>builder()
            .value(PartyDetails.builder()
                       .firstName("firstName")
                       .lastName("lastName")
                       .liveInRefuge(YesOrNo.Yes)
                       .refugeConfidentialityC8Form(applicant1RefugeDocument)
                       .build())
            .build();
        Element<PartyDetails> partyDetailsElement1 = Element.<PartyDetails>builder()
            .value(PartyDetails.builder()
                       .firstName("firstName1")
                       .lastName("lastName")
                       .liveInRefuge(YesOrNo.Yes)
                       .refugeConfidentialityC8Form(applicant2RefugeDocument)
                       .build())
            .build();
        Element<PartyDetails> partyDetailsElement2 = Element.<PartyDetails>builder()
            .value(PartyDetails.builder()
                       .firstName("firstName2")
                       .lastName("lastName")
                       .liveInRefuge(YesOrNo.Yes)
                       .refugeConfidentialityC8Form(applicant3RefugeDocument)
                       .build())
            .build();
        Element<PartyDetails> partyDetailsElement3 = Element.<PartyDetails>builder()
            .value(PartyDetails.builder()
                       .firstName("firstName3")
                       .lastName("lastName")
                       .liveInRefuge(YesOrNo.Yes)
                       .refugeConfidentialityC8Form(applicant4RefugeDocument)
                       .build())
            .build();
        Element<PartyDetails> partyDetailsElement4 = Element.<PartyDetails>builder()
            .value(PartyDetails.builder()
                       .firstName("firstName4")
                       .lastName("lastName")
                       .liveInRefuge(YesOrNo.Yes)
                       .refugeConfidentialityC8Form(null)
                       .build())
            .build();
        Element<PartyDetails> partyDetailsElement5 = Element.<PartyDetails>builder()
            .value(PartyDetails.builder()
                       .firstName("firstName5")
                       .lastName("lastName")
                       .liveInRefuge(YesOrNo.Yes)
                       .refugeConfidentialityC8Form(null)
                       .build())
            .build();

        otherPeople.add(partyDetailsElement);
        otherPeople.add(partyDetailsElement1);
        otherPeople.add(partyDetailsElement2);
        otherPeople.add(partyDetailsElement3);
        otherPeople.add(partyDetailsElement4);
        otherPeople.add(partyDetailsElement5);

        CaseData caseData = CaseData.builder().id(12345L).otherPartyInTheCaseRevised(otherPeople).caseTypeOfApplication(C100_CASE_TYPE).build();
        Map<String, Object> caseDetails = new HashMap<>();
        confidentialityCheckService.processOtherC8Documents(caseDetails, caseData);

        Assert.assertTrue(caseDetails.containsKey("otherAC8RefugeDocument"));
        Assert.assertTrue(caseDetails.containsKey("otherBC8RefugeDocument"));
        Assert.assertTrue(caseDetails.containsKey("otherCC8RefugeDocument"));
        Assert.assertTrue(caseDetails.containsKey("otherDC8RefugeDocument"));
        Assert.assertTrue(caseDetails.containsKey("otherEC8RefugeDocument"));

        Assert.assertEquals(applicant1RefugeDocument, caseDetails.get("otherAC8RefugeDocument"));
        Assert.assertEquals(applicant2RefugeDocument, caseDetails.get("otherBC8RefugeDocument"));
        Assert.assertEquals(applicant3RefugeDocument, caseDetails.get("otherCC8RefugeDocument"));
        Assert.assertEquals(applicant4RefugeDocument, caseDetails.get("otherDC8RefugeDocument"));
        Assert.assertEquals(null, caseDetails.get("otherEC8RefugeDocument"));
    }

    @Test
    public void processOtherPeopleC8DocumentsForFl401() {
        List<Element<PartyDetails>> otherPeople = new ArrayList<>();
        Document applicant1RefugeDocument = Document.builder().documentFileName("other person 1 C8 Refuge Form").build();
        Element<PartyDetails> partyDetailsElement = Element.<PartyDetails>builder()
            .value(PartyDetails.builder()
                .firstName("firstName")
                .lastName("lastName")
                .liveInRefuge(YesOrNo.Yes)
                .refugeConfidentialityC8Form(applicant1RefugeDocument)
                .build())
            .build();

        otherPeople.add(partyDetailsElement);
        CaseData caseData = CaseData.builder().id(12345L).otherPartyInTheCaseRevised(otherPeople).caseTypeOfApplication(FL401_CASE_TYPE).build();
        Map<String, Object> caseDetails = new HashMap<>();
        confidentialityCheckService.processOtherC8Documents(caseDetails, caseData);

        Assert.assertFalse(caseDetails.containsKey("otherAC8RefugeDocument"));
        Assert.assertFalse(caseDetails.containsKey("otherBC8RefugeDocument"));
        Assert.assertFalse(caseDetails.containsKey("otherCC8RefugeDocument"));
        Assert.assertFalse(caseDetails.containsKey("otherDC8RefugeDocument"));
        Assert.assertFalse(caseDetails.containsKey("otherEC8RefugeDocument"));

        Assert.assertEquals(null, caseDetails.get("otherAC8RefugeDocument"));
    }

    @Test
    public void processOtherPeopleC8DocumentsC100NullPartyList() {
        List<Element<PartyDetails>> otherPeople = null;
        CaseData caseData = CaseData.builder().id(12345L).otherPartyInTheCaseRevised(otherPeople).caseTypeOfApplication(C100_CASE_TYPE).build();
        Map<String, Object> caseDetails = new HashMap<>();
        confidentialityCheckService.processOtherC8Documents(caseDetails, caseData);
        Assert.assertNull(caseData.getOtherPartyInTheCaseRevised());
    }
}
