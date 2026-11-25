package uk.gov.hmcts.reform.prl.services.document.docmosis;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "docmosis.pdf")
public class DocmosisBasePdfConfig {

    private String familyCourtImgKey;
    private String familyCourtImgVal;
    private String hmctsImgKey;
    private String hmctsImgVal;
    private String displayTemplateKey;
    private String displayTemplateVal;
}
