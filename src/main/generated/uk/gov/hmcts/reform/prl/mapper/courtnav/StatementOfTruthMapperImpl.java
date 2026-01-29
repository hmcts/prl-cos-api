package uk.gov.hmcts.reform.prl.mapper.courtnav;

import java.time.LocalDate;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.complextypes.StatementOfTruth;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavStatementOfTruth;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-21T11:01:27+0000",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.9 (Amazon.com Inc.)"
)
@Component
public class StatementOfTruthMapperImpl implements StatementOfTruthMapper {

    @Override
    public StatementOfTruth map(CourtNavStatementOfTruth source) {
        if ( source == null ) {
            return null;
        }

        StatementOfTruth.StatementOfTruthBuilder statementOfTruth = StatementOfTruth.builder();

        statementOfTruth.applicantConsent( StatementOfTruthMapper.mapConsentList( source.getApplicantConsent() ) );
        statementOfTruth.signature( source.getSignature() );
        statementOfTruth.fullname( source.getFullname() );
        statementOfTruth.nameOfFirm( source.getNameOfFirm() );
        statementOfTruth.signOnBehalf( source.getSignOnBehalf() );

        statementOfTruth.date( LocalDate.parse(source.getDate().mergeDate()) );

        return statementOfTruth.build();
    }
}
