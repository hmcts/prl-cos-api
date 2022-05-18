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
import uk.gov.hmcts.reform.prl.enums.manageorders.ApplicantSolicitorEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ChildSelectorEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.JudgeOrMagistrateTitleEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.RespondentSelectorEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.RespondentSolicitorEnum;
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
    private final List<ApplicantSolicitorEnum> applicantSolicitorOption1;
    @JsonProperty("applicantSolicitorOption2")
    private final List<ApplicantSolicitorEnum> applicantSolicitorOption2;
    @JsonProperty("applicantSolicitorOption3")
    private final List<ApplicantSolicitorEnum> applicantSolicitorOption3;
    @JsonProperty("applicantSolicitorOption4")
    private final List<ApplicantSolicitorEnum> applicantSolicitorOption4;
    @JsonProperty("applicantSolicitorOption5")
    private final List<ApplicantSolicitorEnum> applicantSolicitorOption5;
    @JsonProperty("applicantSolicitorOption6")
    private final List<ApplicantSolicitorEnum> applicantSolicitorOption6;
    @JsonProperty("applicantSolicitorOption7")
    private final List<ApplicantSolicitorEnum> applicantSolicitorOption7;
    @JsonProperty("applicantSolicitorOption8")
    private final List<ApplicantSolicitorEnum> applicantSolicitorOption8;
    @JsonProperty("applicantSolicitorOption9")
    private final List<ApplicantSolicitorEnum> applicantSolicitorOption9;
    @JsonProperty("applicantSolicitorOption10")
    private final List<ApplicantSolicitorEnum> applicantSolicitorOption10;
    @JsonProperty("applicantSolicitorOption11")
    private final List<ApplicantSolicitorEnum> applicantSolicitorOption11;
    @JsonProperty("applicantSolicitorOption12")
    private final List<ApplicantSolicitorEnum> applicantSolicitorOption12;
    @JsonProperty("applicantSolicitorOption13")
    private final List<ApplicantSolicitorEnum> applicantSolicitorOption13;
    @JsonProperty("applicantSolicitorOption14")
    private final List<ApplicantSolicitorEnum> applicantSolicitorOption14;
    @JsonProperty("applicantSolicitorOption15")
    private final List<ApplicantSolicitorEnum> applicantSolicitorOption15;

    /**
     * Respondent.
     */

    @JsonProperty("respondentSelectorOption1")
    private final List<RespondentSelectorEnum> respondentSelectorOption1;
    @JsonProperty("respondentSelectorOption2")
    private final List<RespondentSelectorEnum> respondentSelectorOption2;
    @JsonProperty("respondentSelectorOption3")
    private final List<RespondentSelectorEnum> respondentSelectorOption3;
    @JsonProperty("respondentSelectorOption4")
    private final List<RespondentSelectorEnum> respondentSelectorOption4;
    @JsonProperty("respondentSelectorOption5")
    private final List<RespondentSelectorEnum> respondentSelectorOption5;
    @JsonProperty("respondentSelectorOption6")
    private final List<RespondentSelectorEnum> respondentSelectorOption6;
    @JsonProperty("respondentSelectorOption7")
    private final List<RespondentSelectorEnum> respondentSelectorOption7;
    @JsonProperty("respondentSelectorOption8")
    private final List<RespondentSelectorEnum> respondentSelectorOption8;
    @JsonProperty("respondentSelectorOption9")
    private final List<RespondentSelectorEnum> respondentSelectorOption9;
    @JsonProperty("respondentSelectorOption10")
    private final List<RespondentSelectorEnum> respondentSelectorOption10;
    @JsonProperty("respondentSelectorOption11")
    private final List<RespondentSelectorEnum> respondentSelectorOption11;
    @JsonProperty("respondentSelectorOption12")
    private final List<RespondentSelectorEnum> respondentSelectorOption12;
    @JsonProperty("respondentSelectorOption13")
    private final List<RespondentSelectorEnum> respondentSelectorOption13;
    @JsonProperty("respondentSelectorOption14")
    private final List<RespondentSelectorEnum> respondentSelectorOption14;
    @JsonProperty("respondentSelectorOption15")
    private final List<RespondentSelectorEnum> respondentSelectorOption15;

    @JsonProperty("respondentList")
    private final String respondentList;

    /**
     * Respondent Solicitor.
     */

    @JsonProperty("respondentSolicitorList")
    private final String respondentSolicitorList;

    @JsonProperty("respondentSolicitorOption1")
    private final List<RespondentSolicitorEnum> respondentSolicitorOption1;
    @JsonProperty("respondentSolicitorOption2")
    private final List<RespondentSolicitorEnum> respondentSolicitorOption2;
    @JsonProperty("respondentSolicitorOption3")
    private final List<RespondentSolicitorEnum> respondentSolicitorOption3;
    @JsonProperty("respondentSolicitorOption4")
    private final List<RespondentSolicitorEnum> respondentSolicitorOption4;
    @JsonProperty("respondentSolicitorOption5")
    private final List<RespondentSolicitorEnum> respondentSolicitorOption5;
    @JsonProperty("respondentSolicitorOption6")
    private final List<RespondentSolicitorEnum> respondentSolicitorOption6;
    @JsonProperty("respondentSolicitorOption7")
    private final List<RespondentSolicitorEnum> respondentSolicitorOption7;
    @JsonProperty("respondentSolicitorOption8")
    private final List<RespondentSolicitorEnum> respondentSolicitorOption8;
    @JsonProperty("respondentSolicitorOption9")
    private final List<RespondentSolicitorEnum> respondentSolicitorOption9;
    @JsonProperty("respondentSolicitorOption10")
    private final List<RespondentSolicitorEnum> respondentSolicitorOption10;
    @JsonProperty("respondentSolicitorOption11")
    private final List<RespondentSolicitorEnum> respondentSolicitorOption11;
    @JsonProperty("respondentSolicitorOption12")
    private final List<RespondentSolicitorEnum> respondentSolicitorOption12;
    @JsonProperty("respondentSolicitorOption13")
    private final List<RespondentSolicitorEnum> respondentSolicitorOption13;
    @JsonProperty("respondentSolicitorOption14")
    private final List<RespondentSolicitorEnum> respondentSolicitorOption14;
    @JsonProperty("respondentSolicitorOption15")
    private final List<RespondentSolicitorEnum> respondentSolicitorOption15;

}
