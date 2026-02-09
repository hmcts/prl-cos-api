package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AwaitingInformation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "feature.toggle", name = "awaitingInformationEnabled", havingValue = "true")
public class AwaitingInformationService {

    public List<String> validateAwaitingInformation(AwaitingInformation awaitingInformation) {
        List<String> errorList = new ArrayList<>();
        if (awaitingInformation.getReviewDate() != null && !awaitingInformation.getReviewDate().isAfter(LocalDate.now())) {
            errorList.add("The date must be in the future");
        }
        return errorList;
    }
}
