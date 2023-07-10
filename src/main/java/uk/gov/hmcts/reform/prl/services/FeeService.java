package uk.gov.hmcts.reform.prl.services;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.clients.FeesRegisterApi;
import uk.gov.hmcts.reform.prl.config.FeesConfig;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.C2ApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.CaApplicantOtherApplicationType;
import uk.gov.hmcts.reform.prl.exception.FeeRegisterException;
import uk.gov.hmcts.reform.prl.framework.exceptions.WorkflowException;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.OtherApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.dto.ccd.UploadAdditionalApplicationData;
import uk.gov.hmcts.reform.prl.models.dto.payment.FeeCodeRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_SPACE_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401;
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

    private FeeType getFeeeType(FeeCodeRequest feeCodeRequest) {
        FeeType feeType = null;
        if (feeCodeRequest != null) {
            if(feeCodeRequest.getApplicationType().equals("C2")){
                if (feeCodeRequest.getOtherPartyConsent().equals("Yes")) {
                    return C2_WITH_NOTICE;
                } else if (feeCodeRequest.getOtherPartyConsent().equals("No")) {
                    return C2_WITHOUT_NOTICE;
                } else {
                    return null;
                }
            }

        }
        return feeType;
    }

    private static String getOtherApplicationType(FeeCodeRequest feeCodeRequest) {
        String otherApplicationType = EMPTY_SPACE_STRING;

        if(feeCodeRequest.getPartyType().equals("applicant")){
            switch (feeCodeRequest.getCaseType()) {
                case C100:
                   // otherApplicationType = CaApplicantOtherApplicationType
                    break;
                case FL401:

                default:
                    throw new IllegalStateException("Unknown Case type");
            }



        } else if(feeCodeRequest.getPartyType().equals("applicant")){

        } else {
            otherApplicationType = EMPTY_SPACE_STRING;
        }

        return otherApplicationType;
    }


    public FeeResponse fetchFeeCode(FeeCodeRequest feeCodeRequest) throws Exception {
        FeeResponse feeResponse = null;

        boolean fl403ApplicationAlreadyPresent = isFl403ApplicationAlreadyPresent(feeCodeRequest);
        CaApplicantOtherApplicationType caApplicantOtherApplicationType = CaApplicantOtherApplicationType.N161_APPELLANT_NOTICE_CA;
        OtherApplicationsBundle otherApplicationsBundle = OtherApplicationsBundle.builder()
            .caApplicantApplicationType(caApplicantOtherApplicationType).build();

        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .typeOfC2Application(C2ApplicationTypeEnum.applicationWithNotice)
            .temporaryOtherApplicationsBundle(otherApplicationsBundle)
            .build();

        FeeType feeType = getFeeeType(feeCodeRequest);

        feeResponse = fetchFeeDetails(FeeType.C100_SUBMISSION_FEE);

        return feeResponse;
    }

    private static boolean isFl403ApplicationAlreadyPresent(FeeCodeRequest feeCodeRequest) {
        boolean fl403ApplicationAlreadyPresent = false;
        return fl403ApplicationAlreadyPresent;
    }


}
