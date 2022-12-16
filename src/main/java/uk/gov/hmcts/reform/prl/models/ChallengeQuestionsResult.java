package uk.gov.hmcts.reform.prl.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.ChallengeQuestion;

import java.util.List;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ChallengeQuestionsResult {

    private List<ChallengeQuestion> questions;
}