package utils;

import connectors.CiBuildConnector;
import connectors.CiDevConnector;
import connectors.CiOpenConnector;
import connectors.DeployQaConnector;
import connectors.DeployStagingConnector;

public class OpenDeployer {

    CiOpenConnector ciOpen;
    CiDevConnector ciDev;
    CiBuildConnector ciBuild;
    DeployQaConnector deployQa;
    DeployStagingConnector deployStaging;
    String appName;
    String buildpackRepo = "git@github.tools.tax.service.gov.uk:HMRC/buildpack-java-jar.git";
    String devReleaseVersion;
    String taggedReleaseVersion;

    public OpenDeployer() {
        ciDev = new CiDevConnector();
        ciOpen = new CiOpenConnector();
        ciBuild = new CiBuildConnector();
        deployQa = new DeployQaConnector();
        deployStaging = new DeployStagingConnector();
    }

    public void deployToDev(String appName) {
        try {
            this.appName = appName;

            devReleaseVersion = ciOpen.getLastSuccessfulBuildRelease(this.appName);

            int createReleaseSlugBuildNumber = ciDev.getNextCreateReleaseSlugBuildNumber();
            ciDev.createReleaseSlug(this.appName, devReleaseVersion, buildpackRepo);

            if (devReleaseSlugIsCreated(createReleaseSlugBuildNumber)) {
                System.out.println("Waiting 30 seconds until deploying to dev...");
                Thread.sleep(30000);
                ciDev.deployMicroservice(appName, devReleaseVersion);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void deployToQaAndStaging(String appName) {
        try {
            this.appName = appName;

            int createReleaseBuildNumber = ciOpen.getNextCreateReleaseBuildNumber();
            ciOpen.createRelease(this.appName, devReleaseVersion, "MINOR");

            if (releaseIsCreated(createReleaseBuildNumber)) {
                System.out.println("Waiting 30 seconds until creating release slug...");
                Thread.sleep(30000);
                taggedReleaseVersion = ciOpen.getTaggedReleaseVersion(createReleaseBuildNumber);

                int createReleaseSlugBuildNumber = ciBuild.getNextCreateReleaseSlugBuildNumber();
                ciBuild.createReleaseSlug(appName, taggedReleaseVersion, buildpackRepo);

                if (releaseSlugIsCreated(createReleaseSlugBuildNumber)) {
                    System.out.println("Waiting 30 seconds until deploying to QA and staging...");
                    Thread.sleep(30000);
                    deployQa.deployMicroservice(appName, taggedReleaseVersion);
                    deployStaging.deployMicroservice(appName, taggedReleaseVersion);
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

    public boolean devReleaseSlugIsCreated(int buildNumber) {
        boolean releaseSlugIsCreated = false;

        while (!releaseSlugIsCreated) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String buildStatus = ciDev.getStatusOfReleaseSlugBuild(buildNumber, appName);
            if (buildStatus == "SUCCESS") {
                releaseSlugIsCreated = true;
                System.out.println("Dev release slug created!");
            }
        }

        return releaseSlugIsCreated;
    }

    public boolean releaseIsCreated(int buildNumber) {
        boolean releaseIsCreated = false;

        while (!releaseIsCreated) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String buildStatus = ciOpen.getStatusOfReleaseBuild(buildNumber, appName);
            if (buildStatus == "SUCCESS") {
                releaseIsCreated = true;
                System.out.println("Release created!");
            }
        }

        return releaseIsCreated;
    }

    public boolean releaseSlugIsCreated(int buildNumber) {
        boolean releaseSlugIsCreated = false;

        while (!releaseSlugIsCreated) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String buildStatus = ciBuild.getStatusOfReleaseSlugBuild(buildNumber, appName);
            if (buildStatus == "SUCCESS") {
                releaseSlugIsCreated = true;
                System.out.println("Release slug created!");
            }
        }

        return releaseSlugIsCreated;
    }

}
