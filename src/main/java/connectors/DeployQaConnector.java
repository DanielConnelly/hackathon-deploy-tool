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

public class DeployQaConnector {

    Properties prop = new Properties();
    InputStream input = null;
    private static String deployQaUsername;
    private static String deployQaUrl;
    private static String deployQaPassword;
    static JenkinsServer jenkins;
    static Map<String, Job> jobs;

    public DeployQaConnector() {
        try {
            input = new FileInputStream("credentials.properties");
            prop.load(input);
            deployQaUrl = prop.getProperty("deployQaUrl");
            deployQaUsername = prop.getProperty("deployQaUsername");
            deployQaPassword = prop.getProperty("deployQaPassword");
            jenkins = new JenkinsServer(new URI(deployQaUrl), deployQaUsername, deployQaPassword);
            jobs = jenkins.getJobs();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deployMicroservice(String appName, String taggedReleaseVersion) {
        System.out.println("Deploying version " + taggedReleaseVersion + " of " + appName + " to QA...");
        Map<String, String> params = new HashMap<String, String>();
        params.put("APP", appName);
        params.put("VERSION", taggedReleaseVersion);
        params.put("DEPLOYMENT_BRANCH", "master");
        try {
            JobWithDetails job = jobs.get("deploy-microservice").details();
            job.build(params);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
