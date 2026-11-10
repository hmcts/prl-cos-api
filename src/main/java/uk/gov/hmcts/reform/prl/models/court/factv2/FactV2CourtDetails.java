package uk.gov.hmcts.reform.prl.models.court.factv2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FactV2CourtDetails {
    private String id;
    private String name;
    private String slug;
    private boolean open;
    private List<FactV2Address> courtAddresses;
    private List<FactV2CourtCode> courtCodes;
    private List<FactV2Contact> courtContactDetails;
    private List<FactV2DxCode> courtDxCodes;
    private List<FactV2ProfessionalInformation> courtProfessionalInformation;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FactV2Address {
        private String addressLine1;
        private String addressLine2;
        private String addressType;
        private String postcode;
        private String townCity;
        private List<FactV2CourtType> courtTypes;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FactV2CourtType {
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FactV2CourtCode {
        private Long countyCourtCode;
        private Long familyCourtCode;
        private String gbs;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FactV2Contact {
        private String email;
        private String explanation;
        private FactV2ContactDescription courtContactDescription;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FactV2ContactDescription {
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FactV2DxCode {
        @JsonProperty("dxCode")
        private String dxCode;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FactV2ProfessionalInformation {
        private Boolean accessScheme;
    }
}
