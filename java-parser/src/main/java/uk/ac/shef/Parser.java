
package uk.ac.shef;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.BlockStmt;

import java.io.FileInputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class Parser {

  private Map<Integer, Statement> statements = new LinkedHashMap<Integer, Statement>();

  private final String sourceName;

  /**
   * Parser constructor
   *
   * @param Statement-suspiciousness vector
   * @param Path to a Java source code
   */
  public Parser(String sourceName) {
    this.sourceName = sourceName;
  }

  public Map<Integer, Statement> getStatements() {
    return this.statements;
  }

  /**
   * 
   * @throws Exception
   */
  public void parse() throws Exception {

    // creates an input stream for the file to be parsed
    FileInputStream in = new FileInputStream(this.sourceName);

    CompilationUnit cu;
    try {
      // parse the file
      cu = JavaParser.parse(in);
    } finally {
      in.close();
    }

    // explore tree
    explore(" ", cu);

    /*for (Integer statement : this.statements.keySet()) {
      System.out.println(statement + " -> " + this.statements.get(statement).toString());
    }*/
  }

  private void explore(String space, Node node) {
    /*System.out.println(space + node.getBeginLine() + ":" + node.getEndLine() + " type: "
        + node.getClass().getCanonicalName() + " has children? "
        + (node.getChildrenNodes().isEmpty() ? "*false*" : "true"));*/

    // ignore everything related to comments
    if (node.getClass().getCanonicalName()
        .startsWith("com.github.javaparser.ast.comments.")) {
      return ;
    }
    if (node.getClass().getCanonicalName()
        .equals("com.github.javaparser.ast.body.EnumConstantDeclaration")) {
      return ;
    }

    if (!node.getChildrenNodes().isEmpty()) {
      for (Node child : node.getChildrenNodes()) {
        explore(space + " ", child);
      }
    }

    // WE ARE A LEAF FROM THIS POINT FORWARD.
  
    Node rootNode = node;
    while (rootNode != null && !(rootNode instanceof BlockStmt)) {
      rootNode = rootNode.getParentNode();
    }

    if (rootNode == null) {
      return;
    }

    // For if statements and similar, try to get one up from the block.
    // This would be more helpful in general.
    if (rootNode.getParentNode() != null) {
      rootNode = rootNode.getParentNode();
    }

    Integer lineNumber = rootNode.getBeginLine();
    Statement statement = null;

    if (statements.containsKey(lineNumber)) {
      statement = statements.get(lineNumber);
    } else {
      statement = new Statement(rootNode);
      statements.put(lineNumber, statement);
    }

    statement.lines.add(node.getBeginLine());
  }
}
