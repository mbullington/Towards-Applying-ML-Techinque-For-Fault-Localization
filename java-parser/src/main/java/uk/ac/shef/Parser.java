
package uk.ac.shef;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;

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
   * @param Path                     to a Java source code
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

    /*
     * for (Integer statement : this.statements.keySet()) {
     * System.out.println(statement + " -> " +
     * this.statements.get(statement).toString()); }
     */
  }

  private void explore(String space, Node node) {
    /*
     * System.out.println(space + node.getBeginLine() + ":" + node.getEndLine() +
     * " type: " + node.getClass().getCanonicalName() + " has children? " +
     * (node.getChildrenNodes().isEmpty() ? "*false*" : "true"));
     */

    // ignore everything related to comments
    if (node.getClass().getCanonicalName().startsWith("com.github.javaparser.ast.comments.")) {
      return;
    }
    if (node.getClass().getCanonicalName().equals("com.github.javaparser.ast.body.EnumConstantDeclaration")) {
      return;
    }

    if (!node.getChildrenNodes().isEmpty()) {
      for (Node child : node.getChildrenNodes()) {
        explore(space + " ", child);
      }
    }

    // WE ARE A LEAF FROM THIS POINT FORWARD.

    // note: find control flow graph, extract block from that?

    Node rootNode = node.getParentNode();
    // is it a statement?
    if (node.getClass().getCanonicalName().startsWith("com.github.javaparser.ast.stmt.")
        && node.getBeginLine() == node.getEndLine()) {
      rootNode = node;
    } else if (rootNode != null && rootNode.getBeginLine() == rootNode.getEndLine()) {
      Node clone = node;
      Node parent = null;

      // to handle special cases: parameters, binary expressions, etc
      // search for the next 'Declaration' or 'Statement'
      while ((parent = clone.getParentNode()) != null) {
        if ((parent.getClass().getCanonicalName().startsWith("com.github.javaparser.ast.stmt."))
            || (parent.getClass().getCanonicalName().equals("com.github.javaparser.ast.body.VariableDeclarator"))
            || (parent.getClass().getCanonicalName().startsWith("com.github.javaparser.ast.body.")
                && parent.getClass().getCanonicalName().endsWith("Declaration"))) {
          rootNode = parent;
          break;
        }

        clone = parent;
      }
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
