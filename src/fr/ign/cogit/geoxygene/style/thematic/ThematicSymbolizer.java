package fr.ign.cogit.geoxygene.style.thematic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.style.AbstractSymbolizer;

@XmlAccessorType(XmlAccessType.FIELD)
public class ThematicSymbolizer extends AbstractSymbolizer {
  @XmlElement(name = "DiagramSymbolizer")
  private List<DiagramSymbolizer> symbolizers = new ArrayList<DiagramSymbolizer>(
      0);

  public List<DiagramSymbolizer> getSymbolizers() {
    return this.symbolizers;
  }

  public void setSymbolizers(List<DiagramSymbolizer> symbolizers) {
    this.symbolizers = symbolizers;
  }

  @XmlTransient
  private Map<IFeature, IDirectPosition> points = new HashMap<IFeature, IDirectPosition>();
  public Map<IFeature, IDirectPosition> getPoints() {
    return points;
}

@XmlTransient
  private Map<IFeature, Double> radius = new HashMap<IFeature, Double>();

public Map<IFeature, Double> getRadius() {
    return radius;
}

//  @SuppressWarnings("unchecked")
//  @Override
//  public void paint(IFeature feature, Viewport viewport, Graphics2D graphics) {
//    if (feature.getGeom() == null || viewport == null) {
//      return;
//    }
//    for (DiagramSymbolizer s : this.getSymbolizers()) {
//      if (s.getDiagramType().equalsIgnoreCase("piechart")) { //$NON-NLS-1$
//        // double size = 1.0;
//        // for (DiagramSizeElement element : s.getDiagramSize()) {
//        // if (element instanceof DiagramRadius) {
//        // size = element.getValue();
//        // }
//        // }
//        IDirectPosition position = this.points.get(feature);
//        Double size = this.radius.get(feature);
//        if (position == null || size == null) {
//          TriangulationJTS t = new TriangulationJTS("TRIANGLE");
//          Chargeur.importAsNodes(feature, t);
//          try {
//            t.triangule("v");
//          } catch (Exception e1) {
//            e1.printStackTrace();
//          }
//          GM_MultiCurve<GM_OrientableCurve> contour = new GM_MultiCurve<GM_OrientableCurve>();
//          if (feature.getGeom() instanceof GM_Polygon) {
//            contour.add((GM_OrientableCurve) ((GM_Polygon) feature.getGeom())
//                .exteriorLineString());
//          } else {
//            for (GM_Polygon surface : (GM_MultiSurface<GM_Polygon>) feature
//                .getGeom()) {
//              contour.add((GM_OrientableCurve) surface.exteriorLineString());
//            }
//          }
//          for (Arc a : t.getPopArcs()) {
//            ((Population<DefaultFeature>) DataSet.getInstance().getPopulation(
//                "Triangulation")).add(new DefaultFeature(a.getGeometrie()));
//          }
//          double maxDistance = Double.MIN_VALUE;
//          Noeud maxNode = null;
//          for (Arc a : t.getPopVoronoiEdges().select(feature.getGeom())) {
//            if (!a.getGeometrie().intersectsStrictement(feature.getGeom())) {
//              ((Population<DefaultFeature>) DataSet.getInstance()
//                  .getPopulation("MedialAxis")).add(new DefaultFeature(a
//                  .getGeometrie()));
//            }
//          }
//          for (Noeud n : t.getPopVoronoiVertices().select(feature.getGeom())) {
//            double d = n.getGeometrie().distance(contour);
//            if (d > maxDistance) {
//              maxDistance = d;
//              maxNode = n;
//            }
//          }
//          size = maxDistance;
//          if (maxNode == null) {
//            AbstractSymbolizer.logger.info(feature.getGeom());
//            return;
//          }
//          position = maxNode.getGeometrie().getPosition();
//          this.points.put(feature, position);
//          this.radius.put(feature, maxDistance);
//        }
//        Point2D point = null;
//        try {
//          point = viewport.toViewPoint(position);
//        } catch (NoninvertibleTransformException e) {
//          e.printStackTrace();
//          return;
//        }
//        double scale = 1;
//        if (this.getUnitOfMeasure() != Symbolizer.PIXEL) {
//          try {
//            scale = viewport.getModelToViewTransform().getScaleX();
//          } catch (NoninvertibleTransformException e) {
//            e.printStackTrace();
//          }
//        }
//        size *= scale;
//        double startAngle = 0.0;
//        for (ThematicClass thematicClass : s.getThematicClass()) {
//          double value = ((Number) thematicClass.getClassValue().evaluate(
//              feature)).doubleValue();
//          // AbstractSymbolizer.logger.info(thematicClass.getClassLabel() + " "
//          // + value);
//          if (value == 0) {
//            continue;
//          }
//          double arcAngle = 3.6 * value;
//          // AbstractSymbolizer.logger.info("\t" + startAngle + " - " +
//          // arcAngle);
//          graphics.setColor(thematicClass.getFill().getColor());
//          graphics.fillArc((int) (point.getX() - size),
//              (int) (point.getY() - size), (int) (2 * size), (int) (2 * size),
//              (int) startAngle, (int) arcAngle);
//          graphics.setColor(Color.BLACK);
//          graphics.drawArc((int) (point.getX() - size),
//              (int) (point.getY() - size), (int) (2 * size), (int) (2 * size),
//              (int) startAngle, (int) arcAngle);
//          startAngle += arcAngle;
//        }
//      }
//    }
//  }
}
