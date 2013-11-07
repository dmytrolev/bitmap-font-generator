import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;

import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class GlyphBank {
  public static class Glyph implements Comparable<Glyph> {
    public final String c;
    public final double w;
    public final double h;
    public Glyph(String symbol, double width, double height) {
      c = symbol;
      w = width;
      h = height;
    }

    @Override
    public int compareTo(Glyph g) { return c.compareTo(g.c); }
  }

  protected String mFont;

  protected Set<Glyph> mGlyphs = new TreeSet<Glyph>();

  public Set<Glyph> glyphs() { return new TreeSet<Glyph>(mGlyphs); }

  public GlyphBank() {
  }

  public void setFont(String font) { mFont = font; }

  public void extract(String text) {
    mGlyphs.clear();
    char[] chars = text.toCharArray();
    Set<String> uniqueChars = new HashSet<String>();
    Text t = new Text();
    t.setFont(Font.font(mFont, FontWeight.BLACK, 40d));
    for(char c : chars) {
      String cc = String.valueOf(c);
      if(!uniqueChars.contains(cc)) {
        uniqueChars.add(cc);
        t.setText(cc);
        mGlyphs.add(new Glyph(cc, t.getBoundsInLocal().getWidth(), t.getBoundsInLocal().getHeight()));
      }
    }
  }
}
