/*******************************************************************************
 * This file is part of the GeOxygene project source files.
 * 
 * GeOxygene aims at providing an open framework which implements OGC/ISO specifications for
 * the development and deployment of geographic (GIS) applications. It is a open source
 * contribution of the COGIT laboratory at the Institut Géographique National (the French
 * National Mapping Agency).
 * 
 * See: http://oxygene-project.sourceforge.net
 * 
 * Copyright (C) 2005 Institut Géographique National
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with
 * this library (see file LICENSE if present); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *******************************************************************************/
/**
 * 
 */
package fr.ign.cogit.appli.geopensim.feature.micro;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import fr.ign.cogit.appli.geopensim.feature.meso.GroupeRoutesDansIlot;

/**
 * @author Julien Perret
 *
 */
@Entity
public abstract class TronconRoute extends Troncon implements Route {

	GroupeRoutesDansIlot groupeRoutes = null;

	boolean impasse = false;
	boolean correction = false;

	/**
	 * @return correction
	 */
	public boolean getCorrection() {return correction;}

	/**
	 * @param correction correction à Définir
	 */
	public void setCorrection(boolean correction) {this.correction = correction;}

	/**
	 * @return groupeRoutes
	 */
	@ManyToOne
	public GroupeRoutesDansIlot getGroupeRoutes() {return groupeRoutes;}

	/**
	 * @param groupeRoutes groupeRoutes à Définir
	 */
	public void setGroupeRoutes(GroupeRoutesDansIlot groupeRoutes) {this.groupeRoutes = groupeRoutes;}

	/**
	 * @return impasse
	 */
	public boolean getImpasse() {return impasse;}

	/**
	 * @param impasse impasse à Définir
	 */
	public void setImpasse(boolean impasse) {this.impasse = impasse;}

	double sinuosite = 0.0;
	
	/**
	 * Renvoie la valeur de l'attribut sinuosite.
	 * @return la valeur de l'attribut sinuosite
	 */
	public double getSinuosite() {return this.sinuosite;}

	/**
	 * Affecte la valeur de l'attribut sinuosite.
	 * @param sinuosite l'attribut sinuosite à affecter
	 */
	public void setSinuosite(double sinuosite) {this.sinuosite = sinuosite;}

	@Override
	public void qualifier() {super.qualifier();}
}
