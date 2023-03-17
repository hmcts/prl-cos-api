package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.OtherOrderDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.Silent.class)
public class DynamicMultiSelectListServiceTest {


    @InjectMocks
    private DynamicMultiSelectListService dynamicMultiSelectListService;

    @Mock
    private ObjectMapper objectMapper;

    private CaseData caseData;
    private static final String TEST_UUID = "00000000-0000-0000-0000-000000000000";
    private List<Element<PartyDetails>> partyDetails;

    @Before
    public void setUp() {
        List<Element<Child>> children = List.of(Element.<Child>builder().id(UUID.fromString(TEST_UUID))
                                                    .value(Child.builder().build()).build());
        partyDetails = List.of(Element.<PartyDetails>builder().id(UUID.fromString(TEST_UUID))
                                                             .value(PartyDetails.builder()
                                                                        .doTheyHaveLegalRepresentation(
                                                                 YesNoDontKnow.yes)
                                                                        .representativeFirstName("test")
                                                                        .representativeLastName("test")
                                                                        .build()).build());
        List<Element<OrderDetails>> orders = List.of(Element.<OrderDetails>builder().id(UUID.fromString(TEST_UUID))
                                                         .value(OrderDetails.builder()
                                                                    .orderTypeId("test")
                                                                    .otherDetails(OtherOrderDetails.builder()
                                                                                      .orderCreatedDate("today").build())
                                                                    .build())
                                                         .build());

        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .children(children)
            .applicants(partyDetails)
            .respondents(partyDetails)
            .orderCollection(orders)
            .othersToNotify(partyDetails)
            .build();
    }

    @Test
    public void testChildDetails() {
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData);
        assertNotNull(listItems);
    }

    @Test
    public void testOrderDetails() throws Exception {
        DynamicMultiSelectList dynamicMultiSelectList = dynamicMultiSelectListService
            .getOrdersAsDynamicMultiSelectList(caseData);
        assertNotNull(dynamicMultiSelectList);
    }

    @Test
    public void testApplicantDetails() throws Exception {
        List<DynamicMultiselectListElement> applicants = dynamicMultiSelectListService
            .getApplicantsMultiSelectList(caseData).get("applicants");
        List<DynamicMultiselectListElement> solicitors = dynamicMultiSelectListService
            .getApplicantsMultiSelectList(caseData).get("applicantSolicitors");
        assertNotNull(applicants);
        assertNotNull(solicitors);
    }

    @Test
    public void testRespondantDetails() throws Exception {
        List<DynamicMultiselectListElement> respondents = dynamicMultiSelectListService
            .getRespondentsMultiSelectList(caseData).get("respondents");
        List<DynamicMultiselectListElement> solicitors = dynamicMultiSelectListService
            .getRespondentsMultiSelectList(caseData).get("respondentSolicitors");

        assertNotNull(respondents);
        assertNotNull(solicitors);
    }

    @Test
    public void testOtherPeopleDetails() {
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getOtherPeopleMultiSelectList(caseData);
        assertNotNull(listItems);
    }

    @Test
    public void testApplicantDetailsFl401() throws Exception {
        caseData = caseData.toBuilder()
            .applicantsFL401(partyDetails.get(0).getValue())
            .respondentsFL401(partyDetails.get(0).getValue())
            .applicants(null)
            .respondents(null)
            .caseTypeOfApplication("FL401")
            .build();
        List<DynamicMultiselectListElement> applicants = dynamicMultiSelectListService
            .getApplicantsMultiSelectList(caseData).get("applicants");
        assertNotNull(applicants);
        List<DynamicMultiselectListElement> solicitors = dynamicMultiSelectListService
            .getApplicantsMultiSelectList(caseData).get("applicantSolicitors");
        assertNotNull(solicitors);
        List<DynamicMultiselectListElement> respondents = dynamicMultiSelectListService
            .getRespondentsMultiSelectList(caseData).get("respondents");
        assertNotNull(respondents);
        List<DynamicMultiselectListElement> rsolicitors = dynamicMultiSelectListService
            .getRespondentsMultiSelectList(caseData).get("respondentSolicitors");
        assertNotNull(rsolicitors);
    }

    @Test
    public void testChildDetailsFl401() throws Exception {
        caseData = caseData.toBuilder().children(null)
            .applicantChildDetails(List.of(Element.<ApplicantChild>builder().id(UUID.fromString(TEST_UUID))
                                               .value(ApplicantChild.builder().fullName("test").build())
                                               .build())).build();
        List<DynamicMultiselectListElement> children = dynamicMultiSelectListService.getChildrenMultiSelectList(caseData);
        assertNotNull(children);
    }
}
