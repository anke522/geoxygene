/*
 * This file is part of the GeOxygene project source files.
 * GeOxygene aims at providing an open framework which implements OGC/ISO
 * specifications for the development and deployment of geographic (GIS)
 * applications. It is a open source contribution of the COGIT laboratory at
 * the Institut Géographique National (the French National Mapping Agency).
 * See: http://oxygene-project.sourceforge.net
 * Copyright (C) 2005 Institut Géographique National
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or any later
 * version.
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details. You should have received a copy of the GNU Lesser General
 * Public License along with this library (see file LICENSE if present); if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package fr.ign.cogit.geoxygene.datatools.ojb;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.metadata.ClassDescriptor;
import org.apache.ojb.broker.metadata.DescriptorRepository;
import org.apache.ojb.broker.metadata.FieldDescriptor;
import org.apache.ojb.odmg.HasBroker;
import org.apache.ojb.odmg.OJB;
import org.odmg.ClassNotPersistenceCapableException;
import org.odmg.DList;
import org.odmg.Database;
import org.odmg.Implementation;
import org.odmg.OQLQuery;
import org.odmg.Transaction;

import fr.ign.cogit.geoxygene.datatools.Metadata;
import fr.ign.cogit.geoxygene.feature.DataSet;
import fr.ign.cogit.geoxygene.feature.FT_Feature;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.schema.schemaConceptuelISOJeu.FeatureType;

/**
 * Implementation d'une Geodatabase utilisant OJB comme mappeur.
 * On utilise la partie ODMG de OJB.
 * Ne pas utiliser directement :
 * classe a specialiser en fonction du SGBD geographique utilise.
 * Attention pour les entiers :
 * pour Oracle, caster en BigDecimal,
 * pour Postgis caster en Long ...
 *
 * @author Thierry Badard & Arnaud Braun
 * @version 1.2
 * 22/07/2008 : modification des fonctions loadAllFeatures pour Récupèrer le
 * SRID dans les métadonnées (Julien Perret)
 */
public class GeodatabaseOjb {

    /**
     * The logger.
     */
    private static final Logger LOGGER
    = Logger.getLogger(GeodatabaseOjb.class.getName());

    /////////////////////////////////////////////////////////////////
    ///// attributs /////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
    /**
     * connection JDBC.
     */
    protected Connection _conn;
    /**
     * implementation ODMG.
     */
    protected Implementation _odmg;
    /**
     * interaction avec une base ODMG.
     */
    protected Database _db;
    /**
     * represente une transaction.
     */
    protected Transaction _tx;
    /**
     * liste des metadonnnees pour les classes persistantes.
     */
    protected List<Metadata> _metadataList;

    /////////////////////////////////////////////////////////////////
    ///// constructeur //////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
    /**
     * Constructeur.
     * @param jcdAlias : l'alias de connection dans repository_database.xml
     */
    GeodatabaseOjb(String jcdAlias) {
        initODMG(jcdAlias);
        initConnection();
        initMetadata();
    }

    /**
     * Constructeur avec la connection par defaut dans repository_database.xml.
     */
    protected GeodatabaseOjb () {
        this (null);
    }

    /////////////////////////////////////////////////////////////////
    /// initialisation des attributs ////////////////////////////////
    /////////////////////////////////////////////////////////////////
    /**
     * Initialise la base ODMG et une transaction.
     */
    protected void initODMG (String jcdAlias) {
        try {
            this._odmg = OJB.getInstance();
            this._db = this._odmg.newDatabase();
            if (jcdAlias != null) {
                this._db.open(jcdAlias, Database.OPEN_READ_WRITE);
            } else {
                this._db.open(null, Database.OPEN_READ_WRITE);
            }
            this._tx = this._odmg.newTransaction();
        } catch ( Exception except ) {
            LOGGER.fatal("Problem reading mapping files"); //$NON-NLS-1$
            LOGGER.fatal("Program will stop"); //$NON-NLS-1$
            LOGGER.fatal(""); //$NON-NLS-1$
            LOGGER.fatal(except.getMessage());
            except.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Initialise la connection JDBC.
     */
    protected void initConnection() {
        try {
            this._tx.begin();
            PersistenceBroker broker = ((HasBroker) this._tx).getBroker();
            this._conn = broker.serviceConnectionManager().getConnection();
            this._tx.commit();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Renseigne l'attribut _metadataList.
     */
    protected void initMetadata()  {
        try {
            this._tx.begin();
            PersistenceBroker broker = ((HasBroker) this._tx).getBroker();
            DescriptorRepository desc = broker.getDescriptorRepository();
            Iterator<?> enDesc = desc.getDescriptorTable().values().iterator();
            this._metadataList = new ArrayList<Metadata>();
            while (enDesc.hasNext()) {
                ClassDescriptor cd = (ClassDescriptor) enDesc.next();
                String className = (cd.getClassNameOfObject());
                if (!(className.equals(
                "org.apache.ojb.broker.util" + //$NON-NLS-1$
                ".sequence.HighLowSequence") //$NON-NLS-1$
                || className.equals("org.apache.ojb.odmg" + //$NON-NLS-1$
                ".collections.DListImpl_2") //$NON-NLS-1$
                || className.equals("org.apache.ojb.odmg" + //$NON-NLS-1$
                ".collections.DListEntry_2") //$NON-NLS-1$
                || className.equals("org.apache.ojb.odmg" + //$NON-NLS-1$
                ".collections.DListImpl") //$NON-NLS-1$
                || className.equals("org.apache.ojb.odmg" + //$NON-NLS-1$
                ".collections.DListEntry"))) { //$NON-NLS-1$
                    Metadata metadataElt = new Metadata();
                    metadataElt.setClassName(className);
                    metadataElt.setTableName(cd.getFullTableName());
                    FieldDescriptor[] fdPK = cd.getPkFields();
                    if (fdPK.length == 0) {
                        LOGGER.warn(
                                "WARNING: " + //$NON-NLS-1$
                                "No identified found for: " //$NON-NLS-1$
                                + cd.getClassNameOfObject());
                        continue;
                    }
                    if (fdPK.length > 1) {
                        if (cd.getClassNameOfObject().compareToIgnoreCase(
                        "org.apache.ojb.broker.util.sequence" + //$NON-NLS-1$
                        ".HighLowSequence") //$NON-NLS-1$
                        != 0) {
                            LOGGER.warn("WARNING - " + //$NON-NLS-1$
                            		"composed primary key: " //$NON-NLS-1$
                                    + cd.getClassNameOfObject());
                        }
                        continue;
                    }
                    metadataElt.setIdColumnName(fdPK[0].getColumnName());
                    metadataElt.setIdFieldName(fdPK[0].getAttributeName());
                    this._metadataList.add(metadataElt);
                }
            }
            this._tx.commit();
        } catch (Exception e) {
            LOGGER.fatal("Problem reading mapping files"); //$NON-NLS-1$
            LOGGER.fatal("Program will stop"); //$NON-NLS-1$
            LOGGER.fatal(""); //$NON-NLS-1$
            LOGGER.fatal(e.getMessage());
            e.printStackTrace( );
            System.exit(0);
        }
    }

    /////////////////////////////////////////////////////////////////
    ///// gestion des transactions //////////////////////////////////
    /////////////////////////////////////////////////////////////////
    /**
     * Ouvre une transaction.
     */
    public void begin() {
        try {
            this._tx.begin();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Commit la transaction sans la fermer.
     */
    public void checkpoint() {
        try {
            this._tx.checkpoint();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Commite et ferme la transaction.
     */
    public void commit() {
        try {
            this._tx.commit();
        } catch (Exception e) {
            System.out.println(e.getCause());
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Annule et ferme la transaction.
     */
    public void abort() {
        try {
            this._tx.abort();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Renvoie true si la transaction est active.
     */
    public boolean isOpen() {
        return this._tx.isOpen();
    }

    /**
     * Ferme la connection (libere les ressources).
     */
    public void close() {
        try {
            this._db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Vide le cache de la transaction.
     * A appeler a l'interieur d'une transaction ouverte.
     */
    public void clearCache() {
        PersistenceBroker broker = ((HasBroker) this._tx).getBroker();
        broker.clearCache();
    }

    /////////////////////////////////////////////////////////////////
    ///// gestion de la persistance /////////////////////////////////
    /////////////////////////////////////////////////////////////////
    /**
     * Rend persistant un objet.
     * A appeler a l'interieur d'une transaction ouverte.
     */
    public void makePersistent(Object obj) {
        try {
            this._db.makePersistent(obj);
        } catch (ClassNotPersistenceCapableException e) {
            LOGGER.error("Couldn't make object persistent."); //$NON-NLS-1$
            LOGGER.error("Please check file 'repository.xml'"); //$NON-NLS-1$
            e.printStackTrace();
            System.exit(0);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Detruit un objet persistant.
     * A appeler a l'interieur d'une transaction ouverte.
     */
    public void deletePersistent(Object obj) {
        try {
            this._db.deletePersistent(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /////////////////////////////////////////////////////////////////
    ///// chargement d'objets ///////////////////////////////////////
    /////////////////////////////////////////////////////////////////
    /**
     * Charge l'objet d'identifiant id.
     * Passer un Integer pour id, si l'identifiant est un int.
     * Renvoie null si l'objet d'identifiant id n'existe pas.
     * A appeler a l'interieur d'une transaction ouverte.
     */
    public <T> T load(Class<T> clazz, Object id) {
        try {
            OQLQuery query = this._odmg.newOQLQuery();
            query.create("select x from " + clazz.getName() //$NON-NLS-1$
                    + " where id = $0"); //$NON-NLS-1$
            query.bind(id);
            DList result = (DList) query.execute();
            if (result.size() > 0) { return clazz.cast(result.get(0)); }
            LOGGER.warn("Object not found - id = " + id); //$NON-NLS-1$
            return null;
        } catch (Exception ee) {
            ee.printStackTrace();
            return null;
        }
    }

    /**
     * Charge tous les objets persistants de la classe theClass et les met dans
     * une liste.
     * A appeler a l'interieur d'une transaction ouverte.
     * TODO Si ce sont des FT_Features, il faut Récupèrere les srids
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> loadAll(Class<T> theClass) {
        try {
            OQLQuery query = this._odmg.newOQLQuery();
            query.create("select x from " + theClass.getName()); //$NON-NLS-1$
            DList result = (DList) query.execute();
            return result;
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.warn(
                        "Class not found in repository: " //$NON-NLS-1$
                        + theClass.getName());
                LOGGER.debug(e.getMessage());
            }
            //e.printStackTrace();
            return null;
        }
    }
    /**
     * Charge tous les objets persistants de la classe theClass dans la classe
     * featureListClass.
     * A appeler a l'interieur d'une transaction ouverte.
     * TODO Si ce sont des FT_Features, il faut Récupèrere les srids
     */
    public <T> T loadAll(Class<?> featureClass, Class<T> featureListClass) {
        T result = null;
        try {
            result = featureListClass.newInstance();
        } catch (Exception e)  {
            e.printStackTrace();
            return null;
        }
        try {
            OQLQuery query = this._odmg.newOQLQuery();
            query.create("select x from " //$NON-NLS-1$
                    + featureClass.getName());
            DList list = (DList) query.execute();
            Iterator<?> iter = list.iterator();
            while (iter.hasNext()) {
                Object feature = iter.next();
                result.getClass().getMethod("add", //$NON-NLS-1$
                        new Class[]{ featureClass }).invoke(result,
                                new Object[] { feature });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Charge tous les FT_Feature de la classe theClass dans la classe
     * FT_FeatureCollection.
     * A appeler a l'interieur d'une transaction ouverte.
     * La classe theClass doit etre une sous-classe de FT_Feature, sinon
     * renvoie une liste vide.
     */
    @SuppressWarnings("unchecked")
    public <T extends FT_Feature> FT_FeatureCollection <T> loadAllFeatures(
            Class<?> featureClass) {
        FT_FeatureCollection<T> result = new FT_FeatureCollection<T>();
        if ((FT_Feature.class).isAssignableFrom(featureClass)) {
            try {
                OQLQuery query = this._odmg.newOQLQuery();
                query.create("select x from " //$NON-NLS-1$
                        + featureClass.getName());
                DList list = (DList) query.execute();
                Iterator<T> iter = list.iterator();
                // on Récupère le srid attribué à cette classe dans les
                // métadonnées
                Metadata metadata = this.getMetadata(featureClass);
                int srid = -1;
                if (metadata != null && metadata.getSRID() != 0) {
                    srid = metadata.getSRID();
                } else {
                    // si cette classe ne contient pas de métadonnées ou si
                    // c'est une classe mère de la classe stockée dans le SGBD
                    // on Récupère le premier élément (s'il existe) et ses
                    // métadonnées.
                    if (iter.hasNext()) {
                        T feature = iter.next();
                        metadata = this.getMetadata(feature.getClass());
                        if (metadata != null) {
                            srid=metadata.getSRID();
                        }
                        if (feature.getGeom() != null) {
                            feature.getGeom().setCRS(srid);
                        }
                        result.add(feature);
                    }
                }
                while (iter.hasNext()) {
                    T feature = iter.next();
                    if (feature.getGeom() != null) {
                        feature.getGeom().setCRS(srid);
                    }
                    result.add(feature);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.warn(
                "The given class is not a subclass of " + //$NON-NLS-1$
                "FT_Feature"); //$NON-NLS-1$
            }
        }
        return result;
    }

    /**
     * Charge tous les FT_Feature de la classe theClass dans la classe
     * featureListClass.
     * A appeler a l'interieur d'une transaction ouverte.
     * La classe theClass doit etre une sous-classe de FT_Feature, sinon
     * renvoie une liste vide.
     * La classe featureListClass doit etre un sous classe de
     * FT_FeatureCollection.
     */
    public <T> T loadAllFeatures(Class<?> featureClass,
            Class<T> featureListClass) {
        T result = null;
        try {
            result = featureListClass.newInstance();
        } catch (Exception e)  {
            e.printStackTrace();
            return null;
        }
        if ((FT_Feature.class).isAssignableFrom(featureClass)) {
            try {
                OQLQuery query = this._odmg.newOQLQuery();
                query.create("select x from " //$NON-NLS-1$
                        + featureClass.getName());
                DList list = (DList) query.execute();
                Iterator<?> iter = list.iterator();
                // on Récupère le srid attribué à cette classe dans les
                // métadonnées
                Metadata metadata = this.getMetadata(featureClass);
                int srid = -1;
                if (metadata != null && metadata.getSRID() != 0) {
                    srid = metadata.getSRID();
                } else {
                    // si cette classe ne contient pas de métadonnées ou si
                    // c'est une classe mère de la classe stockée dans le SGBD
                    // on Récupère le premier élément (s'il existe) et ses
                    // métadonnées.
                    if (iter.hasNext()) {
                        FT_Feature feature = (FT_Feature) iter.next();
                        metadata = this.getMetadata(feature.getClass());
                        if (metadata != null) {
                            srid = metadata.getSRID();
                        }
                        if (feature.getGeom() != null) {
                            feature.getGeom().setCRS(srid);
                        }
                        result.getClass().getMethod("add", //$NON-NLS-1$
                                new Class[]{ FT_Feature.class }).invoke(result,
                                        new Object[] { feature });
                    }
                }
                while (iter.hasNext()) {
                    FT_Feature feature = (FT_Feature) iter.next();
                    if (feature.getGeom() != null) {
                        feature.getGeom().setCRS(srid);
                    }
                    result.getClass().getMethod("add", //$NON-NLS-1$
                            new Class[]{ FT_Feature.class }).invoke(result,
                                    new Object[] { feature });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.warn(
                "The given class is not a subclass of " + //$NON-NLS-1$
                "FT_Feature"); //$NON-NLS-1$
            }
        }
        return result;
    }

    /**
     * Charge tous les FT_Feature de la classe featureClass avec une certaine
     * valeur pour un paramètre dans la classe featureListClass.
     * A appeler a l'interieur d'une transaction ouverte.
     * @param featureClass doit etre une sous-classe de FT_Feature, sinon
     * renvoie une liste vide.
     * @param featureListClass doit etre un sous classe de FT_FeatureCollection
     * @param param nom du paramètre
     * @param value valeur du paramètre
     * @return tous les FT_Feature de la classe featureClass avec une certaine
     * valeur pour un paramètre dans la classe featureListClass.
     */
    public <T> T loadAllFeatures(Class<?> featureClass,
            Class<T> featureListClass, String param, String value) {
        T result = null;
        try {
            result = featureListClass.newInstance();
        } catch (Exception e)  {
            e.printStackTrace();
            return null;
        }
        if ((FT_Feature.class).isAssignableFrom(featureClass)) {
            try {
                OQLQuery query = this._odmg.newOQLQuery();
                query.create("select x from " //$NON-NLS-1$
                        + featureClass.getName()
                        + " where " + param //$NON-NLS-1$
                        + " = " + value); //$NON-NLS-1$
                DList list = (DList) query.execute();
                Iterator<?> iter = list.iterator();
                // on Récupère le srid attribué à cette classe dans les
                // métadonnées
                Metadata metadata = this.getMetadata(featureClass);
                int srid = -1;
                if (metadata != null && metadata.getSRID() != 0) {
                    srid = metadata.getSRID();
                } else {
                    // si cette classe ne contient pas de métadonnées ou si
                    // c'est une classe mère de la classe stockée dans le SGBD
                    // on Récupère le premier élément (s'il existe) et ses
                    // métadonnées.
                    if (iter.hasNext()) {
                        FT_Feature feature = (FT_Feature) iter.next();
                        metadata = this.getMetadata(feature.getClass());
                        if (metadata != null) {
                            srid = metadata.getSRID();
                        }
                        if (feature.getGeom() != null) {
                            feature.getGeom().setCRS(srid);
                        }
                        result.getClass().getMethod("add", //$NON-NLS-1$
                                new Class[]{ FT_Feature.class }).invoke(
                                        result, new Object[] { feature });
                    }
                }
                while (iter.hasNext()) {
                    FT_Feature feature = (FT_Feature) iter.next();
                    if (feature.getGeom() != null) {
                        feature.getGeom().setCRS(srid);
                    }
                    result.getClass().getMethod("add", //$NON-NLS-1$
                            new Class[]{ FT_Feature.class }).invoke(result,
                                    new Object[] { feature });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.warn(
                "The given class is not a subclass of " + //$NON-NLS-1$
                "FT_Feature"); //$NON-NLS-1$
            }
        }
        return result;
    }

    /////////////////////////////////////////////////////////////////
    ///// OQL ///////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
    /**
     * Execute la requete OQL query, la lie avec le parametre param,
     * et met le resultat dans une liste.
     * A appeler a l'interieur d'une transaction ouverte.
     * On peut passer null pour param, si on ne souhaite lier la requete a
     * aucune variable.
     */
    public  List<?> loadOQL(String query, Object param) {
        OQLQuery oqlQuery = this._odmg.newOQLQuery();
        try {
            oqlQuery.create(query);
            oqlQuery.bind(param);
            DList result = (DList) oqlQuery.execute();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Cree une requete OQL.
     */
    public OQLQuery newOQLQuery() {
        return this._odmg.newOQLQuery() ;
    }

    /////////////////////////////////////////////////////////////////
    ///// Metadonnees sur le mapping ////////////////////////////////
    /////////////////////////////////////////////////////////////////
    /**
     * Renvoie le tableau des metadonnees.
     */
    public List<Metadata> getMetadata() {
        return this._metadataList;
    }

    /**
     * Renvoie les metadonnees de la classe theClass.
     * theClass doit etre une classe definie dans le mapping.
     */
    public Metadata getMetadata(Class<?> theClass) {
        for (int i = 0; i < this._metadataList.size(); i++) {
            if (theClass.getName().compareTo((this._metadataList.get(i))
                    .getClassName()) == 0) {
                return this._metadataList.get(i);
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.warn(
                    "The given class can't be found in " + //$NON-NLS-1$
                    "repository: " //$NON-NLS-1$
                    + theClass.getName());
        }
        return null;
    }

    /**
     * Renvoie les metadonnees de la classe mappee avec la table theTable.
     * theTable doit etre une table definie dans le mapping.
     * Si theTable est mappee avec plusieurs classes, en renvoie une.
     */
    public Metadata getMetadata(String theTable) {
        for (int i = 0; i < this._metadataList.size(); i++) {
            if ((this._metadataList.get(i)).getTableName() != null) {
                if (theTable.compareToIgnoreCase((this._metadataList.get(i))
                        .getTableName()) == 0) {
                    return this._metadataList.get(i);
                }
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.warn("The given table can't be found in " + //$NON-NLS-1$
            		"repository: " //$NON-NLS-1$
                    + theTable);
        }
        return null;
    }

    /////////////////////////////////////////////////////////////////
    ///// SQL ///////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
    /**
     * Renvoie la connection JDBC sous-jacente.
     */
    public Connection getConnection() {
        return this._conn;
    }

    /**
     * Execute une commande SQL.
     * Cette commande ne doit pas renvoyer de resultat :
     * INSERT, UPDATE, DELETE, mais pas SELECT.
     */
    public void exeSQL(String query) {
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("SQL query: "+query); //$NON-NLS-1$
            }
            Connection conn = getConnection();
            Statement stm = conn.createStatement();
            stm.executeUpdate(query);
            stm.close();
            conn.commit();
        } catch (Exception e) {
            LOGGER.error("Error while executing SQL query: " //$NON-NLS-1$
                    + query);
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * Execute les commandes SQL contenues dans un fichier.
     * @param fileName fichier contenant des commandes SQL
     */
    public void exeSQLFile(String fileName) {
        try {
            BufferedReader reader
            = new BufferedReader(new FileReader(fileName));
            String query = ""; //$NON-NLS-1$
            String line = reader.readLine();
            while (line != null) {
                query += line;
                if (line.trim().endsWith(";")) { //$NON-NLS-1$
                    exeSQL(query);
                    query = ""; //$NON-NLS-1$
                }
                line = reader.readLine();
            }
            if (!query.isEmpty()) { exeSQL(query); }
        } catch (FileNotFoundException e) {
            LOGGER.error("File " + fileName //$NON-NLS-1$
                    + " does not exist"); //$NON-NLS-1$
        } catch (IOException e) {
            LOGGER.error(
                    "Error while executing queries from " //$NON-NLS-1$
                    + fileName);
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * Execute une requete et met les resultats dans une liste de tableau
     * d'objets.
     * Les tableaux ont la taille du nombre d'objets demandes dans le SELECT.
     * Exemple d'utilisation du resultat :
     * <tt>
     * List edges =
     * db.exeSQLQuery("SELECT edgeID FROM tableName WHERE ...").
     * </tt>
     * Pour recuperer le premier resultat :
     * <tt> edgeId =
     * ( (BigDecimal) ((Object[]) (edges.get(0)) )[0] ).intValue();
     * </tt>
     */
    public List<?> exeSQLQuery(String query) {
        List<Object[]> result = new ArrayList<Object[]>();
        try {
            Connection conn = getConnection();
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(query);
            ResultSetMetaData rsmd = rs.getMetaData();
            int nbCol = rsmd.getColumnCount();
            while (rs.next()) {
                Object[] array = new Object[nbCol];
                for (int i = 1; i <= nbCol; i++) {
                    array[i-1] = rs.getObject(i);
                }
                result.add(array);
            }
            stm.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean tableExists(String tableName) {
        try {
            return getConnection().getMetaData().getTables(null, null,
                    tableName, null).next();
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /////////////////////////////////////////////////////////////////
    // getters ODMG /////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
    public Implementation getODMGImplementation() {
        return this._odmg;
    }

    public Database getODMGDatabase() {
        return this._db;
    }

    public Transaction getODMGTransaction() {
        return this._tx;
    }
    // //////////////////////////////////////////////////////////////
    /*
     * @author Balley
     *
     * méthodes de chargement de données (loadAll, loadAllFeatures) qui prennent
     * en entrée des élémentsde schéma conceptuel
     *
     */
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * chargement de tous les features correspondant à une même classe de
     * schéma conceptuel (il n'est pas nécessaire de connaêtre la classe Java
     * d'implémentation). Si on est dans le contexte d'un DataSet, les
     * Features sont en même temps affectés à une Population propre à leur
     * classe de schéma conceptuel.
     *
     */
    public <T extends FT_Feature> FT_FeatureCollection<T> loadAllFeatures(
            FeatureType featureType) {
        FT_FeatureCollection<T>  coll;
        if (featureType.getNomClasse() != null) {
            coll = loadAllFeaturesCasSimple(featureType);
        } else {
            coll = loadAllFeaturesCasComplexe(featureType);
        }
        return coll;
    }

    /**
     * Chargement dans le cas simple où un featureType correspond strictement à
     * une population (pas de règle d'extraction spécifiée).
     *
     * @param featureType feature type
     * @return FT_FeatureCollection collection
     * TODO Si ce sont des FT_Features, il faut Récupèrere les srids
     */
    @SuppressWarnings("unchecked")
    public <T extends FT_Feature> FT_FeatureCollection<T>
    loadAllFeaturesCasSimple(FeatureType featureType) {
        String nomClasseChargee = featureType.getNomClasse();
        Class<T> theClass = null;
        try {
            theClass = (Class<T>) Class.forName(nomClasseChargee);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        FT_FeatureCollection<T> coll = loadAllFeatures(theClass);
        /*
         * si je suis dans le contexte d'un MdDataSet je remplis sa population
         * correspondante
         */
        if (featureType.getSchema() != null) {
            if (featureType.getSchema().getDataset() != null) {
                Population<FT_Feature> pop
                = (Population<FT_Feature>)featureType.getSchema()
                .getDataset().getPopulation(featureType.getTypeName());
                if (pop == null) {
                    featureType.getSchema().getDataset().initPopulations();
                    pop = (Population<FT_Feature>)featureType.getSchema()
                    .getDataset().getPopulation(featureType.getTypeName());
                }
                pop.addUniqueCollection(coll);
            }
            /*
             * si je ne suis pas dans le contexte d'un schéma conceptuel (donc
             * encore moins d'un mdDataset), je ne remplis pas de population
             * mais j'associe quand même son featureType à chaque feature
             */
            else {
                for (int i = 0; i < coll.size(); i++) {
                    (coll.get(i)).setFeatureType(featureType);
                }
            }
        }
        /*
         * si je ne suis pas dans le contexte d'un MdDataSet, je ne remplis pas
         * de population mais j'associe quand même son featureType à chaque
         * feature
         */
        else {
            for (int i = 0; i < coll.size(); i++) {
                (coll.get(i)).setFeatureType(featureType);
            }
        }
        return coll;
    }

    /**
     * chargement du feature d'identifiant id correspondant à la classe de
     * schéma conceptuel featureType (il n'est pas nécessaire de connaêtre la
     * classe Java d'implémentation)
     * TODO Si ce sont des FT_Features, il faut Récupèrere les srids
     */
    public FT_Feature load(FeatureType featureType, Object id) {
        String nomClasseChargee = featureType.getNomClasse();
        Class<?> theClass = null;
        try {
            theClass = Class.forName(nomClasseChargee);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return (FT_Feature) load(theClass, id);

    }

    /**
     * chargement dans le cas où un featureType ne corespond pas seulement à
     * une population (une RegleExtraction +/- complexe a été spécifiée).
     * @param <T>
     * @param featureType
     * @return all features of the given featuretype
     */
    public <T extends FT_Feature> FT_FeatureCollection<T>
    loadAllFeaturesCasComplexe(FeatureType featureType) {
        FT_FeatureCollection<T> coll = null;
        return coll;
    }

    /**
     * @param <T>
     * @param featureType
     * @return
     * TODO Si ce sont des FT_Features, il faut Récupèrere les srids
     */
    @SuppressWarnings("unchecked")
    public  <T> List<T> loadAll(FeatureType featureType) {
        String nomClasseChargee = featureType.getNomClasse();
        Class<T> theClass = null;
        try {
            theClass = (Class<T>) Class.forName(nomClasseChargee);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        List<T> coll = DataSet.db.loadAll(theClass);
        return coll;
    }

    public void setMapping(Class<?> classe, String tableName) {

    }
}
