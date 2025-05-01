package org.tanzu.cfpulse.cf;

import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.organizations.OrganizationSummary;
import org.cloudfoundry.operations.spaces.SpaceSummary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CfService {
    private final DefaultCloudFoundryOperations.Builder defaultCloudFoundryOperationsBuilder;

    public CfService(DefaultCloudFoundryOperations.Builder defaultCloudFoundryOperationsBuilder) {
        this.defaultCloudFoundryOperationsBuilder = defaultCloudFoundryOperationsBuilder;
    }

    public CloudFoundryOperations createCloudFoundryOperations(String org, String space) {
        DefaultCloudFoundryOperations.Builder builder = defaultCloudFoundryOperationsBuilder;
        if (org != null && !org.isEmpty()) {
            builder = builder.organization(org);
        }
        if (space != null && !space.isEmpty()) {
            builder = builder.space(space);
        }
        return builder.build();
    }

    public List<OrganizationSummary> allOrgs() {
        return createCloudFoundryOperations(null, null).organizations().list().collectList().block();
    }

    public List<SpaceSummary> allSpaces(String org) {
        return createCloudFoundryOperations(org, null).spaces().list().collectList().block();
    }
}
