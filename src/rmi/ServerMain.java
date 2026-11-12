package rmi;

import service.AppServices;
import service.DBConnection;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.Connection;

/**
 * Starts the server: java rmi.ServerMain [db.properties]
 * Options: -Dserver.port=1099, and -Djava.rmi.server.hostname=THIS-COMPUTER'S-ADDRESS when clients are on other computers.
 */
public class ServerMain {
    public static void main(String[] args) throws Exception {
        String settings = args.length > 0 ? args[0] : "db.properties";
        int port = Integer.getInteger("server.port", Registry.REGISTRY_PORT);

        DBConnection database = DBConnection.fromFile(settings);
        try (Connection test = database.getConnection()) {
            System.out.println("Database reachable: " + test.getMetaData().getURL());
        }
        AppServices services = new AppServices(database);
        GatewayImpl gateway = new GatewayImpl(services, port);
        Registry registry = LocateRegistry.createRegistry(port);
        registry.rebind(RemoteGateway.NAME, gateway);
        System.out.println("Inventory tracker server ready on port " + port + ". Press Ctrl+C to stop.");
    }
}
