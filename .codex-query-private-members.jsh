import java.io.*;
import java.sql.*;
import java.util.*;

String setting(Properties properties, String key, String environmentName) {
    String environmentValue = System.getenv(environmentName);
    if (environmentValue != null && !environmentValue.isBlank()) return environmentValue;
    String configuredValue = properties.getProperty(key);
    int defaultSeparator = configuredValue.indexOf(':');
    return configuredValue.substring(defaultSeparator + 1, configuredValue.length() - 1);
}

var properties = new Properties();
try (var input = new FileInputStream("src/main/resources/application.properties")) {
    properties.load(input);
}
var connection = DriverManager.getConnection(
        properties.getProperty("spring.datasource.url"),
        setting(properties, "spring.datasource.username", "DB_USERNAME"),
        setting(properties, "spring.datasource.password", "DB_PASSWORD")
);
var statement = connection.prepareStatement("""
        SELECT m.member_id, m.nickname
        FROM member m
        LEFT JOIN settings s ON s.member_id = m.member_id
        WHERE COALESCE(s.is_stock_public, FALSE) = FALSE
        ORDER BY m.member_id
        """);
var rows = statement.executeQuery();
while (rows.next()) {
    String nickname = rows.getString("nickname");
    StringBuilder escapedNickname = new StringBuilder();
    for (char character : nickname.toCharArray()) {
        escapedNickname.append(character > 127 ? String.format("\\u%04x", (int) character) : character);
    }
    System.out.println(rows.getString("member_id") + "\t" + escapedNickname);
}
rows.close();
statement.close();
connection.close();
/exit
