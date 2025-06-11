package uk.gov.hmcts.reform.prl.rpa.mappers;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndRespondentRelation;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
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
