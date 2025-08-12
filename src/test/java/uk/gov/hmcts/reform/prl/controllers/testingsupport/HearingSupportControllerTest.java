package uk.gov.hmcts.reform.prl.controllers.testingsupport;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.HearingRequest;
import uk.gov.hmcts.reform.prl.services.hearingmanagement.HearingManagementService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.prl.enums.State.PREPARE_FOR_HEARING_CONDUCT_HEARING;

@ExtendWith(MockitoExtension.class)
class HearingSupportControllerTest {

    @Mock
    private HearingManagementService hearingManagementService;

    @InjectMocks
    private HearingSupportController hearingSupportController;

    @Test
    void testPrepareForHearing() {
        hearingSupportController.prepareForHearing(HearingRequest.builder().build());
        verify(hearingManagementService)
            .caseStateChangeForHearingManagement(any(HearingRequest.class), eq(PREPARE_FOR_HEARING_CONDUCT_HEARING));
    }

    @Test
    void testControllerIsEnabled() {
        ResponseEntity<Object> responseEntity = hearingSupportController.isEnabled();
        assertThat(responseEntity.getStatusCode())
            .isEqualTo(HttpStatus.OK);
    }
}
