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
@Builder(builderClassName = "LombokBuilder", builderMethodName = "internalBuilder")
public class WhoDoesTheChildLiveWith {
    private String partyId;
    private String partyFullName;
    private PartyTypeEnum partyType;
    private Address childAddress;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String partyId;
        private String partyFullName;
        private PartyTypeEnum partyType;
        private Address childAddress;

        private static String clean(String value) {
            return (value != null && !value.trim().isEmpty()) ? value.trim() : null;
        }

        public Builder partyId(String partyId) {
            this.partyId = clean(partyId);
            return this;
        }

        public Builder partyFullName(String partyFullName) {
            this.partyFullName = clean(partyFullName);
            return this;
        }

        public Builder partyType(PartyTypeEnum partyType) {
            this.partyType = partyType;
            return this;
        }

        public Builder childAddress(Address childAddress) {
            this.childAddress = childAddress;
            return this;
        }

        public WhoDoesTheChildLiveWith build() {
            return WhoDoesTheChildLiveWith.internalBuilder()
                .partyId(partyId)
                .partyFullName(partyFullName)
                .partyType(partyType)
                .childAddress(childAddress)
                .build();
        }
    }

}
