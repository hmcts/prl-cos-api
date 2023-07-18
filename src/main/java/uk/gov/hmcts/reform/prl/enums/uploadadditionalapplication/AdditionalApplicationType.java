package uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
public enum AdditionalApplicationType {

    C1_CHILD_ORDER(
        "C1_CHILD_ORDER",
        "C1 - Apply for certain orders under the Children Act"
    ),
    C2_CHANGE_SURNAME_OR_REMOVE_JURISDICTION(
        "C2_CHANGE_SURNAME_OR_REMOVE_JURISDICTION",
        "Change surname or remove from jurisdiction."
    ),
    C2_APPOINTMENT_OF_GUARDIAN(
        "C2_APPOINTMENT_OF_GUARDIAN",
        "Appointment of a guardian"
    ),
    C2_TERMINATION_OF_APPOINTMENT_OF_GUARDIAN(
        "C2_TERMINATION_OF_APPOINTMENT_OF_GUARDIAN",
        "Termination of appointment of a guardian"
    ),
    C2_PARENTAL_RESPONSIBILITY(
        "C2_PARENTAL_RESPONSIBILITY",
        "Parental responsibility"
    ),
    C2_REQUESTING_ADJOURNMENT(
        "C2_REQUESTING_ADJOURNMENT",
        "Requesting an adjournment for a scheduled hearing"
    ),
    C3_CHILD_ORDER(
        "C3_CHILD_ORDER",
        "C3 - Application for an order authorizing search and taking charge of a child"
    ),
    C4_CHILD_ORDER(
        "C4_CHILD_ORDER",
        "C4 - Application for an order for disclosure of a child’s whereabouts"
    ),
    C79_CHILD_ORDER(
        "C79_CHILD_ORDER",
        "C79 - Application to enforce a child arrangements order"
    ),
    EX740_CROSS_EXAMINATION_VICTIM(
        "EX740_CROSS_EXAMINATION_VICTIM",
        "EX740 - Application to prohibit cross examination (victim)"
    ),
    EX741_CROSS_EXAMINATION_PERPETRATOR(
        "EX741_CROSS_EXAMINATION_PERPETRATOR",
        "EX741 - Application to prohibit cross examination (perpetrator)"
    ),
    FP25_WITNESS_SUMMONS(
        "FP25_WITNESS_SUMMONS",
        "FP25 - Witness summons"
    ),
    FC600_COMMITTAL_APPLICATION(
        "FC600_COMMITTAL_APPLICATION",
        "FC600 - Committal application"
    ),
    N161_APPELLANT_NOTICE(
        "N161_APPELLANT_NOTICE",
        "N161 - Appellant’s notice"
    ),
    D89_BAILIFF(
        "D89_BAILIFF",
        "D89 - Bailiff"
    ),
    FL403_EXTEND_AN_ORDER(
        "FL403_EXTEND_AN_ORDER",
        "FL403 - Application to vary, discharge or extend an order"
    ),
    FL407_ARREST_WARRANT(
        "FL407_ARREST_WARRANT",
        "FL407 - Application for a warrant of arrest"
    );

    private final String id;
    private final String displayValue;

    @Getter
    public enum AwPListing {
        CAAPPLICANT(
            List.of(
                C1_CHILD_ORDER,
                C3_CHILD_ORDER,
                C4_CHILD_ORDER,
                C79_CHILD_ORDER,
                EX740_CROSS_EXAMINATION_VICTIM,
                EX741_CROSS_EXAMINATION_PERPETRATOR,
                FP25_WITNESS_SUMMONS,
                FC600_COMMITTAL_APPLICATION,
                N161_APPELLANT_NOTICE,
                D89_BAILIFF
            ),
            List.of(
                C2_CHANGE_SURNAME_OR_REMOVE_JURISDICTION,
                C2_APPOINTMENT_OF_GUARDIAN,
                C2_TERMINATION_OF_APPOINTMENT_OF_GUARDIAN,
                C2_PARENTAL_RESPONSIBILITY,
                C2_REQUESTING_ADJOURNMENT
            )
        ),
        CARESPONDENT(
            List.of(
                C1_CHILD_ORDER,
                C3_CHILD_ORDER,
                C4_CHILD_ORDER,
                EX740_CROSS_EXAMINATION_VICTIM,
                EX741_CROSS_EXAMINATION_PERPETRATOR,
                FP25_WITNESS_SUMMONS,
                FC600_COMMITTAL_APPLICATION,
                N161_APPELLANT_NOTICE,
                D89_BAILIFF
            ),
            List.of(
                C2_CHANGE_SURNAME_OR_REMOVE_JURISDICTION,
                C2_APPOINTMENT_OF_GUARDIAN,
                C2_TERMINATION_OF_APPOINTMENT_OF_GUARDIAN,
                C2_PARENTAL_RESPONSIBILITY,
                C2_REQUESTING_ADJOURNMENT
            )
        ),
        DAAPPLICANT(
            List.of(
                FL403_EXTEND_AN_ORDER,
                EX740_CROSS_EXAMINATION_VICTIM,
                EX741_CROSS_EXAMINATION_PERPETRATOR,
                FP25_WITNESS_SUMMONS,
                FL407_ARREST_WARRANT,
                FC600_COMMITTAL_APPLICATION,
                N161_APPELLANT_NOTICE,
                D89_BAILIFF
            ),
            List.of(
                C2_CHANGE_SURNAME_OR_REMOVE_JURISDICTION,
                C2_APPOINTMENT_OF_GUARDIAN,
                C2_TERMINATION_OF_APPOINTMENT_OF_GUARDIAN,
                C2_PARENTAL_RESPONSIBILITY,
                C2_REQUESTING_ADJOURNMENT
            )
        ),
        DARESPONDENT(
            List.of(
                FL403_EXTEND_AN_ORDER,
                EX740_CROSS_EXAMINATION_VICTIM,
                EX741_CROSS_EXAMINATION_PERPETRATOR,
                FP25_WITNESS_SUMMONS,
                N161_APPELLANT_NOTICE,
                D89_BAILIFF
            ),
            List.of(
                C2_CHANGE_SURNAME_OR_REMOVE_JURISDICTION,
                C2_APPOINTMENT_OF_GUARDIAN,
                C2_TERMINATION_OF_APPOINTMENT_OF_GUARDIAN,
                C2_PARENTAL_RESPONSIBILITY,
                C2_REQUESTING_ADJOURNMENT
            )
        ),
        CITIZEN(
            List.of(C1_CHILD_ORDER, C3_CHILD_ORDER, C4_CHILD_ORDER),
            null
        );


        List<AdditionalApplicationType> otherApplicationTypes;
        List<AdditionalApplicationType> c2ApplicationTypes;

        AwPListing(List<AdditionalApplicationType> otherApplicationTypes, List<AdditionalApplicationType> c2ApplicationTypes) {
            this.otherApplicationTypes = otherApplicationTypes;
            this.c2ApplicationTypes = c2ApplicationTypes;
        }
    }
}
