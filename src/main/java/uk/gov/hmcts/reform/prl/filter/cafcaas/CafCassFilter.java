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
        if (caseTypeList != null && !caseTypeList.isEmpty()) {
            caseTypeList = caseTypeList.stream().map(String::trim).collect(Collectors.toList());
            caseStateList = caseStateList.stream().map(String::trim).collect(Collectors.toList());
            filterCaseByApplicationCaseType(cafCassResponse);
            //filterCasesByApplicationValidPostcode(cafCassResponse);
            cafCassResponse.setTotal(cafCassResponse.getCases().size());
        } else {
            log.error(CAFCAAS_CASE_TYPE_OF_APPLICATION_LIST_NOT_CONFIGURED);
        }
    }

    private void filterCaseByApplicationCaseType(CafCassResponse cafCassResponse) {
        log.info("Cafcass response before filtering -> {}", cafCassResponse.getCases().size());
        List<CafCassCaseDetail> cafCassCaseDetailList = cafCassResponse.getCases().stream()
            .filter(filterByCaseTypeAndState())
            .collect(Collectors.toList());
        log.info("Cafcaas records after filtering -> {}", (cafCassCaseDetailList != null && cafCassCaseDetailList.size() != 0)
            ? cafCassCaseDetailList.size() : 0);

        setNonNullEmptyElementList(cafCassCaseDetailList);

        cafCassResponse.setCases(cafCassCaseDetailList);
    }


    /**
     *  This method will filter List of Element type objects present in
     *  caseData object.
     *
     * @param cafCassCaseDetailList - List of CafcassCaseDetail
     */
    private void setNonNullEmptyElementList(List<CafCassCaseDetail> cafCassCaseDetailList) {

        if (cafCassCaseDetailList != null && !cafCassCaseDetailList.isEmpty()) {
            cafCassCaseDetailList.forEach(cafCassCaseDetail -> {
                CafCassCaseData caseData = cafCassCaseDetail.getCaseData();

                final CafCassCaseData cafCassCaseData = caseData.toBuilder().applicants(filterNonValueList(caseData.getApplicants()))
                    .otherPeopleInTheCaseTable(filterNonValueList(caseData.getOtherPeopleInTheCaseTable()))
                    .respondents(filterNonValueList(caseData.getRespondents()))
                    .children(filterNonValueList(caseData.getChildren()))
                    .interpreterNeeds(filterNonValueList(caseData.getInterpreterNeeds()))
                    .otherDocuments(filterNonValueList(caseData.getOtherDocuments()))
                    .manageOrderCollection(filterNonValueList(caseData.getManageOrderCollection()))
                    .orderCollection(filterNonValueList(caseData.getOrderCollection()))
                    .build();

                cafCassCaseDetail.setCaseData(cafCassCaseData);

            });

        }
    }

    /**
     *  This method will accept List of Element object
     *  and will return the list back if value object is not null.
     *
     * @param object - List of Element object
     * @param <T> - Type of element in the List
     * @return
     */
    public <T> List<Element<T>>  filterNonValueList(List<Element<T>> object) {
        if (object != null && !object.isEmpty()) {
            return object.stream().filter(element -> element.getValue() != null).collect(
                Collectors.toList());
        }

        return null;
    }

    private Predicate<CafCassCaseDetail> filterByCaseTypeAndState() {
        return cafCassCaseDetail -> caseTypeList.contains(cafCassCaseDetail.getCaseData().getCaseTypeOfApplication())
            && caseStateList.contains(cafCassCaseDetail.getState());
    }

    private void filterCasesByApplicationValidPostcode(CafCassResponse cafCassResponse) {

        List<CafCassCaseDetail> cafCassCaseDetailList = cafCassResponse.getCases()
            .stream().filter(cafCassCaseDetail -> {
                if (!ObjectUtils.isEmpty(cafCassCaseDetail.getCaseData().getApplicants())) {
                    return hasApplicantValidPostcode(cafCassCaseDetail.getCaseData());
                } else {
                    return false;
                }
            }).collect(Collectors.toList());
        cafCassResponse.setCases(cafCassCaseDetailList);
    }

    private boolean hasApplicantValidPostcode(CafCassCaseData cafCassCaseData) {
        for (Element<ApplicantDetails> applicantDetails: cafCassCaseData.getApplicants()) {
            if (isAddressValid(applicantDetails)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAddressValid(Element<ApplicantDetails> applicationDetails) {
        if (!ObjectUtils.isEmpty(applicationDetails.getValue())
            && !ObjectUtils.isEmpty(applicationDetails.getValue().getAddress())) {
            Address address = applicationDetails.getValue().getAddress();
            return postcodeLookupService.isValidNationalPostCode(address.getPostCode(),
                                                                 CafcassAppConstants.ENGLAND_POSTCODE_NATIONALCODE);
        }
        return false;
    }
}
