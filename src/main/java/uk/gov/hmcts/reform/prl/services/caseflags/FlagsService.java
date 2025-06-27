package uk.gov.hmcts.reform.prl.services.caseflags;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.CaseNoteDetails;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.caseflags.flagdetails.FlagDetail;
import uk.gov.hmcts.reform.prl.models.wa.WaMapper;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_NOTES;

@Slf4j
@RequiredArgsConstructor
@Service
public class FlagsService {

    public static final String SELECTED_REVIEW_LANG_AND_SM_REQ = "selectedReviewLangAndSmReq";
    public static final String IS_REVIEW_LANG_AND_SM_REQ_REVIEWED = "isReviewLangAndSmReqReviewed";
    public static final String REQUESTED = "Requested";
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
        caseDataMap.put(
            IS_REVIEW_LANG_AND_SM_REQ_REVIEWED,
            null);
    }

    public List<String> validateNewFlagStatus(Map<String, Object> caseDataCurrent) {
        List<String> errors = new ArrayList<>();

        getAllFlagsToValidate().stream()
            .map(flag -> objectMapper.convertValue(
                caseDataCurrent.get(flag), new TypeReference<Flags>() {
                }
            ))
            .filter(Objects::nonNull)
            .map(Flags::getDetails)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .map(Element::getValue)
            .max(comparing(FlagDetail::getDateTimeCreated))
            .filter(flagDetail -> REQUESTED.equals(flagDetail.status))
            .ifPresent(flagDetail -> errors.add(REQUESTED_STATUS_IS_NOT_ALLOWED));

        return errors;
    }



    private static List<String> getAllFlagsToValidate() {
        return asList(
            "caseFlags",
            "caApplicant1InternalFlags",
            "caApplicantSolicitor1InternalFlags",
            "caApplicant2InternalFlags",
            "caApplicantSolicitor2InternalFlags",
            "caApplicant3InternalFlags",
            "caApplicantSolicitor3InternalFlags",
            "caApplicant4InternalFlags",
            "caApplicantSolicitor4InternalFlags",
            "caApplicant5InternalFlags",
            "caApplicantSolicitor5InternalFlags",
            "caRespondent1InternalFlags",
            "caRespondentSolicitor1InternalFlags",
            "caRespondent2InternalFlags",
            "caRespondentSolicitor2InternalFlags",
            "caRespondent3InternalFlags",
            "caRespondentSolicitor3InternalFlags",
            "caRespondent4InternalFlags",
            "caRespondentSolicitor4InternalFlags",
            "caRespondent5InternalFlags",
            "caRespondentSolicitor5InternalFlags",
            "caOtherParty1InternalFlags",
            "caOtherParty2InternalFlags",
            "caOtherParty3InternalFlags",
            "caOtherParty4InternalFlags",
            "caOtherParty5InternalFlags",
            "caApplicant1ExternalFlags",
            "caApplicantSolicitor1ExternalFlags",
            "caApplicant2ExternalFlags",
            "caApplicantSolicitor2ExternalFlags",
            "caApplicant3ExternalFlags",
            "caApplicantSolicitor3ExternalFlags",
            "caApplicant4ExternalFlags",
            "caApplicantSolicitor4ExternalFlags",
            "caApplicant5ExternalFlags",
            "caApplicantSolicitor5ExternalFlags",
            "caRespondent1ExternalFlags",
            "caRespondentSolicitor1ExternalFlags",
            "caRespondent2ExternalFlags",
            "caRespondentSolicitor2ExternalFlags",
            "caRespondent3ExternalFlags",
            "caRespondentSolicitor3ExternalFlags",
            "caRespondent4ExternalFlags",
            "caRespondentSolicitor4ExternalFlags",
            "caRespondent5ExternalFlags",
            "caRespondentSolicitor5ExternalFlags",
            "caOtherParty1ExternalFlags",
            "caOtherParty2ExternalFlags",
            "caOtherParty3ExternalFlags",
            "caOtherParty4ExternalFlags",
            "caOtherParty5ExternalFlags",
            "daApplicantInternalFlags",
            "daApplicantSolicitorInternalFlags",
            "daRespondentInternalFlags",
            "daRespondentSolicitorInternalFlags",
            "daApplicantExternalFlags",
            "daApplicantSolicitorExternalFlags",
            "daRespondentExternalFlags",
            "daRespondentSolicitorExternalFlags"
        );
    }
}
