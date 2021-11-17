package com.function.ticket;

import com.microsoft.azure.functions.HttpMethod;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

public class Function {
    @FunctionName("getTicketStatus")
    public HttpResponseMessage getTicketStatus(@HttpTrigger(name = "req", methods = { HttpMethod.GET}, 
            authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Entering GetTicketStatus.");
        
        Connection connection = null;
        PreparedStatement selectStatement = null;
        ResultSet rs = null;
        String status = null;

        try {
            // Parse query parameter
            final String id = request.getQueryParameters().get("id");

            if (id == null) {
                context.getLogger().info("Id must have non-null values");
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("{\"result\":\"error\", \"message\":\"invalidargument\"}").build();
            }
            Properties properties = new Properties();
            properties.load(Function.class.getClassLoader().getResourceAsStream("application.properties"));
            SecretClient secretClient = new SecretClientBuilder().vaultUrl(properties.getProperty("vault-url"))
                    .credential(new DefaultAzureCredentialBuilder().build()).buildClient();
            KeyVaultSecret secret = secretClient.getSecret("db-password");
            connection = DriverManager.getConnection(properties.getProperty("db-url"), 
                properties.getProperty("db-user"), secret.getValue());
            selectStatement = connection
                .prepareStatement("SELECT STATUS FROM TICKET WHERE TICKET_ID = ?");
            selectStatement.setString(1, id);
            rs = selectStatement.executeQuery();
            while(rs.next()) {
                status = rs.getString(1);
            }

            if(status == null) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND).body("{\"result\":\"notfound\"}").build();
            } else {
                return request.createResponseBuilder(HttpStatus.OK).body("{\"result\":\"success\", \"status\":\"" + status + "\"}").build();
            }

        } catch(Throwable e) {
            context.getLogger().log(Level.SEVERE, e.getMessage(), e);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"result\":\"error\", \"message\":\""+ e.getMessage() + "\"}")
                .build();
        } finally {
            try {
                rs.close();
            } catch(Throwable e) {
                // ignore
            }
            try {
                selectStatement.close();
            } catch(Throwable e) {
                // ignore
            }
            try {
                connection.close();
            } catch(Throwable e) {
                // ignore
            }
        }
    }

    @FunctionName("updateTicketStatus")
    public HttpResponseMessage updateTicketStatus(@HttpTrigger(name = "req", methods = { HttpMethod.GET,
            HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Entering UpdateTicketStatus.");
        
        Connection connection = null;
        PreparedStatement updateStatement = null;

        try {
            // Parse query parameter
            final String id = request.getQueryParameters().get("id");
            final String status = request.getQueryParameters().get("status");

            if (id == null || status == null) {
                context.getLogger().info("Both id and status must have non-null values");
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("{\"result\":\"error\", \"message\":\"invalidargument\"}").build();
            }

            Properties properties = new Properties();
            properties.load(Function.class.getClassLoader().getResourceAsStream("application.properties"));

            SecretClient secretClient = new SecretClientBuilder().vaultUrl(properties.getProperty("vault-url"))
                    .credential(new DefaultAzureCredentialBuilder().build()).buildClient();

            KeyVaultSecret secret = secretClient.getSecret("db-password");

            connection = DriverManager.getConnection(properties.getProperty("db-url"), 
                properties.getProperty("db-user"), secret.getValue());
            updateStatement = connection
                .prepareStatement("UPDATE TICKET SET STATUS = ? WHERE TICKET_ID = ?");

            updateStatement.setString(1, status);
            updateStatement.setString(2, id);

            int updated = updateStatement.executeUpdate();

            if(updated == 0) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND).body("{\"result\":\"notfound\"}").build();
            } else {
                return request.createResponseBuilder(HttpStatus.OK).body("{\"result\":\"success\"}").build();
            }

        } catch (Throwable e) {
            context.getLogger().log(Level.SEVERE, e.getMessage(), e);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"result\":\"error\", \"message\":\""+ e.getMessage() + "\"}")
                .build();
        } finally {
            try {
                updateStatement.close();
            } catch(Throwable e) {
                // ignore
            }
            try {
                connection.close();
            } catch(Throwable e) {
                // ignore
            }
        }
    }

}