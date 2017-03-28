import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.IOException;

public interface RMI_Interface extends Remote {
    int saySomething() throws RemoteException;
    int backupRequest() throws IOException, RemoteException;
}
