import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.io.File;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Set;

public class XmlSaver {
  protected final String[] HEADER = {"<?xml version=\"1.0\"?>", "<font>"};
  protected final String INFO =
    "  <info face=\"%s\" size=\"%.0f\" bold=\"%d\" italic=\"%d\" charset=\"%s\" " +
    "unicode=\"%d\" stretchH=\"%d\" smooth=\"%d\" aa=\"%d\" padding=\"%d,%d,%d,%d\" spacing=\"%d,%d\" />";
  protected final String COMMON =
    "  <common lineHeight=\"%.0f\" base=\"%.0f\" scaleW=\"%d\" scaleH=\"%d\" pages=\"%d\" packed=\"%d\" />";
  protected final String PAGE = "  <pages><page id=\"%d\" file=\"%s\" /></pages>";
  protected final String CHARS = "  <chars count=\"%d\" >";
  protected final String CHAR = "    <char id=\"%d\" x=\"%.0f\" y=\"%.0f\" width=\"%.0f\" height=\"%.0f\" "+
    "xoffset=\"%.0f\" yoffset=\"%.0f\" xadvance=\"%.0f\" page=\"%d\" chnl=\"%d\" letter=\"%s\" />";
  protected final String[] FOOTER = {"  </chars>", "</font>"};

  public XmlSaver(final GlyphBank gb, File file) {
    Set<GlyphBank.Glyph> glyphs = gb.glyphs();
    try(BufferedWriter writer = Files.newBufferedWriter(file.toPath(), Charset.forName("UTF-8"))) {
      String s;
      for(String s1 : HEADER) {
        writer.write(s1, 0, s1.length());
        writer.newLine();
      }
      s = String.format(INFO, "my-font", gb.getFont().getSize(), 0, 0, "", 0, 100, 1, 1, 0,0,0,0,0,0);
      writer.write(s, 0, s.length());
      writer.newLine();
      s = String.format(COMMON, gb.lineHeight, gb.baseLine, 128, 64, 1, 0);
      writer.write(s, 0, s.length());
      writer.newLine();
      s = String.format(PAGE, 0, "font.png");
      writer.write(s, 0, s.length());
      writer.newLine();
      s = String.format(CHARS, glyphs.size());
      writer.write(s, 0, s.length());
      writer.newLine();
      for(GlyphBank.Glyph cc : glyphs) {
        s = String.format(CHAR, cc.c.codePointAt(0), cc.x, cc.y, Math.max(cc.w, 0), Math.max(cc.h, 0), cc.w1,
                          gb.baseLine-cc.h1, cc.lw, 0, 0, toLetter(cc.c));
        writer.write(s, 0, s.length());
        writer.newLine();
      }
      for(String s2 : FOOTER) {
        writer.write(s2, 0, s2.length());
        writer.newLine();
      }
    } catch(IOException x) {
      System.err.format("IOException: %s%n", x);
    }
  }

  protected String toLetter(String c) {
    if(" ".equals(c)) return "space";
    if("<".equals(c)) return "less then";
    if(">".equals(c)) return "grater then";
    if("&".equals(c)) return "ampersand";
    if("\\".equals(c)) return "back slash";
    if("/".equals(c)) return "slash";
    if("'".equals(c)) return "quote";
    if("\"".equals(c)) return "double quote";
    return c;
  }
}
