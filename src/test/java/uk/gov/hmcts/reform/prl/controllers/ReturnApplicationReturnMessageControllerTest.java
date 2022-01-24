package uk.gov.hmcts.reform.prl.controllers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.prl.enums.RejectReasonEnum.consentOrderNotProvided;


@PropertySource(value = "classpath:application.yaml")
@RunWith(SpringRunner.class)
public class ReturnApplicationReturnMessageControllerTest {


    @InjectMocks
    private ReturnApplicationReturnMessageController returnApplicationReturnMessageController;


    public static final String authToken = "Bearer TestAuthToken";

    CaseData casedata;


    @Test
    public void whenNoOptionSelectedThenNoRejectReasonSelectedReturnTrue() {
        casedata = CaseData.builder().build();

        assert returnApplicationReturnMessageController.noRejectReasonSelected(casedata);
    }

    @Test
    public void whenHasOptionSelectedThenNoRejectReasonSelectedReturnFalse() {

        casedata = CaseData.builder()
            .rejectReason(Collections.singletonList(consentOrderNotProvided))
            .build();

        assert !returnApplicationReturnMessageController.noRejectReasonSelected(casedata);
    }

    @Test
    public void whenNoApplicantGetLegalFullNameReturnConstantString(){
        casedata = CaseData.builder().build();

        assertEquals(returnApplicationReturnMessageController.getLegalFullName(casedata), "[Legal representative name]");
    }


}
