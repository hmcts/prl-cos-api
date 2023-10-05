package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherChildrenNotInTheCase;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class OtherChildrenNotInTheCaseMapperTest {

    @InjectMocks
    OtherChildrenNotInTheCaseMapper childrenNotInTheCaseMapper;

    OtherChildrenNotInTheCase child;
    List<Element<OtherChildrenNotInTheCase>> children;


    @Before
    public void setUp() {
    }

    @Test
    public void testChildrenMapperWithEmptyValues() {
        children = Collections.emptyList();
        assertTrue(childrenNotInTheCaseMapper.map(null).isEmpty());

    }

    @Test
    public void testChildrenMapperWithAllFields() {

        child = OtherChildrenNotInTheCase.builder().firstName("Lewis").lastName("Christine")
            .dateOfBirth(LocalDate.of(1990, 8, 1))
            .gender(Gender.male).otherGender("").build();
        Element<OtherChildrenNotInTheCase> childElement = Element.<OtherChildrenNotInTheCase>builder().value(child).build();
        children = Collections.singletonList(childElement);
        assertNotNull(childrenNotInTheCaseMapper.map(children));
    }

    @Test
    public void testChildrenMapperWithAllFieldsWithOtherGender() {

        child = OtherChildrenNotInTheCase.builder().firstName("Lewis").lastName("Christine")
            .dateOfBirth(LocalDate.of(1990, 8, 1))
            .gender(Gender.other).otherGender("unknow")
            .build();
        Element<OtherChildrenNotInTheCase> childElement = Element.<OtherChildrenNotInTheCase>builder().value(child).build();
        children = Collections.singletonList(childElement);
        assertNotNull(childrenNotInTheCaseMapper.map(children));
    }


    @Test
    public void testChildrenMapperWithSomeFields() {

        child = OtherChildrenNotInTheCase.builder().firstName("Lewis").lastName("Christine")
            .dateOfBirth(LocalDate.of(1990, 8, 1))
            .gender(Gender.male).otherGender("")
            .build();
        Element<OtherChildrenNotInTheCase> childElement = Element.<OtherChildrenNotInTheCase>builder().value(child).build();
        children = Collections.singletonList(childElement);
        assertNotNull(childrenNotInTheCaseMapper.map(children));
    }


}
