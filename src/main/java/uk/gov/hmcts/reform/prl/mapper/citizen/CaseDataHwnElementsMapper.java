package uk.gov.hmcts.reform.prl.mapper.citizen;

import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildHearingWithoutNoticeElements;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataMapper.COMMA_SEPARATOR;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataMapper.HYPHEN_SEPARATOR;


public class CaseDataHwnElementsMapper {

    private CaseDataHwnElementsMapper() {
    }

    private static final String DETAILS_OF_NOTICE_OTHER_PEOPLE_WILL_DO_SOMETHING = "Details of without notice "
            + "hearing because the other person or people may do something that would obstruct the order";

    public static void updateHearingWithoutNoticeElementsForCaseData(CaseData.CaseDataBuilder<?,?> caseDataBuilder,
                                                               C100RebuildHearingWithoutNoticeElements c100RebuildHearingWithoutNoticeElements) {
        caseDataBuilder
                .doYouNeedAWithoutNoticeHearing(c100RebuildHearingWithoutNoticeElements.getDoYouNeedHearingWithoutNotice())
                .reasonsForApplicationWithoutNotice(buildReasonsForApplicationWithoutNotice(
                        c100RebuildHearingWithoutNoticeElements))
                .doYouRequireAHearingWithReducedNotice(c100RebuildHearingWithoutNoticeElements
                        .getDoYouNeedHearingWithoutNoticeWithoutReducedNotice())
                .setOutReasonsBelow(c100RebuildHearingWithoutNoticeElements
                        .getDoYouNeedHearingWithoutNoticeWithoutReducedNoticeDetails());
    }

    private static String buildReasonsForApplicationWithoutNotice(
            C100RebuildHearingWithoutNoticeElements c100RebuildHearingWithoutNoticeElements) {
        if (Yes.equals(c100RebuildHearingWithoutNoticeElements.getDoYouNeedHearingWithoutNoticeAsOtherPplDoSomething())) {
            return c100RebuildHearingWithoutNoticeElements.getReasonsOfHearingWithoutNotice()
                    + COMMA_SEPARATOR + DETAILS_OF_NOTICE_OTHER_PEOPLE_WILL_DO_SOMETHING
                    + HYPHEN_SEPARATOR +  c100RebuildHearingWithoutNoticeElements
                    .getDoYouNeedHearingWithoutNoticeAsOtherPplDoSomethingDetails();
        } else {
            return c100RebuildHearingWithoutNoticeElements.getReasonsOfHearingWithoutNotice();
        }
    }

}
