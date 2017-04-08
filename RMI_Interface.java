import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.IOException;

public interface RMI_Interface extends Remote {
    int rmiRequest(String type, String args1, String args2) throws IOException, RemoteException, InterruptedException ;
}
