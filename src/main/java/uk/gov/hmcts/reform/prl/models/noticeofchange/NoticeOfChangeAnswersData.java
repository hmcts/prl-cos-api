package uk.gov.hmcts.reform.prl.models.noticeofchange;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NoticeOfChangeAnswersData {
    NoticeOfChangeParties caApplicant1;
    NoticeOfChangeParties caApplicant2;
    NoticeOfChangeParties caApplicant3;
    NoticeOfChangeParties caApplicant4;
    NoticeOfChangeParties caApplicant5;
    NoticeOfChangeParties caRespondent1;
    NoticeOfChangeParties caRespondent2;
    NoticeOfChangeParties caRespondent3;
    NoticeOfChangeParties caRespondent4;
    NoticeOfChangeParties caRespondent5;
    NoticeOfChangeParties daApplicant;
    NoticeOfChangeParties daRespondent;
}
