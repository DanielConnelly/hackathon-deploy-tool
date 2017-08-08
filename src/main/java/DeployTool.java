public class DeployTool {

    public static void main(String[] args) {

            int length = args.length;

            for(int y = 0; y <= length; y = y + 1) {
                System.out.println(y);
                OpenDeployer od = new OpenDeployer();
                String appName = args[y];
                od.deployToDevQaAndStaging(appName);
            }
    }

}
