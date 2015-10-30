import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.List;
import java.util.ArrayList;

import javafx.geometry.VPos;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextBoundsType;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;

public class GlyphBank {
  public double size;
  public double total;
  public double lineHeight;
  public double baseLine;
  public double strokeSize;
  public double letterSpacing;

  public double paddingX = 2;
  public double paddingY = 2;

  public double innerPaddingX = 2;
  public double innerPaddingY = 2;

  public Map<String, GlyphModificator> modifiers = new HashMap<>();

  public static class GlyphModificator {
    public double deltaWidth = 0d;
    public double deltaX = 0d;
    public double deltaY = 0d;
    public final String c;

    public GlyphModificator(String c) { this.c = c; }
  }

  public static class Glyph implements Comparable<Glyph> {
    public final String c;
    public final double w;
    public final double lw;
    public final double w1;
    public final double h;
    public final double h1;
    public final Image image;
    public double x;
    public double y;
    public Glyph(String symbol, double width, double logicalWidth, double height,
                 double overbaseline, double moveright, Image image) {
      c = symbol;
      w = width;
      h = height;
      h1 = overbaseline;
      w1 = moveright;
      lw = logicalWidth;
      this.image = image;
    }

    @Override
    public int compareTo(Glyph g) { return c.compareTo(g.c); }
  }

  public static class Kerning {
    public final String left;
    public final String right;
    public final double kerning;

    public Kerning(String left, String right, double kerning) {
      this.left = left;
      this.right = right;
      this.kerning = kerning;
    }
  }

  protected javafx.scene.text.Font mFont;

  protected Set<Glyph> mGlyphs = new TreeSet<Glyph>();

  protected List<Kerning> mKerning = new ArrayList<Kerning>();

  public Set<Glyph> glyphs() { return new TreeSet<Glyph>(mGlyphs); }

  public List<Kerning> kernings() { return mKerning; }

  public GlyphBank() {
  }

  public javafx.scene.text.Font getFont() { return mFont; }

  public void setFont(javafx.scene.text.Font font) { mFont = font; }

  public void extract(String text, boolean addSpace, double strokeWidth) {
    mGlyphs.clear();
    if(addSpace) text += " ";
    char[] chars = text.toCharArray();
    Set<String> uniqueChars = new HashSet<>();
    Map<String, Glyph> glyphsMap = new HashMap<>();
    Text t = new Text("Z");
    t.setFont(getFont());
    t.setStroke(Color.RED);
    t.setStrokeLineJoin(StrokeLineJoin.ROUND);
    t.setStrokeWidth(strokeWidth);
    t.setBoundsType(TextBoundsType.LOGICAL);
    lineHeight = t.getBoundsInLocal().getHeight();
    baseLine = t.getBaselineOffset();
    t.setTextOrigin(VPos.BASELINE);
    t.setBoundsType(TextBoundsType.VISUAL);
    total = 0d;
    for(char c : chars) {
      String cc = String.valueOf(c);
      if(!uniqueChars.contains(cc)) {
        uniqueChars.add(cc);
        t.setText(cc);
        t.setBoundsType(TextBoundsType.LOGICAL);
        double lw = t.getBoundsInLocal().getWidth();
        t.setBoundsType(TextBoundsType.VISUAL);
        double minY = -t.getLayoutBounds().getMinY();
        double minX = t.getLayoutBounds().getMinX();
        double w = t.getBoundsInLocal().getWidth();
        double h = t.getBoundsInLocal().getHeight();
        // Glyph glyph = new Glyph(cc, w, Math.max(w + minX, lw), h, minY, minX, null);
        Glyph glyph = new Glyph(cc, w, lw + minX, h, minY, minX, null);
        mGlyphs.add(glyph);
        glyphsMap.put(cc, glyph);
        total += w * h;
      }
    }
    mKerning.clear();
    for(String c1 : uniqueChars) {
      if(" ".equals(c1)) continue;
      Glyph left = glyphsMap.get(c1);
      for(String c2 : uniqueChars) {
        if(" ".equals(c2)) continue;
        Glyph right = glyphsMap.get(c2);
        t.setText(c1 + c2);
        double w = t.getBoundsInLocal().getWidth();
        mKerning.add(new Kerning(c1, c2, left.w + right.w + right.w1 - w));
      }
    }
  }

  public void removeGlyph(Glyph g) { mGlyphs.remove(g); }

  public void updateSize() {
    size = 1 << (int)Math.ceil(Math.log(Math.sqrt(total)) / Math.log(2));
    while(!checkSize()) size *= 2;
  }

  public Glyph createImageSymbol(String latter, Image image) {
    double y;
    if(lineHeight - image.getHeight() < 0)
      y = Math.floor((lineHeight - image.getHeight()) / 2);
    else
      y = Math.floor(lineHeight - image.getHeight()) / 2;
    Glyph newGlyph = new Glyph(
        latter,
        image.getWidth(), image.getWidth(),
        image.getHeight(), y, 0,
        image
    );

    mGlyphs.add(newGlyph);
    return newGlyph;
  }

  protected boolean checkSize() {
    double x = paddingX;
    double y = paddingY;
    double maxh = 0;
    for(Glyph cc : mGlyphs) {
      GlyphModificator modifier = modifiers.get(cc.c);
      double extraWidth = modifier != null ? modifier.deltaWidth : 0;
      if(x + cc.w + extraWidth + paddingX > size) {
        x = paddingX;
        y += Math.ceil(maxh) + paddingY;
        maxh = cc.h;
      } maxh = Math.max(maxh, cc.h);
      if(y + cc.h + paddingY > size) return false;
      cc.x = x;
      cc.y = y;
      x += Math.ceil(cc.w) + paddingX + extraWidth;
    }
    return true;
  }
}
