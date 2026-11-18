package rmi;

import dto.LoginResult;
import dto.UserInfo;
import exception.ServerUnavailableException;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Client side of the link. service(SaleService.class) returns an object that looks like the real service but sends
 * every call to the server, so the client holds no database code and no business rules.
 */
public final class RemoteServices {
    private final RemoteGateway gateway;
    private String token;
    private UserInfo user;

    private RemoteServices(RemoteGateway gateway) {
        this.gateway = gateway;
    }

    public static RemoteServices connect(String host, int port) {
        try {
            Registry registry = LocateRegistry.getRegistry(host, port);
            return new RemoteServices((RemoteGateway) registry.lookup(RemoteGateway.NAME));
        } catch (RemoteException | NotBoundException e) {
            throw new ServerUnavailableException("Cannot reach the server at " + host + ":" + port, e);
        }
    }

    public UserInfo login(String username, String password) {
        try {
            LoginResult result = gateway.login(username, password);
            this.token = result.getToken();
            this.user = result.getUser();
            return user;
        } catch (RemoteException e) {
            throw unavailable(e);
        }
    }

    public void logout() {
        try {
            gateway.logout(token);
        } catch (RemoteException e) {
            // the session ends on the client anyway
        }
        token = null;
        user = null;
    }

    public UserInfo user() {
        return user;
    }

    public <T> T service(Class<T> type) {
        Object proxy = Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type},
                (self, method, args) -> invoke(type, self, method, args));
        return type.cast(proxy);
    }

    private Object invoke(Class<?> type, Object self, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            switch (method.getName()) {
                case "toString": return "Remote " + type.getSimpleName();
                case "hashCode": return System.identityHashCode(self);
                default: return self == args[0];
            }
        }
        try {
            return gateway.call(token, type.getSimpleName(), method.getName(), args == null ? new Object[0] : args);
        } catch (RemoteException e) {
            throw unavailable(e);
        }
    }

    private static ServerUnavailableException unavailable(RemoteException e) {
        return new ServerUnavailableException("The server could not be reached or could not handle the request: "
                + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()), e);
    }
}
