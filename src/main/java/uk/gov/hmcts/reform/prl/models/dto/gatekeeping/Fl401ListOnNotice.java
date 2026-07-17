package uk.gov.hmcts.reform.prl.models.dto.gatekeeping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.FL401ListOnNoticeDirectionsEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawLaCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCafcassRAccess;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Fl401ListOnNotice {

    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawLaCrudAccess.class}
    )
    private final String isFl401CaseCreatedForWithOutNotice;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawLaCrudAccess.class}
    )
    private final String fl401WithOutNoticeReasonToRespondent;
    @CCD(
            label = "AdditionalDirections",
            hint = "Select all that apply ",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawLaCrudAccess.class}
    )
    private final List<FL401ListOnNoticeDirectionsEnum> additionalDirections;
    @CCD(
            label = "Reduced notice period",
            hint = "Give further details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawLaCrudAccess.class}
    )
    private final String reducedNoticePeriodDetails;
    @CCD(
            label = "List with Child arrangements case",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawLaCrudAccess.class}
    )
    private final DynamicList linkedCaCasesList;
    @CCD(
            label = "Give further details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawLaCrudAccess.class}
    )
    private final String linkedCaCasesFurtherDetails;
    @CCD(
            label = "Applicant needs to provide further information",
            hint = "Give further details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawLaCrudAccess.class}
    )
    private final String applicantNeedsFurtherInfoDetails;
    @CCD(
            label = "The respondent needs to file a statement",
            hint = "Give further details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawLaCrudAccess.class}
    )
    private final String respondentNeedsFileStatementDetails;
    @CCD(
            label = "Hearing",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawLaCrudAccess.class}
    )
    private final List<Element<HearingData>> fl401ListOnNoticeHearingDetails;
    @CCD(
            label = "Directions to admin:",
            hint = "This may include details of how the respondent should be served - for example, by a solicitor process server.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawLaCrudAccess.class}
    )
    private final String fl401ListOnNoticeDirectionsToAdmin;
    @CCD(
            label = "Once this order is complete, can the application be served?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawLaCrudAccess.class}
    )
    private final YesOrNo fl401LonOrderCompleteToServe;
    @CCD(
            label = " ",
            categoryID = "anyOtherDoc",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawCafcassRAccess.class, CaseworkerPrivatelawLaCrudAccess.class}
    )
    private final Document fl401ListOnNoticeDocument;

}
