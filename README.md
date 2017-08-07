# hackathon-deploy-tool
A tool made in 1 day for the HMRC DDC Worthing hackathon (19-07-17) to automate the deployment process

# How does it work?
- Triggers the necessary existing jenkins jobs in the correct sequence when they are ready, which will deploy to the dev, qa and staging environments.
- Uses the Jenkins API Client for java to interact with jenkins which can be found here: https://github.com/jenkinsci/java-client-api

# How to use
- Replace credentials.properties.example with credentials.properties and enter your jenkins credentials in here.
- Run 'DeployTool' with a single argument, which should be your app name.

# You should know
- This does not show you the console output for the jobs, you will still need to monitor the existing jobs on their own if you want to see what is happening.
- The tool does not wait for the deployment jobs themselves to complete, just triggers them once the depending jobs are complete.
- If a depending job is not successful i.e. create-a-release-slug, the tool will just hang.