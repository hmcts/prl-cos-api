package uk.gov.hmcts.reform.prl.models.noticeofchange;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NoticeOfChangeAnswersData {
    NoticeOfChangeParties respondent0;
    NoticeOfChangeParties respondent1;
    NoticeOfChangeParties respondent2;
    NoticeOfChangeParties respondent3;
    NoticeOfChangeParties respondent4;
    NoticeOfChangeParties respondent5;
    NoticeOfChangeParties respondent6;
    NoticeOfChangeParties respondent7;
    NoticeOfChangeParties respondent8;
    NoticeOfChangeParties respondent9;
}
