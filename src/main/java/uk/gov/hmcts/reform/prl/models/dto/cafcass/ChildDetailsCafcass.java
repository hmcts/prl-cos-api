package uk.gov.hmcts.reform.prl.models.dto.cafcass;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.DontKnow;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.addcafcassofficer.CafcassOfficerPositionEnum;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class ChildDetailsCafcass {

    private String firstName;
    private String lastName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
    private DontKnow isDateOfBirthUnknown; //TODO: field not used
    private Gender gender;
    private String otherGender;
    private List<OrderTypeEnum> orderAppliedFor;
    private String parentalResponsibilityDetails;

    private YesOrNo isFinalOrderIssued;

    private String cafcassOfficerName;
    private CafcassOfficerPositionEnum cafcassOfficerPosition;
    private String cafcassOfficerOtherPosition;
    private String cafcassOfficerEmailAddress;
    private String cafcassOfficerPhoneNo;


}
