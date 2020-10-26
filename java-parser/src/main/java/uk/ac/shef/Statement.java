package uk.ac.shef;

import java.util.LinkedHashSet;
import java.util.Set;

import com.github.javaparser.ast.Node;
import com.google.gson.JsonPrimitive;

public class Statement {
    final Set<Integer> lines = new LinkedHashSet<Integer>();
    final Node node;

    public Statement(Node node) {
        this.node = node;
    }

    public String toJSON() {
        // JsonObject object = new JsonObject();
        // JsonArray arr = new JsonArray();
        // for (Integer line : lines) {
        //     arr.add(line);
        // }
        // object.add("lines", arr);
        // object.addProperty("sourceCode", node.toStringWithoutComments());
        // return object.toString();

        return new JsonPrimitive(node.toStringWithoutComments()).toString();
    }
}
