package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.ApplicantOccupationEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.RespondentOccupationEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
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




    private final String courtName1;
    private final Address courtAddress;
    private final String caseNumber;
    private final String applicantName1;
    private final String applicantReference;
    private final String respondentReference;
    private final String respondentName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respondentDateOfBirth;
    private final Address respondentAddress;
    private final Address addressTheOrderAppliesTo;
    private final String courtDeclares;
    @JsonProperty("courtDeclares2")
    private final List<ApplicantOccupationEnum> courtDeclares2;
    private final String homeRights;
    private final String applicantInstructions;
    private final String theRespondent;
    @JsonProperty("theRespondent2")
    private final List<RespondentOccupationEnum> theRespondent2;
    private final YesOrNo powerOfArrest1;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respondentDay1;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respondentDay2;
    private String respondentStartTime;
    private String respondentEndTime;
    private final YesOrNo powerOfArrest2;
    private final String whenTheyLeave;
    private final YesOrNo powerOfArrest3;
    private final String moreDetails;
    private final YesOrNo powerOfArrest4;
    private final String instructionRelating;
    private final YesOrNo powerOfArrest5;
    private final YesOrNo powerOfArrest6;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateOrderMade1;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateOrderEnds;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate datePlaceHearing;
    private String datePlaceHearingTime;
    private String dateOrderEndsTime;
    private final String courtName2;
    private final Address ukPostcode2;
    private final String applicantCost;
    private final String orderNotice;
    private final String hearingTimeEstimate;


}
