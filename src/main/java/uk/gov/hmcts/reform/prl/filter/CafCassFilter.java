package uk.gov.hmcts.reform.prl.filter;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CafCassFilter {

    public void filterCasesByApplicationValidPostcode(CafCassResponse cafCassResponse) {
        List<CafCassCaseDetail> cafCassCaseDetailList = cafCassResponse.getCases()
            .stream().map(cafCassCaseDetail ->
                              updateCafCassCaseDetails(cafCassCaseDetail)).collect(Collectors.toList());
        cafCassResponse.setCases(cafCassCaseDetailList);
    }

    private CafCassCaseDetail updateCafCassCaseDetails(CafCassCaseDetail cafCassCaseDetail) {
        cafCassCaseDetail.setCases(filterByApplicantValidPostcode(cafCassCaseDetail.getCases()));
        return cafCassCaseDetail;
    }

    private CaseData filterByApplicantValidPostcode(CaseData caseData) {
        List<Applicant> applicantList = caseData
            .getApplicants()
            .stream()
            .filter(applicant ->
                        isAddressValid(applicant.getMValue().getMAddress())).collect(Collectors.toList());
        caseData.setApplicants(applicantList);
        return caseData;
    }

    private boolean isAddressValid(Address mAddress) {
        //TODO: call the postcode api
        String mPostCode = mAddress.getMPostCode();
        return true;
    }
}
