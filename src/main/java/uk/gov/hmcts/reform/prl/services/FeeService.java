package uk.gov.hmcts.reform.prl.services;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.clients.FeesRegisterApi;
import uk.gov.hmcts.reform.prl.config.FeesConfig;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.CaApplicantOtherApplicationType;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.CaRespondentOtherApplicationType;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.DaApplicantOtherApplicationType;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.DaRespondentOtherApplicationType;
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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_SPACE_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.models.FeeType.C2_WITHOUT_NOTICE;
import static uk.gov.hmcts.reform.prl.models.FeeType.C2_WITH_NOTICE;
import static uk.gov.hmcts.reform.prl.models.FeeType.applicationToFeeMap;
import static uk.gov.hmcts.reform.prl.services.ApplicationsFeeCalculator.FL403_APPLICATION_TO_VARY_DISCHARGE_OR_EXTEND_AN_ORDER;

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

    private boolean checkIsHearingDate14DaysAway(String hearingDate) {
        boolean isHearingDate14DaysAway = false;
        if (null != hearingDate) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            LocalDateTime selectedHearingLocalDateTime = LocalDate.parse(
                hearingDate,
                formatter
            ).atStartOfDay();
            isHearingDate14DaysAway = (Duration.between(LocalDateTime.now(), selectedHearingLocalDateTime).toDays() >= 14L);
        }
        return isHearingDate14DaysAway;
    }

    private FeeType getFeeType(FeeRequest feeRequest) {

        FeeType feeType = null;
        if (feeRequest != null) {
            String awpApplicationType = feeRequest.getApplicationType();

            if (awpApplicationType.equals("C2")) {

                if (feeRequest.getHearingDate() == null
                    && feeRequest.getOtherPartyConsent() == null
                    && feeRequest.getNotice() == null) {
                    return C2_WITH_NOTICE;
                }

                // For Adjourn hearing
                if (feeRequest.getHearingDate() != null) {
                    boolean isHearingDate14DaysAway = checkIsHearingDate14DaysAway(feeRequest.getHearingDate());
                    return feeType = getFeeTypeByPartyConsentAndHearing(feeRequest.getOtherPartyConsent(), isHearingDate14DaysAway);
                }

                // For all other requests
                if (feeRequest.getHearingDate() == null) {
                    return feeType = getFeeTypeByPartyConsentAndNotice(feeRequest.getOtherPartyConsent(),feeRequest.getNotice());
                }

            } else {
                String otherApplicationType = getOtherApplicationType(feeRequest);
                log.info("otherApplicationType ==>  {}",otherApplicationType);
                Optional<FeeType> otherApplicationFeeType = fromApplicationType(otherApplicationType);
                if (feeRequest.getIsAdditionalApplicationBundle().equals("Yes")
                    && FL403_APPLICATION_TO_VARY_DISCHARGE_OR_EXTEND_AN_ORDER.equalsIgnoreCase(otherApplicationType)
                    && feeRequest.getExistingBundleForParty().equalsIgnoreCase("respondent")
                    && !feeRequest.getPartyType().equalsIgnoreCase("applicant")) {
                    otherApplicationFeeType =  Optional.of(C2_WITH_NOTICE);
                }

                return otherApplicationFeeType.isPresent() ? otherApplicationFeeType.get() : null;
            }
        }
        return feeType;
    }

    private static Optional<FeeType> fromApplicationType(String applicationType) {
        if (!applicationToFeeMap.containsKey(applicationType)) {
            return Optional.empty();
        }
        return Optional.of(applicationToFeeMap.get(applicationType));
    }

    private FeeType getFeeTypeByPartyConsentAndHearing(String partyConsent, boolean isHearingDate14DaysAway) {
        log.info("inside getFeeTypeByPartyConsent");
        Optional<FeeType> feeType = null;
        feeType = fromOtherPartyConsentAndHearing(partyConsent, isHearingDate14DaysAway);
        log.info("return getC2ApplicationsFeeTypes feeType " + feeType);
        return feeType.isPresent() ? feeType.get() : null;
    }

    private FeeType getFeeTypeByPartyConsentAndNotice(String partyConsent, String notice) {
        log.info("inside getFeeTypeByPartyConsent");
        Optional<FeeType> feeType = null;
        feeType = fromOtherPartyConsentAndNotice(partyConsent, notice);
        log.info("return getC2ApplicationsFeeTypes feeType " + feeType);
        return feeType.isPresent() ? feeType.get() : null;
    }



    private static Optional<FeeType> fromOtherPartyConsentAndHearing(String otherPartyConsent, boolean isHearingDate14DaysAway) {

        if (otherPartyConsent != null) {
            if (otherPartyConsent.equals("No")) {
                return Optional.of(C2_WITH_NOTICE);
            } else if (otherPartyConsent.equals("Yes") && !isHearingDate14DaysAway) {
                return Optional.of(C2_WITHOUT_NOTICE);
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.of(C2_WITH_NOTICE);
        }
    }

    private static Optional<FeeType> fromOtherPartyConsentAndNotice(String otherPartyConsent, String notice) {

        if (otherPartyConsent != null) {
            if (otherPartyConsent.equals("Yes") || (otherPartyConsent.equals("No") && notice.equals("No"))) {
                return Optional.of(C2_WITHOUT_NOTICE);
            } else if (otherPartyConsent.equals("No") && notice.equals("Yes")) {
                return Optional.of(C2_WITH_NOTICE);
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.of(C2_WITH_NOTICE);
        }
    }

    public FeeResponse fetchFeeCode(FeeRequest feeRequest) throws Exception {
        FeeResponse feeResponse = null;

        FeeType feeType = getFeeType(feeRequest);
        log.info("FEE TYPE --> {}",feeType);

        if (feeType != null) {
            feeResponse = fetchFeeDetails(feeType);
        }

        return feeResponse;
    }


    private static String getOtherApplicationType(FeeRequest feeRequest) {
        String otherApplicationType = new String();
        if (feeRequest.getCaseType().equals(C100_CASE_TYPE) && feeRequest.getPartyType().equals("applicant")) {
            Map<String, CaApplicantOtherApplicationType> enumNames = EnumUtils.getEnumMap(CaApplicantOtherApplicationType.class);
            CaApplicantOtherApplicationType caApplicantOtherApplicationType = null;

            for (var entry : enumNames.entrySet()) {
                if (entry.getKey().startsWith(feeRequest.getApplicationType())) {
                    caApplicantOtherApplicationType = entry.getValue();
                }
            }
            otherApplicationType = caApplicantOtherApplicationType.getId();

        } else if (feeRequest.getCaseType().equals(C100_CASE_TYPE) && feeRequest.getPartyType().equals("respondent")) {
            Map<String, CaRespondentOtherApplicationType> enumNames = EnumUtils.getEnumMap(CaRespondentOtherApplicationType.class);
            CaRespondentOtherApplicationType caRespondentOtherApplicationType = null;
            for (var entry : enumNames.entrySet()) {
                if (entry.getKey().startsWith(feeRequest.getApplicationType())) {
                    caRespondentOtherApplicationType = entry.getValue();
                }
            }
            otherApplicationType = caRespondentOtherApplicationType.getId();

        } else if (feeRequest.getCaseType().equals(FL401_CASE_TYPE) && feeRequest.getPartyType().equals("applicant")) {
            Map<String, DaApplicantOtherApplicationType> enumNames = EnumUtils.getEnumMap(DaApplicantOtherApplicationType.class);
            DaApplicantOtherApplicationType daApplicantOtherApplicationType = null;
            for (var entry : enumNames.entrySet()) {
                if (entry.getKey().startsWith(feeRequest.getApplicationType())) {
                    daApplicantOtherApplicationType = entry.getValue();
                }
            }
            otherApplicationType = daApplicantOtherApplicationType.getId();

        } else  if (feeRequest.getCaseType().equals(FL401_CASE_TYPE) && feeRequest.getPartyType().equals("respondent")) {
            Map<String, DaRespondentOtherApplicationType> enumNames = EnumUtils.getEnumMap(DaRespondentOtherApplicationType.class);
            DaRespondentOtherApplicationType daRespondentOtherApplicationType = null;
            for (var entry : enumNames.entrySet()) {
                if (entry.getKey().startsWith(feeRequest.getApplicationType())) {
                    daRespondentOtherApplicationType = entry.getValue();
                }
            }
            otherApplicationType = daRespondentOtherApplicationType.getId();

        } else {
            otherApplicationType = EMPTY_SPACE_STRING;
        }
        return otherApplicationType;
    }

}
