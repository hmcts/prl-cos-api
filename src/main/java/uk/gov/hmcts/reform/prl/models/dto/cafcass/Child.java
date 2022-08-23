package uk.gov.hmcts.reform.prl.models.dto.cafcass;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.*;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class Child {

    private String firstName;
    private String lastName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
    private DontKnow isDateOfBirthUnknown; //TODO: field not used
    private Gender gender;
    private String otherGender;
    private List<OrderTypeEnum> orderAppliedFor;
    private RelationshipsEnum applicantsRelationshipToChild;
    private String otherApplicantsRelationshipToChild;
    private RelationshipsEnum  respondentsRelationshipToChild;
    private String otherRespondentsRelationshipToChild;
    @JsonIgnore
    private Address address;
    @JsonIgnore
    private YesOrNo isChildAddressConfidential;
    private List<LiveWithEnum> childLiveWith;
    private List<Element<OtherPersonWhoLivesWithChild>> personWhoLivesWithChild;
    private String parentalResponsibilityDetails;

    public boolean hasConfidentialInfo() {
        return YesOrNo.Yes.equals(this.isChildAddressConfidential);
    }

}
