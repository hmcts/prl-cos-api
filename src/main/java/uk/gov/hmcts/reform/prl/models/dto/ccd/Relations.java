package uk.gov.hmcts.reform.prl.models.dto.ccd;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndApplicantRelation;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndOtherPeopleRelation;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndRespondentRelation;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class Relations {

    private List<Element<ChildrenAndApplicantRelation>> buffChildAndApplicantRelations;

    private List<Element<ChildrenAndApplicantRelation>> childAndApplicantRelations;

    private List<Element<ChildrenAndRespondentRelation>> buffChildAndRespondentRelations;

    private List<Element<ChildrenAndRespondentRelation>> childAndRespondentRelations;

    private List<Element<ChildrenAndOtherPeopleRelation>> buffChildAndOtherPeopleRelations;

    private List<Element<ChildrenAndOtherPeopleRelation>> childAndOtherPeopleRelations;
}
