package uk.gov.hmcts.reform.prl.filter.cafcaas;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.constants.cafcass.CafcassAppConstants;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.Address;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.ApplicantDetails;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassCaseData;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassCaseDetail;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassResponse;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.Element;
import uk.gov.hmcts.reform.prl.services.cafcass.PostcodeLookupService;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CafCassFilter {
    public static final String CAFCAAS_CASE_TYPE_OF_APPLICATION_LIST_NOT_CONFIGURED = "cafcaas.caseTypeOfApplicationList not configured";
    @Value("#{'${cafcaas.caseTypeOfApplicationList}'.split(',')}")
    private List<String> caseTypeList;

    @Value("#{'${cafcaas.caseState}'.split(',')}")
    private List<String> caseStateList;

    @Autowired
    private PostcodeLookupService postcodeLookupService;

    public void filter(CafCassResponse cafCassResponse) {
        log.info("222222 {} ",cafCassResponse);
        if (caseTypeList != null && !caseTypeList.isEmpty()) {
            log.info("333");
            caseTypeList = caseTypeList.stream().map(String::trim).collect(Collectors.toList());
            caseStateList = caseStateList.stream().map(String::trim).collect(Collectors.toList());
            filterCaseByApplicationCaseType(cafCassResponse);
            filterCasesByApplicationValidPostcode(cafCassResponse);
            cafCassResponse.setTotal(cafCassResponse.getCases().size());
        } else {
            log.info("44444");
            log.error(CAFCAAS_CASE_TYPE_OF_APPLICATION_LIST_NOT_CONFIGURED);
        }
    }

    private void filterCaseByApplicationCaseType(CafCassResponse cafCassResponse) {
        List<CafCassCaseDetail> cafCassCaseDetailList = cafCassResponse.getCases().stream()
            .filter(filterByCaseTypeAndState())
            .collect(Collectors.toList());
        cafCassResponse.setCases(cafCassCaseDetailList);
    }

    private Predicate<CafCassCaseDetail> filterByCaseTypeAndState() {
        return cafCassCaseDetail -> caseTypeList.contains(cafCassCaseDetail.getCaseData().getCaseTypeOfApplication())
            && caseStateList.contains(cafCassCaseDetail.getState());
    }

    private void filterCasesByApplicationValidPostcode(CafCassResponse cafCassResponse) {
        log.info("5555555");
        log.info("cafCassResponse==> {}",cafCassResponse);
        log.info("cafCassResponse.getCases()==> {}",cafCassResponse.getCases());
        for (CafCassCaseDetail cafCassCaseDetail: cafCassResponse.getCases()) {
            log.info("inside for 11=> {}",cafCassCaseDetail);
            log.info("inside for 22=> {}",cafCassCaseDetail.getCaseData());
            log.info("inside for 33=> {}",cafCassCaseDetail.getCaseData().getApplicants());
        }
        List<CafCassCaseDetail> cafCassCaseDetailList = cafCassResponse.getCases()
            .stream().filter(cafCassCaseDetail -> {
                if (!ObjectUtils.isEmpty(cafCassCaseDetail.getCaseData().getApplicants())) {
                    log.info("66666666 {}",cafCassCaseDetail);
                    log.info("6161616161 {}",cafCassCaseDetail.getCaseData());
                    return hasApplicantValidPostcode(cafCassCaseDetail.getCaseData());
                } else {
                    return false;
                }
            }).collect(Collectors.toList());
        log.info("justtt bfr 6666 {}",cafCassCaseDetailList);
        log.info("after 6666666666");
        cafCassResponse.setCases(cafCassCaseDetailList);
    }

    private boolean hasApplicantValidPostcode(CafCassCaseData cafCassCaseData) {
        log.info("7777777");
        for (Element<ApplicantDetails> applicantDetails: cafCassCaseData.getApplicants()) {
            if (isAddressValid(applicantDetails)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAddressValid(Element<ApplicantDetails> applicationDetails) {
        log.info("8888888");
        if (!ObjectUtils.isEmpty(applicationDetails.getValue())
            && !ObjectUtils.isEmpty(applicationDetails.getValue().getAddress())) {
            log.info("9999999");
            Address address = applicationDetails.getValue().getAddress();
            return postcodeLookupService.isValidNationalPostCode(address.getPostCode(),
                                                                 CafcassAppConstants.ENGLAND_POSTCODE_NATIONALCODE);
        }
        return false;
    }
}
