package com.iarecruiter.shared.config;

import com.iarecruiter.shared.security.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
public class TenantAwareDataSource extends DelegatingDataSource {

    public TenantAwareDataSource(DataSource targetDataSource) {
        super(targetDataSource);
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = super.getConnection();
        setTenantId(connection);
        return connection;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection connection = super.getConnection(username, password);
        setTenantId(connection);
        return connection;
    }

    private void setTenantId(Connection connection) {
        String companyId = TenantContext.get();
        if (companyId != null) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("SELECT set_config('app.current_company_id', '"
                        + companyId.replace("'", "''") + "', true)");
            } catch (SQLException e) {
                log.warn("Could not set RLS tenant context on connection: {}", e.getMessage());
            }
        }
    }
}
