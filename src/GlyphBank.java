import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;

import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class GlyphBank {
  public double size;

  public static class Glyph implements Comparable<Glyph> {
    public final String c;
    public final double w;
    public final double h;
    public double x;
    public double y;
    public Glyph(String symbol, double width, double height) {
      c = symbol;
      w = width;
      h = height;
    }

    @Override
    public int compareTo(Glyph g) { return c.compareTo(g.c); }
  }

  protected Font mFont;

  protected Set<Glyph> mGlyphs = new TreeSet<Glyph>();

  public Set<Glyph> glyphs() { return new TreeSet<Glyph>(mGlyphs); }

  public GlyphBank() {
  }

  public void setFont(Font font) { mFont = font; }

  public void extract(String text) {
    mGlyphs.clear();
    char[] chars = text.toCharArray();
    Set<String> uniqueChars = new HashSet<String>();
    Text t = new Text();
    t.setFont(mFont);
    double total = 0d;
    for(char c : chars) {
      String cc = String.valueOf(c);
      if(!uniqueChars.contains(cc)) {
        uniqueChars.add(cc);
        t.setText(cc);
        double w = t.getBoundsInLocal().getWidth();
        double h = t.getBoundsInLocal().getHeight();
        mGlyphs.add(new Glyph(cc, w, h));
        total += w * h;
      }
    }
    System.out.println(total);
    System.out.println(Math.sqrt(total));
    System.out.println(Math.log(Math.sqrt(total)));
    size = 1 << (int)Math.ceil(Math.log(Math.sqrt(total * 1.2)) / Math.log(2));
    System.out.println(size);
  }
}
