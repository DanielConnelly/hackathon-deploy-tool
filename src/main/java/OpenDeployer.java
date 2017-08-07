import java.util.HashMap;
import java.util.Map;

public class OpenDeployer {

    private JenkinsConnector ciOpen;
    private JenkinsConnector ciDev;
    private JenkinsConnector ciBuild;
    private JenkinsConnector deployQa;
    private JenkinsConnector deployStaging;
    private String appName;
    private String buildpackRepo = "git@github.tools.tax.service.gov.uk:HMRC/buildpack-java-jar.git";
    private String devReleaseVersion;

    public OpenDeployer() {
        ciDev = new JenkinsConnector("ci-dev");
        ciOpen = new JenkinsConnector("ci-open");
        ciBuild = new JenkinsConnector("ci-build");
        deployQa = new JenkinsConnector("deploy-qa");
        deployStaging = new JenkinsConnector("deploy-staging");
    }

    private void deployToDev(String appName) {
        try {
            this.appName = appName;

            devReleaseVersion = ciOpen.getLastSuccessfulBuildDescription(this.appName);

            Map<String, String> createReleaseSlugParams = new HashMap<String, String>();
            createReleaseSlugParams.put("APP_NAME", appName);
            createReleaseSlugParams.put("APP_VERSION", devReleaseVersion);
            createReleaseSlugParams.put("BUILDPACK_REPO", buildpackRepo);

            int createReleaseSlugBuildNumber = ciDev.runJob("create-a-release-slug", createReleaseSlugParams);
            System.out.println("Creating dev release slug for version " + devReleaseVersion + " of " + appName + "...");

            if (ciDev.buildSuccessful("create-a-release-slug", appName, createReleaseSlugBuildNumber)) {
                System.out.println("Waiting 30 seconds until deploying to dev...");
                Thread.sleep(30000);

                Map<String, String> deployMicroserviceParams = new HashMap<String, String>();
                deployMicroserviceParams.put("APP", appName);
                deployMicroserviceParams.put("APP_BUILD_NUMBER", devReleaseVersion);
                deployMicroserviceParams.put("DEPLOYMENT_BRANCH", "master");
                deployMicroserviceParams.put("POST_DEPLOY_JOB", "NONE");

                ciDev.runJob("deploy-microservice", deployMicroserviceParams);
                System.out.println("Deploying version " + devReleaseVersion + " of " + appName + " to dev...");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void deployToQaAndStaging(String appName) {
        try {
            this.appName = appName;

            Map<String, String> createReleaseParams = new HashMap<String, String>();
            createReleaseParams.put("ARTEFACT_NAME", appName);
            createReleaseParams.put("RELEASE_CANDIDATE_VERSION", devReleaseVersion);
            createReleaseParams.put("RELEASE_TYPE", "MINOR");

            int createReleaseBuildNumber = ciOpen.runJob("create-a-release", createReleaseParams);
            System.out.println("Creating release for version " + devReleaseVersion + " of " + appName + "...");

            if (ciOpen.buildSuccessful("create-a-release", appName, createReleaseBuildNumber)) {
                System.out.println("Waiting 30 seconds until creating release slug...");
                Thread.sleep(30000);
                String createReleaseDescription = ciOpen.getBuildDescription("create-a-release", createReleaseBuildNumber);
                String taggedReleaseVersion = createReleaseDescription.split("\\s+")[1];

                Map<String, String> createReleaseSlugParams = new HashMap<String, String>();
                createReleaseSlugParams.put("APP_NAME", appName);
                createReleaseSlugParams.put("APP_VERSION", taggedReleaseVersion);
                createReleaseSlugParams.put("BUILDPACK_REPO", buildpackRepo);

                int createReleaseSlugBuildNumber = ciBuild.runJob("create-a-release-slug", createReleaseSlugParams);
                System.out.println("Creating release slug for version " + taggedReleaseVersion + " of " + appName + "...");

                if (ciBuild.buildSuccessful("create-a-release-slug", appName, createReleaseSlugBuildNumber)) {
                    System.out.println("Waiting 30 seconds until deploying to QA and staging...");
                    Thread.sleep(30000);

                    Map<String, String> deployMicroserviceParams = new HashMap<String, String>();
                    deployMicroserviceParams.put("APP", appName);
                    deployMicroserviceParams.put("VERSION", taggedReleaseVersion);
                    deployMicroserviceParams.put("DEPLOYMENT_BRANCH", "master");

                    deployQa.runJob("deploy-microservice", deployMicroserviceParams);
                    System.out.println("Deploying version " + taggedReleaseVersion + " of " + appName + " to QA...");
                    deployStaging.runJob("deploy-microservice-multiactive", deployMicroserviceParams);
                    System.out.println("Deploying version " + taggedReleaseVersion + " of " + appName + " to Staging...");
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void deployToDevQaAndStaging(String appName) {
        deployToDev(appName);
        deployToQaAndStaging(appName);
    }

}
