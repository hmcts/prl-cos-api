package uk.gov.hmcts.reform.prl.services.caseflags;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.caseflags.flagdetails.FlagDetail;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.datamigration.caseflag.CaseFlag;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CaseFlagMigrationService {
    private static final String FLAG_TYPE = "PARTY";
    private static final String DA_APPLICANT = "DA_APPLICANT";
    private static final String DA_RESPONDENT = "DA_RESPONDENT";
    private static final String CA_APPLICANT = "CA_APPLICANT_%s";
    private static final String CA_APPLICANT_1 = "CA_APPLICANT_1";
    private static final String CA_APPLICANT_2 = "CA_APPLICANT_2";
    private static final String CA_APPLICANT_3 = "CA_APPLICANT_3";
    private static final String CA_APPLICANT_4 = "CA_APPLICANT_4";
    private static final String CA_APPLICANT_5 = "CA_APPLICANT_5";
    private static final String CA_RESPONDENT = "CA_RESPONDENT_%s";
    private static final String CA_RESPONDENT_1 = "CA_RESPONDENT_1";
    private static final String CA_RESPONDENT_2 = "CA_RESPONDENT_2";
    private static final String CA_RESPONDENT_3 = "CA_RESPONDENT_3";
    private static final String CA_RESPONDENT_4 = "CA_RESPONDENT_4";
    private static final String CA_RESPONDENT_5 = "CA_RESPONDENT_5";

    private final SystemUserService systemUserService;
    private final RefDataUserService refDataUserService;
    private final ObjectMapper objectMapper;

    public Map<String, Object> migrateCaseForCaseFlags(Map<String, Object> caseDataMap) {
        log.info("making ref data call to get case flags");
        CaseFlag caseFlag = refDataUserService.retrieveCaseFlags(systemUserService.getSysUserToken(), FLAG_TYPE);
        List<uk.gov.hmcts.reform.prl.models.dto.datamigration.caseflag.FlagDetail> flagDetail
            = caseFlag.getFlags().get(0).getFlagDetails();
        log.info("ref data call for case flags completed");

        CaseData caseData = objectMapper.convertValue(
            caseDataMap,
            CaseData.class
        );
        if (PrlAppsConstants.FL401_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            setFL401CaseFlags(caseData, flagDetail);
        } else if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            setC100CaseFlags(caseData, flagDetail);
        }
        return objectMapper.convertValue(caseData.getAllPartyFlags(), Map.class);
    }

    private void setFL401CaseFlags(CaseData caseData, List<uk.gov.hmcts.reform.prl.models.dto.datamigration.caseflag.FlagDetail> flagDetail) {
        PartyDetails applicant = caseData.getApplicantsFL401();
        if (Objects.nonNull(applicant.getPartyLevelFlag())) {
            setCaseFlagsToCaseFlagsNewDataModel(
                applicant.getPartyLevelFlag(),
                flagDetail,
                DA_APPLICANT,
                caseData
            );
        }
        PartyDetails respondent = caseData.getRespondentsFL401();

        if (Objects.nonNull(respondent.getPartyLevelFlag())) {
            setCaseFlagsToCaseFlagsNewDataModel(
                respondent.getPartyLevelFlag(),
                flagDetail,
                DA_RESPONDENT,
                caseData
            );
        }
    }

    private void setC100CaseFlags(CaseData caseData, List<uk.gov.hmcts.reform.prl.models.dto.datamigration.caseflag.FlagDetail> flagDetail) {
        List<Element<PartyDetails>> applicants = caseData.getApplicants();
        int applicantIndex = 0;
        for (Element<PartyDetails> applicantElement : applicants) {
            applicantIndex++;
            PartyDetails applicant = applicantElement.getValue();

            if (Objects.nonNull(applicant.getPartyLevelFlag())) {
                setCaseFlagsToCaseFlagsNewDataModel(
                    applicant.getPartyLevelFlag(),
                    flagDetail,
                    String.format(CA_APPLICANT, applicantIndex),
                    caseData
                );
            }

        }
        List<Element<PartyDetails>> respondents = caseData.getRespondents();
        int respondentIndex = 0;
        for (Element<PartyDetails> respondentElement : respondents) {
            respondentIndex++;
            PartyDetails respondent = respondentElement.getValue();

            if (Objects.nonNull(respondent.getPartyLevelFlag())) {
                setCaseFlagsToCaseFlagsNewDataModel(
                    respondent.getPartyLevelFlag(),
                    flagDetail,
                    String.format(CA_RESPONDENT, respondentIndex),
                    caseData
                );
            }

        }
    }

    private void setCaseFlagsToCaseFlagsNewDataModel(Flags partyLevelFlags,
                                                     List<uk.gov.hmcts.reform.prl.models.dto.datamigration.caseflag.FlagDetail> refDataCaseFlags,
                                                     String partyType, CaseData caseData) {
        if (Objects.nonNull(partyLevelFlags) && CollectionUtils.isNotEmpty(partyLevelFlags.getDetails())) {
            partyLevelFlags.getDetails().forEach(flag -> findMatchingRefDataFlagAndMigrate(
                refDataCaseFlags,
                partyType,
                caseData,
                flag
            ));

        }
    }

    private void findMatchingRefDataFlagAndMigrate(List<uk.gov.hmcts.reform.prl.models.dto.datamigration.caseflag.FlagDetail> refDataCaseFlags,
                                                   String partyType, CaseData caseData, Element<FlagDetail> flag) {
        refDataCaseFlags.forEach(refDataCaseFlag -> {
            if (refDataCaseFlag.getFlagCode().equals(flag.getValue().getFlagCode())) {
                addToExternalOrInternalFlags(partyType, flag, caseData, refDataCaseFlag.getExternallyAvailable());
            } else if (CollectionUtils.isNotEmpty(refDataCaseFlag.getChildFlags())) {
                findMatchingRefDataFlagAndMigrate(
                    refDataCaseFlag.getChildFlags(),
                    partyType,
                    caseData,
                    flag
                );
            }
        });
    }

    private void addToExternalOrInternalFlags(String partyType, Element<FlagDetail> flag, CaseData caseData, boolean isExternal) {
        switch (partyType) {
            case DA_APPLICANT: {
                setCaseFlags(flag, caseData.getAllPartyFlags().getDaApplicantInternalFlags(),
                             caseData.getAllPartyFlags().getDaApplicantExternalFlags(), isExternal
                );
                break;
            }
            case DA_RESPONDENT: {
                setCaseFlags(flag, caseData.getAllPartyFlags().getDaRespondentInternalFlags(),
                             caseData.getAllPartyFlags().getDaRespondentExternalFlags(), isExternal
                );
                break;
            }
            case CA_APPLICANT_1: {
                setCaseFlags(flag, caseData.getAllPartyFlags().getCaApplicant1InternalFlags(),
                             caseData.getAllPartyFlags().getCaApplicant1ExternalFlags(), isExternal
                );
                break;
            }
            case CA_APPLICANT_2: {
                setCaseFlags(flag, caseData.getAllPartyFlags().getCaApplicant2InternalFlags(),
                             caseData.getAllPartyFlags().getCaApplicant2ExternalFlags(), isExternal
                );
                break;
            }
            case CA_APPLICANT_3: {

                setCaseFlags(flag, caseData.getAllPartyFlags().getCaApplicant3InternalFlags(),
                             caseData.getAllPartyFlags().getCaApplicant3ExternalFlags(), isExternal
                );
                break;
            }
            case CA_APPLICANT_4: {
                setCaseFlags(flag, caseData.getAllPartyFlags().getCaApplicant4InternalFlags(),
                             caseData.getAllPartyFlags().getCaApplicant4ExternalFlags(), isExternal
                );
                break;
            }
            case CA_APPLICANT_5: {
                setCaseFlags(flag, caseData.getAllPartyFlags().getCaApplicant5InternalFlags(),
                             caseData.getAllPartyFlags().getCaApplicant5ExternalFlags(), isExternal
                );
                break;
            }
            case CA_RESPONDENT_1: {
                setCaseFlags(flag, caseData.getAllPartyFlags().getCaRespondent1InternalFlags(),
                             caseData.getAllPartyFlags().getCaRespondent1ExternalFlags(), isExternal
                );
                break;
            }
            case CA_RESPONDENT_2: {
                setCaseFlags(flag, caseData.getAllPartyFlags().getCaRespondent2InternalFlags(),
                             caseData.getAllPartyFlags().getCaRespondent2ExternalFlags(), isExternal
                );
                break;
            }
            case CA_RESPONDENT_3: {
                setCaseFlags(flag, caseData.getAllPartyFlags().getCaRespondent3InternalFlags(),
                             caseData.getAllPartyFlags().getCaRespondent3ExternalFlags(), isExternal
                );
                break;
            }
            case CA_RESPONDENT_4: {
                setCaseFlags(flag, caseData.getAllPartyFlags().getCaRespondent4InternalFlags(),
                             caseData.getAllPartyFlags().getCaRespondent4ExternalFlags(), isExternal
                );
                break;
            }
            case CA_RESPONDENT_5: {
                setCaseFlags(flag, caseData.getAllPartyFlags().getCaRespondent5InternalFlags(),
                             caseData.getAllPartyFlags().getCaRespondent5ExternalFlags(), isExternal
                );
                break;
            }
            default: {
                break;
            }
        }
    }

    private void setCaseFlags(Element<FlagDetail> flag,
                              Flags internalFlags,
                              Flags externalFlags,
                              boolean isExternal) {
        (isExternal ? externalFlags : internalFlags).getDetails().add(flag);
    }
}
