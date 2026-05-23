package movie;

record Movie(
    String title, int year) {
}

record SavedMovie(
    Long id,
    Movie movie) {
}

record NewMovie(
    Movie movie) {
}
