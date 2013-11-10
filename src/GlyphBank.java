import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;

import javafx.geometry.VPos;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextBoundsType;

public class GlyphBank {
  public double size;
  public double lineHeight;
  public double baseLine;

  public double paddingX = 2;
  public double paddingY = 2;

  public static class Glyph implements Comparable<Glyph> {
    public final String c;
    public final double w;
    public final double lw;
    public final double w1;
    public final double h;
    public final double h1;
    public double x;
    public double y;
    public Glyph(String symbol, double width, double logicalWidth, double height,
                 double overbaseline, double moveright) {
      c = symbol;
      w = width;
      h = height;
      h1 = overbaseline;
      w1 = moveright;
      lw = logicalWidth;
    }

    @Override
    public int compareTo(Glyph g) { return c.compareTo(g.c); }
  }

  protected Font mFont;

  protected Set<Glyph> mGlyphs = new TreeSet<Glyph>();

  public Set<Glyph> glyphs() { return new TreeSet<Glyph>(mGlyphs); }

  public GlyphBank() {
  }

  public Font getFont() { return mFont; }

  public void setFont(Font font) { mFont = font; }

  public void extract(String text) {
    mGlyphs.clear();
    char[] chars = text.toCharArray();
    Set<String> uniqueChars = new HashSet<String>();
    Text t = new Text("z");
    t.setFont(mFont);
    t.setBoundsType(TextBoundsType.LOGICAL);
    lineHeight = t.getBoundsInLocal().getHeight();
    baseLine = t.getBaselineOffset();
    t.setTextOrigin(VPos.BASELINE);
    t.setBoundsType(TextBoundsType.VISUAL);
    double total = 0d;
    for(char c : chars) {
      String cc = String.valueOf(c);
      if(!uniqueChars.contains(cc)) {
        uniqueChars.add(cc);
        t.setText(cc);
        t.setBoundsType(TextBoundsType.LOGICAL);
        double lw = t.getBoundsInLocal().getWidth();
        t.setTextOrigin(VPos.BASELINE);
        double w = t.getBoundsInLocal().getWidth();
        double h = t.getBoundsInLocal().getHeight();
        mGlyphs.add(new Glyph(cc, w, lw, h, -t.getLayoutBounds().getMinY(), t.getLayoutBounds().getMinX()));
        total += w * h;
      }
    }
    size = 1 << (int)Math.ceil(Math.log(Math.sqrt(total)) / Math.log(2));
    while(!checkSize()) size *= 2;
  }

  protected boolean checkSize() {
    double x = paddingX;
    double y = paddingY;
    double maxh = 0;
    for(Glyph cc : mGlyphs) {
      if(x + cc.w + paddingX > size) {
        x = paddingX;
        y += maxh + paddingY;
        maxh = cc.h;
      } maxh = Math.max(maxh, cc.h);
      if(y + cc.h + paddingY > size) return false;
      cc.x = x;
      cc.y = y;
      x += cc.w + paddingX;
    }
    return true;
  }
}
