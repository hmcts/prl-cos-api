package uk.gov.hmcts.reform.prl;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.lucene.document.FieldType;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeQuestion {

    private String caseTypeId;
    private Integer order;
    private String questionText;
    private FieldType answerFieldType;
    private String displayContextParameter;
    private String challengeQuestionId;
    private String answerField;
    private String questionId;
    private boolean ignoreNullFields;
}
