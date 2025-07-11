package uk.gov.hmcts.reform.prl.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.wa.WaMapper;

import java.util.function.Predicate;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

@RequiredArgsConstructor
@Component
public class TaskUtils {
    private final ObjectMapper objectMapper;

    public String setTaskCompletion(
        String clientContext,
        CaseData caseData,
        Predicate<CaseData> completeTask) {

        return ofNullable(clientContext)
            .map(value -> CaseUtils.getWaMapper(clientContext))
            .map(WaMapper::getClientContext)
            .filter(value -> nonNull(value.getUserTask()))
            .map(value ->
                     value.toBuilder()
                         .userTask(value.getUserTask().toBuilder()
                                       .completeTask(completeTask.test(caseData))
                                       .build())
                         .build())
            .map(
                updatedClientContext ->
                    CaseUtils.base64Encode(WaMapper.builder()
                                     .clientContext(updatedClientContext)
                                     .build(),
                                 objectMapper)
            )
            .orElse(null);
    }
}
