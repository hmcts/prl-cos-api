package uk.gov.hmcts.reform.prl.services.validators.eventschecker;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.prl.services.validators.ApplicantsChecker;
import uk.gov.hmcts.reform.prl.services.validators.ChildChecker;
import uk.gov.hmcts.reform.prl.services.validators.ChildDetailsRevisedChecker;
import uk.gov.hmcts.reform.prl.services.validators.ChildrenAndApplicantsChecker;
import uk.gov.hmcts.reform.prl.services.validators.ChildrenAndOtherPeopleInThisApplicationChecker;
import uk.gov.hmcts.reform.prl.services.validators.ChildrenAndRespondentsChecker;
import uk.gov.hmcts.reform.prl.services.validators.OtherChildrenNotPartOfTheApplicationChecker;
import uk.gov.hmcts.reform.prl.services.validators.OtherPeopleInTheCaseChecker;
import uk.gov.hmcts.reform.prl.services.validators.OtherPeopleInTheCaseRevisedChecker;
import uk.gov.hmcts.reform.prl.services.validators.RespondentBehaviourChecker;
import uk.gov.hmcts.reform.prl.services.validators.RespondentRelationshipChecker;
import uk.gov.hmcts.reform.prl.services.validators.RespondentsChecker;

@Data
public class PartyChecker extends CommonChecker {
    @Autowired
    private ApplicantsChecker applicantsChecker;

    @Autowired
    private ChildChecker childChecker;

    @Autowired
    private ChildrenAndApplicantsChecker childrenAndApplicantsChecker;

    @Autowired
    private OtherChildrenNotPartOfTheApplicationChecker otherChildrenNotPartOfTheApplicationChecker;

    @Autowired
    private ChildrenAndRespondentsChecker childrenAndRespondentsChecker;

    @Autowired
    private ChildrenAndOtherPeopleInThisApplicationChecker childrenAndOtherPeopleInThisApplicationChecker;

    @Autowired
    private ChildDetailsRevisedChecker childDetailsRevisedChecker;

    @Autowired
    private RespondentsChecker respondentsChecker;

    @Autowired
    private RespondentBehaviourChecker respondentBehaviourChecker;

    @Autowired
    private OtherPeopleInTheCaseChecker otherPeopleInTheCaseChecker;
    @Autowired
    private OtherPeopleInTheCaseRevisedChecker otherPeopleInTheCaseRevisedChecker;

    @Autowired
    private RespondentRelationshipChecker respondentRelationshipChecker;
}
