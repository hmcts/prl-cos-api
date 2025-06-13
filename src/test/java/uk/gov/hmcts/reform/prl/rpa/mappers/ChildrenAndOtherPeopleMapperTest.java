package uk.gov.hmcts.reform.prl.rpa.mappers;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndOtherPeopleRelation;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ChildrenAndOtherPeopleMapperTest {


    @InjectMocks
    private ChildrenAndOtherPeopleMapper childrenAndOtherPeopleMapper;


    @Test
    void testNotNull() {
        ChildrenAndOtherPeopleRelation relation = ChildrenAndOtherPeopleRelation.builder().otherPeopleFullName("test")
                .childFullName("test").childAndOtherPeopleRelation(RelationshipsEnum.father)
                .childAndOtherPeopleRelationOtherDetails("").childLivesWith(YesOrNo.Yes)
                .isChildLivesWithPersonConfidential(YesOrNo.Yes).build();
        Element<ChildrenAndOtherPeopleRelation> relationElement = Element.<ChildrenAndOtherPeopleRelation>builder().value(relation).build();
        assertNotNull(childrenAndOtherPeopleMapper.map(Collections.singletonList(relationElement)));
    }

    @Test
    void testEmpty() {
        assertTrue(childrenAndOtherPeopleMapper.map(Collections.EMPTY_LIST).isEmpty());
    }

}
