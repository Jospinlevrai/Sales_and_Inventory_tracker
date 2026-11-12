package rmi;

import dto.LoginResult;

import java.rmi.Remote;
import java.rmi.RemoteException;

/** The only remote object. Every service call goes through it, so the business classes know nothing about RMI. */
public interface RemoteGateway extends Remote {
    String NAME = "InventoryTracker";

    /** Checks the password and opens a session. Keep the returned token and send it with every call. */
    LoginResult login(String username, String password) throws RemoteException;

    void logout(String token) throws RemoteException;

    /** Runs one method of one service interface, for example call(token, "SaleService", "completeSale", new Object[] {request}). */
    Object call(String token, String service, String method, Object[] arguments) throws RemoteException;
}
