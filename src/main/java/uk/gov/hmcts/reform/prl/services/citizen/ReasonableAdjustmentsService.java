package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.caseflags.PartyRole;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.caseflags.flagdetails.FlagDetail;
import uk.gov.hmcts.reform.prl.models.caseflags.request.CitizenPartyFlagsCreateRequest;
import uk.gov.hmcts.reform.prl.models.caseflags.request.CitizenPartyFlagsManageRequest;
import uk.gov.hmcts.reform.prl.models.caseflags.request.FlagDetailRequest;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetailsMeta;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.repositories.CaseRepository;
import uk.gov.hmcts.reform.prl.services.caseflags.PartyLevelCaseFlagsService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.getPartyDetailsMeta;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReasonableAdjustmentsService {
    public static final String PARTY_DETAILS_NOT_FOUND_ERROR_MSG = "Requested party details unavailable in the case";
    public static final String PARTY_EXTERNAL_FLAG_DETAILS_NOT_FOUND_ERROR_MSG = "Requested party external flag not available in the case";
    public static final String PARTY_FLAGS_UPDATED_SUCCESS_MSG = "Requested party support request updated successfully";
    public static final String INVALID_REQUEST_ERROR_MSG = "Invalid request";

    private final CaseRepository caseRepository;
    private final IdamClient idamClient;
    private final ObjectMapper objectMapper;
    private final CcdCoreCaseDataService coreCaseDataService;
    private final PartyLevelCaseFlagsService partyLevelCaseFlagsService;

    public Flags getPartyCaseFlags(String authToken, String caseId, String partyId) {
        CaseDetails caseDetails = caseRepository.getCase(authToken, caseId);
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        Optional<PartyDetailsMeta> partyDetailsMeta = getPartyDetailsMeta(
            partyId,
            caseData.getCaseTypeOfApplication(),
            caseData
        );

        if (partyDetailsMeta.isPresent()
            && partyDetailsMeta.get().getPartyDetails() != null
            && !StringUtils.isEmpty(partyDetailsMeta.get().getPartyDetails().getLabelForDynamicList())) {
            Optional<String> partyExternalCaseFlagField = getPartyExternalCaseFlagField(
                caseData.getCaseTypeOfApplication(),
                partyDetailsMeta.get().getPartyType(),
                partyDetailsMeta.get().getPartyIndex()
            );

            if (partyExternalCaseFlagField.isPresent()) {
                return objectMapper.convertValue(
                    caseDetails.getData().get(partyExternalCaseFlagField.get()),
                    Flags.class
                );
            }
        }

        return null;
    }

    public ResponseEntity<Object> createCitizenReasonableAdjustmentsFlags(
        String caseId, String eventId, String authToken, CitizenPartyFlagsCreateRequest citizenPartyFlagsCreateRequest) {
        log.info("Inside createCitizenReasonableAdjustmentsFlags caseId {}", caseId);
        log.info("Inside createCitizenReasonableAdjustmentsFlags eventId {}", eventId);
        log.info(
            "Inside createCitizenReasonableAdjustmentsFlags citizenPartyFlagsRequest {}",
            citizenPartyFlagsCreateRequest
        );

        if (StringUtils.isEmpty(citizenPartyFlagsCreateRequest.getPartyIdamId()) || ObjectUtils.isEmpty(
            citizenPartyFlagsCreateRequest.getPartyExternalFlags())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(INVALID_REQUEST_ERROR_MSG);
        }

        UserDetails userDetails = idamClient.getUserDetails(authToken);
        CaseEvent caseEvent = CaseEvent.fromValue(eventId);
        EventRequestData eventRequestData = coreCaseDataService.eventRequest(
            caseEvent,
            userDetails.getId()
        );

        StartEventResponse startEventResponse =
            coreCaseDataService.startUpdate(
                authToken,
                eventRequestData,
                caseId,
                false
            );

        CaseData caseData = CaseUtils.getCaseData(startEventResponse.getCaseDetails(), objectMapper);
        Optional<PartyDetailsMeta> partyDetailsMeta = getPartyDetailsMeta(
            citizenPartyFlagsCreateRequest.getPartyIdamId(),
            caseData.getCaseTypeOfApplication(),
            caseData
        );

        if (!partyDetailsMeta.isPresent()
            || null == partyDetailsMeta.get().getPartyDetails()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(PARTY_DETAILS_NOT_FOUND_ERROR_MSG);
        }

        Optional<String> partyExternalCaseFlagField = getPartyExternalCaseFlagField(
            caseData.getCaseTypeOfApplication(),
            partyDetailsMeta.get().getPartyType(),
            partyDetailsMeta.get().getPartyIndex()
        );

        Map<String, Object> caseDataMap = startEventResponse.getCaseDetails().getData();
        if (!partyExternalCaseFlagField.isPresent()
            || !caseDataMap.containsKey(partyExternalCaseFlagField.get())
            || ObjectUtils.isEmpty(caseDataMap.get(partyExternalCaseFlagField.get()))) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(PARTY_EXTERNAL_FLAG_DETAILS_NOT_FOUND_ERROR_MSG);
        }

        try {
            log.info("partyExternalCaseFlagField ===>" + objectMapper.writeValueAsString(partyExternalCaseFlagField.get()));
        } catch (JsonProcessingException e) {
            log.info("error");
        }

        Flags flags = objectMapper.convertValue(
            caseDataMap.get(partyExternalCaseFlagField.get()),
            Flags.class
        );
        try {
            log.info("Existing external Party flags  ===>" + objectMapper.writeValueAsString(flags));
        } catch (JsonProcessingException e) {
            log.info("error");
        }
        flags.getDetails().addAll(convertCreateFlagsRequest(citizenPartyFlagsCreateRequest.getPartyExternalFlags().getDetails()));

        try {
            log.info("Updated external Party flags  ===>" + objectMapper.writeValueAsString(flags));
        } catch (JsonProcessingException e) {
            log.info("error");
        }
        Map<String, Object> updatedCaseDataMap = new HashMap<>();
        updatedCaseDataMap.put(partyExternalCaseFlagField.get(), flags);

        CaseDataContent caseDataContent = coreCaseDataService.createCaseDataContent(
            startEventResponse,
            updatedCaseDataMap
        );

        try {
            log.info("Case data content is  ===>" + objectMapper.writeValueAsString(caseDataContent));
        } catch (JsonProcessingException e) {
            log.info("error");
        }

        coreCaseDataService.submitUpdate(
            authToken,
            eventRequestData,
            caseDataContent,
            caseId,
            false
        );
        return ResponseEntity.status(HttpStatus.OK).body(PARTY_FLAGS_UPDATED_SUCCESS_MSG);
    }

    public ResponseEntity<Object> manageCitizenReasonableAdjustmentsFlags(
        String caseId, String eventId, String authToken, CitizenPartyFlagsManageRequest citizenPartyFlagsManageRequest) {
        log.info("Inside createCitizenReasonableAdjustmentsFlags caseId {}", caseId);
        log.info("Inside createCitizenReasonableAdjustmentsFlags eventId {}", eventId);
        log.info(
            "Inside createCitizenReasonableAdjustmentsFlags citizenPartyFlagsRequest {}",
            citizenPartyFlagsManageRequest
        );

        if (StringUtils.isEmpty(citizenPartyFlagsManageRequest.getPartyIdamId()) || ObjectUtils.isEmpty(
            citizenPartyFlagsManageRequest.getPartyExternalFlags())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("bad request");
        }

        UserDetails userDetails = idamClient.getUserDetails(authToken);
        CaseEvent caseEvent = CaseEvent.fromValue(eventId);
        EventRequestData eventRequestData = coreCaseDataService.eventRequest(
            caseEvent,
            userDetails.getId()
        );

        StartEventResponse startEventResponse =
            coreCaseDataService.startUpdate(
                authToken,
                eventRequestData,
                caseId,
                false
            );

        CaseData caseData = CaseUtils.getCaseData(startEventResponse.getCaseDetails(), objectMapper);
        Optional<PartyDetailsMeta> partyDetailsMeta = getPartyDetailsMeta(
            citizenPartyFlagsManageRequest.getPartyIdamId(),
            caseData.getCaseTypeOfApplication(),
            caseData
        );

        if (!partyDetailsMeta.isPresent()
            || null == partyDetailsMeta.get().getPartyDetails()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(PARTY_DETAILS_NOT_FOUND_ERROR_MSG);
        }

        Optional<String> partyExternalCaseFlagField = getPartyExternalCaseFlagField(
            caseData.getCaseTypeOfApplication(),
            partyDetailsMeta.get().getPartyType(),
            partyDetailsMeta.get().getPartyIndex()
        );

        Map<String, Object> caseDataMap = startEventResponse.getCaseDetails().getData();
        if (!partyExternalCaseFlagField.isPresent()
            || !caseDataMap.containsKey(partyExternalCaseFlagField.get())
            || ObjectUtils.isEmpty(caseDataMap.get(partyExternalCaseFlagField.get()))) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(PARTY_EXTERNAL_FLAG_DETAILS_NOT_FOUND_ERROR_MSG);
        }

        try {
            log.info("partyExternalCaseFlagField ===>" + objectMapper.writeValueAsString(partyExternalCaseFlagField.get()));
        } catch (JsonProcessingException e) {
            log.info("error");
        }

        Flags flags = objectMapper.convertValue(
            caseDataMap.get(partyExternalCaseFlagField.get()),
            Flags.class
        );
        try {
            log.info("Existing external Party flags  ===>" + objectMapper.writeValueAsString(flags));
        } catch (JsonProcessingException e) {
            log.info("error");
        }

        flags = flags.toBuilder()
            .details(convertManageFlagRequest(flags, citizenPartyFlagsManageRequest.getPartyExternalFlags().getDetails()))
            .build();

        try {
            log.info("Updated external Party flags  ===>" + objectMapper.writeValueAsString(flags));
        } catch (JsonProcessingException e) {
            log.info("error");
        }
        Map<String, Object> updatedCaseDataMap = new HashMap<>();
        updatedCaseDataMap.put(partyExternalCaseFlagField.get(), flags);

        CaseDataContent caseDataContent = coreCaseDataService.createCaseDataContent(
            startEventResponse,
            updatedCaseDataMap
        );

        try {
            log.info("Case data content is  ===>" + objectMapper.writeValueAsString(caseDataContent));
        } catch (JsonProcessingException e) {
            log.info("error");
        }

        coreCaseDataService.submitUpdate(
            authToken,
            eventRequestData,
            caseDataContent,
            caseId,
            false
        );
        return ResponseEntity.status(HttpStatus.OK).body(PARTY_FLAGS_UPDATED_SUCCESS_MSG);
    }

    private List<Element<FlagDetail>> convertCreateFlagsRequest(List<FlagDetailRequest> details) {
        List<Element<FlagDetail>> flagDetails = new ArrayList<>();

        for (FlagDetailRequest detail : details) {
            FlagDetail flagDetail = FlagDetail.builder().name(detail.getName())
                .name_cy(detail.getName_cy())
                .subTypeValue(detail.getSubTypeValue())
                .subTypeValue_cy(detail.getSubTypeValue_cy())
                .subTypeKey(detail.getSubTypeKey())
                .otherDescription(detail.getOtherDescription())
                .otherDescription_cy(detail.getOtherDescription_cy())
                .flagComment(detail.getFlagComment())
                .flagComment_cy(detail.getFlagComment_cy())
                .flagUpdateComment(detail.getFlagUpdateComment())
                .dateTimeCreated(detail.getDateTimeCreated())
                .dateTimeModified(detail.getDateTimeModified())
                .path(detail.getPath())
                .hearingRelevant(detail.getHearingRelevant())
                .flagCode(detail.getFlagCode())
                .status(detail.getStatus())
                .availableExternally(detail.getAvailableExternally())
                .build();
            flagDetails.add(element(flagDetail));
        }

        return flagDetails;
    }

    private List<Element<FlagDetail>> convertManageFlagRequest(Flags flags, List<Element<FlagDetailRequest>> details) {
        List<Element<FlagDetail>> flagDetails = flags.getDetails();

        for (Element<FlagDetail> flagDetail : flagDetails) {
            Optional<Element<FlagDetailRequest>> data
                = details.stream().filter(x -> x.getId().equals(flagDetail.getId())).findFirst();

            if (data.isPresent()) {
                FlagDetail flagData = flagDetail.getValue().toBuilder().status(data.get().getValue().getStatus())
                    .dateTimeModified(data.get().getValue().getDateTimeModified())
                    .build();

                int index = flagDetails.indexOf(flagDetail);
                flagDetails.set(index, element(flagDetail.getId(), flagData));
            }
        }

        return flagDetails;
    }

    private Optional<String> getPartyExternalCaseFlagField(String caseType, PartyEnum partyType, Integer partyIndex) {

        Optional<String> partyExternalCaseFlagField = Optional.empty();
        boolean isC100Case = C100_CASE_TYPE.equalsIgnoreCase(caseType);

        if (PartyEnum.applicant == partyType) {
            partyExternalCaseFlagField
                = Optional.ofNullable(partyLevelCaseFlagsService.getPartyCaseDataExternalField(
                caseType,
                isC100Case ? PartyRole.Representing.CAAPPLICANT : PartyRole.Representing.DAAPPLICANT,
                partyIndex
            ));
        } else if (PartyEnum.respondent == partyType) {
            partyExternalCaseFlagField
                = Optional.ofNullable(partyLevelCaseFlagsService.getPartyCaseDataExternalField(
                caseType,
                isC100Case ? PartyRole.Representing.CARESPONDENT : PartyRole.Representing.DARESPONDENT,
                partyIndex
            ));
        }

        return partyExternalCaseFlagField;
    }
}
