package uk.gov.hmcts.reform.prl.filter.cafcaas;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassCaseData;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassResponse;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.Element;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CafCassFilter {

    public void filter(CafCassResponse cafCassResponse) {
        setNonNullEmptyElementList(cafCassResponse);
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
     * @return null
     */
    public <T> List<Element<T>>  filterNonValueList(List<Element<T>> object) {
        if (object != null && !object.isEmpty()) {
            return object.stream().filter(element -> element.getValue() != null).toList();
        }

        return Collections.emptyList();
    }
}
