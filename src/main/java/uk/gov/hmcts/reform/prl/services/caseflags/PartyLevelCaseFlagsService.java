package uk.gov.hmcts.reform.prl.services.caseflags;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseCreatedBy;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.caseflags.PartyRole;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.caseflags.PartyLevelCaseFlagsGenerator;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.caseflags.PartyRole.Representing.CAAPPLICANT;
import static uk.gov.hmcts.reform.prl.utils.caseflags.PartyLevelCaseFlagsGenerator.VISIBILITY_EXTERNAL;
import static uk.gov.hmcts.reform.prl.utils.caseflags.PartyLevelCaseFlagsGenerator.VISIBILITY_INTERNAL;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
public class PartyLevelCaseFlagsService {
    private static final String AMEND_APPLICANTS_DETAILS = "amendApplicantsDetails";
    public static final String CA_APPLICANT = "CA_APPLICANT";
    private final ObjectMapper objectMapper;
    public final PartyLevelCaseFlagsGenerator partyLevelCaseFlagsGenerator;
    private final SystemUserService systemUserService;
    private final CcdCoreCaseDataService coreCaseDataService;

    public CaseDetails generateAndStoreCaseFlags(String caseId) {
        String systemAuthorisation = systemUserService.getSysUserToken();
        String systemUpdateUserId = systemUserService.getUserId(systemAuthorisation);

        EventRequestData eventRequestData = coreCaseDataService.eventRequest(
            CaseEvent.UPDATE_ALL_TABS,
            systemUpdateUserId
        );
        StartEventResponse startEventResponse =
            coreCaseDataService.startUpdate(
                systemAuthorisation,
                eventRequestData,
                caseId,
                true
            );
        CaseData startEventResponseData = CaseUtils.getCaseData(startEventResponse.getCaseDetails(), objectMapper);
        Map<String, Object> raPartyFlags = generatePartyCaseFlags(startEventResponseData);
        CaseDataContent caseDataContent;
        caseDataContent = coreCaseDataService.createCaseDataContent(
            startEventResponse,
            raPartyFlags
        );
        return coreCaseDataService.submitUpdate(
            systemAuthorisation,
            eventRequestData,
            caseDataContent,
            caseId,
            true
        );
    }

    public Map<String, Object> generatePartyCaseFlags(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            data.putAll(generateC100PartyCaseFlags(caseData, CAAPPLICANT));
            data.putAll(generateC100PartyCaseFlags(caseData, PartyRole.Representing.CARESPONDENT));
            data.putAll(generateC100PartyCaseFlags(caseData, PartyRole.Representing.CAOTHERPARTY));
            if (!CaseCreatedBy.CITIZEN.equals(caseData.getCaseCreatedBy())) {
                data.putAll(generateC100PartyCaseFlags(caseData, PartyRole.Representing.CAAPPLICANTSOLICITOR));
                data.putAll(generateC100PartyCaseFlags(caseData, PartyRole.Representing.CARESPONDENTSOLICITOR));
            }
        } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            data.putAll(generateFl401PartyCaseFlags(caseData, PartyRole.Representing.DAAPPLICANT));
            data.putAll(generateFl401PartyCaseFlags(caseData, PartyRole.Representing.DAAPPLICANTSOLICITOR));
            data.putAll(generateFl401PartyCaseFlags(caseData, PartyRole.Representing.DARESPONDENT));
            data.putAll(generateFl401PartyCaseFlags(caseData, PartyRole.Representing.DARESPONDENTSOLICITOR));
        }
        return data;
    }

    public Map<String, Object> generateC100PartyCaseFlags(CaseData caseData, PartyRole.Representing representing) {
        Map<String, Object> data = new HashMap<>();
        List<Element<PartyDetails>> caElements = representing.getCaTarget().apply(caseData);
        int numElements = null != caElements ? caElements.size() : 0;
        List<PartyRole> partyRoles = PartyRole.matchingRoles(representing);
        for (int i = 0; i < partyRoles.size(); i++) {
            PartyRole partyRole = partyRoles.get(i);
            if (null != caElements) {
                Optional<Element<PartyDetails>> partyDetails = i < numElements ? Optional.of(caElements.get(i)) : Optional.empty();
                findAndGeneratePartyFlags(representing, i, partyDetails, data, partyRole);
            }
        }
        return data;
    }

    private void findAndGeneratePartyFlags(PartyRole.Representing representing,
                                           int partyIndex,
                                           Optional<Element<PartyDetails>> partyDetails,
                                           Map<String, Object> data,
                                           PartyRole partyRole) {
        String caseDataExternalField = String.format(representing.getCaseDataExternalField(), partyIndex + 1);
        String caseDataInternalField = String.format(representing.getCaseDataInternalField(), partyIndex + 1);
        String groupId = String.format(representing.getGroupId(), partyIndex + 1);
        switch (representing) {
            case CAAPPLICANT, CARESPONDENT, CAOTHERPARTY: {
                if (partyDetails.isPresent()
                    && !StringUtils.isEmpty(partyDetails.get().getValue().getLabelForDynamicList())) {
                    data.put(
                        caseDataExternalField,
                        partyLevelCaseFlagsGenerator.generateExternalPartyFlags(
                            partyDetails.get().getValue().getLabelForDynamicList(),
                            partyRole.getCaseRoleLabel(),
                            groupId
                        )
                    );
                    data.put(
                        caseDataInternalField,
                        partyLevelCaseFlagsGenerator.generateInternalPartyFlags(
                            partyDetails.get().getValue().getLabelForDynamicList(),
                            partyRole.getCaseRoleLabel(),
                            groupId
                        )
                    );
                } else {
                    data.put(
                        caseDataExternalField,
                        Optional.empty()
                    );
                    data.put(
                        caseDataInternalField,
                        Optional.empty()
                    );
                }
                break;
            }
            case CAAPPLICANTSOLICITOR, CARESPONDENTSOLICITOR: {
                if (partyDetails.isPresent()
                    && !StringUtils.isEmpty(partyDetails.get().getValue().getRepresentativeFullNameForCaseFlags())) {
                    data.put(
                        caseDataExternalField,
                        partyLevelCaseFlagsGenerator.generateExternalPartyFlags(
                            partyDetails.get().getValue().getRepresentativeFullNameForCaseFlags(),
                            partyRole.getCaseRoleLabel(),
                            groupId
                        )
                    );
                    data.put(
                        caseDataInternalField,
                        partyLevelCaseFlagsGenerator.generateInternalPartyFlags(
                            partyDetails.get().getValue().getRepresentativeFullNameForCaseFlags(),
                            partyRole.getCaseRoleLabel(),
                            groupId
                        )
                    );
                } else {
                    data.put(
                        caseDataExternalField,
                        Optional.empty()
                    );
                    data.put(
                        caseDataInternalField,
                        Optional.empty()
                    );
                }
                break;
            }
            default: {
                break;
            }
        }
    }

    public Map<String, Object> generateFl401PartyCaseFlags(CaseData caseData, PartyRole.Representing representing) {
        Map<String, Object> data = new HashMap<>();
        PartyDetails partyDetails = representing.getDaTarget().apply(caseData);

        List<PartyRole> partyRoles = PartyRole.matchingRoles(representing);
        for (int i = 0; i < partyRoles.size(); i++) {
            PartyRole partyRole = partyRoles.get(i);
            if (null != partyDetails) {
                String caseDataExternalField = String.format(representing.getCaseDataExternalField(), i + 1);
                String caseDataInternalField = String.format(representing.getCaseDataInternalField(), i + 1);
                String groupId = String.format(representing.getGroupId(), i + 1);
                switch (representing) {
                    case DAAPPLICANT, DARESPONDENT: {
                        if (!StringUtils.isEmpty(partyDetails.getLabelForDynamicList())) {
                            data.put(
                                caseDataExternalField,
                                partyLevelCaseFlagsGenerator.generateExternalPartyFlags(
                                    partyDetails.getLabelForDynamicList(),
                                    partyRole.getCaseRoleLabel(),
                                    groupId
                                )
                            );
                            data.put(
                                caseDataInternalField,
                                partyLevelCaseFlagsGenerator.generateInternalPartyFlags(
                                    partyDetails.getLabelForDynamicList(),
                                    partyRole.getCaseRoleLabel(),
                                    groupId
                                )
                            );
                        }
                        break;
                    }
                    case DAAPPLICANTSOLICITOR, DARESPONDENTSOLICITOR: {
                        if (!StringUtils.isEmpty(partyDetails.getRepresentativeFullNameForCaseFlags())) {
                            data.put(
                                caseDataExternalField,
                                partyLevelCaseFlagsGenerator.generateExternalPartyFlags(
                                    partyDetails.getRepresentativeFullNameForCaseFlags(),
                                    partyRole.getCaseRoleLabel(),
                                    groupId
                                )
                            );
                            data.put(
                                caseDataInternalField,
                                partyLevelCaseFlagsGenerator.generateInternalPartyFlags(
                                    partyDetails.getRepresentativeFullNameForCaseFlags(),
                                    partyRole.getCaseRoleLabel(),
                                    groupId
                                )
                            );
                        }
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
        }
        return data;
    }

    public CaseData generateIndividualPartySolicitorCaseFlags(CaseData caseData,
                                                              int partyIndex,
                                                              PartyRole.Representing representing,
                                                              boolean solicitorRepresented) {
        switch (representing) {
            case CAAPPLICANTSOLICITOR, CARESPONDENTSOLICITOR: {
                List<Element<PartyDetails>> caElements = representing.getCaTarget().apply(caseData);
                Optional<Element<PartyDetails>> partyDetails = Optional.ofNullable(caElements.get(partyIndex));
                if (partyDetails.isPresent()) {
                    caseData = regenerateSolicitorFlags(
                        caseData,
                        partyDetails.get().getValue(),
                        representing,
                        partyIndex,
                        solicitorRepresented
                    );
                }
                break;
            }
            case DAAPPLICANTSOLICITOR, DARESPONDENTSOLICITOR: {
                Optional<PartyDetails> partyDetails = Optional.ofNullable(representing.getDaTarget().apply(caseData));
                if (partyDetails.isPresent()) {
                    caseData = regenerateSolicitorFlags(
                        caseData,
                        partyDetails.get(),
                        representing,
                        partyIndex,
                        solicitorRepresented
                    );
                }
                break;
            }
            default: {
                break;
            }
        }
        return caseData;
    }

    private CaseData regenerateSolicitorFlags(CaseData caseData,
                                              PartyDetails partyDetails,
                                              PartyRole.Representing representing,
                                              int partyIndex,
                                              boolean solicitorRepresented) {
        String caseDataExternalField = String.format(representing.getCaseDataExternalField(), partyIndex + 1);
        String caseDataInternalField = String.format(representing.getCaseDataInternalField(), partyIndex + 1);
        String groupId = String.format(representing.getGroupId(), partyIndex + 1);
        Optional<PartyRole> partyRole = PartyRole.fromRepresentingAndIndex(
            representing,
            partyIndex + 1
        );
        if (partyRole.isPresent()) {
            if (!StringUtils.isEmpty(partyDetails.getRepresentativeFullNameForCaseFlags())
                && solicitorRepresented) {
                caseData = partyLevelCaseFlagsGenerator.generatePartyFlags(
                    caseData,
                    partyDetails.getRepresentativeFullNameForCaseFlags(),
                    caseDataExternalField,
                    partyRole.get().getCaseRoleLabel(),
                    false,
                    groupId
                );
                caseData = partyLevelCaseFlagsGenerator.generatePartyFlags(
                    caseData,
                    partyDetails.getRepresentativeFullNameForCaseFlags(),
                    caseDataInternalField,
                    partyRole.get().getCaseRoleLabel(),
                    true,
                    groupId
                );
            } else {
                partyLevelCaseFlagsGenerator.generatePartyFlags(
                    caseData,
                    PrlAppsConstants.EMPTY_STRING,
                    caseDataExternalField,
                    partyRole.get().getCaseRoleLabel(),
                    false,
                    groupId
                );
                partyLevelCaseFlagsGenerator.generatePartyFlags(
                    caseData,
                    PrlAppsConstants.EMPTY_STRING,
                    caseDataInternalField,
                    partyRole.get().getCaseRoleLabel(),
                    true,
                    groupId
                );
            }
        }
        return caseData;
    }

    public CaseData generateC100AllPartyCaseFlags(CaseData caseData, CaseData startEventResponseData) {
        caseData = generateC100IndividualPartyCaseFlags(
            caseData,
            startEventResponseData,
            CAAPPLICANT
        );
        caseData = generateC100IndividualPartyCaseFlags(
            caseData,
            startEventResponseData,
            PartyRole.Representing.CAAPPLICANTSOLICITOR
        );
        caseData = generateC100IndividualPartyCaseFlags(
            caseData,
            startEventResponseData,
            PartyRole.Representing.CARESPONDENT
        );
        caseData = generateC100IndividualPartyCaseFlags(
            caseData,
            startEventResponseData,
            PartyRole.Representing.CARESPONDENTSOLICITOR
        );
        caseData = generateC100IndividualPartyCaseFlags(
            caseData,
            startEventResponseData,
            PartyRole.Representing.CAOTHERPARTY
        );
        return caseData;
    }

    public CaseData generateC100IndividualPartyCaseFlags(CaseData caseData, CaseData startEventResponseData, PartyRole.Representing representing) {
        List<Element<PartyDetails>> caElements = representing.getCaTarget().apply(startEventResponseData);
        int numElements = null != caElements ? caElements.size() : 0;
        List<PartyRole> partyRoles = PartyRole.matchingRoles(representing);
        for (int i = 0; i < partyRoles.size(); i++) {
            PartyRole partyRole = partyRoles.get(i);
            if (null != caElements) {
                Optional<Element<PartyDetails>> partyDetails = i < numElements ? Optional.of(caElements.get(i)) : Optional.empty();
                if (partyDetails.isPresent()) {
                    caseData = generateC100OnlyPartyCaseFlags(caseData, representing, i, partyDetails, partyRole);
                }
            }
        }
        return caseData;
    }

    private CaseData generateC100OnlyPartyCaseFlags(CaseData caseData,
                                                    PartyRole.Representing representing,
                                                    int partyIndex,
                                                    Optional<Element<PartyDetails>> partyDetails,
                                                    PartyRole partyRole) {
        String caseDataExternalField = String.format(representing.getCaseDataExternalField(), partyIndex + 1);
        String caseDataInternalField = String.format(representing.getCaseDataInternalField(), partyIndex + 1);
        String groupId = String.format(representing.getGroupId(), partyIndex + 1);
        switch (representing) {
            case CAAPPLICANT, CARESPONDENT, CAOTHERPARTY: {
                if (partyDetails.isPresent()
                    && !StringUtils.isEmpty(partyDetails.get().getValue().getLabelForDynamicList())) {
                    caseData = partyLevelCaseFlagsGenerator.generatePartyFlags(
                        caseData,
                        partyDetails.get().getValue().getLabelForDynamicList(),
                        caseDataExternalField,
                        partyRole.getCaseRoleLabel(),
                        false,
                        groupId
                    );
                    caseData = partyLevelCaseFlagsGenerator.generatePartyFlags(
                        caseData,
                        partyDetails.get().getValue().getLabelForDynamicList(),
                        caseDataInternalField,
                        partyRole.getCaseRoleLabel(),
                        true,
                        groupId
                    );
                }
                break;
            }
            case CAAPPLICANTSOLICITOR, CARESPONDENTSOLICITOR: {
                if (partyDetails.isPresent()
                    && !StringUtils.isEmpty(partyDetails.get().getValue().getRepresentativeFullNameForCaseFlags())) {
                    caseData = partyLevelCaseFlagsGenerator.generatePartyFlags(
                        caseData,
                        partyDetails.get().getValue().getRepresentativeFullNameForCaseFlags(),
                        caseDataExternalField,
                        partyRole.getCaseRoleLabel(),
                        false,
                        groupId
                    );
                    caseData = partyLevelCaseFlagsGenerator.generatePartyFlags(
                        caseData,
                        partyDetails.get().getValue().getLabelForDynamicList(),
                        caseDataInternalField,
                        partyRole.getCaseRoleLabel(),
                        true,
                        groupId
                    );
                }
                break;
            }
            default: {
                break;
            }
        }
        return caseData;
    }

    public String getPartyCaseDataExternalField(String caseType, PartyRole.Representing representing, int partyIndex) {
        return C100_CASE_TYPE.equalsIgnoreCase(caseType) ? String.format(
            representing.getCaseDataExternalField(),
            partyIndex + 1
        ) : representing.getCaseDataExternalField();
    }

    public void amendCaseFlags(Map<String, Object> oldCaseDataMap, Map<String, Object> updatedCaseDataMap, String eventId) {
        CaseData updatedCaseData = objectMapper.convertValue(updatedCaseDataMap, CaseData.class);
        CaseData oldCaseData = objectMapper.convertValue(oldCaseDataMap, CaseData.class);
        log.info("event ID {}",eventId);
        if (StringUtils.equals(AMEND_APPLICANTS_DETAILS, eventId)) {
            if (C100_CASE_TYPE.equals(updatedCaseData.getCaseTypeOfApplication())) {
                List<Element<PartyDetails>> applicants = updatedCaseData.getApplicants();
                List<Element<PartyDetails>> oldApplicants = oldCaseData.getApplicants();
                if (CollectionUtils.isNotEmpty(applicants) && CollectionUtils.isNotEmpty(oldApplicants)) {
                    Map<String, Integer> oldApplicantIdToIndex = getPartyIdToIndexMapping(oldApplicants);
                    log.info("old Applicant To Index Map {}",oldApplicantIdToIndex);
                    Map<String, Integer> applicantIdToIndex = getPartyIdToIndexMapping(applicants);
                    log.info("new Applicant To Index Map {}",applicantIdToIndex);
                    updateCaseFlagDataIfPartiesRemoved(oldCaseDataMap, updatedCaseDataMap, oldApplicantIdToIndex, applicantIdToIndex,CA_APPLICANT);
                    refreshPartyFlags(updatedCaseDataMap, updatedCaseData.getApplicants(),CA_APPLICANT, Arrays.asList(
                        CAAPPLICANT, PartyRole.Representing.CAAPPLICANTSOLICITOR));

                }
            }
        }

    }

    private void refreshPartyFlags(Map<String, Object> updatedCaseDataMap, List<Element<PartyDetails>> parties, String partyType,
                                   List<PartyRole.Representing> representing) {

        List<String> caseFlagsToBeUpdated = Arrays.asList(
            "caApplicant%sExternalFlags",
            "caApplicantSolicitor%sExternalFlags",
            "caApplicant%sInternalFlags",
            "caApplicantSolicitor%sInternalFlags"
        );
        switch (partyType) {
            case CA_APPLICANT:
                for (int i = 0; i < 5; i++) {
                    if (i < parties.size()) {
                        Optional<Element<PartyDetails>> partyDetailsElement = Optional.ofNullable(parties.get(i));
                        int caseFlagsIndex = i + 1;

                        partyDetailsElement.ifPresent(detailsElement -> representing.forEach(representing1 -> {
                            log.info("refreshing the name for party {}", partyDetailsElement.get().getId());
                            setCaseFlagInformation(representing1,detailsElement,updatedCaseDataMap,caseFlagsIndex);
                        }));

                    } else {
                        int caseFlagsIndex = i + 1;
                        caseFlagsToBeUpdated.forEach(key -> {
                            String caseFlagKey = String.format(key, caseFlagsIndex);
                            log.info("refreshing casedata key  {}",caseFlagKey);
                            updatedCaseDataMap.put(caseFlagKey, Flags
                                .builder().build());
                        });
                    }
                }
                break;
            default:
                break;
        }
    }

    private void setCaseFlagInformation(PartyRole.Representing representing1, Element<PartyDetails> partyDetailsElement,
                                        Map<String, Object> updatedCaseDataMap, int caseFlagsIndex) {

        String externalCaseFlagsKey = getCaseFlagsFiledKey(representing1.getCaseDataExternalField(), caseFlagsIndex);
        String internalCaseFlagsKey = getCaseFlagsFiledKey(representing1.getCaseDataInternalField(), caseFlagsIndex);
        Flags caseFlagsExternal = getCaseFlagsForParty(updatedCaseDataMap.get(externalCaseFlagsKey));
        Flags caseFlagsInternal = getCaseFlagsForParty(updatedCaseDataMap.get(internalCaseFlagsKey));
        switch (representing1) {
            case CAAPPLICANT:
                setCaseFlagInformationForParty(
                    representing1,
                    partyDetailsElement,
                    updatedCaseDataMap,
                    caseFlagsIndex,
                    externalCaseFlagsKey,
                    caseFlagsExternal,
                    VISIBILITY_EXTERNAL
                );
                setCaseFlagInformationForParty(
                    representing1,
                    partyDetailsElement,
                    updatedCaseDataMap,
                    caseFlagsIndex,
                    internalCaseFlagsKey,
                    caseFlagsInternal,
                    VISIBILITY_INTERNAL
                );
                break;
            case CAAPPLICANTSOLICITOR:
                setCaseFlagInformationSolicitor(
                    representing1,
                    partyDetailsElement,
                    updatedCaseDataMap,
                    caseFlagsIndex,
                    externalCaseFlagsKey,
                    caseFlagsExternal,
                    VISIBILITY_EXTERNAL
                );
                setCaseFlagInformationSolicitor(
                    representing1,
                    partyDetailsElement,
                    updatedCaseDataMap,
                    caseFlagsIndex,
                    internalCaseFlagsKey,
                    caseFlagsInternal,
                    VISIBILITY_INTERNAL
                );
                break;
            default:
                break;

        }

    }

    private void setCaseFlagInformationSolicitor(PartyRole.Representing representing1, Element<PartyDetails> partyDetailsElement,
                                                 Map<String, Object> updatedCaseDataMap, int caseFlagsIndex, String externalCaseFlagsKey,
                                                 Flags caseFlags, String visibility) {
        Optional<PartyRole> partyRole = getPartyRole(representing1, caseFlagsIndex);
        Flags caseFlag = null;
        if (partyRole.isPresent() && ObjectUtils.isNotEmpty(caseFlags)) {
            caseFlag = caseFlags.toBuilder().partyName(partyDetailsElement.getValue().getRepresentativeFullName()).groupId(
                representing1.getGroupId()).roleOnCase(partyRole.get().getCaseRoleLabel()).build();
        } else if (partyRole.isPresent() && Objects.isNull(caseFlags)) {

            caseFlag = Flags
                .builder()
                .partyName(partyDetailsElement.getValue().getRepresentativeFullName())
                .roleOnCase(partyRole.get().getCaseRoleLabel())
                .visibility(visibility)
                .groupId(representing1.getGroupId())
                .details(Collections.emptyList())
                .build();
        }
        updatedCaseDataMap.put(externalCaseFlagsKey, caseFlag);

    }

    private Optional<PartyRole> getPartyRole(PartyRole.Representing representing1, int caseFlagsIndex) {
        List<PartyRole> partyRoles = PartyRole.matchingRoles(representing1);
        Optional<PartyRole> partyRole = partyRoles.stream().filter(role -> role.getIndex() == caseFlagsIndex).findFirst();
        return partyRole;
    }

    private String getCaseFlagsFiledKey(String key, int caseFlagsIndex) {
        return String.format(key, caseFlagsIndex);
    }

    private void setCaseFlagInformationForParty(PartyRole.Representing representing1, Element<PartyDetails> partyDetailsElement,
                                                Map<String, Object> updatedCaseDataMap, int caseFlagsIndex, String externalCaseFlagsKey,
                                                Flags caseFlags, String visibilityExternal) {
        Optional<PartyRole> partyRole = getPartyRole(representing1, caseFlagsIndex);
        Flags caseFlag = null;
        if (partyRole.isPresent() && ObjectUtils.isNotEmpty(caseFlags)) {
            caseFlag = caseFlags.toBuilder().partyName(partyDetailsElement.getValue().getLabelForDynamicList()).groupId(
                representing1.getGroupId()).roleOnCase(partyRole.get().getCaseRoleLabel()).build();
        } else if (partyRole.isPresent() && Objects.isNull(caseFlags)) {
            caseFlag = Flags
                .builder()
                .partyName(partyDetailsElement.getValue().getLabelForDynamicList())
                .roleOnCase(partyRole.get().getCaseRoleLabel())
                .visibility(visibilityExternal)
                .groupId(representing1.getGroupId())
                .details(Collections.emptyList())
                .build();
        }
        updatedCaseDataMap.put(externalCaseFlagsKey, caseFlag);
    }

    private Flags getCaseFlagsForParty(Object updatedCaseDataMap) {
        return objectMapper.convertValue(
            updatedCaseDataMap,
            Flags.class
        );
    }

    private void updateCaseFlagDataIfPartiesRemoved(Map<String, Object> oldCaseDataMap, Map<String,
        Object> updatedCaseDataMap, Map<String, Integer> oldApplicantIdToIndex, Map<String,
        Integer> applicantIdToIndex, String partyType) {

        if (MapUtils.isNotEmpty(applicantIdToIndex)) {
            applicantIdToIndex.forEach((key, index) -> {
                Optional<Integer> oldIndex = Optional.ofNullable(oldApplicantIdToIndex.get(key));
                log.info("old index {}", oldIndex);
                log.info("new index {}", index);
                if (oldIndex.isPresent() && !oldIndex.get().equals(index)) {
                    updateCaseFlagsData(oldIndex.get(), index, oldCaseDataMap, updatedCaseDataMap,
                                        partyType
                    );
                } else if (oldIndex.isEmpty()) {
                    generateCaseFlagForNewParty(index, updatedCaseDataMap, partyType);
                }
            });
        }
    }

    private void generateCaseFlagForNewParty(Integer index, Map<String, Object> updatedCaseDataMap, String partyType) {

    }

    private Map<String, Integer> getPartyIdToIndexMapping(List<Element<PartyDetails>> parties) {
        return IntStream.range(0, parties.size())
            .boxed().collect(Collectors.toMap(i -> String.valueOf(parties.get(i).getId()), i -> i));
    }

    private void updateCaseFlagsData(int oldIndex, Integer index, Map<String, Object> oldCaseDataMap,
                                     Map<String, Object> updatedCaseDataMap, String partyType) {

        switch (partyType) {
            case CA_APPLICANT:
                List<String> caseFlagsToBeUpdated = Arrays.asList(
                    "caApplicant%sExternalFlags",
                    "caApplicantSolicitor%sExternalFlags",
                    "caApplicant%sInternalFlags",
                    "caApplicantSolicitor%sInternalFlags"
                );
                caseFlagsToBeUpdated.forEach(key -> {
                    String oldCaseDataKey = String.format(key, oldIndex + 1);
                    log.info("old case data key {}",oldCaseDataKey);
                    String caseDataKey = String.format(key, index + 1);
                    log.info("new case data key {}",caseDataKey);
                    Flags oldCaseFlags = objectMapper.convertValue(
                        oldCaseDataMap.get(oldCaseDataKey),
                        Flags.class
                    );
                    Flags newCaseFlags = objectMapper.convertValue(
                        oldCaseDataMap.get(oldCaseDataKey),
                        Flags.class
                    );
                    updatedCaseDataMap.put(caseDataKey, oldCaseDataMap.get(oldCaseDataKey));
                });
                break;
            default:
                break;
        }


    }

}
