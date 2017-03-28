import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMI_Client {

    private RMI_Client() {}

    public static void main(String[] args) {
        System.out.println(args[0]);
        //String host = (args.length < 1) ? null : args[0];
        String host = null;
        try {
            Registry registry = LocateRegistry.getRegistry(host);
            RMI_Interface stub = (RMI_Interface) registry.lookup("RMI_Interface" + args[0]);
            int r = stub.backupRequest();
            System.out.println("stub ran " + r);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}