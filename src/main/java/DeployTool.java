public class DeployTool {

    public static void main(String[] args) {

            int length = args.length;

            //possible feature switch.

            for(int y = 0; y < length;) {
                OpenDeployer od = new OpenDeployer();
                String appName = args[y];
                od.deployToDevQaAndStaging(appName);
                y = y + 1;
            }
    }
}
