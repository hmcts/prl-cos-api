package uk.gov.hmcts.reform.prl.models.dto.ccd;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class WelshCourtEmail {
    @Value("${welsh.court.email-mapping}")
    protected String welshCourtEmailMapping;

}
