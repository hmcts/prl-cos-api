package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.complextypes.DocumentsDynamicList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ServiceOfDocumentsService {

    private final SendAndReplyService sendAndReplyService;

    public Map<String, Object> aboutToStart(String authorisation,
                                            CallbackRequest callbackRequest) {

        Map<String, Object> caseDataMap = new HashMap<>();

        caseDataMap.put("sodDocumentsList", List.of(element(DocumentsDynamicList.builder()
                                                                .documentsList(sendAndReplyService.getCategoriesAndDocuments(
                                                                    authorisation,
                                                                    String.valueOf(callbackRequest.getCaseDetails().getId())
                                                                ))
                                                                .build())));
        return caseDataMap;
    }
}
