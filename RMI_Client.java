import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMI_Client {

    private RMI_Client() {}

    public static void main(String[] args) {
        String host = null;
        try {
            Registry registry = LocateRegistry.getRegistry(host);
            RMI_Interface stub = (RMI_Interface) registry.lookup("RMI_Interface" + args[0]);
            int r = -1;

            if(args.length == 3)
                r = stub.rmiRequest(args[1], args[2], null);
            else
                if(args.length == 4){
                    r = stub.rmiRequest(args[1], args[2], args[3]);
                }
            else
                if(args.length == 2){
                    r = stub.rmiRequest(args[1], null, null);
                }
            else{
                System.out.println("Wrong number of arguments");
                System.exit(-1);
            }
            System.out.println("stub ran " + r);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
