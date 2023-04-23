package uk.gov.hmcts.reform.prl.models.dto.gatekeeping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.AllocatedJudgeTypeEnum;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.FL401ListOnNoticeDirectionsEnum;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.TierOfJudiciaryEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Fl401ListOnNotice {

    private final String isFl401CaseCreatedForWithOutNotice;
    private final String fl401WithOutNoticeReasonToRespondent;
    private final List<FL401ListOnNoticeDirectionsEnum> additionalDirections;
    private final String reducedNoticePeriodDetails;
    private final DynamicList linkedCaCasesList;
    private final String linkedCaCasesFurtherDetails;
    private final String applicantNeedsFurtherInfoDetails;
    private final String respondentNeedsFileStatementDetails;
    private final List<Element<HearingData>> fl401ListOnNoticeHearingDetails;
    private final String fl401ListOnNoticeDirectionsToAdmin;
    private final YesOrNo fl401LonOrderCompleteToServe;
    private final YesOrNo fl401LonAllocateSpecificJudgeOrLa;
    private final AllocatedJudgeTypeEnum fl401LonIsJudgeOrLegalAdviser;
    private final JudicialUser fl401LonJudgeNameAndEmail;
    private final DynamicList fl401LonLegalAdviserList;
    private final TierOfJudiciaryEnum fl401LonTierOfJudiciary;
    private final Document fl401ListOnNoticeDocument;

}
