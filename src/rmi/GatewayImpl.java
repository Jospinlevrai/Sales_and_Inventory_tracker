package rmi;

import dto.ActingUserRequest;
import dto.LoginResult;
import dto.UserInfo;
import exception.BusinessRuleException;
import service.ActingUser;
import service.AppServices;
import service.AuthService;
import service.BackupService;
import service.CategoryService;
import service.ProductService;
import service.PurchaseService;
import service.ReportService;
import service.SaleService;
import service.StockService;
import service.SupplierService;
import service.UserService;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server side of the link. Only methods declared on the service interfaces can be called, and only by a logged-in
 * user (the first-administrator set-up is the one exception). The id of the user doing the action is taken from the
 * session, never from the client, so a modified client cannot act as somebody else.
 */
public class GatewayImpl extends UnicastRemoteObject implements RemoteGateway {
    private static final long serialVersionUID = 1L;
    private static final Set<String> WITHOUT_LOGIN = new HashSet<>(Arrays.asList("UserService.registerFirstAdmin"));

    private static final class Operation {
        final Object service;
        final Method method;
        final boolean withoutLogin;
        final List<Integer> actingUserParameters = new ArrayList<>();

        Operation(Object service, Method method, boolean withoutLogin) {
            this.service = service;
            this.method = method;
            this.withoutLogin = withoutLogin;
            Annotation[][] annotations = method.getParameterAnnotations();
            for (int i = 0; i < annotations.length; i++) {
                for (Annotation annotation : annotations[i]) {
                    if (annotation instanceof ActingUser) {
                        actingUserParameters.add(i);
                    }
                }
            }
        }
    }

    private final transient AuthService auth;
    private final transient Map<String, Operation> operations = new HashMap<>();
    private final transient Map<String, Integer> sessions = new ConcurrentHashMap<>();

    /** Exports the gateway on the given port (use the registry's port so only one port needs to be open). */
    public GatewayImpl(AppServices services, int port) throws RemoteException {
        super(port);
        this.auth = services.auth();
        register(AuthService.class, services.auth());
        register(UserService.class, services.users());
        register(CategoryService.class, services.categories());
        register(SupplierService.class, services.suppliers());
        register(ProductService.class, services.products());
        register(SaleService.class, services.sales());
        register(PurchaseService.class, services.purchases());
        register(StockService.class, services.stock());
        register(ReportService.class, services.reports());
        register(BackupService.class, services.backups());
    }

    private void register(Class<?> type, Object service) {
        for (Method method : type.getMethods()) {
            String key = type.getSimpleName() + "." + method.getName();
            if (operations.containsKey(key)) {
                throw new IllegalStateException(key + " is overloaded; remote calls find methods by name");
            }
            operations.put(key, new Operation(service, method, WITHOUT_LOGIN.contains(key)));
        }
    }

    @Override
    public LoginResult login(String username, String password) {
        UserInfo user = auth.login(username, password);
        String token = UUID.randomUUID().toString();
        sessions.put(token, user.getUserId());
        return new LoginResult(token, user);
    }

    @Override
    public void logout(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }

    @Override
    public Object call(String token, String service, String method, Object[] arguments) throws RemoteException {
        Operation operation = operations.get(service + "." + method);
        if (operation == null) {
            throw new RemoteException("Unknown operation " + service + "." + method);
        }
        Object[] args = arguments == null ? new Object[0] : arguments.clone();
        if (args.length != operation.method.getParameterCount()) {
            throw new RemoteException("Wrong number of arguments for " + service + "." + method);
        }
        Integer userId = token == null ? null : sessions.get(token);
        if (userId == null && !operation.withoutLogin) {
            throw new BusinessRuleException("Please log in first");
        }
        if (userId != null) {
            for (int index : operation.actingUserParameters) {
                args[index] = userId;
            }
            for (Object arg : args) {
                if (arg instanceof ActingUserRequest) {
                    ((ActingUserRequest) arg).setUserId(userId);
                }
            }
        }
        try {
            return operation.method.invoke(operation.service, args);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                if (!(cause instanceof BusinessRuleException)) {
                    System.err.println("Server error in " + service + "." + method + ": " + cause);
                }
                throw (RuntimeException) cause;
            }
            throw new RemoteException("The server failed in " + service + "." + method + ": " + cause, cause);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new RemoteException("Invalid request for " + service + "." + method + ": " + e.getMessage(), e);
        }
    }
}
