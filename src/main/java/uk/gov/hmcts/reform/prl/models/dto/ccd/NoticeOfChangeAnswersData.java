package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NoticeOfChangeAnswersData {
    NoticeOfChangeAnswers noticeOfChangeAnswers0;
    NoticeOfChangeAnswers noticeOfChangeAnswers1;
}
