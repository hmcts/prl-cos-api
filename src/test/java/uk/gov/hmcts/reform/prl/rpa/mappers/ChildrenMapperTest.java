package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.LiveWithEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonWhoLivesWithChild;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class ChildrenMapperTest {

    @InjectMocks
    ChildrenMapper childrenMapper;
    @Mock
    AddressMapper addressMapper;
    Child child;
    List<Element<Child>> children;
    List<LiveWithEnum> liveWith;
    List<OrderTypeEnum> appliedFor;
    List<Element<OtherPersonWhoLivesWithChild>> personWhoLivesWithChild;
    OtherPersonWhoLivesWithChild otherPersonWhoLivesWithChild;
    Address address;

    @Before
    public void setUp() {
        liveWith = new ArrayList<>();
        liveWith.add(LiveWithEnum.applicant);
        liveWith.add(LiveWithEnum.respondent);
        appliedFor = new ArrayList<>();
        appliedFor.add(OrderTypeEnum.childArrangementsOrder);
        appliedFor.add(OrderTypeEnum.prohibitedStepsOrder);
        address = Address.builder()
            .addressLine1("55 Test Street")
            .postTown("Town")
            .postCode("N12 3BH")
            .build();
        otherPersonWhoLivesWithChild = OtherPersonWhoLivesWithChild.builder().firstName("FirstName")
            .lastName("LastName").relationshipToChildDetails("Father").address(address)
            .isPersonIdentityConfidential(YesOrNo.Yes).build();
        Element<OtherPersonWhoLivesWithChild> otherPersonWhoLivesWithChildElement = Element
            .<OtherPersonWhoLivesWithChild>builder().value(otherPersonWhoLivesWithChild).build();
        personWhoLivesWithChild = Collections.singletonList(otherPersonWhoLivesWithChildElement);


    }

    @Test
    public void testChildrenMapperWithEmptyValues() {
        children = Collections.emptyList();
        assertTrue(childrenMapper.map(children).isEmpty());

    }

    @Test
    public void testChildrenMapperWithAllFields() {

        child = (Child.builder().firstName("Lewis").lastName("Christine")
            .dateOfBirth(LocalDate.of(1990, 8, 1))
            .gender(Gender.male).otherGender("").childLiveWith(liveWith).orderAppliedFor(appliedFor)
            .applicantsRelationshipToChild(RelationshipsEnum.father))
            .parentalResponsibilityDetails("parental responsibility details to be mentioned")
            .respondentsRelationshipToChild(RelationshipsEnum.father).otherApplicantsRelationshipToChild("Guardian")
            .otherRespondentsRelationshipToChild("Guardian").personWhoLivesWithChild(personWhoLivesWithChild)
            .build();
        Element<Child> childElement = Element.<Child>builder().value(child).build();
        children = Collections.singletonList(childElement);
        assertNotNull(childrenMapper.map(children));
    }

    @Test
    public void testChildrenMapperWithSomeFields() {

        child = (Child.builder().firstName("Lewis").lastName("Christine")
            .dateOfBirth(LocalDate.of(1990, 8, 1))
            .gender(Gender.male).otherGender("").childLiveWith(liveWith).orderAppliedFor(appliedFor)
            .applicantsRelationshipToChild(RelationshipsEnum.father))
            .parentalResponsibilityDetails("parental responsibility details to be mentioned")
            .respondentsRelationshipToChild(RelationshipsEnum.father).otherApplicantsRelationshipToChild("Guardian")
            .otherRespondentsRelationshipToChild("Guardian")
            .build();
        Element<Child> childElement = Element.<Child>builder().value(child).build();
        children = Collections.singletonList(childElement);
        assertNotNull(childrenMapper.map(children));
    }


}
