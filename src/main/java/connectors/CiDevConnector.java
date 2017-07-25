package connectors;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Job;
import com.offbytwo.jenkins.model.JobWithDetails;

public class CiDevConnector {

    Properties prop = new Properties();
    InputStream input = null;
    private static String ciDevUsername;
    private static String ciDevUrl;
    private static String ciDevPassword;
    static JenkinsServer jenkins;
    static Map<String, Job> jobs;

    public CiDevConnector() {
        try {
            input = new FileInputStream("credentials.properties");
            prop.load(input);
            ciDevUrl = prop.getProperty("ciDevUrl");
            ciDevUsername = prop.getProperty("ciDevUsername");
            ciDevPassword = prop.getProperty("ciDevPassword");
            jenkins = new JenkinsServer(new URI(ciDevUrl), ciDevUsername, ciDevPassword);
            jobs = jenkins.getJobs();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createReleaseSlug(String appName, String devReleaseVersion, String buildpackRepo) {
        System.out.println("Creating dev release slug for version " + devReleaseVersion + " of " + appName + "...");
        Map<String, String> params = new HashMap<String, String>();
        params.put("APP_NAME", appName);
        params.put("APP_VERSION", devReleaseVersion);
        params.put("BUILDPACK_REPO", buildpackRepo);

        try {
            JobWithDetails createAReleaseSlugJob = jobs.get("create-a-release-slug").details();
            createAReleaseSlugJob.build(params);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deployMicroservice(String appName, String devReleaseVersion) {
        System.out.println("Deploying version " + devReleaseVersion + " of " + appName + " to dev...");
        Map<String, String> params = new HashMap<String, String>();
        params.put("APP", appName);
        params.put("APP_BUILD_NUMBER", devReleaseVersion);
        params.put("DEPLOYMENT_BRANCH", "master");
        params.put("POST_DEPLOY_JOB", "NONE");

        try {
            JobWithDetails job = jobs.get("deploy-microservice").details();
            job.build(params);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getNextCreateReleaseSlugBuildNumber() {
        int buildNumber = 0;

        try {
            JobWithDetails job = jobs.get("create-a-release-slug").details();
            buildNumber = job.getNextBuildNumber();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return buildNumber;
    }

    public String getStatusOfReleaseSlugBuild(int buildNumber, String appName) {
        String status = null;

        try {
            JobWithDetails job = jobs.get("create-a-release-slug").details();
            String consoleOutput = job.getBuildByNumber(buildNumber).details().getConsoleOutputText();

            if (consoleOutput.contains("Finished: SUCCESS") && consoleOutput.contains(appName)) {
                status = "SUCCESS";
            } else {
                status = "";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return status;
    }

}
