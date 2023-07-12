package uk.gov.hmcts.reform.prl.services;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.clients.FeesRegisterApi;
import uk.gov.hmcts.reform.prl.config.FeesConfig;
import uk.gov.hmcts.reform.prl.exception.FeeRegisterException;
import uk.gov.hmcts.reform.prl.framework.exceptions.WorkflowException;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.dto.payment.FeeRequest;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.models.FeeType.C2_WITHOUT_NOTICE;
import static uk.gov.hmcts.reform.prl.models.FeeType.C2_WITH_NOTICE;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FeeService {

    @Autowired
    private FeesConfig feesConfig;

    @Autowired
    private FeesRegisterApi feesRegisterApi;

    public FeeResponse fetchFeeDetails(FeeType feeType) throws Exception {
        FeesConfig.FeeParameters parameters = feesConfig.getFeeParametersByFeeType(feeType);
        try {
            return feesRegisterApi.findFee(
                parameters.getChannel(),
                parameters.getEvent(),
                parameters.getJurisdiction1(),
                parameters.getJurisdiction2(),
                parameters.getKeyword(),
                parameters.getService()
            );
        } catch (FeignException ex) {
            log.error("Fee response error for {}\n\tstatus: {} => message: \"{}\"",
                      parameters, ex.status(), ex.contentUTF8(), ex
            );

            throw new WorkflowException(ex.getMessage(), ex);
        }
    }


    public FeeResponse getFeesDataForAdditionalApplications(List<FeeType> applicationsFeeTypes) {
        List<FeeResponse> feeResponses = new ArrayList<>();
        applicationsFeeTypes.stream().forEach(feeType -> {
            try {
                FeeResponse feeResponse = fetchFeeDetails(feeType);
                feeResponses.add(feeResponse);
            } catch (Exception ex) {
                throw new FeeRegisterException(ex.getMessage());
            }
        });
        return getFeeResponseWithHighestCharges(feeResponses);
    }

    private Optional<FeeResponse> extractFeeToUse(List<FeeResponse> feeResponses) {
        return ofNullable(feeResponses).stream()
            .flatMap(Collection::stream)
            .filter(Objects::nonNull)
            .max(Comparator.comparing(FeeResponse::getAmount));
    }

    private FeeResponse getFeeResponseWithHighestCharges(List<FeeResponse> feeResponses) {
        var feeResponse = extractFeeToUse(feeResponses);
        return feeResponse.isPresent() ? feeResponse.get() : null;
    }

    private boolean shouldSkipPayments(FeeRequest feeRequest) {
        boolean skipPayments = false;
        if (null != feeRequest.getHearingDate()) {
            String selectedHearingDate = feeRequest.getHearingDate();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            LocalDateTime selectedHearingLocalDateTime = LocalDate.parse(
                selectedHearingDate,
                formatter
            ).atStartOfDay();
            skipPayments = (Duration.between(LocalDateTime.now(), selectedHearingLocalDateTime).toDays() >= 14L);
        }
        return skipPayments;
    }

    private FeeType getFeeType(FeeRequest feeRequest) {

        FeeType feeType = null;
        if (feeRequest != null) {
            if (feeRequest.getApplicationType().equals("C2")) {
                boolean skipPayments = shouldSkipPayments(feeRequest);
                feeType = getFeeTypeByPartyConsent(feeRequest, skipPayments);
            } else {
                log.info("other type of application logic");
            }

        }
        return feeType;
    }

    private FeeType getFeeTypeByPartyConsent(FeeRequest feeRequest, boolean skipPayment) {
        log.info("inside getFeeTypeByPartyConsent");
        Optional<FeeType> feeType = null;
        feeType = fromOtherPartyConsent(feeRequest.getOtherPartyConsent(), skipPayment);
        log.info("return getC2ApplicationsFeeTypes feeType " + feeType);
        return feeType.isPresent() ? feeType.get() : null;
    }

    private static Optional<FeeType> fromOtherPartyConsent(String otherPartyConsent, boolean skipPayment) {
        if (otherPartyConsent.equals("No")) {
            return Optional.of(C2_WITH_NOTICE);
        } else if (otherPartyConsent.equals("Yes") && !skipPayment) {
            return Optional.of(C2_WITHOUT_NOTICE);
        } else {
            return Optional.empty();
        }
    }

    public FeeResponse fetchFeeCode(FeeRequest feeRequest) throws Exception {
        FeeResponse feeResponse = null;

        FeeType feeType = getFeeType(feeRequest);
        log.info("Feetype==== {}",feeType);

        if (feeType != null) {
            feeResponse = fetchFeeDetails(feeType);
        }

        return feeResponse;
    }

    private static boolean isFl403ApplicationAlreadyPresent(FeeRequest feeRequest) {
        boolean fl403ApplicationAlreadyPresent = false;
        return fl403ApplicationAlreadyPresent;
    }


}
