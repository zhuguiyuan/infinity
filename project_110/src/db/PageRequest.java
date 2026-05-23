package db;

public record PageRequest(
    int page,
    int size) {

  public int getOffset() {
    return page * size;
  }
}
