package org.tanzu.cfpulse.cf;

import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.applications.*;
import org.cloudfoundry.operations.organizations.OrganizationSummary;
import org.cloudfoundry.operations.services.*;
import org.cloudfoundry.operations.spaceadmin.GetSpaceQuotaRequest;
import org.cloudfoundry.operations.spaceadmin.SpaceQuota;
import org.cloudfoundry.operations.spaces.SpaceSummary;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Component
public class CfTools {

    /*
        Applications
     */
    private static final String APPLICATION_LIST = "Return the applications (apps) in my Cloud Foundry space";
    private static final String ORG_PARAM = "Name of the Cloud Foundry organization";
    private static final String SPACE_PARAM = "Name of the Cloud Foundry space";
    private static final String APPLICATION_DETAILS = "Gets detailed information about a Cloud Foundry application";
    private static final String PUSH_APPLICATION = "Push an application JAR file to the Cloud Foundry space.";
    private static final String NAME_PARAM = "Name of the Cloud Foundry application";
    private static final String PATH_PARAM = "Fully qualified directory pathname to the compiled JAR file for the application";
    private static final String NO_START_PARAM = "Set this flag to true if you want to explicitly prevent the app from starting after being pushed.";
    private static final String SCALE_APPLICATION = "Scale the number of instances, memory, or disk size of an application. ";
    private static final String INSTANCES_PARAM = "The new number of instances of the Cloud Foundry application";
    private static final String MEMORY_PARAM = "The memory limit, in megabytes, of the Cloud Foundry application";
    private static final String DISK_PARAM = "The disk size, in megabytes, of the Cloud Foundry application";
    private static final String START_APPLICATION = "Start a Cloud Foundry application";
    private static final String STOP_APPLICATION = "Stop a running Cloud Foundry application";
    private static final String RESTART_APPLICATION = "Restart a running Cloud Foundry application";
    private static final String DELETE_APPLICATION = "Delete a Cloud Foundry application";
    /*
        Organizations
     */
    private static final String ORGANIZATION_LIST = "Return the organizations (orgs) in my Cloud Foundry foundation";
    /*
        Services
    */
    private static final String SERVICE_INSTANCE_LIST = "Return the service instances (SIs) in my Cloud Foundry space";
    private static final String SERVICE_INSTANCE_DETAIL = "Get detailed information about a service instance in my Cloud Foundry space";
    private static final String SERVICE_OFFERINGS_LIST = "Return the service offerings available to me in the Cloud Foundry marketplace";
    private static final String BIND_SERVICE_INSTANCE = "Bind a service instance to a Cloud Foundry application";
    private static final String SI_NAME_PARAM = "Name of the Cloud Foundry service instance";
    private static final String UNBIND_SERVICE_INSTANCE = "Unbind a service instance from a Cloud Foundry application";
    private static final String DELETE_SERVICE_INSTANCE = "Delete a service instance from a Cloud Foundry space";
    private static final String CREATE_USER_PROVIDED_SERVICE_INSTANCE = "Creates a user provided service instance (cups) in the Cloud Foundry space";
    private static final String CREDENTIALS_PARAM = "Key/value pairs for credentials that will be part of the user provided service instance";
    private static final String TAGS_PARAM = "Tags that will be associated with the user provided service instance";
    /*
        Spaces
     */
    private static final String SPACE_LIST = "Returns the spaces in my Cloud Foundry organization (org)";
    private static final String GET_SPACE_QUOTA = "Returns a quota (set of resource limits) scoped to a Cloud Foundry space";
    private static final String SPACE_QUOTA_NAME_PARAM = "Name of the Cloud Foundry space quota";
    private final DefaultCloudFoundryOperations.Builder defaultCloudFoundryOperationsBuilder;

    public CfTools(DefaultCloudFoundryOperations.Builder defaultCloudFoundryOperationsBuilder) {
        this.defaultCloudFoundryOperationsBuilder = defaultCloudFoundryOperationsBuilder;
    }

    private CloudFoundryOperations cloudFoundryOperations(String org, String space) {
        return defaultCloudFoundryOperationsBuilder.organization(org).space(space).build();
    }

    @Tool(description = APPLICATION_LIST)
    public List<ApplicationSummary> applicationsList(@ToolParam(description = ORG_PARAM) String org, @ToolParam(description = SPACE_PARAM) String space) {
        return cloudFoundryOperations(org, space).applications().list().collectList().block();
    }

    @Tool(description = APPLICATION_DETAILS)
    public ApplicationDetail applicationDetails(@ToolParam(description = NAME_PARAM) String applicationName,
                                                @ToolParam(description = ORG_PARAM) String org, @ToolParam(description = SPACE_PARAM) String space) {
        GetApplicationRequest request = GetApplicationRequest.builder().name(applicationName).build();
        return cloudFoundryOperations(org, space).applications().get(request).block();
    }

    @Tool(description = PUSH_APPLICATION)
    public void pushApplication(@ToolParam(description = NAME_PARAM) String applicationName,
                                @ToolParam(description = PATH_PARAM) String path,
                                @ToolParam(description = NO_START_PARAM, required = false) Boolean noStart,
                                @ToolParam(description = MEMORY_PARAM, required = false) Integer memory,
                                @ToolParam(description = DISK_PARAM, required = false) Integer disk,
                                @ToolParam(description = ORG_PARAM) String org,
                                @ToolParam(description = SPACE_PARAM) String space) {
        PushApplicationRequest request = PushApplicationRequest.builder().
                name(applicationName).
                path(Paths.get(path)).
                noStart(true).
                buildpack("java_buildpack_offline").
                memory(memory).
                diskQuota(disk).
                build();
        cloudFoundryOperations(org, space).applications().push(request).block();

        SetEnvironmentVariableApplicationRequest envRequest = SetEnvironmentVariableApplicationRequest.builder().
                name(applicationName).variableName("JBP_CONFIG_OPEN_JDK_JRE").variableValue("{ jre: { version: 17.+ } }").
                build();
        cloudFoundryOperations(org, space).applications().setEnvironmentVariable(envRequest).block();

        if (noStart == null || !noStart) {
            StartApplicationRequest startApplicationRequest = StartApplicationRequest.builder().
                    name(applicationName).
                    build();
            cloudFoundryOperations(org, space).applications().start(startApplicationRequest).block();
        }
    }

    @Tool(description = SCALE_APPLICATION)
    public void scaleApplication(@ToolParam(description = NAME_PARAM) String applicationName,
                                 @ToolParam(description = INSTANCES_PARAM, required = false) Integer instances,
                                 @ToolParam(description = MEMORY_PARAM, required = false) Integer memory,
                                 @ToolParam(description = DISK_PARAM, required = false) Integer disk,
                                 @ToolParam(description = ORG_PARAM) String org,
                                 @ToolParam(description = SPACE_PARAM) String space) {
        ScaleApplicationRequest scaleApplicationRequest = ScaleApplicationRequest.builder().
                name(applicationName).
                instances(instances).
                diskLimit(disk).
                memoryLimit(memory).
                build();
        cloudFoundryOperations(org, space).applications().scale(scaleApplicationRequest).block();
    }

    @Tool(description = START_APPLICATION)
    public void startApplication(@ToolParam(description = NAME_PARAM) String applicationName,
                                 @ToolParam(description = ORG_PARAM) String org,
                                 @ToolParam(description = SPACE_PARAM) String space) {
        StartApplicationRequest startApplicationRequest = StartApplicationRequest.builder().
                name(applicationName).
                build();
        cloudFoundryOperations(org, space).applications().start(startApplicationRequest).block();
    }

    @Tool(description = STOP_APPLICATION)
    public void stopApplication(@ToolParam(description = NAME_PARAM) String applicationName,
                                @ToolParam(description = ORG_PARAM) String org,
                                @ToolParam(description = SPACE_PARAM) String space) {
        StopApplicationRequest stopApplicationRequest = StopApplicationRequest.builder().
                name(applicationName).
                build();
        cloudFoundryOperations(org, space).applications().stop(stopApplicationRequest).block();
    }

    @Tool(description = RESTART_APPLICATION)
    public void restartApplication(@ToolParam(description = NAME_PARAM) String applicationName,
                                   @ToolParam(description = ORG_PARAM) String org,
                                   @ToolParam(description = SPACE_PARAM) String space) {
        RestartApplicationRequest request = RestartApplicationRequest.builder().name(applicationName).build();
        cloudFoundryOperations(org, space).applications().restart(request).block();
    }

    @Tool(description = DELETE_APPLICATION)
    public void deleteApplication(@ToolParam(description = NAME_PARAM) String applicationName,
                                  @ToolParam(description = ORG_PARAM) String org,
                                  @ToolParam(description = SPACE_PARAM) String space) {
        DeleteApplicationRequest deleteApplicationRequest = DeleteApplicationRequest.builder().
                name(applicationName).
                build();
        cloudFoundryOperations(org, space).applications().delete(deleteApplicationRequest).block();
    }

    @Tool(description = ORGANIZATION_LIST)
    public List<OrganizationSummary> organizationsList(@ToolParam(description = ORG_PARAM, required = false) String org,
                                                       @ToolParam(description = SPACE_PARAM, required = false) String space) {
        return cloudFoundryOperations(org, space).organizations().list().collectList().block();
    }

    @Tool(description = SERVICE_INSTANCE_LIST)
    public List<ServiceInstanceSummary> serviceInstancesList(@ToolParam(description = ORG_PARAM) String org,
                                                             @ToolParam(description = SPACE_PARAM) String space) {
        return cloudFoundryOperations(org, space).services().listInstances().collectList().block();
    }

    @Tool(description = SERVICE_INSTANCE_DETAIL)
    public ServiceInstance serviceInstanceDetails(@ToolParam(description = NAME_PARAM) String serviceInstanceName,
                                                  @ToolParam(description = ORG_PARAM) String org,
                                                  @ToolParam(description = SPACE_PARAM) String space) {
        GetServiceInstanceRequest request = GetServiceInstanceRequest.builder().name(serviceInstanceName).build();
        return cloudFoundryOperations(org, space).services().getInstance(request).block();
    }

    @Tool(description = SERVICE_OFFERINGS_LIST)
    public List<ServiceOffering> serviceOfferingsList(@ToolParam(description = ORG_PARAM) String org,
                                                      @ToolParam(description = SPACE_PARAM) String space) {
        ListServiceOfferingsRequest request = ListServiceOfferingsRequest.builder().build();
        return cloudFoundryOperations(org, space).services().listServiceOfferings(request).collectList().block();
    }

    @Tool(description = BIND_SERVICE_INSTANCE)
    public void bindServiceInstance(@ToolParam(description = SI_NAME_PARAM) String serviceInstanceName,
                                    @ToolParam(description = NAME_PARAM) String applicationName,
                                    @ToolParam(description = ORG_PARAM) String org,
                                    @ToolParam(description = SPACE_PARAM) String space) {
        BindServiceInstanceRequest request = BindServiceInstanceRequest.builder().
                serviceInstanceName(serviceInstanceName).
                applicationName(applicationName).
                build();
        cloudFoundryOperations(org, space).services().bind(request).block();
    }

    @Tool(description = UNBIND_SERVICE_INSTANCE)
    public void unbindServiceInstance(@ToolParam(description = SI_NAME_PARAM) String serviceInstanceName,
                                      @ToolParam(description = NAME_PARAM) String applicationName,
                                      @ToolParam(description = ORG_PARAM) String org,
                                      @ToolParam(description = SPACE_PARAM) String space) {
        UnbindServiceInstanceRequest request = UnbindServiceInstanceRequest.builder().
                serviceInstanceName(serviceInstanceName).
                applicationName(applicationName).
                build();
        cloudFoundryOperations(org, space).services().unbind(request).block();
    }

    @Tool(description = DELETE_SERVICE_INSTANCE)
    public void deleteServiceInstance(@ToolParam(description = SI_NAME_PARAM) String serviceInstanceName,
                                      @ToolParam(description = ORG_PARAM) String org,
                                      @ToolParam(description = SPACE_PARAM) String space) {
        DeleteServiceInstanceRequest request = DeleteServiceInstanceRequest.builder().
                name(serviceInstanceName).
                build();
        cloudFoundryOperations(org, space).services().deleteInstance(request).block();
    }

    @Tool(description = CREATE_USER_PROVIDED_SERVICE_INSTANCE)
    public void createUserProvidedServiceInstance(@ToolParam(description = SI_NAME_PARAM) String serviceInstanceName,
                                                  @ToolParam(description = CREDENTIALS_PARAM) Map<String, String> credentials,
                                                  @ToolParam(description = TAGS_PARAM, required = false) List<String> tags,
                                                  @ToolParam(description = ORG_PARAM) String org,
                                                  @ToolParam(description = SPACE_PARAM) String space) {
        CreateUserProvidedServiceInstanceRequest request = CreateUserProvidedServiceInstanceRequest.builder().
                name(serviceInstanceName).
                credentials(credentials).
                tags(tags).
                build();
        cloudFoundryOperations(org, space).services().createUserProvidedInstance(request).block();
    }

    @Tool(description = SPACE_LIST)
    public List<SpaceSummary> spacesList(@ToolParam(description = ORG_PARAM) String org,
                                         @ToolParam(description = SPACE_PARAM, required = false) String space) {
        return cloudFoundryOperations(org, space).spaces().list().collectList().block();
    }

    @Tool(description = GET_SPACE_QUOTA)
    public SpaceQuota getSpaceQuota(@ToolParam(description = SPACE_QUOTA_NAME_PARAM) String spaceName,
                                    @ToolParam(description = ORG_PARAM) String org,
                                    @ToolParam(description = SPACE_PARAM) String space) {
        GetSpaceQuotaRequest request = GetSpaceQuotaRequest.builder().name(spaceName).build();
        return cloudFoundryOperations(org, space).spaceAdmin().get(request).block();
    }
}
