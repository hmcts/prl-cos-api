package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class ChildDetailsRevisedMapperTest {

    @InjectMocks
    ChildDetailsRevisedMapper childDetailsRevisedMapper;

    ChildDetailsRevised child;
    List<Element<ChildDetailsRevised>> children;

    List<OrderTypeEnum> appliedFor;

    @Before
    public void setUp() {
        appliedFor = new ArrayList<>();
        appliedFor.add(OrderTypeEnum.childArrangementsOrder);
        appliedFor.add(OrderTypeEnum.prohibitedStepsOrder);


    }

    @Test
    public void testChildrenMapperWithEmptyValues() {
        children = Collections.emptyList();
        assertTrue(childDetailsRevisedMapper.map(null).isEmpty());

    }

    @Test
    public void testChildrenMapperWithAllFields() {

        child = ChildDetailsRevised.builder().firstName("Lewis").lastName("Christine")
            .dateOfBirth(LocalDate.of(1990, 8, 1))
            .gender(Gender.male).otherGender("").orderAppliedFor(appliedFor)
            .build();
        Element<ChildDetailsRevised> childElement = Element.<ChildDetailsRevised>builder().value(child).build();
        children = Collections.singletonList(childElement);
        assertNotNull(childDetailsRevisedMapper.map(children));
    }

    @Test
    public void testChildrenMapperWithAllFieldsWithOtherGender() {

        child = ChildDetailsRevised.builder().firstName("Lewis").lastName("Christine")
            .dateOfBirth(LocalDate.of(1990, 8, 1))
            .gender(Gender.other).otherGender("unknow").orderAppliedFor(appliedFor)
            .build();
        Element<ChildDetailsRevised> childElement = Element.<ChildDetailsRevised>builder().value(child).build();
        children = Collections.singletonList(childElement);
        assertNotNull(childDetailsRevisedMapper.map(children));
    }


    @Test
    public void testChildrenMapperWithSomeFields() {

        child = ChildDetailsRevised.builder().firstName("Lewis").lastName("Christine")
            .dateOfBirth(LocalDate.of(1990, 8, 1))
            .gender(Gender.male).otherGender("").orderAppliedFor(appliedFor)
            .parentalResponsibilityDetails("parental responsibility details to be mentioned")
            .build();
        Element<ChildDetailsRevised> childElement = Element.<ChildDetailsRevised>builder().value(child).build();
        children = Collections.singletonList(childElement);
        assertNotNull(childDetailsRevisedMapper.map(children));
    }


}
