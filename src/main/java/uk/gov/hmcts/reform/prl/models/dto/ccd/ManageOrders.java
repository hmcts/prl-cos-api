package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.OrderWithWithoutNoticeEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.RespondentMustNotListEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ManageOrders {

    @JsonProperty("cafcassEmailAddress")
    private final List<Element<String>> cafcassEmailAddress;
    @JsonProperty("otherEmailAddress")
    private final List<Element<String>> otherEmailAddres;
    @JsonProperty("isCaseWithdrawn")
    private final YesOrNo isCaseWithdrawn;
    private final String recitalsOrPreamble;
    private final String orderDirections;
    private final String furtherDirectionsIfRequired;
    private final String courtNameInput;
    private final Address courtAddressInput;
    private final String caseNumberInput;
    private final String applicantNameInput;
    private final String applicantRefInput;
    private final String respondentRefInput;
    private final String respondentNameInput;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respondentDOBInput;
    private final Address respondentAddress;
    private final YesOrNo orderPropertyYesNo;
    private final Address orderPropertyAddress;
    private final RespondentMustNotListEnum respondentMustNotList;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateOrderMadeInput;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateOrderEndsInput;
    private final String timeOrderEndsInput;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateNextHearingInput;
    private final String timeNextHearingInput;
    private final String hearingTimeEstimateInput;
    private final String courtNameHearingInput;
    private final Address courtAddressHearingInput;
    private final String costOfApplication;
    private final OrderWithWithoutNoticeEnum orderWithWithoutNotice;
    private final String addMoreDetails;
    private final String addSchoolName;





}
