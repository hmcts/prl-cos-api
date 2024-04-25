package uk.gov.hmcts.reform.prl.services;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ProceedingDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OtherProceedingsService {
    public void populateCaseDocumentsData(CaseData caseData, Map<String, Object> caseDataUpdated) {
        if (caseData.getExistingProceedings() != null) {
            List<Element<ProceedingDetails>> filteredExistingProceedings = caseData.getExistingProceedings().stream().filter(eachP -> eachP.getValue()
                    .getUploadRelevantOrder() != null).collect(Collectors.toList());
            if (!filteredExistingProceedings.isEmpty()) {
                caseDataUpdated.put("existingProceedingsWithDoc", filteredExistingProceedings);
            }
        }

    }
}
