import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.IOException;

public interface RMI_Interface extends Remote {
    int rmiRequest(String type, String message) throws IOException, RemoteException;
}
