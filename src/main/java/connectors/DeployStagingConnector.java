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

public class DeployStagingConnector {

    Properties prop = new Properties();
    InputStream input = null;
    private static String deployStagingUsername;
    private static String deployStagingUrl;
    private static String deployStagingPassword;
    static JenkinsServer jenkins;
    static Map<String, Job> jobs;

    public DeployStagingConnector() {
        try {
            input = new FileInputStream("credentials.properties");
            prop.load(input);
            deployStagingUrl = prop.getProperty("deployStagingUrl");
            deployStagingUsername = prop.getProperty("deployStagingUsername");
            deployStagingPassword = prop.getProperty("deployStagingPassword");
            jenkins = new JenkinsServer(new URI(deployStagingUrl), deployStagingUsername, deployStagingPassword);
            jobs = jenkins.getJobs();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deployMicroservice(String appName, String taggedReleaseVersion) {
        System.out.println("Deploying version " + taggedReleaseVersion + " of " + appName + " to Staging...");
        Map<String, String> params = new HashMap<String, String>();
        params.put("APP", appName);
        params.put("VERSION", taggedReleaseVersion);
        params.put("DEPLOYMENT_BRANCH", "master");
        try {
            JobWithDetails job = jobs.get("deploy-microservice-multiactive").details();
            job.build(params);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
