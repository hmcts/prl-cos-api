package uk.gov.hmcts.reform.prl.services;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.clients.FeesRegisterApi;
import uk.gov.hmcts.reform.prl.config.FeesConfig;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FeeService {

    private final FeesConfig feesConfig;
    private final FeesRegisterApi feesRegisterApi;

    public FeeResponse fetchFeeDetails(FeeType feeType) throws Exception {
        FeesConfig.FeeParameters parameters = feesConfig.getFeeParametersByFeeType(feeType);
        try {
            FeeResponse fee = feesRegisterApi.findFee(
                parameters.getChannel(),
                parameters.getEvent(),
                parameters.getJurisdiction1(),
                parameters.getJurisdiction2(),
                parameters.getKeyword(),
                parameters.getService()
            );

            return fee;
        } catch (FeignException ex) {
            log.error("Fee response error for {}\n\tstatus: {} => message: \"{}\"",
                      parameters, ex.status(), ex.contentUTF8(), ex
            );

            throw new Exception(ex);
        }
    }
}
