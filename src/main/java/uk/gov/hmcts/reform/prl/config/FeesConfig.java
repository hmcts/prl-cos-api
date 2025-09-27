package uk.gov.hmcts.reform.prl.config;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import uk.gov.hmcts.reform.prl.models.FeeType;

import java.util.Map;

@Getter
@ConfigurationProperties("fees-register")
public class FeesConfig {

    private final Map<FeeType, FeeParameters> parameters;

    public FeesConfig(Map<FeeType, FeeParameters> parameters) {
        this.parameters = parameters;
    }

    public FeeParameters getFeeParametersByFeeType(FeeType feeType) {
        return parameters.get(feeType);
    }

    @Builder
    @Getter
    @ToString
    public static class FeeParameters {
        private String channel;
        private String event;
        private String jurisdiction1;
        private String jurisdiction2;
        private String keyword;
        private String service;

        public FeeParameters(String channel, String event, String jurisdiction1, String jurisdiction2, String keyword, String service) {
            this.channel = channel;
            this.event = event;
            this.jurisdiction1 = jurisdiction1;
            this.jurisdiction2 = jurisdiction2;
            this.keyword = keyword;
            this.service = service;
        }
    }
}
