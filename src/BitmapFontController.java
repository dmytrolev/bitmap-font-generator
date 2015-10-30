import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.FlowPane;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.canvas.Canvas;
import javafx.scene.effect.Shadow;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.FontPosture;
import javafx.scene.text.TextAlignment;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import javafx.scene.SnapshotParameters;
import javafx.event.ActionEvent;
import javafx.geometry.VPos;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.beans.value.ObservableValue;
import java.io.File;
import java.nio.file.FileSystems;

import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.net.URL;

public class BitmapFontController implements Initializable {
  protected GlyphBank mGlyphBank = new GlyphBank();

  protected boolean mEffect = false;

  @FXML public CheckBox mFillCb;
  @FXML public ColorPicker mFillColor;

  @FXML public ComboBox mSubFont;

  @FXML public CheckBox mStrokeCb;
  @FXML public TextField mStrokeWidthText;
  @FXML public ColorPicker mStrokeColor;

  @FXML public TextArea mText;
  @FXML public CheckBox mAlwaysAddSpace;
  @FXML public TextField mFontSizeText;
  @FXML public ListView mFontsList;

  @FXML public FlowPane mCustomImages;
  @FXML public TextField mCustomImageLetter;

  @FXML public CheckBox mShowBorderCb;
  @FXML public TextField mPaddingXText;
  @FXML public TextField mPaddingYText;

  @FXML public TextField mGlyphXMove;
  @FXML public TextField mGlyphDeltaWidth;
  @FXML public TextField mGlyphName;

  @FXML public TextField mLetterSpacing;

  @FXML public Canvas mCanvas;

  @FXML public Stage mStage;

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
    mFillColor.setValue(Color.WHITE);
    mStrokeColor.setValue(Color.BLACK);

    // setTestData();
    coreImportFont(FileSystems.getDefault().getPath("C:/Users/dlev/Downloads/framd.ttf").toFile());
    setASCIISymbols();
  }

  protected Map<CustomImageSymbolController, GlyphBank.Glyph> mExtraGlyphs = new HashMap<>();

  @FXML
  protected void addCustomSymbol() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image file", "*.png;*.jpg;*.jpeg"));
    fileChooser.setTitle("Add Image File");
    File imageFile = fileChooser.showOpenDialog(mStage);
    if(null == imageFile) return;
    String url = imageFile.toPath().toUri().toString();

    CustomImageSymbolController cisc = new CustomImageSymbolController(url, mCustomImageLetter.getText());
    cisc.callback = this::removeCustomSymbol;
    mCustomImages.getChildren().add(cisc);
    GlyphBank.Glyph newGlyph = mGlyphBank.createImageSymbol(mCustomImageLetter.getText(), cisc.img);
    mExtraGlyphs.put(cisc, newGlyph);
    drawText();
  }

  protected void removeCustomSymbol(CustomImageSymbolController cisc) {
    mCustomImages.getChildren().remove(cisc);
    GlyphBank.Glyph g = mExtraGlyphs.remove(cisc);
    mGlyphBank.removeGlyph(g);
    drawText();
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
    mText.setText("qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890,.:;?!-_'\"\\|/()[]{}+=*&%$@~#^><");
    drawText();
  }

  public void setTestData() {
    mText.setText("StFi");
    drawText();
  }

  protected String mSelectedGlyph = null;

  public void selectGlyph(MouseEvent event) {
    mSelectedGlyph = null;
    mGlyphName.setText("");
    mGlyphXMove.setText("0.0");
    mGlyphDeltaWidth.setText("0.0");
    double x = event.getX();
    double y = event.getY();
    System.out.printf("Click: %.2f %.2f\n", x, y);
    Set<GlyphBank.Glyph> chars = mGlyphBank.glyphs();
    for(GlyphBank.Glyph cc : chars) {
      if(x >= cc.x && x <= cc.x + Math.ceil(cc.w) && y >= cc.y && y <= cc.y + Math.ceil(cc.h)) {
        mSelectedGlyph = cc.c;
        mGlyphName.setText(mSelectedGlyph);
        if(mGlyphBank.modifiers.containsKey(mSelectedGlyph)) {
          GlyphBank.GlyphModificator modifier = mGlyphBank.modifiers.get(mSelectedGlyph);
          mGlyphXMove.setText(String.format("%.1f", modifier.deltaX));
          mGlyphDeltaWidth.setText(String.format("%.1f", modifier.deltaWidth));
        }
        break;
      }
    }
    mGlyphXMove.disableProperty().setValue(null == mSelectedGlyph);
    mGlyphDeltaWidth.disableProperty().setValue(null == mSelectedGlyph);
    drawText();
  }

  public void glyphDataChange(ActionEvent event) {
    if(null == mSelectedGlyph) return;

    double dx = getValue(mGlyphXMove, 0.0);
    double dw = getValue(mGlyphDeltaWidth, 0.0);
    boolean modifies = dx != 0.0d || dw != 0.0d;

    GlyphBank.GlyphModificator modifier = null;
    if(mGlyphBank.modifiers.containsKey(mSelectedGlyph)) {
      if(!modifies) {
        mGlyphBank.modifiers.remove(mSelectedGlyph);
        return;
      }
      modifier = mGlyphBank.modifiers.get(mSelectedGlyph);
    } else {
      if(!modifies) return;
      modifier = new GlyphBank.GlyphModificator(mSelectedGlyph);
      mGlyphBank.modifiers.put(mSelectedGlyph, modifier);
    }

    modifier.deltaWidth = dw;
    modifier.deltaX = dx;

    drawText();
  }

  public void drawText(ActionEvent event) { drawText(); }

  public void drawText() {
    GraphicsContext gc = mCanvas.getGraphicsContext2D();
    // build glyphs
    mGlyphBank.setFont(getFont());
    mGlyphBank.paddingX = getValue(mPaddingXText, 0.0);
    mGlyphBank.paddingY = getValue(mPaddingYText, 0.0);
    mGlyphBank.letterSpacing = getValue(mLetterSpacing, 0.0);
    mGlyphBank.extract(
      mText.getText(),
      mAlwaysAddSpace.isSelected(),
      mStrokeCb.isSelected() ? getValue(mStrokeWidthText, 1.0) : 0.0
    );
    Set<CustomImageSymbolController> customSymbols = new HashSet<>(mExtraGlyphs.keySet());
    mExtraGlyphs.clear();
    for(CustomImageSymbolController cisc : customSymbols) {
      GlyphBank.Glyph newGlyph = mGlyphBank.createImageSymbol(cisc.letterSymbol, cisc.img);
      mExtraGlyphs.put(cisc, newGlyph);
    }
    mGlyphBank.updateSize();
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
    drawGlyphs(gc, mGlyphBank, false);
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
    drawGlyphs(toSave.getGraphicsContext2D(), mGlyphBank, true);
    SnapshotParameters pp = new SnapshotParameters();
    pp.setFill(new Color(0d, 0d, 0d, 0d));
    toSave.snapshot(result -> {
        new BitmapSaver(result.getImage(), saveFile);
        new XmlSaver(mGlyphBank, saveFile);
        return null;
      }, pp, new WritableImage((int)mGlyphBank.size, (int)mGlyphBank.size));
  }

  public void saveBoxes(ActionEvent event) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image File", "*.png"));
    fileChooser.setTitle("Save bitmap font bounding boxes");
    fileChooser.setInitialFileName("font-boxes.png");
    File saveFile = fileChooser.showSaveDialog(mStage);
    if(null == saveFile) return;

    Canvas toSave = new Canvas(mGlyphBank.size, mGlyphBank.size);
    GraphicsContext gc = toSave.getGraphicsContext2D();
    toSave.getGraphicsContext2D().clearRect(0, 0, mGlyphBank.size, mGlyphBank.size);
    gc.setLineWidth(1d);
    gc.setStroke(Color.GREEN);
    drawBoundingBoxes(toSave.getGraphicsContext2D(), mGlyphBank);
    SnapshotParameters pp = new SnapshotParameters();
    pp.setFill(new Color(0d, 0d, 0d, 0d));
    toSave.snapshot(result -> {
        new BitmapSaver(result.getImage(), saveFile);
        return null;
      }, pp, new WritableImage((int)mGlyphBank.size, (int)mGlyphBank.size));
  }

  public void importFont(ActionEvent event) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("True Type Font", "*.ttf"));
    fileChooser.setTitle("Load font file");
    File fontFile = fileChooser.showOpenDialog(mStage);
    if(null == fontFile) return;
  }

  protected void coreImportFont(File fontFile) {

    Font newFont = Font.loadFont(fontFile.toURI().toString(), 20d);
    mFontsList.getItems().add(newFont.getFamily());
    mFontsList.getSelectionModel().selectLast();
  }

  protected void drawGlyphs(GraphicsContext gc, GlyphBank gb, boolean hideBorder) {
    gc.setFont(getFont());
    java.awt.Font otherFont = new java.awt.Font(gc.getFont().getName(), java.awt.Font.PLAIN,
                                                (int)Math.round(gc.getFont().getSize()));
    Set<GlyphBank.Glyph> chars = gb.glyphs();
    for(GlyphBank.Glyph cc : chars) {
      GlyphBank.GlyphModificator modifier = mGlyphBank.modifiers.get(cc.c);

      gc.setTextBaseline(VPos.BASELINE);
      double x = cc.x - cc.w1;
      double y = cc.y + Math.ceil(cc.h1);
      double gx = x;
      double gy = y;
      if(null != modifier) {
        gx += modifier.deltaX;
        gy += modifier.deltaY;
      }
      if(null != cc.image) {
        gc.drawImage(cc.image, gx, gy - Math.ceil(cc.h1));
        if(mShowBorderCb.isSelected() && !hideBorder) {
          gc.setLineWidth(1d);
          if(null != mSelectedGlyph && mSelectedGlyph.equals(cc.c)) gc.setStroke(Color.RED);
          else gc.setStroke(null == modifier ? Color.GREEN : Color.CYAN);
          if(null != modifier)
            gc.strokeRect(cc.x,cc.y,cc.w + modifier.deltaWidth,cc.h);
          else gc.strokeRect(cc.x,cc.y,cc.w,cc.h);
        }
        continue;
      }
      Double strokeWidth = getValue(mStrokeWidthText, 1.0);
      if(mStrokeCb.isSelected() && strokeWidth > 0) {
        gc.setLineWidth(strokeWidth);
        gc.setLineJoin(StrokeLineJoin.ROUND);
        gc.setStroke(mStrokeColor.getValue());
        gc.strokeText(cc.c, gx, gy);
      }
      if(mEffect) {
        gc.setEffect(new Shadow(5, Color.BLACK));
        gc.fillText(cc.c, gx, gy);
        gc.setEffect(null);
      }
      if(mFillCb.isSelected()) {
        gc.setFill(mFillColor.getValue());
        gc.fillText(cc.c, gx, gy);
      }
      if(!hideBorder) {
        if(mShowBorderCb.isSelected() || (null != mSelectedGlyph && mSelectedGlyph.equals(cc.c))) {
          gc.setLineWidth(1d);
          if(null != mSelectedGlyph && mSelectedGlyph.equals(cc.c)) gc.setStroke(Color.RED);
          else gc.setStroke(null == modifier ? Color.GREEN : Color.CYAN);
          if(null != modifier) {
            gc.strokeRect(cc.x, cc.y, Math.ceil(cc.w) + modifier.deltaWidth, Math.ceil(cc.h));
          } else gc.strokeRect(cc.x, cc.y, Math.ceil(cc.w), Math.ceil(cc.h));
        }
      }
    }
  }

  protected void drawBoundingBoxes(GraphicsContext gc, GlyphBank gb) {
    gc.setFont(getFont());
    for(GlyphBank.Glyph cc : gb.glyphs()) {
      GlyphBank.GlyphModificator modifier = gb.modifiers.get(cc.c);
      gc.setLineWidth(1d);
      if(null != modifier) {
        gc.strokeRect(cc.x, cc.y, Math.ceil(cc.w) + modifier.deltaWidth, Math.ceil(cc.h));
      } else gc.strokeRect(cc.x, cc.y, Math.ceil(cc.w), Math.ceil(cc.h));
    }
  }

  protected Font getFont() {
    boolean bold = mFont.indexOf("Bold") >= 0;
    boolean oblique = mFont.indexOf("Oblique") >= 0 || mFont.indexOf("Italic") >= 0;
    return Font.font(mFont, bold ? FontWeight.BOLD : FontWeight.NORMAL,
                     oblique ? FontPosture.ITALIC : FontPosture.REGULAR, getValue(mFontSizeText, 14.0));
  }

  protected double getValue(TextField txt, double defVal) {
    String sizeText = txt.getText();
    double size = defVal;
    if(null != sizeText && !sizeText.isEmpty()) {
      try {
        size = Double.parseDouble(sizeText);
      } catch(NumberFormatException e) {
        size = defVal;
      }
    }
    return size;
  }
}
