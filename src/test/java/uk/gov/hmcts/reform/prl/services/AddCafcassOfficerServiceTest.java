package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.addcafcassofficer.ChildAndCafcassOfficer;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AddCafcassOfficerServiceTest {

    @InjectMocks
    private AddCafcassOfficerService addCafcassOfficerService;

    @Mock
    ApplicationsTabService applicationsTabService;

    @Test
    public void testPrePopulateChildName() {
        List<Element<Child>> children = new ArrayList<>();
        Child child = Child.builder()
            .firstName("test")
            .lastName("test")
            .build();

        Element<Child> childElement = element(UUID.fromString("1accfb1e-2574-4084-b97e-1cd53fd14815"), child);
        children.add(childElement);
        CaseData caseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .children(children)
            .build();
        List<Element<ChildAndCafcassOfficer>> childAndCafcassOfficers = addCafcassOfficerService.prePopulateChildName(
            caseData);
        assertEquals("1accfb1e-2574-4084-b97e-1cd53fd14815", childAndCafcassOfficers.get(0).getValue().getChildId());
        assertEquals("Child name: test test", childAndCafcassOfficers.get(0).getValue().getChildName());
    }

    @Test
    public void testPopulateCafcassOfficerDetails() {
        List<Element<Child>> children = new ArrayList<>();
        Child child = Child.builder()
            .firstName("test")
            .lastName("test")
            .build();
        Element<Child> childElement = element(UUID.fromString("1accfb1e-2574-4084-b97e-1cd53fd14815"), child);
        children.add(childElement);

        List<Element<ChildAndCafcassOfficer>> childAndCafcassOfficers = new ArrayList<>();
        ChildAndCafcassOfficer childAndCafcassOfficer = ChildAndCafcassOfficer.builder()
            .childId("1accfb1e-2574-4084-b97e-1cd53fd14815")
            .childName("test test")
            .cafcassOfficerName("a a")
            .cafcassOfficerEmailAddress("abc@test.net")
            .cafcassOfficerPhoneNo("01234567890")
            .build();
        Element<ChildAndCafcassOfficer> childAndCafcassOfficerElement = element(childAndCafcassOfficer);
        childAndCafcassOfficers.add(childAndCafcassOfficerElement);
        CaseData caseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .children(children)
            .childAndCafcassOfficers(childAndCafcassOfficers)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        addCafcassOfficerService.populateCafcassOfficerDetails(caseData,
                                                               stringObjectMap,
                                                               childAndCafcassOfficerElement);
        assertEquals("a a", caseData.getChildren().get(0).getValue().getCafcassOfficerName());
        assertEquals("abc@test.net", caseData.getChildren().get(0).getValue().getCafcassOfficerEmailAddress());
        assertEquals("01234567890", caseData.getChildren().get(0).getValue().getCafcassOfficerPhoneNo());
    }

}
