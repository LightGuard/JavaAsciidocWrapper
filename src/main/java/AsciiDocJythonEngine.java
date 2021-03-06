import java.io.*;

import javax.script.*;
import org.python.core.PySystemState;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;

/**
 * This class executes the AsciiDoc python scripts (asciidoc.py and a2x.py) from Java using the Jython interpreter.
 * 
 * <p>
 * The goal is to eventually use this class in a Maven or Gradle plugin to allow AsciiDoc to be integrated into a build.
 * </p>
 * 
 * <p>
 * Ideally, we would like to use the JSR-233 APIs. However, at the time of writing, the Jython implementation of JSR-233 did not support passing
 * arguments to the script, which is essential for using the AsciiDoc scripts.
 * </p>
 * 
 * Compile:
 * 
 * javac -cp jython.jar AsciiDoc.java
 * 
 * Usage:
 * 
 * java -cp .:jython.jar AsciiDoc mydoc.asciidoc java -cp .:jython.jar AsciiDoc mydoc.asciidoc html5 java -cp .:jython.jar AsciiDoc mydoc.asciidoc
 * docbook
 */
public class AsciiDocJythonEngine {
  private final File asciidocProgram;
  private final File a2xProgram;

  public enum Mode {
    docbook45, xhtml11, html4, html5, slidy, wordpress, latex;
  }

  public AsciiDocJythonEngine(String asciidocHome) {
    super();
    this.asciidocProgram = new File(asciidocHome + File.separator + "asciidoc.py");
    this.a2xProgram = new File(asciidocHome + File.separator + "a2x.py");
  }

  public AsciiDocJythonEngine() {
    this(ASCIIDOC_INSTALL_DIR);
  }

  // private static final String ASCIIDOC_INSTALL_DIR =
  // System.getProperty("user.home") + "/mirror/checkout/asciidoc";
  private static final String ASCIIDOC_INSTALL_DIR = "./asciidoc-8.6.7";

  public void main(String[] args) {
    if (args.length == 0) {
      System.err.println("Expecting a document to process");
      System.exit(-1);
    }
    String document = args[0];
    String outputFormat = "html5";
    if (args.length == 2) {
      outputFormat = args[1];
    }
    File program = a2xProgram;
    if ("html5".equals(outputFormat)) {
      program = asciidocProgram;
    }
    processDoc(document, outputFormat, program);
  }

  void processDoc(String document, Mode mode) {

      processDoc(document, mode.name(), asciidocProgram);
  }

  private static void processDoc(String document, String outputFormat, File programPath) {

    StringBuilder command = new StringBuilder(programPath.getAbsolutePath());
    PySystemState sys = new PySystemState();
    sys.argv.clear();
    sys.argv.append(new PyString(programPath.getAbsolutePath()));

    if ("html5".equals(outputFormat) || "xhtml11".equals(outputFormat)) {
      sys.argv.append(new PyString("-b" + outputFormat));
      sys.argv.append(new PyString("-atoc2"));
      sys.argv.append(new PyString("-apygments"));
      // sys.argv.append(new PyString("-aicons"));
      // sys.argv.append(new PyString("-adata-uri"));
      // sys.argv.append(new PyString("-atheme=default"));
    } else {
      sys.argv.append(new PyString("-f" + outputFormat));
      command.append(" -f" + outputFormat);
    }
    sys.argv.append(new PyString(document));

    PythonInterpreter python = new PythonInterpreter(null, sys);
    python.set("__file__", programPath.getAbsolutePath());
    long before = System.currentTimeMillis();
    System.out.println(command);
    python.exec(readFile(programPath));
    System.out.println("Executed in " + ((System.currentTimeMillis() - before) / 1000d) + "s");
  }

  private static String readFile(File f) {
    StringBuilder contents = new StringBuilder();

    try {
      BufferedReader input = new BufferedReader(new FileReader(f));
      try {
        String line = null;
        while ((line = input.readLine()) != null) {
          contents.append(line);
          contents.append(System.getProperty("line.separator"));
        }
      } finally {
        input.close();
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }

    return contents.toString();
  }
}