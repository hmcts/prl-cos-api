package uk.gov.hmcts.reform.prl;

import com.google.common.io.Resources;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLib;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLibConfigurer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class CftLibConfig implements CFTLibConfigurer {

    @Override
    public void configure(CFTLib lib) throws Exception {
        createCcdRoles(lib);
        configureRoleAssignments(lib);
        importCcdDefinitions(lib);
    }

    private void createCcdRoles(CFTLib lib) {
        lib.createRoles(
            "caseworker-privatelaw-judge",
            "caseworker-privatelaw-courtadmin",
            "caseworker-privatelaw-la",
            "caseworker-privatelaw-superuser",
            "caseworker-privatelaw-solicitor",
            "caseworker-privatelaw-systemupdate",
            "caseworker-privatelaw-readonly",
            "citizen",
            "courtnav",
            "payments",
            "caseworker-wa-task-configuration",
            "caseworker-ras-validation",
            "GS_profile",
            "caseworker-caa",
            "caseworker-privatelaw-bulkscansystemupdate",
            "caseworker-privatelaw-bulkscan",
            "pui-case-manager",
            "caseworker-approver",
            "caseworker-privatelaw-cafcass",
            "caseworker-privatelaw-externaluser-viewonly",
            "tribunal-caseworker",
            "allocated-magistrate",
            "ctsc-team-leader",
            "hearing-centre-team-leader",
            "hearing-centre-admin",
            "judge",
            "senior-tribunal-caseworker",
            "caseworker-privatelaw-courtadmin-casecreator",
            "ctsc"
        );
    }

    private void configureRoleAssignments(CFTLib lib) throws IOException {
        var json = Resources.toString(Resources.getResource("cftlib-am-role-assignments.json"), StandardCharsets.UTF_8);
        lib.configureRoleAssignments(json);
    }

    private void importCcdDefinitions(CFTLib lib) throws IOException {
        var def = Files.readAllBytes(Path.of("build/definitionsToBeImported/ccd-config-PRL-local.xlsx"));
        lib.importDefinition(def);
    }
}
