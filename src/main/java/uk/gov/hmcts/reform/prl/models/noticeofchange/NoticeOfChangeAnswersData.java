package uk.gov.hmcts.reform.prl.models.noticeofchange;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.reform.prl.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerApproverCaseworkerCaaCitizenCruAccess;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NoticeOfChangeAnswersData {
    @CCD(label = " ", access = {DefaultAccess.class, CaseworkerApproverCaseworkerCaaCitizenCruAccess.class})
    NoticeOfChangeParties caApplicant1;
    @CCD(label = " ", access = {DefaultAccess.class, CaseworkerApproverCaseworkerCaaCitizenCruAccess.class})
    NoticeOfChangeParties caApplicant2;
    @CCD(label = " ", access = {DefaultAccess.class, CaseworkerApproverCaseworkerCaaCitizenCruAccess.class})
    NoticeOfChangeParties caApplicant3;
    @CCD(label = " ", access = {DefaultAccess.class, CaseworkerApproverCaseworkerCaaCitizenCruAccess.class})
    NoticeOfChangeParties caApplicant4;
    @CCD(label = " ", access = {DefaultAccess.class, CaseworkerApproverCaseworkerCaaCitizenCruAccess.class})
    NoticeOfChangeParties caApplicant5;
    @CCD(label = " ", access = {DefaultAccess.class, CaseworkerApproverCaseworkerCaaCitizenCruAccess.class})
    NoticeOfChangeParties caRespondent1;
    @CCD(label = " ", access = {DefaultAccess.class, CaseworkerApproverCaseworkerCaaCitizenCruAccess.class})
    NoticeOfChangeParties caRespondent2;
    @CCD(label = " ", access = {DefaultAccess.class, CaseworkerApproverCaseworkerCaaCitizenCruAccess.class})
    NoticeOfChangeParties caRespondent3;
    @CCD(label = " ", access = {DefaultAccess.class, CaseworkerApproverCaseworkerCaaCitizenCruAccess.class})
    NoticeOfChangeParties caRespondent4;
    @CCD(label = " ", access = {DefaultAccess.class, CaseworkerApproverCaseworkerCaaCitizenCruAccess.class})
    NoticeOfChangeParties caRespondent5;
    @CCD(label = " ", access = {DefaultAccess.class, CaseworkerApproverCaseworkerCaaCitizenCruAccess.class})
    NoticeOfChangeParties daApplicant;
    @CCD(label = " ", access = {DefaultAccess.class, CaseworkerApproverCaseworkerCaaCitizenCruAccess.class})
    NoticeOfChangeParties daRespondent;
}
