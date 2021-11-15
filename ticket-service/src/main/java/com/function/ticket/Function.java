package com.function.ticket;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;

public class Function {
    @FunctionName("TicketAPI")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = { HttpMethod.GET,
            HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");
        try {
            // Parse query parameter
            final String id = request.getQueryParameters().get("id");
            final String status = request.getQueryParameters().get("status");

            if (id == null || status == null) {
                context.getLogger().info("Bot id and status must have non-null values");
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("{\"result\":\"error\"}").build();
            }

            Properties properties = new Properties();
            properties.load(Function.class.getClassLoader().getResourceAsStream("application.properties"));

            SecretClient secretClient = new SecretClientBuilder().vaultUrl(properties.getProperty("vault-url"))
                    .credential(new DefaultAzureCredentialBuilder().build()).buildClient();

            KeyVaultSecret secret = secretClient.getSecret("db-password");

            Connection connection = DriverManager.getConnection(properties.getProperty("db-url"), 
                properties.getProperty("db-user"), secret.getValue());
            PreparedStatement updateStatement = connection
                .prepareStatement("UPDATE TICKET SET STATUS = ? WHERE TICKET_ID = ?");

            updateStatement.setString(1, status);
            updateStatement.setString(2, id);

            int updated = updateStatement.executeUpdate();

            connection.close();

            if(updated == 0) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND).body("{\"result\":\"notfound\"}").build();
            } else {
                return request.createResponseBuilder(HttpStatus.OK).body("{\"result\":\"success\"}").build();
            }
            
        } catch (Throwable e) {
            context.getLogger().log(Level.SEVERE, e.getMessage(), e);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"result\":\"error\", \"message\":\""+ e.getMessage() + "\"}")
                    .build();
        }
    }
}
