package movie;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import db.DB;
import db.PageRequest;
import db.PageResult;

class MovieDao {
  private static final String URL = "jdbc:sqlite:mydb.db";
  private final DB db = new DB(URL);

  MovieDao() {
    db.execute(
        "CREATE TABLE IF NOT EXISTS movies ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "title TEXT NOT NULL,"
            + "year INTEGER NOT NULL)",
        ps -> {
        });
  }

  Optional<SavedMovie> findbyId(Long id) {
    return db.queryOne(
        "SELECT id, title, year FROM movies WHERE id = ?",
        ps -> ps.setLong(1, id),
        this::mapRow);
  }

  List<SavedMovie> findByIds(List<Long> ids) {
    if (ids.isEmpty()) {
      return List.of();
    }
    var placeholders = String.join(", ", Collections.nCopies(ids.size(), "?"));
    return db.queryList(
        "SELECT id, title, year FROM movies WHERE id IN (" + placeholders + ")",
        ps -> {
          for (int i = 0; i < ids.size(); i++) {
            ps.setLong(i + 1, ids.get(i));
          }
        },
        this::mapRow);
  }

  PageResult<SavedMovie> findByPage(PageRequest pageRequest) {
    long total = db.queryOne(
        "SELECT COUNT(*) FROM movies",
        ps -> {
        },
        rs -> rs.getLong(1))
        .orElse(0L);
    var data = db.queryList(
        "SELECT id, title, year FROM movies LIMIT ? OFFSET ?",
        ps -> {
          ps.setInt(1, pageRequest.size());
          ps.setInt(2, pageRequest.page() * pageRequest.size());
        },
        this::mapRow);
    return new PageResult<>(data, total, pageRequest.page(), pageRequest.size());
  }

  SavedMovie save(NewMovie movie) {
    var r = db.execute(
        "INSERT INTO movies (title, year) VALUES (?, ?)",
        ps -> {
          ps.setString(1, movie.movie().title());
          ps.setInt(2, movie.movie().year());
        });
    return new SavedMovie(r.generatedKey(), movie.movie());
  }

  List<SavedMovie> saveAll(List<NewMovie> movies) {
    return movies.stream().map(this::save).toList();
  }

  boolean update(SavedMovie movie) {
    return db.execute(
        "UPDATE movies SET title = ?, year = ? WHERE id = ?",
        ps -> {
          ps.setString(1, movie.movie().title());
          ps.setInt(2, movie.movie().year());
          ps.setLong(3, movie.id());
        }).affectedRows() > 0;
  }

  int updateAll(List<SavedMovie> movies) {
    return movies.stream().mapToInt(m -> update(m) ? 1 : 0).sum();
  }

  boolean delete(Long id) {
    return db.execute(
        "DELETE FROM movies WHERE id = ?",
        ps -> ps.setLong(1, id)).affectedRows() > 0;
  }

  int deleteByIds(List<Long> ids) {
    if (ids.isEmpty()) {
      return 0;
    }
    var placeholders = String.join(", ", Collections.nCopies(ids.size(), "?"));
    return db.execute(
        "DELETE FROM movies WHERE id IN (" + placeholders + ")",
        ps -> {
          for (int i = 0; i < ids.size(); i++) {
            ps.setLong(i + 1, ids.get(i));
          }
        }).affectedRows();
  }

  private SavedMovie mapRow(ResultSet rs) throws SQLException {
    var movie = new Movie(rs.getString("title"), rs.getInt("year"));
    return new SavedMovie(rs.getLong("id"), movie);
  }

  public static void main(String[] args) {
    var dao = new MovieDao();

    var saved = dao.save(new NewMovie(new Movie("你好世界", 2026)));
    System.out.println("save: id=" + saved.id());

    var found = dao.findbyId(saved.id());
    System.out.println("found: " + found);

    var updated = dao.update(new SavedMovie(saved.id(), new Movie("再见世界", 2025)));
    System.out.println("update: " + updated);

    var deleted = dao.delete(saved.id());
    System.out.println("delete: " + deleted);
  }
}
