package uk.gov.hmcts.reform.prl.models.c100rebuild;

    import com.fasterxml.jackson.annotation.JsonProperty;
    import lombok.AllArgsConstructor;
    import lombok.Builder;
    import lombok.Data;
    import lombok.NoArgsConstructor;
    import uk.gov.hmcts.reform.prl.enums.Gender;
    import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
    import uk.gov.hmcts.reform.prl.enums.YesOrNo;

    import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class C100RebuildChildDetailsElements {

    @JsonProperty("cd_childrenKnownToSocialServices")
    private YesNoDontKnow childrenKnownToSocialServices;
    @JsonProperty("cd_childrenKnownToSocialServicesDetails")
    private String childrenKnownToSocialServicesDetails;
    @JsonProperty("cd_childrenSubjectOfProtectionPlan")
    private YesNoDontKnow childrenSubjectOfProtectionPlan;
    @JsonProperty("cd_children")
    private List<ChildDetail> childDetails;

    @Data
    @Builder(toBuilder = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public class ChildDetail {
        @JsonProperty("id")
        private String id;
        @JsonProperty("firstName")
        private String firstName;
        @JsonProperty("lastName")
        private String lastName;
        @JsonProperty("personalDetails")
        private PersonalDetails personalDetails;
        @JsonProperty("childMatters")
        private ChildMatters childMatters;
        @JsonProperty("ParentialResponsibility")
        private ParentialResponsibility parentialResponsibility;
    }

    @Data
    @Builder(toBuilder = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public class PersonalDetails {
        private DateofBirth dateofBirth;
        private YesOrNo isDateOfBirthUnknown;
        private DateofBirth approxDateOfBirth;
        private Gender gender;
        private String otherGenderDetails;
    }

    @Data
    @Builder(toBuilder = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public class DateofBirth {
        private String year;
        private String month;
        private String day;
    }

    @Data
    @Builder(toBuilder = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public class ChildMatters {
        private List<String> needsResolution;
    }

    @Data
    @Builder(toBuilder = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public class ParentialResponsibility {
        private String statement;
    }
}
