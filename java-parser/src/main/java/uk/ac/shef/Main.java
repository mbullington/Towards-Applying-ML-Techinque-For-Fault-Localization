package uk.ac.shef;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Map;

import com.google.gson.JsonObject;

public class Main {

  public static final String PATH_SEPARATOR = System.getProperty("path.separator");
  public static final String FILE_SEPARATOR = System.getProperty("file.separator");

  public static void main(String[] args) throws Exception {

    if (args.length < 3) {
      System.err.println("Usage \n"
          + "  $ java -jar java-parser-0.0.1-SNAPSHOT-jar-with-dependencies.jar "
          + "<dir.src.classes> "
          + "<list of loaded classes> "
          + "<output file name>");
      System.exit(-1);
    }

    String srcClasses = args[0];
    String listLoadedClasses = args[1];
    String outputFileName = args[2];

    File file = new File(outputFileName);
    // if file does not exist, then create it
    if (!file.exists()) {
      file.createNewFile();
    }

    FileWriter fw = new FileWriter(file.getAbsoluteFile());
    BufferedWriter bw = new BufferedWriter(fw);

    // Output a JSON object of statements.
    // Don't use GSON for this object it tries to use way too much memory.
    bw.write("{");

    // Sacrifice disk space to make lookup a 2 * N(1) operation.
    JsonObject keyMap = new JsonObject();

    for (String clazz : listLoadedClasses.split(PATH_SEPARATOR)) {
      String filePath = srcClasses + FILE_SEPARATOR + clazz.replace(".", FILE_SEPARATOR) + ".java";

      // parse a Java file and get a Map<Statement number, List of *all* lines
      // that compose a Statement
      Parser p = new Parser(filePath);
      p.parse();

      Map<Integer, Statement> statements = p.getStatements();      
      for (Integer statementBeginLine : statements.keySet()) {
        Statement statement = statements.get(statementBeginLine);
        String jsonPrefix = clazz.replace(".", "/") + ".java#";
        String jsonKey = new Integer((jsonPrefix + statementBeginLine).hashCode()).toString();

        for (Integer line : statement.lines) {
          keyMap.addProperty(jsonPrefix + line, jsonKey);
        }

        bw.write("\"" + jsonKey + "\":" + statement.toJSON());
        // Last comma is picked up by the keyMap.
        bw.write(",");
      }
    }

    bw.write("\"keyMap\":" + keyMap.toString());

    // Finish JSON object.
    bw.write("}");
    bw.close();
    System.exit(0);
  }
}
