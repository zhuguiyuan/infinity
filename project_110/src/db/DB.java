package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DB {
  private final String url;

  static {
    try {
      Class.forName("org.sqlite.JDBC");
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public DB(String url) {
    this.url = url;
  }

  @FunctionalInterface
  private interface ConnectionTask<T> {
    T run(Connection conn) throws SQLException;
  }

  private <T> T withConnection(ConnectionTask<T> task) {
    try (Connection conn = DriverManager.getConnection(url)) {
      return task.run(conn);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public <T> List<T> queryList(String sql, Binder binder, RowMapper<T> mapper) {
    return withConnection(conn -> {
      try (PreparedStatement ps = conn.prepareStatement(sql)) {
        binder.bind(ps);
        try (ResultSet rs = ps.executeQuery()) {
          List<T> result = new ArrayList<>();
          while (rs.next()) {
            result.add(mapper.map(rs));
          }
          return result;
        }
      }
    });
  }

  public <T> Optional<T> queryOne(String sql, Binder binder, RowMapper<T> mapper) {
    List<T> list = queryList(sql, binder, mapper);
    return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
  }

  public Result execute(String sql, Binder binder) {
    return withConnection(conn -> {
      try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        binder.bind(ps);
        int affectedRows = ps.executeUpdate();
        long generatedKey = 0;
        try (ResultSet keys = ps.getGeneratedKeys()) {
          if (keys.next()) {
            generatedKey = keys.getLong(1);
          }
        }
        return new Result(generatedKey, affectedRows);
      }
    });
  }
}
