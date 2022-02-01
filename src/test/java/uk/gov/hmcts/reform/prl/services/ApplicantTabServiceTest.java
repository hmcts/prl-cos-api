package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Applicant;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.HearingUrgency;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Respondent;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.TypeOfApplication;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ApplicantTabServiceTest {

    @InjectMocks
    ApplicationsTabService applicationsTabService;

    @Mock
    ObjectMapper objectMapper;


    CaseData caseDataWithParties;
    CaseData emptyCaseData;
    Address address;
    List<Element<PartyDetails>> partyList;
    PartyDetails partyDetails;

    @Before
    public void setup() {
        address = Address.builder()
            .addressLine1("55 Test Street")
            .postTown("Town")
            .postCode("N12 3BH")
            .build();

        partyDetails = PartyDetails.builder()
            .firstName("First name")
            .lastName("Last name")
            .dateOfBirth(LocalDate.of(1989, 11, 30))
            .gender(Gender.male)
            .address(address)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("test@test.com")
            .build();

        Element<PartyDetails> partyDetailsElement = Element.<PartyDetails>builder().value(partyDetails).build();
        partyList = Collections.singletonList(partyDetailsElement);

        caseDataWithParties = CaseData.builder()
            .applicants(partyList)
            .respondents(partyList)
            //type of application
            .ordersApplyingFor(Collections.singletonList(OrderTypeEnum.childArrangementsOrder))
            .typeOfChildArrangementsOrder(ChildArrangementOrderTypeEnum.spendTimeWithOrder)
            .natureOfOrder("Test nature of order")
            // hearing urgency
            .isCaseUrgent(YesOrNo.Yes)
            .caseUrgencyTimeAndReason("Test String")
            .doYouRequireAHearingWithReducedNotice(YesOrNo.No)
            .build();

        emptyCaseData = CaseData.builder().build();
    }


    @Test
    public void testApplicantTableMapper() {
        Applicant applicant = Applicant.builder()
            .firstName("First name")
            .lastName("Last name")
            .dateOfBirth(LocalDate.of(1989, 11, 30))
            .gender("Male") //the new POJOs use strings as the enums are causing errors
            .address(address)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("test@test.com")
            .build();

        Element<Applicant> applicantElement = Element.<Applicant>builder().value(applicant).build();
        List<Element<Applicant>> expectedApplicantList =  Collections.singletonList(applicantElement);
        Applicant emptyApplicant = Applicant.builder().build();
        Element<Applicant> emptyApplicantElement = Element.<Applicant>builder().value(emptyApplicant).build();
        List<Element<Applicant>> emptyApplicantList =  Collections.singletonList(emptyApplicantElement);

        when(objectMapper.convertValue(partyDetails, Applicant.class)).thenReturn(applicant);
        assertEquals(expectedApplicantList, applicationsTabService.getApplicantsTable(caseDataWithParties));
        assertEquals(emptyApplicantList, applicationsTabService.getApplicantsTable(emptyCaseData));
    }

    @Test
    public void testRespondentTableMapper() {
        Respondent respondent = Respondent.builder()
            .firstName("First name")
            .lastName("Last name")
            .dateOfBirth(LocalDate.of(1989, 11, 30))
            .gender("Male") //the new POJOs use strings as the enums are causing errors
            .address(address)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("test@test.com")
            .build();

        Element<Respondent> respondentElement = Element.<Respondent>builder().value(respondent).build();
        List<Element<Respondent>> expectedRespondentList =  Collections.singletonList(respondentElement);
        Respondent emptyRespondent = Respondent.builder().build();
        Element<Respondent> emptyRespondentElement = Element.<Respondent>builder().value(emptyRespondent).build();
        List<Element<Respondent>> emptyRespondentList =  Collections.singletonList(emptyRespondentElement);

        when(objectMapper.convertValue(partyDetails, Respondent.class)).thenReturn(respondent);
        assertEquals(expectedRespondentList, applicationsTabService.getRespondentsTable(caseDataWithParties));
        assertEquals(emptyRespondentList, applicationsTabService.getRespondentsTable(emptyCaseData));
    }

    @Test
    public void testDeclarationTable() {
        Map<String, Object> expectedDeclarationMap = new HashMap<>();
        String declarationText = "I understand that proceedings for contempt of court may be brought"
            + " against anyone who makes, or causes to be made, a false statement in a document verified"
            + " by a statement of truth without an honest belief in its truth. The applicant believes "
            + "that the facts stated in this form and any continuation sheets are true. [Solicitor Name] "
            + "is authorised by the applicant to sign this statement.";
        expectedDeclarationMap.put("declarationText", declarationText);
        expectedDeclarationMap.put("agreedBy", "<Solicitor name>");

        assertEquals(expectedDeclarationMap, applicationsTabService.getDeclarationTable(caseDataWithParties));
    }

    @Test
    public void testHearingUrgencyTableMapper() {
        HearingUrgency hearingUrgency = HearingUrgency.builder()
            .isCaseUrgent(YesOrNo.Yes)
            .caseUrgencyTimeAndReason("Test String")
            .doYouRequireAHearingWithReducedNotice(YesOrNo.No)
            .build();
        Map<String, Object> hearingUrgencyMap = Map.of(
                    "isCaseUrgent", "Yes",
                    "caseUrgencyTimeAndReason", "Test String",
                    "doYouRequireAHearingWithReducedNotice", "No"
        );

        when(objectMapper.convertValue(caseDataWithParties, HearingUrgency.class)).thenReturn(hearingUrgency);
        when(objectMapper.convertValue(hearingUrgency, Map.class)).thenReturn(hearingUrgencyMap);
        assertEquals(hearingUrgencyMap, applicationsTabService.getHearingUrgencyTable(caseDataWithParties));
    }

    @Test
    public void testApplicationTypeTableMapper() {
        TypeOfApplication typeOfApplication = TypeOfApplication.builder()
            .ordersApplyingFor("Child Arrangements Order")
            .typeOfChildArrangementsOrder("Spend time with order")
            .natureOfOrder("Test nature of order")
            .build();
        Map<String, Object> typeOfApplicationMap = Map.of(
            "ordersApplyingFor", "Child Arrangements Order",
            "typeOfChildArrangementsOrder", "Spend time with order",
            "natureOfOrder", "Test nature of order"
        );

        when(objectMapper.convertValue(typeOfApplication, Map.class)).thenReturn(typeOfApplicationMap);
        assertEquals(typeOfApplicationMap, applicationsTabService.getTypeOfApplicationTable(caseDataWithParties));
    }







}
