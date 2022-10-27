package uk.gov.hmcts.reform.prl.models.c100rebuild;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class C100RebuildChildDetailsElements {

    @JsonProperty("cd_childrenKnownToSocialServices")
    private String childrenKnownToSocialServices;
    @JsonProperty("cd_childrenKnownToSocialServicesDetails")
    private String childrenKnownToSocialServicesDetails;
    @JsonProperty("cd_childrenSubjectOfProtectionPlan")
    private String childrenSubjectOfProtectionPlan;
    @JsonProperty("cd_children")
    private List<ChildDetail> childDetails;

    @Data
    @Builder(toBuilder = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChildDetail {
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
        @JsonProperty("parentialResponsibility")
        private ParentialResponsibility parentialResponsibility;
    }

    @Data
    @Builder(toBuilder = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonalDetails {
        private DateofBirth dateOfBirth;
        private String isDateOfBirthUnknown;
        private DateofBirth approxDateOfBirth;
        private String gender;
        private String otherGenderDetails;
    }

    @Data
    @Builder(toBuilder = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DateofBirth {
        private String year;
        private String month;
        private String day;
    }

    @Data
    @Builder(toBuilder = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChildMatters {
        private List<String> needsResolution;
    }

    @Data
    @Builder(toBuilder = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParentialResponsibility {
        private String statement;
    }
}
