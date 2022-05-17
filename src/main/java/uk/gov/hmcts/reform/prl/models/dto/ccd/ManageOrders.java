package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.ApplicantSelectorEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ChildSelectorEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.JudgeOrMagistrateTitleEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.UnderTakingEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ManageOrders {

    private final String childListForSpecialGuardianship;

    @JsonProperty("cafcassEmailAddress")
    private final List<Element<String>> cafcassEmailAddress;
    @JsonProperty("otherEmailAddress")
    private final List<Element<String>> otherEmailAddres;
    @JsonProperty("isCaseWithdrawn")
    private final YesOrNo isCaseWithdrawn;
    private final String recitalsOrPreamble;
    private final String orderDirections;
    private final String furtherDirectionsIfRequired;

    private final String manageOrdersCourtName;
    @JsonIgnore
    private final Address manageOrdersCourtAddress;
    private final String manageOrdersCaseNo;
    private final String manageOrdersApplicant;
    private final String manageOrdersApplicantReference;
    private final String manageOrdersRespondent;
    private final String manageOrdersRespondentReference;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate manageOrdersRespondentDob;
    @JsonIgnore
    private final Address manageOrdersRespondentAddress;
    private final YesOrNo manageOrdersUnderTakingRepr;
    private final UnderTakingEnum underTakingSolicitorCounsel;
    private final String manageOrdersUnderTakingPerson;
    @JsonIgnore
    private final Address manageOrdersUnderTakingAddress;
    private final String manageOrdersUnderTakingTerms;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate manageOrdersDateOfUnderTaking;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate underTakingDateExpiry;
    private final String underTakingExpiryTime;
    private final YesOrNo underTakingFormSign;

    private final YesOrNo isTheOrderByConsent;
    private final JudgeOrMagistrateTitleEnum judgeOrMagistrateTitle;

    /**
     * child list.
     */

    @JsonProperty("childSelectorOption1")
    private final List<ChildSelectorEnum> childSelectorOption1;
    @JsonProperty("childSelectorOption2")
    private final List<ChildSelectorEnum> childSelectorOption2;
    @JsonProperty("childSelectorOption3")
    private final List<ChildSelectorEnum> childSelectorOption3;
    @JsonProperty("childSelectorOption4")
    private final List<ChildSelectorEnum> childSelectorOption4;
    @JsonProperty("childSelectorOption5")
    private final List<ChildSelectorEnum> childSelectorOption5;
    @JsonProperty("childSelectorOption6")
    private final List<ChildSelectorEnum> childSelectorOption6;
    @JsonProperty("childSelectorOption7")
    private final List<ChildSelectorEnum> childSelectorOption7;
    @JsonProperty("childSelectorOption8")
    private final List<ChildSelectorEnum> childSelectorOption8;
    @JsonProperty("childSelectorOption9")
    private final List<ChildSelectorEnum> childSelectorOption9;
    @JsonProperty("childSelectorOption10")
    private final List<ChildSelectorEnum> childSelectorOption10;
    @JsonProperty("childSelectorOption11")
    private final List<ChildSelectorEnum> childSelectorOption11;
    @JsonProperty("childSelectorOption12")
    private final List<ChildSelectorEnum> childSelectorOption12;
    @JsonProperty("childSelectorOption13")
    private final List<ChildSelectorEnum> childSelectorOption13;
    @JsonProperty("childSelectorOption14")
    private final List<ChildSelectorEnum> childSelectorOption14;
    @JsonProperty("childSelectorOption15")
    private final List<ChildSelectorEnum> childSelectorOption15;

    /**
     * Applicant.
     */

    @JsonProperty("applicantSelectorOption1")
    private final List<ApplicantSelectorEnum> applicantSelectorOption1;
    @JsonProperty("applicantSelectorOption2")
    private final List<ApplicantSelectorEnum> applicantSelectorOption2;
    @JsonProperty("applicantSelectorOption3")
    private final List<ApplicantSelectorEnum> applicantSelectorOption3;
    @JsonProperty("applicantSelectorOption4")
    private final List<ApplicantSelectorEnum> applicantSelectorOption4;
    @JsonProperty("applicantSelectorOption5")
    private final List<ApplicantSelectorEnum> applicantSelectorOption5;
    @JsonProperty("applicantSelectorOption6")
    private final List<ApplicantSelectorEnum> applicantSelectorOption6;
    @JsonProperty("applicantSelectorOption7")
    private final List<ApplicantSelectorEnum> applicantSelectorOption7;
    @JsonProperty("applicantSelectorOption8")
    private final List<ApplicantSelectorEnum> applicantSelectorOption8;
    @JsonProperty("applicantSelectorOption9")
    private final List<ApplicantSelectorEnum> applicantSelectorOption9;
    @JsonProperty("applicantSelectorOption10")
    private final List<ApplicantSelectorEnum> applicantSelectorOption10;
    @JsonProperty("applicantSelectorOption11")
    private final List<ApplicantSelectorEnum> applicantSelectorOption11;
    @JsonProperty("applicantSelectorOption12")
    private final List<ApplicantSelectorEnum> applicantSelectorOption12;
    @JsonProperty("applicantSelectorOption13")
    private final List<ApplicantSelectorEnum> applicantSelectorOption13;
    @JsonProperty("applicantSelectorOption14")
    private final List<ApplicantSelectorEnum> applicantSelectorOption14;
    @JsonProperty("applicantSelectorOption15")
    private final List<ApplicantSelectorEnum> applicantSelectorOption15;

    @JsonProperty("applicantList")
    private final String applicantList;

    /**
     * Applicant Solicitor.
     */

    @JsonProperty("applicantSolicitorList")
    private final String applicantSolicitorList;

    @JsonProperty("applicantSolicitorOption1")
    private final List<ApplicantSelectorEnum> applicantSolicitorOption1;
    @JsonProperty("applicantSolicitorOption2")
    private final List<ApplicantSelectorEnum> applicantSolicitorOption2;
    @JsonProperty("applicantSolicitorOption3")
    private final List<ApplicantSelectorEnum> applicantSolicitorOption3;
    @JsonProperty("applicantSolicitorOption4")
    private final List<ApplicantSelectorEnum> applicantSolicitorOption4;
    @JsonProperty("applicantSolicitorOption5")
    private final List<ApplicantSelectorEnum> applicantSolicitorOption5;
    @JsonProperty("applicantSolicitorOption6")
    private final List<ApplicantSelectorEnum> applicantSolicitorOption6;
    @JsonProperty("applicantSolicitorOption7")
    private final List<ApplicantSelectorEnum> applicantSolicitorOption7;
    @JsonProperty("applicantSolicitorOption8")
    private final List<ApplicantSelectorEnum> applicantSolicitorOption8;
    @JsonProperty("applicantSolicitorOption9")
    private final List<ApplicantSelectorEnum> applicantSolicitorOption9;
    @JsonProperty("applicantSolicitorOption10")
    private final List<ApplicantSelectorEnum> applicantSolicitorOption10;
    @JsonProperty("applicantSolicitorOption11")
    private final List<ApplicantSelectorEnum> applicantSolicitorOption11;
    @JsonProperty("applicantSolicitorOption12")
    private final List<ApplicantSelectorEnum> applicantSolicitorOption12;
    @JsonProperty("applicantSolicitorOption13")
    private final List<ApplicantSelectorEnum> applicantSolicitorOption13;
    @JsonProperty("applicantSolicitorOption14")
    private final List<ApplicantSelectorEnum> applicantSolicitorOption14;
    @JsonProperty("applicantSolicitorOption15")
    private final List<ApplicantSelectorEnum> applicantSolicitorOption15;

}
