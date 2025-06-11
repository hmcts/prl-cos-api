package uk.gov.hmcts.reform.prl.rpa.mappers;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndApplicantRelation;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class  ChildrenAndApplicantsMapperTest {


    @InjectMocks
    private ChildrenAndApplicantsMapper childrenAndApplicantsMapper;


    @Test
    public void testNotNull() {
        ChildrenAndApplicantRelation relation = ChildrenAndApplicantRelation.builder().applicantFullName("test")
                .childFullName("test").childAndApplicantRelation(RelationshipsEnum.father)
                .childAndApplicantRelationOtherDetails("").childLivesWith(YesOrNo.Yes).build();
        Element<ChildrenAndApplicantRelation> relationElement = Element.<ChildrenAndApplicantRelation>builder().value(relation).build();
        assertNotNull(childrenAndApplicantsMapper.map(Collections.singletonList(relationElement)));
    }

    @Test
    public void testEmpty() {
        assertTrue(childrenAndApplicantsMapper.map(Collections.EMPTY_LIST).isEmpty());
    }

}
