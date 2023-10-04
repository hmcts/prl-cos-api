package uk.gov.hmcts.reform.prl.services.caseflags;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.caseflags.PartyRole;
import uk.gov.hmcts.reform.prl.models.Element;
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
        log.info("*** inside generatePartyCaseFlags");
        Map<String, Object> data = new HashMap<>();
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            data.putAll(generateC100PartyCaseFlags(caseData, PartyRole.Representing.CAAPPLICANT));
            data.putAll(generateC100PartyCaseFlags(caseData, PartyRole.Representing.CAAPPLICANTSOLICITOR));
            data.putAll(generateC100PartyCaseFlags(caseData, PartyRole.Representing.CARESPONDENT));
            data.putAll(generateC100PartyCaseFlags(caseData, PartyRole.Representing.CARESPONDENTSOLCIITOR));
            data.putAll(generateC100PartyCaseFlags(caseData, PartyRole.Representing.CAOTHERPARTY));
        } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            data.putAll(generateC100PartyCaseFlags(caseData, PartyRole.Representing.DAAPPLICANT));
            data.putAll(generateC100PartyCaseFlags(caseData, PartyRole.Representing.DAAPPLICANTSOLICITOR));
            data.putAll(generateC100PartyCaseFlags(caseData, PartyRole.Representing.DARESPONDENT));
            data.putAll(generateC100PartyCaseFlags(caseData, PartyRole.Representing.DARESPONDENTSOLCIITOR));
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

            if (null != caElements) {
                Optional<Element<PartyDetails>> partyDetails = i < numElements ? Optional.of(caElements.get(i)) : Optional.empty();
                if (partyDetails.isPresent()) {
                    if (uk.gov.hmcts.reform.prl.enums.caseflags.PartyRole.Representing.CAAPPLICANT.equals(representing)) {
                        if (StringUtils.isEmpty(partyDetails.get().getValue().getPartyFullName())) {
                            data.putAll(partyLevelCaseFlagsGenerator.generateFlags(partyDetails.get().getValue().getPartyFullName(),
                                                                                   String.format(representing.getCaseDataField(),
                                                                                                 i + 1
                                                                                   ),
                                                                                   partyRole.getCaseRoleLabel()
                            ));
                        }
                    } else if (uk.gov.hmcts.reform.prl.enums.caseflags.PartyRole.Representing.CAAPPLICANTSOLICITOR.equals(
                        representing)) {
                        if (StringUtils.isEmpty(partyDetails.get().getValue().getRepresentativeFullNameForCaseFlags())) {
                            data.putAll(partyLevelCaseFlagsGenerator.generateFlags(partyDetails.get().getValue().getRepresentativeFullNameForCaseFlags(),
                                                                                   String.format(representing.getCaseDataField(),
                                                                                                 i + 1
                                                                                   ),
                                                                                   partyRole.getCaseRoleLabel()
                            ));
                        }

                    } else if (PartyRole.Representing.CARESPONDENT.equals(representing)) {
                        if (StringUtils.isEmpty(partyDetails.get().getValue().getPartyFullName())) {
                            data.putAll(partyLevelCaseFlagsGenerator.generateFlags(partyDetails.get().getValue().getPartyFullName(),
                                                                                   String.format(representing.getCaseDataField(),
                                                                                                 i + 1
                                                                                   ),
                                                                                   partyRole.getCaseRoleLabel()
                            ));
                        }
                    } else if (PartyRole.Representing.CARESPONDENTSOLCIITOR.equals(representing)) {
                        if (StringUtils.isEmpty(partyDetails.get().getValue().getRepresentativeFullNameForCaseFlags())) {
                            data.putAll(partyLevelCaseFlagsGenerator.generateFlags(partyDetails.get().getValue().getRepresentativeFullNameForCaseFlags(),
                                                                                   String.format(representing.getCaseDataField(),
                                                                                                 i + 1
                                                                                   ),
                                                                                   partyRole.getCaseRoleLabel()
                            ));
                        }
                    } else if (uk.gov.hmcts.reform.prl.enums.caseflags.PartyRole.Representing.CAOTHERPARTY.equals(
                        representing)) {
                        if (StringUtils.isEmpty(partyDetails.get().getValue().getPartyFullName())) {
                            data.putAll(partyLevelCaseFlagsGenerator.generateFlags(partyDetails.get().getValue().getPartyFullName(),
                                                                                   String.format(representing.getCaseDataField(),
                                                                                                 i + 1
                                                                                   ),
                                                                                   partyRole.getCaseRoleLabel()
                            ));
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

            if (null != partyDetails) {
                if (uk.gov.hmcts.reform.prl.enums.caseflags.PartyRole.Representing.DAAPPLICANT.equals(representing)) {
                    if (StringUtils.isEmpty(partyDetails.getPartyFullName())) {
                        data.putAll(partyLevelCaseFlagsGenerator.generateFlags(partyDetails.getPartyFullName(),
                                                                               String.format(representing.getCaseDataField(),
                                                                                             i + 1
                                                                               ),
                                                                               partyRole.getCaseRoleLabel()
                        ));
                    }
                } else if (uk.gov.hmcts.reform.prl.enums.caseflags.PartyRole.Representing.DAAPPLICANTSOLICITOR.equals(
                    representing)) {
                    if (StringUtils.isEmpty(partyDetails.getRepresentativeFullNameForCaseFlags())) {
                        data.putAll(partyLevelCaseFlagsGenerator.generateFlags(partyDetails.getRepresentativeFullNameForCaseFlags(),
                                                                               String.format(representing.getCaseDataField(),
                                                                                             i + 1
                                                                               ),
                                                                               partyRole.getCaseRoleLabel()
                        ));
                    }
                } else if (PartyRole.Representing.DARESPONDENT.equals(representing)) {
                    if (StringUtils.isEmpty(partyDetails.getPartyFullName())) {
                        data.putAll(partyLevelCaseFlagsGenerator.generateFlags(partyDetails.getPartyFullName(),
                                                                               String.format(representing.getCaseDataField(),
                                                                                             i + 1
                                                                               ),
                                                                               partyRole.getCaseRoleLabel()
                        ));
                    }
                } else if (PartyRole.Representing.DARESPONDENTSOLCIITOR.equals(representing)) {
                    if (StringUtils.isEmpty(partyDetails.getRepresentativeFullNameForCaseFlags())) {
                        data.putAll(partyLevelCaseFlagsGenerator.generateFlags(partyDetails.getRepresentativeFullNameForCaseFlags(),
                                                                               String.format(representing.getCaseDataField(),
                                                                                             i + 1
                                                                               ),
                                                                               partyRole.getCaseRoleLabel()
                        ));
                    }
                }
            }
        }
        return data;
    }
}
