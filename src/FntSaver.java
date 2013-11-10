import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.io.File;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Set;

public class FntSaver {
  protected final String INFO =
    "info face=\"%s\" size=%d bold=%d italic=%d charset=\"%s\" " +
    "unicode=%d stretchH=%d smooth=%d aa=%d padding=%d,%d,%d,%d spacing=%d,%d";
  protected final String COMMON =
    "common lineHeight=%d base=%d scaleW=%d scaleH=%d pages=%d packed=%d";
  protected final String PAGE = "page id=%d file=\"%s\"";
  protected final String CHARS = "chars count=%d";
  protected final String CHAR =
    "char id=%d x=%.0f y=%.0f width=%.0f height=%.0f xoffset=%d yoffset=%d xadvance=%d page=%d chnl=%d letter=\"%s\"";

  public FntSaver(final GlyphBank gb) {
    Set<GlyphBank.Glyph> glyphs = gb.glyphs();
    Path file = Paths.get("my-font.fnt");
    try(BufferedWriter writer = Files.newBufferedWriter(file, Charset.forName("UTF-8"))) {
      String s = String.format(INFO, "my-font", 40, 0, 0, "", 0, 100, 1, 1, 0,0,0,0,0,0);
      writer.write(s, 0, s.length());
      writer.newLine();
      s = String.format(COMMON, 42, 35, 128, 64, 1, 0);
      writer.write(s, 0, s.length());
      writer.newLine();
      s = String.format(PAGE, 0, "imageops-snapshot.png");
      writer.write(s, 0, s.length());
      writer.newLine();
      s = String.format(CHARS, glyphs.size());
      writer.write(s, 0, s.length());
      writer.newLine();
      for(GlyphBank.Glyph cc : glyphs) {
        s = String.format(CHAR, cc.c.codePointAt(0), cc.x, cc.y, cc.w, cc.h, 0, 0, 0, 0, 0, toLetter(cc.c));
        writer.write(s, 0, s.length());
        writer.newLine();
      }
    } catch(IOException x) {
      System.err.format("IOException: %s%n", x);
    }
  }

  protected String toLetter(String c) {
    if(" ".equals(c)) return "space";
    return c;
  }
}
