package uk.gov.hmcts.reform.prl.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.gov.hmcts.reform.prl.models.ChallengeQuestionsResult;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@FeignClient(
        name = "ccd-definition-store-api",
        url = "${ccd_definition_store.api.url}",
        configuration = FeignClientProperties.FeignClientConfiguration.class
)
public interface CcdDefinitionStoreApi {

    String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/api/display/challenge-questions/case-type/{ctid}/question-groups/{id}",
            headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    ChallengeQuestionsResult fetchChallengeQuestions(
            @RequestHeader(AUTHORIZATION) String authorisation,
            @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
            @PathVariable("ctid") String caseTypeId,
            @PathVariable("id") String challengeQuestionId
    );
}

