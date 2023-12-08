package uk.gov.hmcts.reform.prl.services.caseflags;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.dto.caseflag.CaseFlag;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
public class CaseFlagMigrationService {


    @Autowired
    private SystemUserService systemUserService;

    private static final String FLAG_TYPE = "PARTY";

    @Autowired
    RefDataUserService refDataUserService;


    public Map<String, Object> migrateCaseForCaseFlags(Map<String, Object> caseDataMap, CaseData caseData) {

        log.info("making refdata call");
        CaseFlag caseFlag = refDataUserService.retrieveCaseFlags(systemUserService.getSysUserToken(), FLAG_TYPE);
        log.info("refdata call completed");


        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            caseData.getApplicants().forEach(applicant -> {
                if (Objects.nonNull(applicant.getValue().getPartyLevelFlag())) {
                    Flags flags = applicant.getValue().getPartyLevelFlag();
                    log.info("list of case flags {} for the user {}", flags, applicant.getId());
                    if (Objects.nonNull(flags.getDetails()) && CollectionUtils.isNotEmpty(flags.getDetails())) {
                        flags.getDetails().forEach(flag -> {

                        });

                    }

                }
            });

            caseData.getRespondents().forEach(respondent -> {
                if (Objects.nonNull(respondent.getValue().getPartyLevelFlag())) {
                    Flags flags = respondent.getValue().getPartyLevelFlag();
                    log.info("list of case flags {} for the user {}", flags, respondent.getId());


                }
            });

        } else {

            if (Objects.nonNull(caseData.getApplicantsFL401())) {
                Flags flags = caseData.getApplicantsFL401().getPartyLevelFlag();
                log.info("list of case flags {} for the user {}", flags, caseData.getApplicantsFL401().getFirstName());
            }
            if (Objects.nonNull(caseData.getRespondentsFL401())) {
                Flags flags = caseData.getApplicantsFL401().getPartyLevelFlag();
                log.info("list of case flags {} for the user {}", flags, caseData.getRespondentsFL401().getFirstName());
            }

        }


        return caseDataMap;
    }

    private boolean isExternalFlag(String flagCode) {

        return true;
    }


}
