/*
 * This file is part of the GeOxygene project source files. GeOxygene aims at
 * providing an open framework which implements OGC/ISO specifications for the
 * development and deployment of geographic (GIS) applications. It is a open
 * source contribution of the COGIT laboratory at the Institut Géographique
 * National (the French National Mapping Agency). See:
 * http://oxygene-project.sourceforge.net Copyright (C) 2005 Institut
 * Géographique National This library is free software; you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the License,
 * or any later version. This library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details. You should have received a copy of
 * the GNU Lesser General Public License along with this library (see file
 * LICENSE if present); if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package fr.ign.cogit.geoxygene.appli;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import fr.ign.cogit.geoxygene.style.ExternalGraphic;
import fr.ign.cogit.geoxygene.style.FeatureTypeStyle;
import fr.ign.cogit.geoxygene.style.Layer;
import fr.ign.cogit.geoxygene.style.LineSymbolizer;
import fr.ign.cogit.geoxygene.style.Mark;
import fr.ign.cogit.geoxygene.style.PointSymbolizer;
import fr.ign.cogit.geoxygene.style.PolygonSymbolizer;
import fr.ign.cogit.geoxygene.style.RasterSymbolizer;
import fr.ign.cogit.geoxygene.style.Rule;
import fr.ign.cogit.geoxygene.style.Style;
import fr.ign.cogit.geoxygene.style.Symbolizer;
import fr.ign.cogit.geoxygene.style.UserStyle;

/**
 * @author Julien Perret
 */
public class LayerStylesPanel extends JPanel {

  /**
   * serial uid.
   */
  private static final long serialVersionUID = 1L;
  private Layer layer;

  public Layer getLayer() {
    return this.layer;
  }

  private int margin = 2;

  /**
   * Constructor.
   */
  public LayerStylesPanel(Layer aLayer) {
    this.layer = aLayer;
    this.setBackground(Color.white);
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g;
    Color originalColor = g.getColor();
    g.setColor(this.getBackground());
    g2.setStroke(new BasicStroke());
    g.fillRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
    // count how many rules we have
    int numberOfRules = 0;
    for (Style style : this.layer.getStyles()) {
      UserStyle userStyle = (UserStyle) style;
      for (FeatureTypeStyle fts : userStyle.getFeatureTypeStyles()) {
        numberOfRules += fts.getRules().size();
      }
    }
    int numberOfColumns = Math.round((float) Math.sqrt(numberOfRules));
    int numberOfRows = numberOfRules / numberOfColumns;

    int columnsWidth = (this.getWidth() - numberOfColumns * this.margin - this.margin)
        / numberOfColumns;
    int rowHeight = (this.getHeight() - numberOfRows * this.margin - this.margin)
        / numberOfRows;

    int currentColumn = 0;
    int currentRow = 0;

    for (Style style : this.layer.getStyles()) {
      UserStyle userStyle = (UserStyle) style;
      for (FeatureTypeStyle fts : userStyle.getFeatureTypeStyles()) {
        for (Rule rule : fts.getRules()) {
          for (Symbolizer symbolizer : rule.getSymbolizers()) {
            // g.setColor(symbolizer.getStroke().getColor());
            if (symbolizer.isLineSymbolizer()) {
              this.paintLine((LineSymbolizer) symbolizer, g2, currentColumn,
                  currentRow, columnsWidth, rowHeight);
            } else {
              if (symbolizer.isPointSymbolizer()) {
                this.paintPoint((PointSymbolizer) symbolizer, g2,
                    currentColumn, currentRow, columnsWidth, rowHeight);
                currentRow++;
              } else {
                if (symbolizer.isPolygonSymbolizer()) {
                  this.paintPolygon((PolygonSymbolizer) symbolizer, g2,
                      currentColumn, currentRow, columnsWidth, rowHeight);
                  currentRow++;
                } else {
                  if (symbolizer.isRasterSymbolizer()) {
                    this.paintRaster((RasterSymbolizer) symbolizer, g2,
                        currentColumn, currentRow, columnsWidth, rowHeight);
                    currentRow++;
                  }
                }
              }
            }
          }
          currentColumn++;
          if (currentColumn >= numberOfColumns) {
            currentColumn = 0;
          }
        }
      }
    }
    g.setColor(originalColor);
  }

  private void paintLine(LineSymbolizer symbolizer, Graphics2D g2,
      int currentColumn, int currentRow, int columnsWidth, int rowHeight) {
    g2.setStroke(symbolizer.getStroke().toAwtStroke());
    g2.setColor(symbolizer.getStroke().getColor());
    if (this.layer.getStyles().size() == 1) {
      g2.drawLine(currentColumn * (columnsWidth + this.margin), currentRow
          * (rowHeight + this.margin) + rowHeight / 2, (currentColumn + 1)
          * (columnsWidth + this.margin) + this.margin, currentRow
          * (rowHeight + this.margin) + rowHeight / 2);
    } else {
      g2.drawLine(currentColumn * (columnsWidth + this.margin), currentRow
          * (rowHeight + this.margin) + rowHeight, (currentColumn + 1)
          * (columnsWidth + this.margin) + this.margin, currentRow
          * (rowHeight + this.margin) + rowHeight);
    }
  }

  @Override
  public void setBounds(int x, int y, int w, int h) {
    super.setBounds(x, y, w, h);
    this.validate();
  }

  private void paintPoint(PointSymbolizer symbolizer, Graphics2D g2,
      int currentColumn, int currentRow, int columnsWidth, int rowHeight) {
    for (Mark mark : symbolizer.getGraphic().getMarks()) {
      Shape markShape = mark.toShape();
      float size = symbolizer.getGraphic().getSize();
      AffineTransform at = AffineTransform.getTranslateInstance(this.margin
          + currentColumn * (columnsWidth + this.margin) + columnsWidth / 2,
          this.margin + currentRow * (rowHeight + this.margin) + rowHeight / 2);
      at.rotate(symbolizer.getGraphic().getRotation());
      at.scale(3 * size, 3 * size);
      markShape = at.createTransformedShape(markShape);
      g2.setColor((mark.getFill() == null) ? Color.gray : mark.getFill()
          .getColor());
      g2.fill(markShape);
      g2.setStroke(mark.getStroke().toAwtStroke());
      g2.setColor((mark.getStroke() == null) ? Color.black : mark.getStroke()
          .getColor());
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);
      g2.draw(markShape);
    }
    for (ExternalGraphic theGraphic : symbolizer.getGraphic()
        .getExternalGraphics()) {
      Image onlineImage = theGraphic.getOnlineResource();
      g2.drawImage(onlineImage, this.margin + currentColumn
          * (columnsWidth + this.margin) + columnsWidth / 2
          - onlineImage.getWidth(null) / 2, this.margin + currentRow
          * (rowHeight + this.margin) + rowHeight / 2
          - onlineImage.getHeight(null) / 2, null);
    }
  }

  private void paintPolygon(PolygonSymbolizer symbolizer, Graphics2D g2,
      int currentColumn, int currentRow, int columnsWidth, int rowHeight) {
    g2.setColor(symbolizer.getFill().getColor());
    g2.fillRect(currentColumn * (columnsWidth + this.margin) + this.margin,
        currentRow * (rowHeight + this.margin) + this.margin, columnsWidth,
        rowHeight);
    g2.setColor(symbolizer.getStroke().getColor());
    g2.setStroke(symbolizer.getStroke().toAwtStroke());
    g2.drawRect(currentColumn * (columnsWidth + this.margin) + this.margin,
        currentRow * (rowHeight + this.margin) + this.margin, columnsWidth,
        rowHeight);
  }

  private void paintRaster(RasterSymbolizer symbolizer, Graphics2D g2,
      int currentColumn, int currentRow, int columnsWidth, int rowHeight) {
    int x = currentColumn * (columnsWidth + this.margin) + this.margin;
    int y = currentRow * (rowHeight + this.margin) + this.margin;
    int width = columnsWidth;
    int height = rowHeight;
    BufferedImage image = this.layer.getImage(symbolizer);
    g2.drawImage(image, x, y, width, height, null);
  }
}
