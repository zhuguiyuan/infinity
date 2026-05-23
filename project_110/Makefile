SRC_DIR := src
BIN_DIR := bin
LIB_DIR := lib

SRCS := $(shell find $(SRC_DIR) -name '*.java')
JARS := $(shell find $(LIB_DIR) -name '*.jar' 2>/dev/null)
CP := $(BIN_DIR):$(shell echo $(JARS) | tr ' ' ':')

.PHONY: compile run clean

compile: $(SRCS)
	@mkdir -p $(BIN_DIR)
	javac -cp "$(CP)" -d $(BIN_DIR) $(SRCS)

run: compile
	java -cp "$(CP)" movie.MovieDao

clean:
	rm -rf $(BIN_DIR) mydb.db
