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

public class CiOpenConnector {

    Properties prop = new Properties();
    InputStream input = null;
    private static String ciOpenUrl;
    private static String ciOpenUsername;
    private static String ciOpenPassword;
    static JenkinsServer jenkins;
    static Map<String, Job> jobs;

    public CiOpenConnector() {
        try {
            input = new FileInputStream("credentials.properties");
            prop.load(input);
            ciOpenUrl = prop.getProperty("ciOpenUrl");
            ciOpenUsername = prop.getProperty("ciOpenUsername");
            ciOpenPassword = prop.getProperty("ciOpenPassword");
            jenkins = new JenkinsServer(new URI(ciOpenUrl), ciOpenUsername, ciOpenPassword);
            jobs = jenkins.getJobs();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getLastSuccessfulBuildRelease(String jobName) {
        String release = "";
        try {
            JobWithDetails vatRegistrationFrontendJob = jobs.get(jobName).details();
            release = vatRegistrationFrontendJob.getLastSuccessfulBuild().details().getDescription();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return release;
    }

    public void createRelease(String appName, String devReleaseVersion, String releaseType) {
        System.out.println("Creating release for version " + devReleaseVersion + " of " + appName + "...");
        Map<String, String> params = new HashMap<String, String>();
        params.put("ARTEFACT_NAME", appName);
        params.put("RELEASE_CANDIDATE_VERSION", devReleaseVersion);
        params.put("RELEASE_TYPE", releaseType);
        try {
            JobWithDetails createAReleaseJob = jobs.get("create-a-release").details();
            createAReleaseJob.build(params);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getStatusOfReleaseBuild(int buildNumber, String appName) {
        String status = null;
        try {
            JobWithDetails job = jobs.get("create-a-release").details();
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

    public int getNextCreateReleaseBuildNumber() {
        int buildNumber = 0;

        try {
            JobWithDetails job = jobs.get("create-a-release").details();
            buildNumber = job.getNextBuildNumber();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return buildNumber;
    }

    public String getTaggedReleaseVersion(int buildNumber) {
        String taggedReleaseVersion = "";
        try {
            JobWithDetails job = jobs.get("create-a-release").details();
            taggedReleaseVersion = job.getBuildByNumber(buildNumber).details().getDescription();
            String[] splitted = taggedReleaseVersion.split("\\s+");
            taggedReleaseVersion = splitted[1];
        } catch (IOException e) {
            e.printStackTrace();
        }
        return taggedReleaseVersion;
    }

}
