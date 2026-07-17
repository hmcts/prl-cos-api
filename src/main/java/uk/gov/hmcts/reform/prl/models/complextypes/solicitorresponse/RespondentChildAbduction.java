package uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.respondentsolicitor.WhomConsistPassportList;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class RespondentChildAbduction {

    @CCD(
            label = "*Why do you believe the child(ren) may be abducted?",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String reasonForChildAbductionBelief;
    @CCD(
            label = "*Have there been any previous threats, attempts to abduct or actual  abduction of the child(ren)?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo previousThreatsForChildAbduction;
    @CCD(
            label = "*Give details",
            showCondition = "previousThreatsForChildAbduction=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String previousThreatsForChildAbductionDetails;
    @CCD(label = "*Where is/are the child(ren) now?", searchable = false, typeOverride = FieldType.TextArea)
    private final String whereIsChild;
    @CCD(label = "*Has the passport office been notifed? ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo hasPassportOfficeNotified;
    @CCD(
            label = "*Were the police or any other organisation/agency involved in any previous incident of attempted abduction or abduction?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo anyOrgInvolvedInPreviousAbduction;
    @CCD(
            label = "*Give details",
            showCondition = "anyOrgInvolvedInPreviousAbduction=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String anyOrgInvolvedInPreviousAbductionDetails;
    @CCD(label = "*Do the children have a passport?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo childrenHavePassport;
    @CCD(
            label = "*Do the children have more than one passport?",
            showCondition = "childrenHavePassport=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo childrenHaveMoreThanOnePassport;
    @CCD(
            label = "*Who has the children’s passport(s)?",
            showCondition = "childrenHavePassport=\"Yes\"",
            searchable = false
    )
    private final List<WhomConsistPassportList> whoHasChildPassport;
    @CCD(
            label = "*Who has the children’s passport(s)?",
            showCondition = "childrenHavePassport=\"Yes\" AND whoHasChildPassport CONTAINS \"otherPeople\"",
            searchable = false
    )
    private final String whoHasChildPassportOther;

}
