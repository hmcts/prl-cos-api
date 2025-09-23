package uk.gov.hmcts.reform.prl.services.caseflags;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
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
import uk.gov.hmcts.reform.prl.models.caseflags.AllPartyFlags;
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
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.caseflags.PartyRole.Representing.CAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.caseflags.PartyRole.Representing.CAOTHERPARTY;
import static uk.gov.hmcts.reform.prl.enums.caseflags.PartyRole.Representing.CARESPONDENT;
import static uk.gov.hmcts.reform.prl.utils.caseflags.PartyLevelCaseFlagsGenerator.VISIBILITY_EXTERNAL;
import static uk.gov.hmcts.reform.prl.utils.caseflags.PartyLevelCaseFlagsGenerator.VISIBILITY_INTERNAL;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
public class PartyLevelCaseFlagsService {
    private static final String AMEND_APPLICANTS_DETAILS = "amendApplicantsDetails";
    private static final String AMEND_RESPONDENT_DETAILS = "amendRespondentsDetails";
    private static final String AMEND_OTHER_PEOPLE_IN_THE_CASE = "amendOtherPeopleInTheCaseRevised";
    private static final String ADMIN_ADD_BARRISTER = "adminAddBarrister";
    private static final String SOLICITOR_ADD_BARRISTER = "solicitorAddBarrister";

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

    public void updateCaseDataWithGeneratePartyCaseFlags(CaseData caseData,
                                                         Function<CaseData, Map<String, Object>> flagUpdater) {
        Map<String, Object> existingPartyFlags = objectMapper.convertValue(caseData.getAllPartyFlags(),
                                                                           new TypeReference<>() {});

        Map<String, Object> updatedFlags = flagUpdater.apply(caseData);
        existingPartyFlags.putAll(updatedFlags);

        AllPartyFlags allPartyFlags = objectMapper.convertValue(existingPartyFlags,
                                                               new TypeReference<>() {});
        caseData.setAllPartyFlags(allPartyFlags);
    }


    public Map<String, Object> generatePartyCaseFlagsForBarristerOnly(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            if (!CaseCreatedBy.CITIZEN.equals(caseData.getCaseCreatedBy())) {
                data.putAll(generateC100PartyCaseFlagsForBarrister(caseData, PartyRole.Representing.CAAPPLICANTBARRISTER));
                data.putAll(generateC100PartyCaseFlagsForBarrister(caseData, PartyRole.Representing.CARESPONDENTBARRISTER));
            }
        } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            data.putAll(generateFL401PartyCaseFlagsForBarrister(caseData, PartyRole.Representing.DAAPPLICANTBARRISTER));
            data.putAll(generateFL401PartyCaseFlagsForBarrister(caseData, PartyRole.Representing.DARESPONDENTBARRISTER));
        }
        return data;
    }

    public Map<String, Object> generatePartyCaseFlags(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            data.putAll(generateC100PartyCaseFlags(caseData, CAAPPLICANT));
            data.putAll(generateC100PartyCaseFlags(caseData, PartyRole.Representing.CARESPONDENT));
            data.putAll(generateC100PartyCaseFlags(caseData, CAOTHERPARTY));
            if (!CaseCreatedBy.CITIZEN.equals(caseData.getCaseCreatedBy())) {
                data.putAll(generateC100PartyCaseFlags(caseData, PartyRole.Representing.CAAPPLICANTSOLICITOR));
                data.putAll(generateC100PartyCaseFlags(caseData, PartyRole.Representing.CARESPONDENTSOLICITOR));
                data.putAll(generateC100PartyCaseFlags(caseData, PartyRole.Representing.CAAPPLICANTBARRISTER));
                data.putAll(generateC100PartyCaseFlags(caseData, PartyRole.Representing.CARESPONDENTBARRISTER));
            }
        } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            data.putAll(generateFl401PartyCaseFlags(caseData, PartyRole.Representing.DAAPPLICANT));
            data.putAll(generateFl401PartyCaseFlags(caseData, PartyRole.Representing.DAAPPLICANTSOLICITOR));
            data.putAll(generateFl401PartyCaseFlags(caseData, PartyRole.Representing.DAAPPLICANTBARRISTER));
            data.putAll(generateFl401PartyCaseFlags(caseData, PartyRole.Representing.DARESPONDENT));
            data.putAll(generateFl401PartyCaseFlags(caseData, PartyRole.Representing.DARESPONDENTSOLICITOR));
            data.putAll(generateFl401PartyCaseFlags(caseData, PartyRole.Representing.DARESPONDENTBARRISTER));
        }
        return data;
    }

    private Map<String, Object> generateFL401PartyCaseFlagsForBarrister(CaseData caseData, PartyRole.Representing representing) {
        Map<String, Object> data = new HashMap<>();
        List<PartyRole> partyRoles = PartyRole.matchingRoles(representing);
        PartyDetails partyDetails = representing.getDaTarget().apply(caseData);
        for (int i = 0; i < partyRoles.size(); i++) {
            PartyRole partyRole = partyRoles.get(i);
            if (partyDetails.getPartyId() != null
                && isFlagUpdateRequired(caseData, partyDetails.getPartyId())) {
                findAndGeneratePartyFlagsForBarristerOnly(representing,
                                                          i,
                                                          partyDetails.getBarristerFullNameForCaseFlags(),
                                                          data,
                                                          partyRole);
            }
        }
        return data;
    }

    private Map<String, Object> generateC100PartyCaseFlagsForBarrister(CaseData caseData, PartyRole.Representing representing) {
        Map<String, Object> data = new HashMap<>();
        List<Element<PartyDetails>> caElements = representing.getCaTarget().apply(caseData);
        int numElements = null != caElements ? caElements.size() : 0;
        List<PartyRole> partyRoles = PartyRole.matchingRoles(representing);
        for (int i = 0; i < partyRoles.size(); i++) {
            PartyRole partyRole = partyRoles.get(i);
            if (null != caElements) {
                Optional<Element<PartyDetails>> partyDetailsElement = i < numElements ? Optional.of(caElements.get(i)) : Optional.empty();
                if (partyDetailsElement.isPresent()
                    && isFlagUpdateRequired(caseData, partyDetailsElement.get().getId())) {
                    findAndGeneratePartyFlagsForBarristerOnly(representing,
                                                              i,
                                                              partyDetailsElement.get().getValue().getBarristerFullNameForCaseFlags(),
                                                              data,
                                                              partyRole);
                }
            }
        }
        return data;
    }

    private boolean isFlagUpdateRequired(CaseData caseData, UUID partyId) {
        return  caseData.getAllocatedBarrister() != null
            && partyId.equals(caseData.getAllocatedBarrister().getPartyList().getValueCodeAsUuid());
    }

    private Map<String, Object> generateC100PartyCaseFlags(CaseData caseData, PartyRole.Representing representing) {
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
            case CAAPPLICANTBARRISTER, CARESPONDENTBARRISTER: {
                if (partyDetails.isPresent()
                    && !StringUtils.isEmpty(partyDetails.get().getValue().getBarristerFullNameForCaseFlags())) {
                    data.put(
                        caseDataExternalField,
                        partyLevelCaseFlagsGenerator.generateExternalPartyFlags(
                            partyDetails.get().getValue().getBarristerFullNameForCaseFlags(),
                            partyRole.getCaseRoleLabel(),
                            groupId
                        )
                    );
                    data.put(
                        caseDataInternalField,
                        partyLevelCaseFlagsGenerator.generateInternalPartyFlags(
                            partyDetails.get().getValue().getBarristerFullNameForCaseFlags(),
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

    private void findAndGeneratePartyFlagsForBarristerOnly(PartyRole.Representing representing,
                                           int partyIndex,
                                           String barristerFullName,
                                           Map<String, Object> data,
                                           PartyRole partyRole) {
        String caseDataExternalField = String.format(representing.getCaseDataExternalField(), partyIndex + 1);
        String caseDataInternalField = String.format(representing.getCaseDataInternalField(), partyIndex + 1);
        String groupId = String.format(representing.getGroupId(), partyIndex + 1);
        switch (representing) {
            case CAAPPLICANTBARRISTER, CARESPONDENTBARRISTER, DAAPPLICANTBARRISTER, DARESPONDENTBARRISTER: {
                if (!StringUtils.isEmpty(barristerFullName)) {
                    data.put(
                        caseDataExternalField,
                        partyLevelCaseFlagsGenerator.generateExternalPartyFlags(
                            barristerFullName,
                            partyRole.getCaseRoleLabel(),
                            groupId
                        )
                    );
                    data.put(
                        caseDataInternalField,
                        partyLevelCaseFlagsGenerator.generateInternalPartyFlags(
                            barristerFullName,
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
                    case DAAPPLICANTBARRISTER, DARESPONDENTBARRISTER: {
                        if (!StringUtils.isEmpty(partyDetails.getBarristerFullNameForCaseFlags())) {
                            data.put(
                                caseDataExternalField,
                                partyLevelCaseFlagsGenerator.generateExternalPartyFlags(
                                    partyDetails.getBarristerFullNameForCaseFlags(),
                                    partyRole.getCaseRoleLabel(),
                                    groupId
                                )
                            );
                            data.put(
                                caseDataInternalField,
                                partyLevelCaseFlagsGenerator.generateInternalPartyFlags(
                                    partyDetails.getBarristerFullNameForCaseFlags(),
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
            case CAAPPLICANTBARRISTER, CARESPONDENTBARRISTER: {
                List<Element<PartyDetails>> caElements = representing.getCaTarget().apply(caseData);
                Optional<Element<PartyDetails>> partyDetails = Optional.ofNullable(caElements.get(partyIndex));
                if (partyDetails.isPresent()) {
                    caseData = regenerateBarristerFlags(
                        caseData,
                        partyDetails.get().getValue(),
                        representing,
                        partyIndex
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
            case DAAPPLICANTBARRISTER, DARESPONDENTBARRISTER: {
                Optional<PartyDetails> partyDetails = Optional.ofNullable(representing.getDaTarget().apply(caseData));
                if (partyDetails.isPresent()) {
                    caseData = regenerateBarristerFlags(
                        caseData,
                        partyDetails.get(),
                        representing,
                        partyIndex
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

    private CaseData regenerateBarristerFlags(CaseData caseData,
                                              PartyDetails partyDetails,
                                              PartyRole.Representing representing,
                                              int partyIndex) {
        String caseDataExternalField = String.format(representing.getCaseDataExternalField(), partyIndex + 1);
        String caseDataInternalField = String.format(representing.getCaseDataInternalField(), partyIndex + 1);
        String groupId = String.format(representing.getGroupId(), partyIndex + 1);
        Optional<PartyRole> partyRole = PartyRole.fromRepresentingAndIndex(
            representing,
            partyIndex + 1
        );
        if (partyRole.isPresent() && !StringUtils.isEmpty(partyDetails.getBarristerFullNameForCaseFlags())) {
            caseData = partyLevelCaseFlagsGenerator.generatePartyFlags(
                caseData,
                partyDetails.getBarristerFullNameForCaseFlags(),
                caseDataExternalField,
                partyRole.get().getCaseRoleLabel(),
                false,
                groupId
            );
            caseData = partyLevelCaseFlagsGenerator.generatePartyFlags(
                caseData,
                partyDetails.getBarristerFullNameForCaseFlags(),
                caseDataInternalField,
                partyRole.get().getCaseRoleLabel(),
                true,
                groupId
            );
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
            PartyRole.Representing.CAAPPLICANTBARRISTER
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
            PartyRole.Representing.CARESPONDENTBARRISTER
        );
        caseData = generateC100IndividualPartyCaseFlags(
            caseData,
            startEventResponseData,
            CAOTHERPARTY
        );
        return caseData;
    }

    private CaseData generateC100IndividualPartyCaseFlags(CaseData caseData, CaseData startEventResponseData, PartyRole.Representing representing) {
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
            case CAAPPLICANTSOLICITOR, CARESPONDENTSOLICITOR, CAAPPLICANTBARRISTER, CARESPONDENTBARRISTER: {
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
        if (C100_CASE_TYPE.equals(updatedCaseData.getCaseTypeOfApplication())
            && Arrays.asList(AMEND_APPLICANTS_DETAILS,
                             AMEND_RESPONDENT_DETAILS,
                             AMEND_OTHER_PEOPLE_IN_THE_CASE,
                             ADMIN_ADD_BARRISTER,
                             SOLICITOR_ADD_BARRISTER).contains(eventId)) {
            List<Element<PartyDetails>> parties = getPartiesBaseOnEventID(updatedCaseData, eventId);
            List<Element<PartyDetails>> oldParties = getPartiesBaseOnEventID(oldCaseData, eventId);
            if (CollectionUtils.isNotEmpty(parties) && CollectionUtils.isNotEmpty(oldParties)) {
                Map<String, Integer> oldPartyIdToIndex = getPartyIdToIndexMapping(oldParties);
                Map<String, Integer> partyToIndex = getPartyIdToIndexMapping(parties);
                updateCaseFlagDataIfPartiesRemoved(updatedCaseDataMap, oldPartyIdToIndex, partyToIndex,
                                                   getRepresentingForEventId(eventId), parties
                );
                refreshPartyFlags(updatedCaseDataMap, getPartiesBaseOnEventID(updatedCaseData, eventId),
                                  getRepresentingForEventId(eventId)
                );

            }

        }
    }

    private List<PartyRole.Representing> getRepresentingForEventId(String eventId) {

        switch (eventId) {
            case AMEND_APPLICANTS_DETAILS -> {
                return Arrays.asList(
                    CAAPPLICANT, PartyRole.Representing.CAAPPLICANTSOLICITOR,
                    PartyRole.Representing.CAAPPLICANTBARRISTER);
            }
            case AMEND_RESPONDENT_DETAILS -> {
                return Arrays.asList(
                    CARESPONDENT, PartyRole.Representing.CARESPONDENTSOLICITOR,
                    PartyRole.Representing.CARESPONDENTBARRISTER);
            }
            case AMEND_OTHER_PEOPLE_IN_THE_CASE -> {
                return List.of(CAOTHERPARTY);
            }
            case ADMIN_ADD_BARRISTER, SOLICITOR_ADD_BARRISTER -> {
                return List.of(PartyRole.Representing.CAAPPLICANTBARRISTER,
                               PartyRole.Representing.CARESPONDENTBARRISTER,
                               PartyRole.Representing.DAAPPLICANTBARRISTER,
                               PartyRole.Representing.DARESPONDENTBARRISTER
                               );
            }
            default -> {
                return Collections.emptyList();
            }
        }

    }

    private List<Element<PartyDetails>> getPartiesBaseOnEventID(CaseData caseData, String eventId) {

        switch (eventId) {
            case AMEND_APPLICANTS_DETAILS -> {
                return caseData.getApplicants();
            }
            case AMEND_RESPONDENT_DETAILS -> {
                return caseData.getRespondents();
            }
            case AMEND_OTHER_PEOPLE_IN_THE_CASE -> {
                return caseData.getOtherPartyInTheCaseRevised();
            }
            default -> {
                return Collections.emptyList();
            }
        }
    }

    private void refreshPartyFlags(Map<String, Object> updatedCaseDataMap, List<Element<PartyDetails>> parties,
                                   List<PartyRole.Representing> representing) {
        representing.forEach(representing1 -> {
            List<String> caseFlagsToBeUpdated = Arrays.asList(
                representing1.getCaseDataExternalField(),
                representing1.getCaseDataInternalField()
            );
            if (representing1 == CAAPPLICANT || representing1 == PartyRole.Representing.CAAPPLICANTSOLICITOR
                || representing1 == PartyRole.Representing.CAAPPLICANTBARRISTER
                || representing1 == CAOTHERPARTY || representing1 == PartyRole.Representing.CARESPONDENTSOLICITOR
                || representing1 == PartyRole.Representing.CARESPONDENTBARRISTER
                || representing1 == CARESPONDENT) {
                for (int i = 0; i < 5; i++) {
                    if (i >= parties.size()) {
                        int caseFlagsIndex = i + 1;
                        caseFlagsToBeUpdated.forEach(key -> {
                            String caseFlagKey = String.format(key, caseFlagsIndex);
                            updatedCaseDataMap.put(caseFlagKey, Flags
                                .builder().build());
                        });
                    }
                }
            }

        });
    }


    private Optional<PartyRole> getPartyRole(PartyRole.Representing representing1, int caseFlagsIndex) {
        List<PartyRole> partyRoles = PartyRole.matchingRoles(representing1);
        return partyRoles.stream().filter(role -> role.getIndex() == caseFlagsIndex).findFirst();
    }


    private Flags getCaseFlagsForParty(Object flags) {
        return objectMapper.convertValue(
            flags,
            Flags.class
        );
    }

    private void updateCaseFlagDataIfPartiesRemoved(Map<String,
        Object> updatedCaseDataMap, Map<String, Integer> oldApplicantIdToIndex, Map<String,
        Integer> applicantIdToIndex, List<PartyRole.Representing> representing, List<Element<PartyDetails>> parties) {

        if (MapUtils.isNotEmpty(applicantIdToIndex)) {
            applicantIdToIndex.forEach((key, index) -> {
                Optional<Integer> oldIndex = Optional.ofNullable(oldApplicantIdToIndex.get(key));
                if (oldIndex.isPresent() && !oldIndex.get().equals(index)) {
                    updateCaseFlagsData(oldIndex.get(), index, updatedCaseDataMap,
                                        representing, parties

                    );
                } else if (oldIndex.isEmpty()) {
                    generateNewPartyFlags(index, updatedCaseDataMap, parties, representing);
                }
            });
        }
    }

    private void generateNewPartyFlags(Integer index, Map<String, Object> updatedCaseDataMap,
                                       List<Element<PartyDetails>> applicants,
                                       List<PartyRole.Representing> representingList) {
        representingList.forEach(representing -> {
            String externalCaseDataField = representing.getCaseDataExternalField();
            String internalCaseDataField = representing.getCaseDataInternalField();
            setupNewCaseFlag(index, updatedCaseDataMap, applicants, representing, externalCaseDataField,VISIBILITY_EXTERNAL);
            setupNewCaseFlag(index, updatedCaseDataMap, applicants, representing, internalCaseDataField,VISIBILITY_INTERNAL);
        });
    }

    private void setupNewCaseFlag(Integer index,  Map<String, Object> updatedCaseDataMap, List<Element<PartyDetails>> applicants,
                                  PartyRole.Representing representing, String externalCaseDataField, String visibilityExternal) {
        String caseDataKey = String.format(externalCaseDataField, index + 1);
        Optional<PartyRole> partyRole = getPartyRole(representing, index);
        Optional<Element<PartyDetails>> partyDetailsElement = Optional.ofNullable(applicants.get(index));
        if (partyDetailsElement.isPresent()) {
            int caseFlagsIndex = index + 1;
            Flags caseFlag = Flags
                .builder()
                .partyName(getPartyName(partyDetailsElement.get().getValue(), representing))
                .roleOnCase(partyRole.isPresent() ? partyRole.get().getCaseRoleLabel() : StringUtils.EMPTY)
                .visibility(visibilityExternal)
                .groupId(String.format(representing.getGroupId(), caseFlagsIndex))
                .details(Collections.emptyList())
                .build();
            updatedCaseDataMap.put(caseDataKey, caseFlag);
        }
    }

    private Map<String, Integer> getPartyIdToIndexMapping(List<Element<PartyDetails>> parties) {
        return IntStream.range(0, parties.size())
            .boxed().collect(Collectors.toMap(i -> String.valueOf(parties.get(i).getId()), i -> i));
    }

    private void updateCaseFlagsData(int oldIndex, Integer index,
                                     Map<String, Object> updatedCaseDataMap, List<PartyRole.Representing> representingList,
                                     List<Element<PartyDetails>> applicants) {
        representingList.forEach(representing -> {
            List<String> caseFlagsToBeUpdated = Arrays.asList(
                representing.getCaseDataExternalField(),
                representing.getCaseDataInternalField()
            );
            caseFlagsToBeUpdated.forEach(key -> {
                String oldCaseDataKey = String.format(key, oldIndex + 1);
                String caseDataKey = String.format(key, index + 1);
                Flags oldFlags = getCaseFlagsForParty(updatedCaseDataMap.get(oldCaseDataKey));
                Flags newFlags = getCaseFlagsForParty(updatedCaseDataMap.get(caseDataKey));
                PartyDetails applicant = applicants.get(index).getValue();
                updatedCaseDataMap.put(
                    caseDataKey,
                    oldFlags.toBuilder().partyName(getPartyName(
                        applicant,
                        representing
                    )).roleOnCase(newFlags.getRoleOnCase()).groupId(
                        newFlags.getGroupId()).build()
                );

            });

        });

    }

    private String getPartyName(PartyDetails applicant, PartyRole.Representing representing) {
        switch (representing) {
            case CAAPPLICANT,CARESPONDENT,CAOTHERPARTY -> {
                return applicant.getLabelForDynamicList();
            }
            case CAAPPLICANTSOLICITOR,CARESPONDENTSOLICITOR,CAAPPLICANTBARRISTER,CARESPONDENTBARRISTER -> {
                return applicant.getRepresentativeFullNameForCaseFlags();
            }
            default -> {
                return StringUtils.EMPTY;
            }
        }
    }

}
