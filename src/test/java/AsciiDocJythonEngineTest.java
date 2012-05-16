import java.io.IOException;

import org.springframework.core.io.ClassPathResource;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AsciiDocJythonEngineTest {
  final AsciiDocJythonEngine engine = new AsciiDocJythonEngine();

  @Test(dataProvider = "sourcefiles")
  public void test(String path)  {
    engine.processDoc(path, AsciiDocJythonEngine.Mode.xhtml11);
  }

  @DataProvider(name = "sourcefiles")
  public Object[][] createDataProvider()  {
    return new String[][] { { createAbsolutePathFromRelativePath("javaeeworkshop.asciidoc") },
        { createAbsolutePathFromRelativePath("tutorial.asciidoc") },
    { createAbsolutePathFromRelativePath("sample.asciidoc") }
    };
  }

  private static final String createAbsolutePathFromRelativePath(String relativePath) {
    try {
      final ClassPathResource doc = new ClassPathResource(relativePath);
      Assert.assertTrue(doc.exists());
      return doc.getFile().getAbsolutePath();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
