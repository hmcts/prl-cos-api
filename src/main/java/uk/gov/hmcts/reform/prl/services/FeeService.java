package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.prl.clients.FeesRegisterApi;
import uk.gov.hmcts.reform.prl.config.FeesConfig;
import uk.gov.hmcts.reform.prl.enums.AwpApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.OtherApplicationType;
import uk.gov.hmcts.reform.prl.exception.FeeRegisterException;
import uk.gov.hmcts.reform.prl.framework.exceptions.WorkflowException;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.payment.FeeRequest;
import uk.gov.hmcts.reform.prl.models.dto.payment.FeeResponseForCitizen;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

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
import static org.apache.logging.log4j.util.Strings.isBlank;
import static org.apache.logging.log4j.util.Strings.isNotBlank;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.NO;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.YES;
import static uk.gov.hmcts.reform.prl.enums.AwpApplicationReasonEnum.DELAY_CANCEL_HEARING_DATE;
import static uk.gov.hmcts.reform.prl.enums.AwpApplicationTypeEnum.FL403;
import static uk.gov.hmcts.reform.prl.models.FeeType.C2_WITHOUT_NOTICE;
import static uk.gov.hmcts.reform.prl.models.FeeType.C2_WITH_NOTICE;
import static uk.gov.hmcts.reform.prl.models.FeeType.FL403_EXTEND_AN_ORDER;
import static uk.gov.hmcts.reform.prl.models.FeeType.NO_FEE;
import static uk.gov.hmcts.reform.prl.models.FeeType.applicationToFeeMapForCitizen;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FeeService {

    public static final String ZERO_AMOUNT = "0.00";
    private final FeesConfig feesConfig;
    private final FeesRegisterApi feesRegisterApi;
    private final CoreCaseDataApi coreCaseDataApi;
    private final ObjectMapper objectMapper;

    public FeeResponse fetchFeeDetails(FeeType feeType) throws Exception {
        FeesConfig.FeeParameters parameters = feesConfig.getFeeParametersByFeeType(feeType);
        try {
            log.info("parameters are ===>" + objectMapper.writeValueAsString(parameters));
        } catch (JsonProcessingException e) {
            log.info("error");
        }
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
                log.info("feeType is ===>" + objectMapper.writeValueAsString(feeType));
                FeeResponse feeResponse = fetchFeeDetails(feeType);
                try {
                    log.info("feeResponse is ===>" + objectMapper.writeValueAsString(feeResponse));
                } catch (JsonProcessingException e) {
                    log.info("error");
                }
                feeResponses.add(feeResponse);
            } catch (Exception ex) {
                throw new FeeRegisterException(ex.getMessage());
            }
        });
        return getFeeResponseWithHighestCharges(feeResponses);
    }

    private Optional<FeeResponse> extractFeeToUse(List<FeeResponse> feeResponses) {
        try {
            log.info("feeResponse is ===>" + objectMapper.writeValueAsString(feeResponses));
        } catch (JsonProcessingException e) {
            log.info("error");
        }
        return ofNullable(feeResponses).stream()
            .flatMap(Collection::stream)
            .filter(Objects::nonNull)
            .max(Comparator.comparing(FeeResponse::getAmount));
    }

    private FeeResponse getFeeResponseWithHighestCharges(List<FeeResponse> feeResponses) {
        var feeResponse = extractFeeToUse(feeResponses);
        return feeResponse.isPresent() ? feeResponse.get() : null;
    }

    private boolean checkIsHearingDate14DaysAway(String hearingDate, String applicationReason) {
        boolean isHearingDate14DaysAway = false;
        if (onlyApplyingForAnAdjournment(applicationReason)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDateTime selectedHearingLocalDateTime = LocalDate.parse(
                hearingDate,
                formatter
            ).atStartOfDay();
            isHearingDate14DaysAway = (Duration.between(
                LocalDateTime.now(),
                selectedHearingLocalDateTime
            ).toDays() >= 14L);

        }
        return isHearingDate14DaysAway;
    }

    public boolean onlyApplyingForAnAdjournment(String applicationReason) {
        return applicationReason.equals(DELAY_CANCEL_HEARING_DATE.getId());
    }

    private FeeType getFeeType(FeeRequest feeRequest, CaseData caseData) {

        FeeType feeType = null;
        if (feeRequest != null) {
            String awpApplicationType = feeRequest.getApplicationType();

            if (AwpApplicationTypeEnum.C2.toString().equals(awpApplicationType)) {

                // feeCode logic at the time of citizen guidance page
                if (isBlank(feeRequest.getHearingDate())
                    && isBlank(feeRequest.getOtherPartyConsent())
                    && isBlank(feeRequest.getNotice())) {
                    return C2_WITH_NOTICE;
                }

                // For C2 - Adjourn Hearing
                if (isNotBlank(feeRequest.getHearingDate())) {
                    boolean isHearingDate14DaysAway = checkIsHearingDate14DaysAway(
                        feeRequest.getHearingDate(),
                        feeRequest.getApplicationReason()
                    );
                    return getFeeTypeByPartyConsentAndHearing(
                        feeRequest.getOtherPartyConsent(),
                        isHearingDate14DaysAway
                    );
                }

                // For C2 - All other requests
                return getFeeTypeByPartyConsentAndNotice(feeRequest.getOtherPartyConsent(), feeRequest.getNotice());

            } else {

                // For AWP types other than C2
                String key = (feeRequest.getCaseType() + "_" + feeRequest.getApplicationType() + "_" + feeRequest.getPartyType()).toUpperCase();
                feeType = applicationToFeeMapForCitizen.get(key);
                if (feeRequest.getApplicationType().equals(FL403.name())
                    && "respondent".equals(feeRequest.getPartyType())
                    && isFl403ApplicationAlreadyPresent(caseData)) {
                    feeType = FL403_EXTEND_AN_ORDER;
                }

                return feeType;
            }
        }
        return feeType;
    }

    public static boolean isFl403ApplicationAlreadyPresent(CaseData caseData) {
        boolean fl403ApplicationAlreadyPresent = false;
        if (CollectionUtils.isNotEmpty(caseData.getAdditionalApplicationsBundle())) {
            for (Element<AdditionalApplicationsBundle> additionalApplicationsBundle : caseData.getAdditionalApplicationsBundle()) {
                if (null != additionalApplicationsBundle.getValue().getOtherApplicationsBundle()
                    && OtherApplicationType.FL403_EXTEND_AN_ORDER.equals(
                    additionalApplicationsBundle.getValue().getOtherApplicationsBundle().getApplicationType())) {
                    fl403ApplicationAlreadyPresent = true;
                    break;
                }
            }
        }
        return fl403ApplicationAlreadyPresent;
    }

    private FeeType getFeeTypeByPartyConsentAndHearing(String partyConsent, boolean isHearingDate14DaysAway) {
        Optional<FeeType> feeType;
        feeType = fromOtherPartyConsentAndHearing(partyConsent, isHearingDate14DaysAway);
        return feeType.isPresent() ? feeType.get() : null;
    }

    private FeeType getFeeTypeByPartyConsentAndNotice(String partyConsent, String notice) {
        return fromOtherPartyConsentAndNotice(partyConsent, notice);
    }

    private static Optional<FeeType> fromOtherPartyConsentAndHearing(String otherPartyConsent, boolean isHearingDate14DaysAway) {
        if (YES.equals(otherPartyConsent)) {
            return isHearingDate14DaysAway ? Optional.of(NO_FEE) : Optional.of(C2_WITHOUT_NOTICE);
        } else if (NO.equals(otherPartyConsent)) {
            return Optional.of(C2_WITH_NOTICE);
        } else {
            return Optional.empty();
        }
    }

    private static FeeType fromOtherPartyConsentAndNotice(String otherPartyConsent, String notice) {

        if (YES.equals(otherPartyConsent)) {
            return C2_WITHOUT_NOTICE;
        } else {
            if (NO.equals(notice)) {
                return C2_WITHOUT_NOTICE;
            } else if (YES.equals(notice)) {
                return C2_WITH_NOTICE;
            } else {
                return null;
            }
        }
    }

    public FeeResponseForCitizen fetchFeeCode(FeeRequest feeRequest,
                                              String authorization,
                                              String serviceAuthorization) throws Exception {

        String caseId = feeRequest.getCaseId();

        FeeResponse feeResponse = null;
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = coreCaseDataApi.getCase(
            authorization,
            serviceAuthorization,
            caseId
        );

        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);

        FeeType feeType = getFeeType(feeRequest, caseData);
        if (feeType == null) {
            return FeeResponseForCitizen.builder()
                .errorRetrievingResponse("Invalid Parameters to fetch fee code").build();
        }

        if (NO_FEE.equals(feeType)) {
            return FeeResponseForCitizen.builder()
                .amount(ZERO_AMOUNT).build();
        } else {
            feeResponse = fetchFeeDetails(feeType);

            return FeeResponseForCitizen.builder()
                .amount(feeResponse.getAmount().toString())
                .feeType(feeType.toString())
                .build();

        }
    }
}
