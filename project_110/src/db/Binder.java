package db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface Binder {
  void bind(PreparedStatement ps) throws SQLException;
}
