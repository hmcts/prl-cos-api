package uk.gov.hmcts.reform.prl.mapper.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildApplicantDetailsElements;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.caseflags.flagdetails.FlagDetail;
import uk.gov.hmcts.reform.prl.models.caseflags.request.C100FlagDetailRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.C100_REQUEST_SUPPORT;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReasonableAdjustmentsMapper {
    private final IdamClient idamClient;
    private final CcdCoreCaseDataService coreCaseDataService;
    @Autowired
    private final ObjectMapper objectMapper;
    private final CaseService caseService;

    public CaseData mapRAforC100MainApplicant(String applicants, CaseData caseData, String authToken) throws JsonProcessingException {
        log.info("Inside mapReasonableAdjustmentsForC100MainApplicant applicants {}", applicants);
        log.info("Inside mapReasonableAdjustmentsForC100MainApplicant caseData {}", caseData);

        if (isNotEmpty(applicants)) {
            String caseId = String.valueOf(caseData.getId());
            C100RebuildApplicantDetailsElements applicantDetails = new ObjectMapper()
                .readValue(
                    applicants,
                    C100RebuildApplicantDetailsElements.class
                );

            log.info("Inside mapReasonableAdjustmentsForC100MainApplicant applicantDetails {}", applicantDetails);

            if (CollectionUtils.isNotEmpty(applicantDetails.getApplicants()) && CollectionUtils.isNotEmpty(
                applicantDetails.getApplicants().get(0).getReasonableAdjustmentsFlags())) {
                log.info(
                    "Inside mapReasonableAdjustmentsForC100MainApplicant RAFlags {}",
                    applicantDetails.getApplicants().get(0).getReasonableAdjustmentsFlags()
                );
                UserDetails userDetails = idamClient.getUserDetails(authToken);
                CaseEvent caseEvent = CaseEvent.fromValue(String.valueOf(C100_REQUEST_SUPPORT));
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

                Map<String, Object> updatedCaseData = startEventResponse.getCaseDetails().getData();
                Optional<String> partyExternalCaseFlagField = caseService.getPartyExternalCaseFlagField(
                    caseData.getCaseTypeOfApplication(),
                    PartyEnum.applicant,
                    0
                );

                if (partyExternalCaseFlagField.isPresent()) {
                    try {
                        log.info("partyExternalCaseFlagField ===>" + objectMapper.writeValueAsString(
                            partyExternalCaseFlagField.get()));
                    } catch (JsonProcessingException e) {
                        log.info("error");
                    }

                    Flags flags = objectMapper.convertValue(
                        updatedCaseData.get(partyExternalCaseFlagField.get()),
                        Flags.class
                    );
                    try {
                        log.info("Existing external Party flags  ===>" + objectMapper.writeValueAsString(flags));
                    } catch (JsonProcessingException e) {
                        log.info("error");
                    }
                    List<Element<FlagDetail>> flagDetails = new ArrayList<>();
                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(
                        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                        Locale.ENGLISH
                    );
                    for (C100FlagDetailRequest flag : applicantDetails.getApplicants().get(0).getReasonableAdjustmentsFlags()) {
                        List<Element<String>> path = new ArrayList<>();
                        for (String pathDetail : flag.getPath()) {
                            path.add(element(pathDetail));
                        }

                        FlagDetail flagDetail = FlagDetail.builder().name(flag.getName()).name_cy(flag.getName_cy())
                            .subTypeValue(flag.getSubTypeValue())
                            .subTypeValue_cy(flag.getSubTypeValue_cy())
                            .subTypeKey(flag.getSubTypeKey())
                            .otherDescription(flag.getOtherDescription())
                            .otherDescription_cy(flag.getOtherDescription_cy())
                            .flagComment(flag.getFlagComment())
                            .flagComment_cy(flag.getFlagComment_cy())
                            .flagUpdateComment(flag.getFlagUpdateComment())
                            .dateTimeCreated(LocalDateTime.parse(flag.getDateTimeCreated(), dateTimeFormatter))
                            .dateTimeModified(null != flag.getDateTimeModified() ? LocalDateTime.parse(
                                flag.getDateTimeModified(),
                                dateTimeFormatter
                            ) : null)
                            .path(path)
                            .hearingRelevant(flag.getHearingRelevant())
                            .flagCode(flag.getFlagCode())
                            .status(flag.getStatus())
                            .availableExternally(flag.getAvailableExternally())
                            .build();
                        flagDetails.add(element(flagDetail));
                    }

                    flags = flags.toBuilder()
                        .details(flagDetails)
                        .build();
                    try {
                        log.info("Updated external Party flags  ===>" + objectMapper.writeValueAsString(flags));
                    } catch (JsonProcessingException e) {
                        log.info("error");
                    }
                    Map<String, Object> externalCaseFlagMap = new HashMap<>();
                    externalCaseFlagMap.put(partyExternalCaseFlagField.get(), flags);

                    CaseDataContent caseDataContent = coreCaseDataService.createCaseDataContent(
                        startEventResponse,
                        externalCaseFlagMap
                    );
                    try {
                        log.info("Case data content is  ===>" + objectMapper.writeValueAsString(caseDataContent));
                    } catch (JsonProcessingException e) {
                        log.info("error");
                    }

                    CaseDetails updatedCaseDetails = coreCaseDataService.submitUpdate(
                        authToken,
                        eventRequestData,
                        caseDataContent,
                        caseId,
                        false
                    );
                    try {
                        log.info("updatedCaseDetails is  ===>" + objectMapper.writeValueAsString(updatedCaseDetails));
                    } catch (JsonProcessingException e) {
                        log.info("error");
                    }

                    return CaseUtils.getCaseData(updatedCaseDetails, objectMapper);
                }
                return caseData;
            }
        }

        return caseData;
    }
}
