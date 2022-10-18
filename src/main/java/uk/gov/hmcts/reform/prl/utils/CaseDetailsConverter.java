package uk.gov.hmcts.reform.prl.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

@Component
@RequiredArgsConstructor
public class CaseDetailsConverter {

    @Autowired
    private final ObjectMapper objectMapper;


    public CaseData extractCase(CaseDetails caseDetails) {
        return objectMapper.convertValue(caseDetails, CaseData.class);
    }
}
