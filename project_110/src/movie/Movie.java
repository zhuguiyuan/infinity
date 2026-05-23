package movie;

sealed interface MovieEntity {
    String title();

    int year();
}

record NewMovie(String title, int year) implements MovieEntity {
}

record SavedMovie(long id, String title, int year) implements MovieEntity {
}
