import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.NotBoundException;

public class TestApp {
    private TestApp() {}

    public static void main(String[] args) {
        if(args.length == 0){
            printUsage();
            System.exit(0);
        }

        String host = null;
        try {
            Registry registry = LocateRegistry.getRegistry(host);
            RMI_Interface stub = (RMI_Interface) registry.lookup("RMI_Interface" + args[0]);
            int r = -1;

            if(args.length == 3)
                r = stub.rmiRequest(args[1].toUpperCase(), args[2], null);
            else
                if(args.length == 4){
                    r = stub.rmiRequest(args[1].toUpperCase(), args[2], args[3]);
                }
            else
                if(args.length == 2){
                    r = stub.rmiRequest(args[1].toUpperCase(), null, null);
                }
            else{
                System.out.println("Wrong number of arguments");
                System.exit(-1);
            }
            System.out.println("stub ran " + r);
        } catch(NotBoundException  nbe){
            System.out.println("The Peer " + args[0] + "was not found!");
        }catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public static void printUsage(){
        System.out.println("Usage for TestApp:");
        System.out.println("java TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>");
        System.out.println("             <peer_ap> - the id of the peer (integer)");
        System.out.println("             <sub_protocol> - BACKUP, RESTORE, DELETE, RECLAIM, STATE");
        System.out.println("             <opnd_1> - filename for backup, restore and delete protocols");
        System.out.println("                        space to reclaim in reclaim protocol");
        System.out.println("             <opnd_2> - desired replication degree in the backup sub_protocol");
    }
}
