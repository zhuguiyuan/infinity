package db;

import java.util.List;

public record PageResult<T>(
    List<T> data,
    long total,
    int page,
    int size) {
  public int getTotalPages() {
    return (int) Math.ceil((double) total / size);
  }
}
