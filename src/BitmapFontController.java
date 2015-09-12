import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.canvas.Canvas;
import javafx.scene.effect.Shadow;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.FontPosture;
import javafx.scene.text.TextAlignment;
import javafx.scene.SnapshotParameters;
import javafx.event.ActionEvent;
import javafx.geometry.VPos;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.fxml.Initializable;
import javafx.beans.value.ObservableValue;
import java.io.File;

import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;

import java.util.Set;
import java.util.List;
import java.util.ResourceBundle;
import java.net.URL;

public class BitmapFontController implements Initializable {
  protected GlyphBank mGlyphBank = new GlyphBank();

  protected boolean mEffect = false;

  public CheckBox mFillCb;
  public ColorPicker mFillColor;

  public ComboBox mSubFont;

  public CheckBox mStrokeCb;
  public Slider mStrokeWidth;
  public ColorPicker mStrokeColor;

  public TextArea mText;
  public Slider mFontSize;
  public ListView mFontsList;

  public CheckBox mShowBorderCb;
  public Slider mPaddingX;
  public Slider mPaddingY;

  public Canvas mCanvas;

  public Stage mStage;

  protected String mFont = "Calibri";
  protected String mFamily = "Calibri";

  static class FontNameCell extends ListCell<String> {
    public FontNameCell() {
      setPrefHeight(60);
      setAlignment(Pos.CENTER);
    }

    @Override
    public void updateItem(String item, boolean empty) {
      super.updateItem(item, empty);
      setFont(Font.font(item, FontWeight.NORMAL, 12));
      setTextAlignment(TextAlignment.CENTER);
      setText(item);
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    mFontsList.setCellFactory(list -> new FontNameCell());
    ObservableList fonts = FXCollections.observableArrayList(Font.getFamilies());
    mFontsList.setItems(fonts);
    mFontsList.getSelectionModel().selectedItemProperty().addListener(this::onFontSelectChange);
    mSubFont.getSelectionModel().selectedItemProperty().addListener(this::onSubFontSelectChange);
    mFillColor.setValue(Color.BLACK);

    setASCIISymbols();
  }

  protected void onFontSelectChange(ObservableValue ov, Object oldv, Object newv) {
    mFamily = (String)newv;
    List<String> names = Font.getFontNames(mFamily);
    mSubFont.getItems().clear();
    for(String name : names) {
      String shortName = name.startsWith(mFamily) ? name.substring(mFamily.length()) : name;
      if(shortName.isEmpty()) shortName = " default";
      mSubFont.getItems().add(shortName);
    }
    if(null == mSubFont.getSelectionModel().getSelectedItem())
      mSubFont.getSelectionModel().selectFirst();
    drawText();
  }

  protected void onSubFontSelectChange(ObservableValue ov, Object oldv, Object newv) {
    if(null == newv) return;
    String subtype = (String)newv;
    mFont = mFamily + (" default".equals(subtype) ? "" : subtype);
    drawText();
  }

  public void setASCIISymbols() {
    mText.setText("qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890,.:;?!-_'\"\\|/[]{}+=*&%$@~#^><");
    drawText();
  }

  public void drawText(ActionEvent event) { drawText(); }

  public void drawText() {
    GraphicsContext gc = mCanvas.getGraphicsContext2D();
    // build glyphs
    mGlyphBank.setFont(getFont());
    mGlyphBank.paddingX = mPaddingX.getValue();
    mGlyphBank.paddingY = mPaddingY.getValue();
    mGlyphBank.extract(mText.getText());
    // resize canvas
    mCanvas.setWidth(mGlyphBank.size);
    mCanvas.setHeight(mGlyphBank.size);
    // prepare background
    gc.clearRect(0, 0, Double.MAX_VALUE, Double.MAX_VALUE);
    gc.setFill(Color.WHITE);
    gc.setLineWidth(1d);
    gc.setStroke(Color.BLACK);
    gc.fillRect(0, 0, mGlyphBank.size, mGlyphBank.size);
    gc.strokeRect(0, 0, mGlyphBank.size, mGlyphBank.size);
    // print glyphs
    drawGlyphs(gc, mGlyphBank);
  }

  public void saveFont(ActionEvent event) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Font File", "*.fnt"));
    fileChooser.setTitle("Save bitmap font");
    fileChooser.setInitialFileName("font.fnt");
    File saveFile = fileChooser.showSaveDialog(mStage);
    if(null == saveFile) return;

    Canvas toSave = new Canvas(mGlyphBank.size, mGlyphBank.size);
    toSave.getGraphicsContext2D().clearRect(0, 0, mGlyphBank.size, mGlyphBank.size);
    drawGlyphs(toSave.getGraphicsContext2D(), mGlyphBank);
    SnapshotParameters pp = new SnapshotParameters();
    pp.setFill(new Color(0d, 0d, 0d, 0d));
    toSave.snapshot(result -> {
        new BitmapSaver(result.getImage(), saveFile);
        new XmlSaver(mGlyphBank, saveFile);
        return null;
      }, pp, new WritableImage((int)mGlyphBank.size, (int)mGlyphBank.size));
  }

  public void importFont(ActionEvent event) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("True Type Font", "*.ttf"));
    fileChooser.setTitle("Load font file");
    File fontFile = fileChooser.showOpenDialog(mStage);
    if(null == fontFile) return;

    Font newFont = Font.loadFont(fontFile.toURI().toString(), 20d);
    mFontsList.getItems().add(newFont.getFamily());
    mFontsList.getSelectionModel().selectLast();
  }

  protected void drawGlyphs(GraphicsContext gc, GlyphBank gb) {
    gc.setFont(getFont());
    System.out.println("Trying font " + gc.getFont().getName());
    java.awt.Font otherFont = new java.awt.Font(gc.getFont().getName(), java.awt.Font.PLAIN,
                                                (int)Math.round(gc.getFont().getSize()));
    Set<GlyphBank.Glyph> chars = gb.glyphs();
    for(GlyphBank.Glyph cc : chars) {

      FontRenderContext frc = new FontRenderContext(new AffineTransform(1,0,0,1,0,0), false, false);
      GlyphVector glyphs = otherFont.createGlyphVector(frc, cc.c.toCharArray());
      GlyphMetrics metrix = glyphs.getGlyphMetrics(0);
      System.out.println(String.format("%6$b advance=%1$.2f advanceX=%2$.2f advanceY=%3$.2g LSB=%4$.2g RSB=%5$.2g, " +
                                       "%7$b %8$b %9$b %10$b X=%11$.2f Y=%12$.2f %13$c w=%14$.2f h=%15$.2f",
                                       metrix.getAdvance(), metrix.getAdvanceX(), metrix.getAdvanceY(),
                                       metrix.getLSB(), metrix.getRSB(), otherFont.canDisplay(cc.c.toCharArray()[0]),
                                       metrix.isCombining(), metrix.isComponent(),
                                       metrix.isLigature(), metrix.isStandard(),
                                       metrix.getBounds2D().getX(), metrix.getBounds2D().getY(), cc.c.toCharArray()[0],
                                       metrix.getBounds2D().getWidth(), metrix.getBounds2D().getHeight()));

      gc.setTextBaseline(VPos.BASELINE);
      double x = cc.x - cc.w1;
      double y = cc.y + cc.h1;
      if(mEffect) {
        gc.setEffect(new Shadow(5, Color.BLACK));
        gc.fillText(cc.c, x, y);
        gc.setEffect(null);
      }
      if(mFillCb.isSelected()) {
        gc.setFill(mFillColor.getValue());
        gc.fillText(cc.c, x, y);
      }
      if(mStrokeCb.isSelected() && mStrokeWidth.getValue() > 0) {
        gc.setLineWidth(mStrokeWidth.getValue());
        gc.setStroke(mStrokeColor.getValue());
        gc.strokeText(cc.c, x, y);
      }
      if(mShowBorderCb.isSelected()) {
        gc.setLineWidth(1d);
        gc.strokeRect(cc.x,cc.y,cc.w,cc.h);
      }
    }
  }

  protected Font getFont() {
    boolean bold = mFont.indexOf("Bold") >= 0;
    boolean oblique = mFont.indexOf("Oblique") >= 0 || mFont.indexOf("Italic") >= 0;
    return Font.font(mFont, bold ? FontWeight.BOLD : FontWeight.NORMAL,
                     oblique ? FontPosture.ITALIC : FontPosture.REGULAR, mFontSize.getValue());
  }
}
