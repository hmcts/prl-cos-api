package uk.gov.hmcts.reform.prl.services;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AwaitingInformation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Builder
@RequiredArgsConstructor
@Service
public class AwaitingInformationService {

    private final FeatureToggleService featureToggleService;

    public List<String> validateAwaitingInformation(AwaitingInformation awaitingInformation) {
        List<String> errorList = new ArrayList<>();
        if (featureToggleService.isAwaitingInformationEnabled() && awaitingInformation.getReviewDate()
            != null && !awaitingInformation.getReviewDate().isAfter(LocalDate.now())) {
            errorList.add("The date must be in the future");
        }
        return errorList;
    }
}
