package it.chrisvoo.utils;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.typesafe.config.Config;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Utilities for operating with MongoDB
 */
public class DbUtils {
    /**
     * Builds a client from the connection string.
     * @param connectionString Mongo's connection string.
     * @return The client
     */
    public static MongoClient getClient(String connectionString) {
        return MongoClients.create(connectionString);
    }

    /**
     * It automatically builds the connection string by the passed config.
     * @param config The configuration file instance
     * @return The client
     */
    public static MongoClient getClient(Config config) {
        return MongoClients.create(buildConnectionString(config));
    }

    /**
     * If the username or password includes the at sign @, colon :, slash /, or the percent sign % character,
     * use percent encoding
     * @param s The username or password
     * @return The encoded string.
     */
    public static String percentEncode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    /**
     * It build the connection string. It has the following format:
     * <code>mongodb://[username:password@]host1[:port1][,...hostN[:portN]][/[database][?options]]</code>
     * @param config The {@link Config} instance
     * @return A connection string usable by {@link com.mongodb.reactivestreams.client.MongoClients#create(String)}
     */
    public static String buildConnectionString(Config config) {
        String protocol = config.hasPath("scanner.db.protocol")
                ? config.getString("scanner.db.protocol")
                : "mongodb";

        String host = config.hasPath("scanner.db.host")
                ? config.getString("scanner.db.host")
                : "localhost";

        String username = config.hasPath("scanner.db.username")
                ? percentEncode(config.getString("scanner.db.username"))
                : "";

        String password = config.hasPath("scanner.db.password")
                ? percentEncode(config.getString("scanner.db.password"))
                : "";

        String credentials = !username.isBlank() && !password.isBlank()
                ? username + ":" + password + "@"
                : "";

        String port = config.hasPath("scanner.db.port")
                ? config.getString("scanner.db.port") : "27017";

        String options = config.hasPath("scanner.db.options")
                ? "?" + config.getString("scanner.db.options")
                : "?connectTimeoutMS=5000&appName=MusicManager";

        return protocol + "://" + credentials + host + ":" + port + "/admin" + options;
    }
}
