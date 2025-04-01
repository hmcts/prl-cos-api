package uk.gov.hmcts.reform.prl.models.dto.cafcass;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderClassName = "Builder", toBuilder = true)
public class MiamExemptions {
    private String childProtectionEvidence;
    private String domesticViolenceEvidence;
    private String otherGroundsEvidence;
    private String previousAttendenceEvidence;
    private String urgencyEvidence;
    private String reasonsForMiamExemption;

    public static class Builder {
        private static String clean(String val) {
            return (val != null && !val.trim().isEmpty()) ? val : null;
        }

        public Builder childProtectionEvidence(String value) {
            this.childProtectionEvidence = clean(value);
            return this;
        }

        public Builder domesticViolenceEvidence(String value) {
            this.domesticViolenceEvidence = clean(value);
            return this;
        }

        public Builder otherGroundsEvidence(String value) {
            this.otherGroundsEvidence = clean(value);
            return this;
        }

        public Builder previousAttendenceEvidence(String value) {
            this.previousAttendenceEvidence = clean(value);
            return this;
        }

        public Builder urgencyEvidence(String value) {
            this.urgencyEvidence = clean(value);
            return this;
        }

        public Builder reasonsForMiamExemption(String value) {
            this.reasonsForMiamExemption = clean(value);
            return this;
        }
    }
}
