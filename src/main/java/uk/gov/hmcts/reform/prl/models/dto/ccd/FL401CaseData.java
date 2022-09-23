package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantFamilyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.FL401OtherProceedingDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.Home;
import uk.gov.hmcts.reform.prl.models.complextypes.InterpreterNeed;
import uk.gov.hmcts.reform.prl.models.complextypes.LinkToCA;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherDetailsOfWithoutNoticeOrder;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.ReasonForWithoutNoticeOrder;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentBailConditionDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentBehaviour;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentRelationDateInfo;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentRelationObjectType;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentRelationOptionsInfo;
import uk.gov.hmcts.reform.prl.models.complextypes.StatementOfTruth;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.complextypes.WelshNeed;
import uk.gov.hmcts.reform.prl.models.complextypes.WithoutNoticeOrderDetails;

import java.util.List;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder(toBuilder = true)
public class FL401CaseData {

    private final String applicantCaseName;

    /**
     * FL401 Type of Application.
     */
    @JsonProperty("typeOfApplicationOrders")
    private final TypeOfApplicationOrders typeOfApplicationOrders;
    @JsonProperty("typeOfApplicationLinkToCA")
    private final LinkToCA typeOfApplicationLinkToCA;

    /**
     * Without Notice Order.
     */
    @JsonProperty("orderWithoutGivingNoticeToRespondent")
    private final WithoutNoticeOrderDetails orderWithoutGivingNoticeToRespondent;
    @JsonProperty("reasonForOrderWithoutGivingNotice")
    private final ReasonForWithoutNoticeOrder reasonForOrderWithoutGivingNotice;
    @JsonProperty("bailDetails")
    private final RespondentBailConditionDetails bailDetails;
    @JsonProperty("anyOtherDtailsForWithoutNoticeOrder")
    private final OtherDetailsOfWithoutNoticeOrder anyOtherDtailsForWithoutNoticeOrder;

    /**
     * Applicants Details.
     */
    @JsonProperty("applicantsFL401")
    private final PartyDetails applicantsFL401;

    /**
     * Respondent Details.
     */
    @JsonProperty("respondentsFL401")
    private final PartyDetails respondentsFL401;

    /**
     * Applicants Family.
     */
    @JsonProperty("applicantFamilyDetails")
    private final ApplicantFamilyDetails applicantFamilyDetails;
    @JsonProperty("applicantChildDetails")
    private final List<Element<ApplicantChild>> applicantChildDetails;

    /**
     * Home Situation DA.
     */
    private final Home home;

    /**
     * FL401 Respondents relationship.
     */
    private final RespondentRelationObjectType respondentRelationObject;
    private final RespondentRelationDateInfo respondentRelationDateInfoObject;
    private final RespondentRelationOptionsInfo respondentRelationOptions;

    /**
     * Respondent Behaviour.
     */
    private final RespondentBehaviour respondentBehaviourData;

    /**
     * Attending the hearing.
     */
    private final YesOrNo isWelshNeeded;
    @JsonAlias({"welshNeeds", "fl401WelshNeeds"})
    private final List<Element<WelshNeed>> welshNeeds;
    private final YesOrNo isInterpreterNeeded;
    private final List<Element<InterpreterNeed>> interpreterNeeds;
    private final YesOrNo isDisabilityPresent;
    private final String adjustmentsRequired;
    private final YesOrNo isSpecialArrangementsRequired;
    private final String specialArrangementsRequired;
    private final YesOrNo isIntermediaryNeeded;
    private final String reasonsForIntermediary;


    /**
     * FL401 Other Proceedings.
     */
    private final FL401OtherProceedingDetails fl401OtherProceedingDetails;

    /**
     *  FL401 Statement Of truth and submit.
     */
    @JsonProperty("fl401StmtOfTruth")
    private final StatementOfTruth fl401StmtOfTruth;

}
