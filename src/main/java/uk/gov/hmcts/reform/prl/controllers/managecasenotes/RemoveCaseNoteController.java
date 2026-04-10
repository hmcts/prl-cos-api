package uk.gov.hmcts.reform.prl.controllers.managecasenotes;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.prl.controllers.AbstractCallbackController;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;

@Slf4j
@RestController
@RequestMapping("/remove-case-notes")
@SecurityRequirement(name = "Bearer Authentication")
public class RemoveCaseNoteController extends AbstractCallbackController {

    private final AuthorisationService authorisationService;

    @Autowired
    public RemoveCaseNoteController(ObjectMapper objectMapper,
                                    EventService eventPublisher,
                                    AuthorisationService authorisationService) {
        super(objectMapper, eventPublisher);
        this.authorisationService = authorisationService;
    }
}
