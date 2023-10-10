package uk.gov.hmcts.reform.prl.services.caseflags;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.caseflags.PartyRole;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseflags.AllPartyFlags;
import uk.gov.hmcts.reform.prl.models.caseflags.PartyFlags;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.caseflags.PartyLevelCaseFlagsGenerator;
import uk.gov.hmcts.reform.prl.utils.noticeofchange.NoticeOfChangePartiesConverter;
import uk.gov.hmcts.reform.prl.utils.noticeofchange.RespondentPolicyConverter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
public class PartyLevelCaseFlagsService {
    public final NoticeOfChangePartiesConverter partiesConverter;
    public final RespondentPolicyConverter policyConverter;
    public final PartyLevelCaseFlagsGenerator partyLevelCaseFlagsGenerator;

    public Map<String, Object> generatePartyCaseFlags(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            data.putAll(generateC100PartyCaseFlags(caseData, PartyRole.Representing.CAAPPLICANT));
            data.putAll(generateC100PartyCaseFlags(caseData, PartyRole.Representing.CAAPPLICANTSOLICITOR));
            data.putAll(generateC100PartyCaseFlags(caseData, PartyRole.Representing.CARESPONDENT));
            data.putAll(generateC100PartyCaseFlags(caseData, PartyRole.Representing.CARESPONDENTSOLCIITOR));
            data.putAll(generateC100PartyCaseFlags(caseData, PartyRole.Representing.CAOTHERPARTY));
        } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            data.putAll(generateFl401PartyCaseFlags(caseData, PartyRole.Representing.DAAPPLICANT));
            data.putAll(generateFl401PartyCaseFlags(caseData, PartyRole.Representing.DAAPPLICANTSOLICITOR));
            data.putAll(generateFl401PartyCaseFlags(caseData, PartyRole.Representing.DARESPONDENT));
            data.putAll(generateFl401PartyCaseFlags(caseData, PartyRole.Representing.DARESPONDENTSOLCIITOR));
        }
        log.info("*** flags we have now: " + data);
        return data;
    }

    public Map<String, Object> generateC100PartyCaseFlags(CaseData caseData, PartyRole.Representing representing) {
        Map<String, Object> data = new HashMap<>();
        List<Element<PartyDetails>> caElements = representing.getCaTarget().apply(caseData);
        int numElements = null != caElements ? caElements.size() : 0;
        List<PartyRole> partyRoles = PartyRole.matchingRoles(representing);
        for (int i = 0; i < partyRoles.size(); i++) {
            PartyRole partyRole = partyRoles.get(i);
            log.info("Party role we have now::" + partyRole.getCaseRoleLabel());
            log.info("Representing is now::" + partyRole.getCaseRoleLabel());
            if (null != caElements) {
                Optional<Element<PartyDetails>> partyDetails = i < numElements ? Optional.of(caElements.get(i)) : Optional.empty();
                if (partyDetails.isPresent()) {
                    log.info("party details is present");
                    String caseDataField = String.format(representing.getCaseDataField(), i + 1);
                    log.info("caseDataField is::" + caseDataField);
                    switch (representing) {
                        case CAAPPLICANT, CARESPONDENT, CAOTHERPARTY -> {
                            if (!StringUtils.isEmpty(partyDetails.get().getValue().getLabelForDynamicList())) {
                                data.put(
                                    caseDataField,
                                    partyLevelCaseFlagsGenerator.generatePartyFlags(
                                        partyDetails.get().getValue().getLabelForDynamicList(),
                                        caseDataField,
                                        partyRole.getCaseRoleLabel()
                                    )
                                );
                            }
                            break;
                        }
                        case CAAPPLICANTSOLICITOR, CARESPONDENTSOLCIITOR -> {
                            if (!StringUtils.isEmpty(partyDetails.get().getValue().getRepresentativeFullNameForCaseFlags())) {
                                data.put(
                                    caseDataField,
                                    partyLevelCaseFlagsGenerator.generatePartyFlags(
                                        partyDetails.get().getValue().getRepresentativeFullNameForCaseFlags(),
                                        caseDataField,
                                        partyRole.getCaseRoleLabel()
                                    )
                                );
                            }
                            break;
                        }
                        default -> {
                            break;
                        }
                    }
                }
            }
        }
        return data;
    }

    public Map<String, Object> generateFl401PartyCaseFlags(CaseData caseData, PartyRole.Representing representing) {
        Map<String, Object> data = new HashMap<>();
        PartyDetails partyDetails = representing.getDaTarget().apply(caseData);

        List<PartyRole> partyRoles = PartyRole.matchingRoles(representing);
        for (int i = 0; i < partyRoles.size(); i++) {
            PartyRole partyRole = partyRoles.get(i);
            log.info("Party role we have now::" + partyRole.getCaseRoleLabel());
            log.info("Representing is now::" + partyRole.getCaseRoleLabel());
            if (null != partyDetails) {
                log.info("party details is present");
                String caseDataField = String.format(representing.getCaseDataField(), i + 1);
                log.info("caseDataField is::" + caseDataField);
                switch (representing) {
                    case DAAPPLICANT, DARESPONDENT -> {
                        if (!StringUtils.isEmpty(partyDetails.getLabelForDynamicList())) {
                            data.put(
                                caseDataField,
                                partyLevelCaseFlagsGenerator.generatePartyFlags(
                                    partyDetails.getLabelForDynamicList(),
                                    caseDataField,
                                    partyRole.getCaseRoleLabel()
                                )
                            );
                        }
                        break;
                    }
                    case DAAPPLICANTSOLICITOR, DARESPONDENTSOLCIITOR -> {
                        if (!StringUtils.isEmpty(partyDetails.getRepresentativeFullNameForCaseFlags())) {
                            data.put(
                                caseDataField,
                                partyLevelCaseFlagsGenerator.generatePartyFlags(
                                    partyDetails.getRepresentativeFullNameForCaseFlags(),
                                    caseDataField,
                                    partyRole.getCaseRoleLabel()
                                )
                            );
                        }
                        break;
                    }
                    default -> {
                        break;
                    }
                }
            }
        }
        return data;
    }

    public void generateIndividualPartySolicitorCaseFlags(CaseData caseData, int partyIndex, PartyRole.Representing representing) {
        String caseDataField = String.format(representing.getCaseDataField(), partyIndex + 1);
        Optional<Object> partyFlags = Optional.empty();
        log.info("caseDataField is::" + caseDataField);
        switch (representing) {
            case CAAPPLICANTSOLICITOR, CARESPONDENTSOLCIITOR -> {
                List<Element<PartyDetails>> caElements = representing.getCaTarget().apply(caseData);
                Optional<Element<PartyDetails>> partyDetails = Optional.of(caElements.get(partyIndex));
                log.info("About to generate solicitor flags");
                if (partyDetails.isPresent()) {
                    regenerateSolicitorFlags(
                        caseData,
                        partyDetails.get().getValue(),
                        representing,
                        caseDataField,
                        partyIndex
                    );
                }
                break;
            }
            case DAAPPLICANTSOLICITOR, DARESPONDENTSOLCIITOR -> {
                Optional<PartyDetails> partyDetails = Optional.ofNullable(representing.getDaTarget().apply(caseData));
                if (partyDetails.isPresent()) {
                    regenerateSolicitorFlags(caseData, partyDetails.get(), representing, caseDataField, partyIndex);
                }
                break;
            }
            default -> {
                break;
            }
        }
    }

    private void regenerateSolicitorFlags(CaseData caseData,
                                          PartyDetails partyDetails,
                                          PartyRole.Representing representing,
                                          String caseDataField,
                                          int partyIndex) {
        Optional<Object> partyFlags = Optional.empty();
        log.info("regenerateSolicitorFlags");
        if (!StringUtils.isEmpty(partyDetails.getRepresentativeFullNameForCaseFlags())
            && PartyRole.fromRepresentingAndIndex(representing, partyIndex + 1).isPresent()) {
            log.info("inside now");
            partyFlags = Optional.ofNullable(partyLevelCaseFlagsGenerator.generatePartyFlags(
                partyDetails.getRepresentativeFullNameForCaseFlags(),
                caseDataField,
                String.valueOf(PartyRole.fromRepresentingAndIndex(
                    representing,
                    partyIndex + 1
                ).get())
            ));
            log.info("got the flags");
        }
        if (partyFlags.isPresent()) {
            PartyFlags updatedPartyFlags = (PartyFlags) partyFlags.get();
            log.info("updatedPartyFlags is::" + updatedPartyFlags);
            AllPartyFlags allPartyFlags = AllPartyFlags.builder().build();
            switch (caseDataField) {
                case "caApplicantSolicitor1Flags":
                    allPartyFlags = caseData.getAllPartyFlags().toBuilder().caApplicantSolicitor1Flags(updatedPartyFlags).build();
                    break;
                case "caApplicantSolicitor2Flags":
                    allPartyFlags = caseData.getAllPartyFlags().toBuilder().caApplicantSolicitor2Flags(updatedPartyFlags).build();
                    break;
                case "caApplicantSolicitor3Flags":
                    allPartyFlags = caseData.getAllPartyFlags().toBuilder().caApplicantSolicitor3Flags(updatedPartyFlags).build();
                    break;
                case "caApplicantSolicitor4Flags":
                    allPartyFlags = caseData.getAllPartyFlags().toBuilder().caApplicantSolicitor4Flags(updatedPartyFlags).build();
                    break;
                case "caApplicantSolicitor5Flags":
                    allPartyFlags = caseData.getAllPartyFlags().toBuilder().caApplicantSolicitor5Flags(updatedPartyFlags).build();
                    break;
                case "caRespondentSolicitor1Flags":
                    allPartyFlags = caseData.getAllPartyFlags().toBuilder().caRespondentSolicitor1Flags(
                        updatedPartyFlags).build();
                    break;
                case "caRespondentSolicitor2Flags":
                    allPartyFlags = caseData.getAllPartyFlags().toBuilder().caRespondentSolicitor2Flags(
                        updatedPartyFlags).build();
                    break;
                case "caRespondentSolicitor3Flags":
                    allPartyFlags = caseData.getAllPartyFlags().toBuilder().caRespondentSolicitor3Flags(
                        updatedPartyFlags).build();
                    break;
                case "caRespondentSolicitor4Flags":
                    allPartyFlags = caseData.getAllPartyFlags().toBuilder().caRespondentSolicitor4Flags(
                        updatedPartyFlags).build();
                    break;
                case "caRespondentSolicitor5Flags":
                    allPartyFlags = caseData.getAllPartyFlags().toBuilder().caRespondentSolicitor5Flags(
                        updatedPartyFlags).build();
                    break;
                case "daApplicantSolicitorFlags":
                    allPartyFlags = caseData.getAllPartyFlags().toBuilder().daApplicantSolicitorFlags(updatedPartyFlags).build();
                    break;
                case "daRespondentSolicitorFlags":
                    allPartyFlags = caseData.getAllPartyFlags().toBuilder().daRespondentSolicitorFlags(updatedPartyFlags).build();
                    break;
                default:
                    break;
            }
            log.info("allPartyFlags is now::" + allPartyFlags);
            caseData.toBuilder().allPartyFlags(allPartyFlags).build();
        }
    }
}
