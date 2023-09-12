package uk.gov.hmcts.reform.prl.rpa.mappers;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndRespondentRelation;

import java.util.Collections;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class ChildrenAndRespondentsMapperTest {


    @InjectMocks
    private ChildrenAndRespondentsMapper childrenAndRespondentsMapper;


    @Test
    public void testNotNull() {
        ChildrenAndRespondentRelation relation = ChildrenAndRespondentRelation.builder().respondentFullName("test")
                .childFullName("test").childAndRespondentRelation(RelationshipsEnum.father)
                .childAndRespondentRelationOtherDetails("").childLivesWith(YesOrNo.Yes).build();
        Element<ChildrenAndRespondentRelation> relationElement = Element.<ChildrenAndRespondentRelation>builder().value(relation).build();
        assertNotNull(childrenAndRespondentsMapper.map(Collections.singletonList(relationElement)));
    }

    @Test
    public void testEmpty() {
        assertTrue(childrenAndRespondentsMapper.map(Collections.EMPTY_LIST).isEmpty());
    }

}