package uk.gov.hmcts.reform.prl.services.document.docmosis;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.services.document.docmosis.TemplateConstants.CASE_DATA;
import static uk.gov.hmcts.reform.prl.services.document.docmosis.TemplateConstants.CASE_DETAILS;
import static uk.gov.hmcts.reform.prl.services.document.docmosis.TemplateConstants.TEMP_PARTY_NAMES_KEY;

@Component
@RequiredArgsConstructor
public class TemplateDataMapper {

    private final DocmosisBasePdfConfig docmosisBasePdfConfig;

    @SuppressWarnings("unchecked")
    public Map<String, Object> map(Map<String, Object> placeholders) {
        Map<String, Object> data = new HashMap<>();
        // Get case data
        if (placeholders.containsKey(CASE_DETAILS)) {
            Map<String, Object> caseDetails = (Map<String, Object>) placeholders.get(CASE_DETAILS);
            if (caseDetails.containsKey(CASE_DATA)) {
                data = (Map<String, Object>) caseDetails.get(CASE_DATA);
            }
            //EXUI -1144 - party names
            if (placeholders.containsKey(TEMP_PARTY_NAMES_KEY)) {
                data.putAll((Map<String, Object>) placeholders.get(TEMP_PARTY_NAMES_KEY));
            }
        } else {
            data.putAll(placeholders);
        }

        // Get page assets
        data.putAll(getPageAssets());
        return data;
    }

    private Map<String, Object> getPageAssets() {
        Map<String, Object> pageAssets = new HashMap<>();
        pageAssets.put(docmosisBasePdfConfig.getDisplayTemplateKey(), docmosisBasePdfConfig.getDisplayTemplateVal());
        pageAssets.put(docmosisBasePdfConfig.getFamilyCourtImgKey(), docmosisBasePdfConfig.getFamilyCourtImgVal());
        pageAssets.put(docmosisBasePdfConfig.getHmctsImgKey(), docmosisBasePdfConfig.getHmctsImgVal());

        return pageAssets;
    }
}
