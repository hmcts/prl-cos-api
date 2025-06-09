package uk.gov.hmcts.reform.prl.services.caseflags;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.CaseNoteDetails;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.caseflags.flagdetails.FlagDetail;
import uk.gov.hmcts.reform.prl.models.wa.WaMapper;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_NOTES;

@Slf4j
@RequiredArgsConstructor
@Service
public class CaseFlagsService {

    public static final String SELECTED_REVIEW_LANG_AND_SM_REQ = "selectedReviewLangAndSmReq";
    public static final String IS_REVIEW_LANG_AND_SM_REQ_REVIEWED = "isReviewLangAndSmReqReviewed";
    public static final String PLEASE_REVIEW_THE_LANGUAGE_AND_SM_REQUEST = "Please review the Language and SM Request";
    public static final String REQUESTED = "Requested";
    public static final String CASE_FLAGS = "caseFlags";
    public static final String REQUESTED_STATUS_IS_NOT_ALLOWED = "Requested status is not allowed";
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
                               SELECTED_REVIEW_LANG_AND_SM_REQ,
                               element.getValue()
                           ));
    }

    public List<String> isLangAndSmReqReviewed(Map<String, Object> caseDataMap) {
        List<String> errors = new ArrayList<>();
        YesOrNo yesOrNo = Optional.ofNullable(caseDataMap.get(IS_REVIEW_LANG_AND_SM_REQ_REVIEWED))
            .filter(Objects::nonNull)
            .map(object -> objectMapper.convertValue(
                caseDataMap.get(IS_REVIEW_LANG_AND_SM_REQ_REVIEWED),
                new TypeReference<YesOrNo>() {
                }
            ))
            .filter(value -> value.equals(YesOrNo.Yes))
            .orElse(YesOrNo.No);

        if (yesOrNo.equals(YesOrNo.No)) {
            errors.add(PLEASE_REVIEW_THE_LANGUAGE_AND_SM_REQUEST);
        }
        return errors;
    }


    public List<String> validateNewCaseFlagStatus(Map<String, Object> caseDataBefore,
                                                  Map<String, Object> caseDataCurrent) {
        List<String> errors = new ArrayList<>();
        log.info("validateNewCaseFlagStatus");
        Flags caseFlagsBefore = objectMapper.convertValue(
            caseDataBefore.get(CASE_FLAGS), new TypeReference<>() {
            }
        );

        List<Element<FlagDetail>> flagDetailsBefore = Optional.ofNullable(caseFlagsBefore)
            .map(Flags::getDetails)
            .orElse(Collections.emptyList());

        Flags caseFlagsCurrent = objectMapper.convertValue(
            caseDataCurrent.get(CASE_FLAGS), new TypeReference<>() {
            }
        );
        log.info("caseFlagsBefore {} caseFlagsCurrent {}", flagDetailsBefore, caseFlagsCurrent);
        caseFlagsCurrent.getDetails()
            .stream()
            .filter(not(flagDetailsBefore::contains))
            .map(Element::getValue)
            .peek(flagDetail -> log.info("flagDetail : {}", flagDetail))
            .filter(flagDetail -> REQUESTED.equals(flagDetail.status))
            .findFirst()
            .ifPresent(flagDetail -> errors.add(REQUESTED_STATUS_IS_NOT_ALLOWED));
        log.info("errors {}", errors);
        return errors;
    }
}
