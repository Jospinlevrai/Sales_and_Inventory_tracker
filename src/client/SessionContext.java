package client;

import dto.UserInfo;
import rmi.RemoteServices;
import service.AuthService;
import service.CategoryService;
import service.ProductService;
import service.PurchaseService;
import service.ReportService;
import service.SaleService;
import service.StockService;
import service.SupplierService;
import service.UserService;

public final class SessionContext {
    private static RemoteServices remote;
    private static UserInfo currentUser;
    private static String host = "localhost";
    private static int port = 1099;

    private SessionContext() {
    }

    public static synchronized void connectAndLogin(String serverHost, int serverPort, String username, String password) {
        host = serverHost;
        port = serverPort;
        RemoteServices services = RemoteServices.connect(serverHost, serverPort);
        UserInfo user = services.login(username, password);
        remote = services;
        currentUser = user;
    }

    public static synchronized RemoteServices connectUnauthenticated(String serverHost, int serverPort) {
        host = serverHost;
        port = serverPort;
        return RemoteServices.connect(serverHost, serverPort);
    }

    public static synchronized void logout() {
        if (remote != null) {
            remote.logout();
        }
        remote = null;
        currentUser = null;
    }

    public static UserInfo getUser() {
        return currentUser;
    }

    public static int getUserId() {
        return currentUser == null ? 0 : currentUser.getUserId();
    }

    public static String getHost() {
        return host;
    }

    public static int getPort() {
        return port;
    }

    public static AuthService auth() { return remote.service(AuthService.class); }
    public static UserService users() { return remote.service(UserService.class); }
    public static CategoryService categories() { return remote.service(CategoryService.class); }
    public static SupplierService suppliers() { return remote.service(SupplierService.class); }
    public static ProductService products() { return remote.service(ProductService.class); }
    public static SaleService sales() { return remote.service(SaleService.class); }
    public static PurchaseService purchases() { return remote.service(PurchaseService.class); }
    public static StockService stock() { return remote.service(StockService.class); }
    public static ReportService reports() { return remote.service(ReportService.class); }
}