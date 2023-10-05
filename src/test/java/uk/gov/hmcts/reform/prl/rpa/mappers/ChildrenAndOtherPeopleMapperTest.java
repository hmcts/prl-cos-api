package uk.gov.hmcts.reform.prl.rpa.mappers;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndOtherPeopleRelation;

import java.util.Collections;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class ChildrenAndOtherPeopleMapperTest {


    @InjectMocks
    private ChildrenAndOtherPeopleMapper childrenAndOtherPeopleMapper;


    @Test
    public void testNotNull() {
        ChildrenAndOtherPeopleRelation relation = ChildrenAndOtherPeopleRelation.builder().otherPeopleFullName("test")
                .childFullName("test").childAndOtherPeopleRelation(RelationshipsEnum.father)
                .childAndOtherPeopleRelationOtherDetails("").childLivesWith(YesOrNo.Yes)
                .isChildLivesWithPersonConfidential(YesOrNo.Yes).build();
        Element<ChildrenAndOtherPeopleRelation> relationElement = Element.<ChildrenAndOtherPeopleRelation>builder().value(relation).build();
        assertNotNull(childrenAndOtherPeopleMapper.map(Collections.singletonList(relationElement)));
    }

    @Test
    public void testEmpty() {
        assertTrue(childrenAndOtherPeopleMapper.map(Collections.EMPTY_LIST).isEmpty());
    }

}
