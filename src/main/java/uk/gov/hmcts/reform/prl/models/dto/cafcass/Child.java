package uk.gov.hmcts.reform.prl.models.dto.cafcass;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.DontKnow;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.LiveWithEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(
    toBuilder = true,
    builderClassName = "Builder",
    builderMethodName = "internalBuilder")
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
    private RelationshipsEnum respondentsRelationshipToChild;
    private String otherRespondentsRelationshipToChild;

    @JsonIgnore
    private Address address;

    @JsonIgnore
    private YesOrNo isChildAddressConfidential;

    private List<LiveWithEnum> childLiveWith;
    private List<Element<OtherPersonWhoLivesWithChild>> personWhoLivesWithChild;

    private String parentalResponsibilityDetails;
    private WhoDoesTheChildLiveWith whoDoesTheChildLiveWith;

    public boolean hasConfidentialInfo() {
        return YesOrNo.Yes.equals(this.isChildAddressConfidential);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private static String clean(String value) {
            return (value != null && !value.trim().isEmpty()) ? value : null;
        }

        public Builder firstName(String firstName) {
            this.firstName = clean(firstName);
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = clean(lastName);
            return this;
        }

        public Builder otherGender(String otherGender) {
            this.otherGender = clean(otherGender);
            return this;
        }

        public Builder otherApplicantsRelationshipToChild(String otherRel) {
            this.otherApplicantsRelationshipToChild = clean(otherRel);
            return this;
        }

        public Builder otherRespondentsRelationshipToChild(String otherRel) {
            this.otherRespondentsRelationshipToChild = clean(otherRel);
            return this;
        }

        public Builder parentalResponsibilityDetails(String prDetails) {
            this.parentalResponsibilityDetails = clean(prDetails);
            return this;
        }
    }
}
