package fr.ign.cogit.geoxygene.appli.plugin.cartagen.dataset;

import java.io.IOException;

import org.apache.log4j.Logger;

import fr.ign.cogit.cartagen.core.genericschema.energy.IElectricityLine;
import fr.ign.cogit.cartagen.core.genericschema.hydro.IWaterArea;
import fr.ign.cogit.cartagen.core.genericschema.hydro.IWaterLine;
import fr.ign.cogit.cartagen.core.genericschema.land.ISimpleLandUseArea;
import fr.ign.cogit.cartagen.core.genericschema.partition.IMask;
import fr.ign.cogit.cartagen.core.genericschema.railway.IRailwayLine;
import fr.ign.cogit.cartagen.core.genericschema.relief.IContourLine;
import fr.ign.cogit.cartagen.core.genericschema.relief.IReliefElementLine;
import fr.ign.cogit.cartagen.core.genericschema.relief.ISpotHeight;
import fr.ign.cogit.cartagen.core.genericschema.road.IPathLine;
import fr.ign.cogit.cartagen.core.genericschema.road.IRoadLine;
import fr.ign.cogit.cartagen.core.genericschema.urban.IBuilding;
import fr.ign.cogit.cartagen.software.CartAGenDataSet;
import fr.ign.cogit.cartagen.software.dataset.CartAGenDoc;
import fr.ign.cogit.cartagen.software.dataset.ShapeFileDB;
import fr.ign.cogit.cartagen.software.dataset.ShapeFileLoader;
import fr.ign.cogit.cartagen.software.dataset.SourceDLM;
import fr.ign.cogit.cartagen.software.interfacecartagen.dataloading.ProgressFrame;
import fr.ign.cogit.cartagen.software.interfacecartagen.symbols.SymbolGroup;
import fr.ign.cogit.cartagen.software.interfacecartagen.symbols.SymbolList;
import fr.ign.cogit.cartagen.software.interfacecartagen.symbols.SymbolsUtil;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IPolygon;
import fr.ign.cogit.geoxygene.api.spatial.geomprim.IPoint;
import fr.ign.cogit.geoxygene.appli.plugin.cartagen.CartAGenPlugin;
import fr.ign.cogit.geoxygene.util.index.Tiling;

public class CartAGenLoader {

  private static Logger logger = Logger.getLogger(CartAGenLoader.class
      .getName());

  private CartAGenPlugin appli;
  private String dbName;

  public CartAGenLoader(CartAGenPlugin appli, String dbName) {
    super();
    this.appli = appli;
    this.dbName = dbName;
  }

  public void loadData(String absolutePath, SourceDLM bdsource, int scale,
      CartAGenDataSet dataset) {

    if (bdsource == SourceDLM.SPECIAL_CARTAGEN) {
      this.loadData_Special_Cartagen(absolutePath, bdsource, scale);

    } else if (bdsource == SourceDLM.BD_TOPO_V2) {
      this.loadData_BDTopoV2(absolutePath, scale, dataset);

    } else if (bdsource == SourceDLM.BD_CARTO) {
      this.loadData_BDCarto(absolutePath, scale, dataset);

    }

    if (logger.isInfoEnabled()) {
      logger.info("end data loading");
    }

  }

  public void loadData_Special_Cartagen(String absolutePath,
      SourceDLM sourceDlm, int scale) {

    ProgressFrame progressFrame = new ProgressFrame(
        "Data loading in progress...", true);
    progressFrame.setTextAndValue("Loading buildings", 0);
    progressFrame.setVisible(true);

    CartAGenDataSet dataSet = CartAGenDoc.getInstance().getCurrentDataset();

    try {
      SymbolGroup symbGroup = SymbolsUtil.getSymbolGroup(sourceDlm, scale);
      dataSet.setSymbols(SymbolList.getSymbolList(symbGroup));
      if (logger.isInfoEnabled()) {
        logger.info("buildings spatial index creation");
      }

      if (ShapeFileLoader.loadBuildingsFromSHP(absolutePath + "/batiment",
          dataSet)) {
        // create a new ShapeFileClass object in the CartAGen dataset
        ((ShapeFileDB) dataSet.getCartAGenDB()).addShapeFile(absolutePath
            + "/batiment", IBuilding.FEAT_TYPE_NAME, IPolygon.class);
      }
      dataSet.getBuildings().initSpatialIndex(Tiling.class, false);

      progressFrame.setTextAndValue("Loading road network", 30);
      if (ShapeFileLoader.loadRoadLinesShapeFile(absolutePath
          + "/troncon_route", sourceDlm, dataSet.getSymbols(), dataSet)) {
        // create a new ShapeFileClass object in the CartAGen dataset
        ((ShapeFileDB) dataSet.getCartAGenDB()).addShapeFile(absolutePath
            + "/troncon_route", IRoadLine.FEAT_TYPE_NAME, ILineString.class);
      }
      progressFrame.setTextAndValue("Loading hydrologic network", 60);
      if (ShapeFileLoader.loadWaterLinesFromSHP(absolutePath
          + "/troncon_cours_eau", dataSet.getSymbols(), dataSet)) {
        // create a new ShapeFileClass object in the CartAGen dataset
        ((ShapeFileDB) dataSet.getCartAGenDB()).addShapeFile(absolutePath
            + "/troncon_cours_eau", IWaterLine.FEAT_TYPE_NAME,
            ILineString.class);
      }
      if (ShapeFileLoader.loadWaterAreasFromSHP(absolutePath + "/surface_eau",
          dataSet.getSymbols(), dataSet)) {
        // create a new ShapeFileClass object in the CartAGen dataset
        ((ShapeFileDB) dataSet.getCartAGenDB()).addShapeFile(absolutePath
            + "/surface_eau", IWaterArea.FEAT_TYPE_NAME, IPolygon.class);
      }

      progressFrame.setTextAndValue("Loading railo roads and electric network",
          70);
      if (ShapeFileLoader.loadRailwayLineFromSHP(absolutePath
          + "/troncon_voie_ferree", dataSet.getSymbols(), null, dataSet)) {
        // create a new ShapeFileClass object in the CartAGen dataset
        ((ShapeFileDB) dataSet.getCartAGenDB()).addShapeFile(absolutePath
            + "/troncon_voie_ferree", IRailwayLine.FEAT_TYPE_NAME,
            ILineString.class);
      }
      if (ShapeFileLoader.loadElectricityLinesFromSHP(absolutePath
          + "/troncon_electrique", dataSet.getSymbols(), dataSet)) {
        // create a new ShapeFileClass object in the CartAGen dataset
        ((ShapeFileDB) dataSet.getCartAGenDB()).addShapeFile(absolutePath
            + "/troncon_electrique", IElectricityLine.FEAT_TYPE_NAME,
            ILineString.class);
      }
      if (ShapeFileLoader.loadContourLinesFromSHP(absolutePath + "/cn",
          dataSet.getSymbols(), dataSet)) {
        // create a new ShapeFileClass object in the CartAGen dataset
        ((ShapeFileDB) dataSet.getCartAGenDB()).addShapeFile(absolutePath
            + "/cn", IContourLine.FEAT_TYPE_NAME, ILineString.class);
      }

      progressFrame.setTextAndValue("Loading contour lines and DTM", 90);
      if (ShapeFileLoader.loadReliefElementLinesFromSHP(absolutePath
          + "/ligne_orographique", dataSet.getSymbols(), dataSet)) {
        // create a new ShapeFileClass object in the CartAGen dataset
        ((ShapeFileDB) dataSet.getCartAGenDB()).addShapeFile(absolutePath
            + "/ligne_orographique", IReliefElementLine.FEAT_TYPE_NAME,
            ILineString.class);
      }
      ShapeFileLoader.loadDEMPixelsFromSHP(absolutePath + "/mnt", dataSet);

      if (ShapeFileLoader.loadSpotHeightsFromSHP(absolutePath + "/point_cote",
          dataSet.getSymbols(), dataSet)) {
        // create a new ShapeFileClass object in the CartAGen dataset
        ((ShapeFileDB) dataSet.getCartAGenDB()).addShapeFile(absolutePath
            + "/point_cote", ISpotHeight.FEAT_TYPE_NAME, IPoint.class);
      }

      if (ShapeFileLoader.loadMaskFromSHP(absolutePath + "/masque", dataSet)) {
        // create a new ShapeFileClass object in the CartAGen dataset
        ((ShapeFileDB) dataSet.getCartAGenDB()).addShapeFile(absolutePath
            + "/masque", IMask.FEAT_TYPE_NAME, IPolygon.class);
      }

      if (logger.isInfoEnabled()) {
        logger.info("end data loading");
      }

    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      progressFrame.setVisible(false);

      progressFrame = null;
    }

  }

  public void loadData_BDCarto(String absolutePath, int scale,
      CartAGenDataSet dataSet) {

    ProgressFrame progressFrame = new ProgressFrame(
        "Data loading in progress...", true);
    progressFrame.setVisible(true);

    SymbolGroup symbGroup = SymbolsUtil.getSymbolGroup(SourceDLM.BD_CARTO,
        scale);
    dataSet.setSymbols(SymbolList.getSymbolList(symbGroup));
    progressFrame.setTextAndValue("Loading road network", 0);

    try {
      ShapeFileLoader.loadRoadLinesFromSHP(absolutePath + "/troncon_route",
          SourceDLM.BD_CARTO, dataSet.getSymbols(), dataSet);

    } catch (IOException e) {
      e.printStackTrace();
    }

    progressFrame.dispose();

  }

  public void loadData_BDTopoV2(String absolutePath, int scale,
      CartAGenDataSet dataSet) {

    ProgressFrame progressFrame = new ProgressFrame(
        "Data loading in progress...", true);
    progressFrame.setVisible(true);

    SymbolGroup symbGroup = SymbolsUtil.getSymbolGroup(SourceDLM.BD_TOPO_V2,
        scale);
    dataSet.setSymbols(SymbolList.getSymbolList(symbGroup));
    progressFrame.setTextAndValue("Loading road network", 0);
    ((ShapeFileDB) dataSet.getCartAGenDB()).setSystemPath(absolutePath);
    try {
      if (ShapeFileLoader.loadRoadLinesShapeFile(absolutePath
          + "/A_RESEAU_ROUTIER/ROUTE", SourceDLM.BD_TOPO_V2,
          dataSet.getSymbols(), dataSet)) {
        ((ShapeFileDB) dataSet.getCartAGenDB()).addShapeFile(absolutePath
            + "/A_RESEAU_ROUTIER/ROUTE", IRoadLine.FEAT_TYPE_NAME,
            ILineString.class);
      }

      if (ShapeFileLoader.loadPathsBDTopoV2_25FromSHP(absolutePath
          + "/A_RESEAU_ROUTIER/CHEMIN", SourceDLM.BD_TOPO_V2,
          dataSet.getSymbols(), dataSet)) {
        ((ShapeFileDB) dataSet.getCartAGenDB()).addShapeFile(absolutePath
            + "/A_RESEAU_ROUTIER/CHEMIN", IPathLine.FEAT_TYPE_NAME,
            ILineString.class);
      }

      progressFrame.setTextAndValue("Loading buildings", 20);

      if (ShapeFileLoader.loadBuildingsFromSHP(absolutePath
          + "/E_BATI/BATI_INDIFFERENCIE", dataSet)) {
        ((ShapeFileDB) dataSet.getCartAGenDB()).addShapeFile(absolutePath
            + "/E_BATI/BATI_INDIFFERENCIE", IBuilding.FEAT_TYPE_NAME,
            IPolygon.class);
      }

      if (ShapeFileLoader.loadBuildingsFromSHP(absolutePath
          + "/E_BATI/BATI_INDUSTRIEL", dataSet)) {
        ((ShapeFileDB) dataSet.getCartAGenDB()).addShapeFile(absolutePath
            + "/E_BATI/BATI_INDUSTRIEL", IBuilding.FEAT_TYPE_NAME,
            IPolygon.class);
      }

      if (ShapeFileLoader.loadBuildingsFromSHP(absolutePath
          + "/E_BATI/BATI_REMARQUABLE", dataSet)) {
        ((ShapeFileDB) dataSet.getCartAGenDB()).addShapeFile(absolutePath
            + "/E_BATI/BATI_REMARQUABLE", IBuilding.FEAT_TYPE_NAME,
            IPolygon.class);
      }

      ShapeFileLoader.loadBuildingsFromSHP(absolutePath
          + "/E_BATI/CONSTRUCTION_SURFACIQUE", dataSet);
      if (logger.isInfoEnabled()) {
        logger.info("buildings spatial index creation");
      }

      dataSet.getBuildings().initSpatialIndex(Tiling.class, false);

      progressFrame.setTextAndValue("Loading hydrographic network ", 60);

      if (ShapeFileLoader.loadWaterLinesFromSHP(absolutePath
          + "/D_HYDROGRAPHIE/TRONCON_COURS_EAU", dataSet.getSymbols(), dataSet)) {
        ((ShapeFileDB) dataSet.getCartAGenDB()).addShapeFile(absolutePath
            + "/D_HYDROGRAPHIE/TRONCON_COURS_EAU", IWaterLine.FEAT_TYPE_NAME,
            ILineString.class);
      }

      if (ShapeFileLoader.loadWaterAreasFromSHP(absolutePath
          + "/D_HYDROGRAPHIE/SURFACE_EAU", dataSet.getSymbols(), dataSet)) {
        ((ShapeFileDB) dataSet.getCartAGenDB()).addShapeFile(absolutePath
            + "/D_HYDROGRAPHIE/SURFACE_EAU", IWaterArea.FEAT_TYPE_NAME,
            IPolygon.class);
      }

      progressFrame.setTextAndValue("Loading rail roads and other networks ",
          70);

      if (ShapeFileLoader.loadRailwayLineFromSHP(absolutePath
          + "/B_VOIES_FERREES_ET_AUTRES/TRONCON_VOIE_FERREE",
          dataSet.getSymbols(), null, dataSet)) {
        ((ShapeFileDB) dataSet.getCartAGenDB()).addShapeFile(absolutePath
            + "/B_VOIES_FERREES_ET_AUTRES/TRONCON_VOIE_FERREE",
            IRailwayLine.FEAT_TYPE_NAME, ILineString.class);
      }

      if (ShapeFileLoader.loadElectricityLinesFromSHP(absolutePath
          + "/C_TRANSPORT_ENERGIE/LIGNE_ELECTRIQUE", dataSet.getSymbols(),
          dataSet)) {
        ((ShapeFileDB) dataSet.getCartAGenDB()).addShapeFile(absolutePath
            + "/C_TRANSPORT_ENERGIE/LIGNE_ELECTRIQUE",
            IElectricityLine.FEAT_TYPE_NAME, ILineString.class);
      }

      progressFrame.setTextAndValue("Loading landuse", 80);

      if (ShapeFileLoader.loadLandUseAreasFromSHP(absolutePath
          + "/F_VEGETATION/ZONE_VEGETATION", 1.0, 1, dataSet)) {
        ((ShapeFileDB) dataSet.getCartAGenDB()).addShapeFile(absolutePath
            + "/F_VEGETATION/ZONE_VEGETATION",
            ISimpleLandUseArea.FEAT_TYPE_NAME, IPolygon.class);
      }

      if (ShapeFileLoader.loadLandUseAreasFromSHP(absolutePath
          + "/I_ZONE_ACTIVITE/ZONE_ACTIVITE", 1.0, 2, dataSet)) {
        ((ShapeFileDB) dataSet.getCartAGenDB()).addShapeFile(absolutePath
            + "/I_ZONE_ACTIVITE/ZONE_ACTIVITE",
            ISimpleLandUseArea.FEAT_TYPE_NAME, IPolygon.class);
      }

      progressFrame.setTextAndValue("Loading Contour lines", 90);

      if (ShapeFileLoader.loadReliefElementLinesFromSHP(absolutePath
          + "/G_OROGRAPHIE/LIGNE_OROGRAPHIQUE", dataSet.getSymbols(), dataSet)) {
        ((ShapeFileDB) dataSet.getCartAGenDB()).addShapeFile(absolutePath
            + "/G_OROGRAPHIE/LIGNE_OROGRAPHIQUE",
            IReliefElementLine.FEAT_TYPE_NAME, ILineString.class);
      }

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      progressFrame.setVisible(false);

      progressFrame = null;
    }

  }

}
