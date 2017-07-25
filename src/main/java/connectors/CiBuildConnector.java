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

public class CiBuildConnector {

    Properties prop = new Properties();
    InputStream input = null;
    private static String ciBuildUsername;
    private static String ciBuildUrl;
    private static String ciBuildPassword;
    static JenkinsServer jenkins;
    static Map<String, Job> jobs;

    public CiBuildConnector() {
        try {
            input = new FileInputStream("credentials.properties");
            prop.load(input);
            ciBuildUrl = prop.getProperty("ciBuildUrl");
            ciBuildUsername = prop.getProperty("ciBuildUsername");
            ciBuildPassword = prop.getProperty("ciBuildPassword");
            jenkins = new JenkinsServer(new URI(ciBuildUrl), ciBuildUsername, ciBuildPassword);
            jobs = jenkins.getJobs();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createReleaseSlug(String appName, String taggedReleaseVersion, String buildpackRepo) {
        System.out.println("Creating release slug for version " + taggedReleaseVersion + " of " + appName + "...");
        Map<String, String> params = new HashMap<String, String>();
        params.put("APP_NAME", appName);
        params.put("APP_VERSION", taggedReleaseVersion);
        params.put("BUILDPACK_REPO", buildpackRepo);

        try {
            JobWithDetails createAReleaseSlugJob = jobs.get("create-a-release-slug").details();
            createAReleaseSlugJob.build(params);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

}
