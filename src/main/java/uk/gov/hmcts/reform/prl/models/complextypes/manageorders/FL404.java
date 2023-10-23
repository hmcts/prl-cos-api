package uk.gov.hmcts.reform.prl.models.complextypes.manageorders;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.prl.enums.manageorders.DateOrderEndsTimeEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Builder(toBuilder = true)
@Data
public class FL404 {

    private final String fl404bCourtName;
    private final Address fl404bCourtAddress;
    private final String fl404bCaseNumber;
    private final String fl404bApplicantName;
    private final String fl404bApplicantReference;
    private final String fl404bRespondentName;
    private final String fl404bRespondentReference;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate fl404bRespondentDob;
    private final Address fl404bRespondentAddress;
    private final String fl404bHearingOutcome;
    //private final String fl404bChangedCourtLocation;

    private final String fl404bPowerOfArrestParagraph;
    private final String fl404bRiskOfSignificantHarm;
    private final String fl404bDateOrderMade;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private String fl404bDateOrderEnd;
    private final String fl404bDateOrderEndTime;

    private final String fl404bMentionedProperty;
    private final String fl404bAddressOfProperty;
    private final List<String> fl404bRespondentNotToThreat;
    private final List<String> fl404bRespondentNotIntimidate;
    private final List<String> fl404bRespondentNotToTelephone;
    private final List<String> fl404bRespondentNotToDamageOrThreat;
    private final List<String> fl404bRespondentNotToDamage;
    private final List<String> fl404bRespondentNotToEnterProperty;
    private final List<String> fl404bRespondentNotToThreatChild;
    private final List<String> fl404bRespondentNotHarassOrIntimidate;
    private final List<String> fl404bRespondentNotToTelephoneChild;
    private final List<String> fl404bRespondentNotToEnterSchool;
    private final String fl404bAddMoreDetailsPhoneChild;
    private final String fl404bAddMoreDetailsTelephone;
    private final String fl404bAddMoreDetailsProperty;
    private final String fl404bAddSchool;
    private final String fl404bAddMoreDetailsSchool;
    private final String fl404bCourtName1;
    private final Address fl404bOtherCourtAddress;
    private final String fl404bCostOfApplication;
    private final String fl404bIsNoticeGiven;
    private final String fl404bTimeEstimate;
    private final Address fl404bAddressAppliedFor;
    private final List<String> fl404bApplicantIsEntitledToOccupy;
    private final List<String> fl404bApplicantHasHomeRight;
    private final List<String> fl404bApplicantHasRightToEnter;
    private final List<String> fl404bApplicantHasOtherInstruction;
    private final String fl404bApplicantHomeInstruction;
    private final String fl404bApplicantOtherInstruction;
    private final List<String> fl404bApplicantAllowedToOccupy;
    private final String fl404bIsPowerOfArrest1;
    private final List<String> fl404bRespondentMustNotOccupyAddress;

    private final String fl404bIsPowerOfArrest2;
    private final List<String> fl404bRespondentShallLeaveAddress;
    private final String fl404bWhenRespondentShallLeave;
    private final String fl404bIsPowerOfArrest3;
    private final List<String> fl404bRespondentMustNotEnterAddress;
    private final String fl404bAddMoreDetails;
    private final String fl404bIsPowerOfArrest4;
    private final List<String> fl404bRespondentObstructOrHarass;
    private final String fl404bIsPowerOfArrest5;
    private final List<String> fl404bRespondentOtherInstructions;
    private final String fl404bAddAnotherInstructions;
    private final String fl404bIsPowerOfArrest6;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private final String fl404bOccupationDate1;
    private final String fl404bOccupationTime1;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private final String fl404bOccupationDate2;
    private final String fl404bOccupationTime2;
    private final String fl404bDateOfNextHearing;
    private final String fl404bTimeOfNextHearing;

    //Draft order changes
    @JsonProperty("addDirections")
    private final List<Element<DirectionDetails>> addDirections;

    private DateOrderEndsTimeEnum orderEndDateAndTimeOptions;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime orderSpecifiedDateTime;

    @JsonSetter("fl404bDateOrderEnd")
    public void setFl404bDateOrderEnd(String fl404bDateOrderEnd) {
        log.info("inside :: setFl404bDateOrderEnd ");
        this.fl404bDateOrderEnd = fl404bDateOrderEnd;
    }

    @JsonGetter("fl404bDateOrderEnd")
    public String getFl404bDateOrderEnd() {
        log.info("inside :: getFl404bDateOrderEnd");
        return fl404bDateOrderEnd;
    }

}
