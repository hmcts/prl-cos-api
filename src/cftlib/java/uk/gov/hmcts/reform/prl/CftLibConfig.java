package uk.gov.hmcts.reform.prl;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLib;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLibConfigurer;


@Component
public class CftLibConfig implements CFTLibConfigurer {
    @Override
    public void configure(CFTLib lib) throws Exception {
        lib.createRoles(
            "caseworker-privatelaw-judge",
            "caseworker-privatelaw-courtadmin",
            "caseworker-privatelaw-la",
            "caseworker-privatelaw-superuser",
            "caseworker-privatelaw-solicitor",
            "caseworker-privatelaw-systemupdate",
            "citizen"
        );

        //var def = Files.readAllBytes(Path.of("build/ccd-config/ccd-A58-dev.xlsx"));
        //lib.importDefinition(def);
    }
}

