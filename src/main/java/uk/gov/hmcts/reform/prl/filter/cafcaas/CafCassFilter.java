package uk.gov.hmcts.reform.prl.filter.cafcaas;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.stream.Collectors;

@Service
@Slf4j
public class CafCassFilter {

    @Autowired
    private PostcodeLookupService postcodeLookupService;

    public void filter(CafCassResponse cafCassResponse) {
        setNonNullEmptyElementList(cafCassResponse);
        //        filterCasesByApplicationValidPostcode(cafCassResponse);
        cafCassResponse.setTotal(cafCassResponse.getCases().size());
    }

    /**
     *  This method will filter List of Element type objects present in
     *  caseData object.
     *
     * @param cafCassResponse - CafCassResponse
     */
    private void setNonNullEmptyElementList(CafCassResponse cafCassResponse) {
        cafCassResponse.getCases().forEach(cafCassCaseDetail -> {
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
        log.info("total number of records after applying england postcode filters - {}", cafCassResponse.getCases().size());
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
            boolean isPostCodeValid = false;
            try {
                isPostCodeValid = postcodeLookupService.isValidNationalPostCode(
                    address.getPostCode(),
                    CafcassAppConstants.ENGLAND_POSTCODE_NATIONALCODE
                );

                return isPostCodeValid;

            } catch (Exception e) {
                log.error("Postcode Lookup Failed for postcode {} - {} ", address.getPostCode(), e.getMessage());
            }
        }
        return false;
    }
}
