package uk.gov.hmcts.reform.prl.controllers.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.controllers.AbstractCallbackController;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.caseflags.CaseFlagMigrationService;
import uk.gov.hmcts.reform.prl.services.caseflags.PartyLevelCaseFlagsService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.Map;

@Tag(name = "migration-controller")
@Slf4j
@RestController
@RequestMapping("/migrate-data")
@SecurityRequirement(name = "Bearer Authentication")
public class MigrationController extends AbstractCallbackController {
    @Qualifier("allTabsService")
    private final AllTabServiceImpl tabService;
    private final PartyLevelCaseFlagsService partyLevelCaseFlagsService;
    private final CaseFlagMigrationService caseFlagMigrationService;

    @Autowired
    public MigrationController(ObjectMapper objectMapper, EventService eventPublisher, AllTabServiceImpl tabService,
                               PartyLevelCaseFlagsService partyLevelCaseFlagsService, CaseFlagMigrationService caseFlagMigrationService) {
        super(objectMapper, eventPublisher);
        this.tabService = tabService;
        this.partyLevelCaseFlagsService = partyLevelCaseFlagsService;
        this.caseFlagMigrationService = caseFlagMigrationService;
    }


    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestHeader("Authorization")
                                                                    @Parameter(hidden = true) String authorisation,
                                                                    @RequestBody CallbackRequest callbackRequest) {

        Map<String, Object> caseDataMap = callbackRequest.getCaseDetails().getData();
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        caseDataMap.putAll(partyLevelCaseFlagsService.generatePartyCaseFlags(caseData));
        caseDataMap.putAll(caseFlagMigrationService.migrateCaseForCaseFlags(caseDataMap));
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataMap).build();
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest,
                                @RequestHeader(HttpHeaders.AUTHORIZATION)
                                @Parameter(hidden = true) String authorisation) {
        tabService.updateAllTabsIncludingConfTab(String.valueOf(callbackRequest.getCaseDetails().getId()));
    }
}
