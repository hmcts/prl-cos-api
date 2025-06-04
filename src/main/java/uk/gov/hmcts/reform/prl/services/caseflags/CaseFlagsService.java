package uk.gov.hmcts.reform.prl.services.caseflags;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.CaseNoteDetails;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.wa.WaMapper;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_NOTES;

@RequiredArgsConstructor
@Service
public class CaseFlagsService {

    public static final String ADD_CASE_NOTE_TYPE = "addCaseNoteType";
    public static final String IS_REVIEW_LANG_AND_SM_REQ_REVIEWED = "isReviewLangAndSmReqReviewed";
    private final ObjectMapper objectMapper;

    public void prepareSelectedReviewLangAndSmReq(Map<String, Object> caseDataMap, String clientContext) {
        WaMapper waMapper = CaseUtils.getWaMapper(clientContext);
        UUID uuid = UUID.fromString(waMapper.getClientContext().getUserTask().getTaskData().getAdditionalProperties().getCaseNoteId());

        List<Element<CaseNoteDetails>> caseNoteDetails = objectMapper.convertValue(
            caseDataMap.get(CASE_NOTES),
            new TypeReference<>() {
            }
        );

        caseNoteDetails.stream()
            .filter(caseNoteDetailsElement -> uuid.equals(caseNoteDetailsElement.getId()))
            .findFirst()
            .ifPresent(element ->
                           caseDataMap.put(
                               ADD_CASE_NOTE_TYPE,
                               element.getValue()
                           ));

        caseDataMap.put(
            IS_REVIEW_LANG_AND_SM_REQ_REVIEWED,
            YesOrNo.No);
    }
}
