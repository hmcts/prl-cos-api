package uk.gov.hmcts.reform.prl.services.validators.eventschecker;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.prl.services.validators.AllegationsOfHarmChecker;
import uk.gov.hmcts.reform.prl.services.validators.AllegationsOfHarmRevisedChecker;
import uk.gov.hmcts.reform.prl.services.validators.ApplicationTypeChecker;
import uk.gov.hmcts.reform.prl.services.validators.AttendingTheHearingChecker;
import uk.gov.hmcts.reform.prl.services.validators.CaseNameChecker;
import uk.gov.hmcts.reform.prl.services.validators.FL401ApplicantFamilyChecker;
import uk.gov.hmcts.reform.prl.services.validators.FL401ApplicationTypeChecker;
import uk.gov.hmcts.reform.prl.services.validators.FL401OtherProceedingsChecker;
import uk.gov.hmcts.reform.prl.services.validators.FL401ResubmitChecker;
import uk.gov.hmcts.reform.prl.services.validators.FL401StatementOfTruthAndSubmitChecker;
import uk.gov.hmcts.reform.prl.services.validators.HearingUrgencyChecker;
import uk.gov.hmcts.reform.prl.services.validators.HomeChecker;
import uk.gov.hmcts.reform.prl.services.validators.InternationalElementChecker;
import uk.gov.hmcts.reform.prl.services.validators.LitigationCapacityChecker;
import uk.gov.hmcts.reform.prl.services.validators.MiamChecker;
import uk.gov.hmcts.reform.prl.services.validators.MiamPolicyUpgradeChecker;
import uk.gov.hmcts.reform.prl.services.validators.OtherProceedingsChecker;
import uk.gov.hmcts.reform.prl.services.validators.PdfChecker;
import uk.gov.hmcts.reform.prl.services.validators.SubmitAndPayChecker;
import uk.gov.hmcts.reform.prl.services.validators.SubmitChecker;
import uk.gov.hmcts.reform.prl.services.validators.WelshLanguageRequirementsChecker;
import uk.gov.hmcts.reform.prl.services.validators.WithoutNoticeOrderChecker;

@Data
public class CommonChecker {

    // Common attributes between C100 and FL401
    @Autowired
    private CaseNameChecker caseNameChecker;

    @Autowired
    private ApplicationTypeChecker applicationTypeChecker;


    @Autowired
    private OtherProceedingsChecker otherProceedingsChecker;

    @Autowired
    private AttendingTheHearingChecker attendingTheHearingChecker;

    @Autowired
    private WelshLanguageRequirementsChecker welshLanguageRequirementsChecker;

    @Autowired
    private PdfChecker pdfChecker;



    // C100 specific attributes

    @Autowired
    private HearingUrgencyChecker hearingUrgencyChecker;

    @Autowired
    private MiamChecker miamChecker;

    @Autowired
    private MiamPolicyUpgradeChecker miamPolicyUpgradeChecker;

    @Autowired
    private AllegationsOfHarmChecker allegationsOfHarmChecker;

    @Autowired
    private AllegationsOfHarmRevisedChecker allegationsOfHarmRevisedChecker;

    @Autowired
    private InternationalElementChecker internationalElementChecker;

    @Autowired
    private LitigationCapacityChecker litigationCapacityChecker;

    @Autowired
    private SubmitAndPayChecker submitAndPayChecker;


    // FL-401 attributes
    @Autowired
    private HomeChecker homeChecker;

    @Autowired
    private FL401ApplicationTypeChecker fl401ApplicationTypeChecker;

    @Autowired
    private FL401ApplicantFamilyChecker fl401ApplicantFamilyChecker;

    @Autowired
    private FL401StatementOfTruthAndSubmitChecker fl401StatementOfTruthAndSubmitChecker;

    @Autowired
    private WithoutNoticeOrderChecker withoutNoticeOrderChecker;

    @Autowired
    private FL401OtherProceedingsChecker fl401OtherProceedingsChecker;

    @Autowired
    private SubmitChecker submitChecker;

    @Autowired
    private FL401ResubmitChecker fl401ResubmitChecker;
}
