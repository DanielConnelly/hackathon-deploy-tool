public class DeployTool {

    public static void main(String[] args) {
        OpenDeployer od = new OpenDeployer();
        od.deployToDevQaAndStaging(args[0]);
    }

}
