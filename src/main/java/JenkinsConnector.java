import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Job;
import com.offbytwo.jenkins.model.JobWithDetails;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;

public class JenkinsConnector {

    private Properties props = new Properties();
    private InputStream input = null;
    private String username;
    private String url;
    private String password;
    private JenkinsServer jenkins;
    private String jenkinsInstanceName;
    private Map<String, Job> jobs;

    public JenkinsConnector(String jenkinsInstanceName) {
        try {
            input = new FileInputStream("credentials.properties");
            props.load(input);
            url = props.getProperty(jenkinsInstanceName + "-url");
            username = props.getProperty(jenkinsInstanceName + "-username");
            password = props.getProperty(jenkinsInstanceName + "-password");
            jenkins = new JenkinsServer(new URI(url), username, password);
            this.jenkinsInstanceName = jenkinsInstanceName;
            jobs = jenkins.getJobs();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public int runJob(String jobName, Map params) {
        int buildNumber = 0;
        try {
            JobWithDetails job = jobs.get(jobName).details();
            buildNumber = job.getNextBuildNumber();
            job.build(params);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buildNumber;
    }

    public String getLastSuccessfulBuildDescription(String jobName) {
        String description = "";
        try {
            JobWithDetails job = jobs.get(jobName).details();
            description = job.getLastSuccessfulBuild().details().getDescription();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return description;
    }

    public String getBuildDescription(String jobName, int buildNumber) {
        String description = "";
        try {
            JobWithDetails job = jobs.get(jobName).details();
            description = job.getBuildByNumber(buildNumber).details().getDescription();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return description;
    }

    public boolean buildSuccessful(String jobName, String checkTerm, int buildNumber) {
        Boolean success = false;
        try {
            while (!success) {
                Thread.sleep(10000);
                JobWithDetails job = jobs.get(jobName).details();
                String consoleOutput = job.getBuildByNumber(buildNumber).details().getConsoleOutputText();
                if (consoleOutput.contains("Finished: SUCCESS") && consoleOutput.contains(checkTerm)) {
                    System.out.println("Build " + buildNumber + " of " + jobName + " in " + jenkinsInstanceName + " was successful!");
                    success = true;
                } else {
                    System.out.println("Polling build " + buildNumber + " of " + jobName + " in " + jenkinsInstanceName + " until successful...");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return success;
    }

}
