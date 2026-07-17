package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Builder
@Data
public class HearingUrgency {

    @CCD(label = "Is the case urgent", searchable = false, typeOverride = FieldType.YesOrNo)
    public final YesOrNo isCaseUrgent;
    @CCD(label = "Reasons for urgency", searchable = false, typeOverride = FieldType.TextArea)
    public final String caseUrgencyTimeAndReason;
    @CCD(label = "Efforts to notify", searchable = false, typeOverride = FieldType.TextArea)
    public final String effortsMadeWithRespondents;
    @CCD(label = "Do you need a without notice hearing?", searchable = false, typeOverride = FieldType.YesOrNo)
    public final YesOrNo doYouNeedAWithoutNoticeHearing;
    @CCD(label = "Reasons for without notice", searchable = false, typeOverride = FieldType.TextArea)
    public final String reasonsForApplicationWithoutNotice;
    @CCD(label = "Do you require a hearing with reduced notice?", searchable = false, typeOverride = FieldType.YesOrNo)
    public final YesOrNo doYouRequireAHearingWithReducedNotice;
    @CCD(label = "Reasons for reduced notice", searchable = false, typeOverride = FieldType.TextArea)
    public final String setOutReasonsBelow;
    @CCD(label = "Are respondents aware of proceedings?", searchable = false, typeOverride = FieldType.YesOrNo)
    public final YesOrNo areRespondentsAwareOfProceedings;

}
