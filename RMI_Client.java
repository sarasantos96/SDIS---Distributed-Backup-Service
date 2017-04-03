import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMI_Client {

    private RMI_Client() {}

    public static void main(String[] args) {
        //String host = (args.length < 1) ? null : args[0];
        String host = null;
        try {
            Registry registry = LocateRegistry.getRegistry(host);
            RMI_Interface stub = (RMI_Interface) registry.lookup("RMI_Interface" + args[0]);
            int r = stub.rmiRequest(args[1], args[2], args[3]);
            System.out.println("stub ran " + r);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}