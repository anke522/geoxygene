/*******************************************************************************
 * This file is part of the GeOxygene project source files.
 * 
 * GeOxygene aims at providing an open framework which implements OGC/ISO
 * specifications for the development and deployment of geographic (GIS)
 * applications. It is a open source contribution of the COGIT laboratory at the
 * Institut Géographique National (the French National Mapping Agency).
 * 
 * See: http://oxygene-project.sourceforge.net
 * 
 * Copyright (C) 2005 Institut Géographique National
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library (see file LICENSE if present); if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 *******************************************************************************/

package fr.ign.cogit.geoxygene.appli.render.texture;

import fr.ign.cogit.geoxygene.appli.task.AbstractTask;
import fr.ign.cogit.geoxygene.util.gl.Texture;

/**
 * @author JeT Abstract task for async texture generation
 */
public abstract class AbstractTextureTask<TextureType extends Texture> extends
        AbstractTask implements TextureTask<TextureType> {
    private String id = null;
    private boolean needWriting = false;

    /**
     * constructor
     * 
     * @param name
     *            task name
     */
    public AbstractTextureTask(String name) {
        super(name);
    }

    /**
     * @return the id
     */
    @Override
    public final String getID() {
        return this.id;
    }

    /**
     * @param id
     *            the id to set
     */
    @Override
    public void setID(String id) {
        this.id = id;
    }

    /**
     * @return the needWriting
     */
    public boolean needWriting() {
        return this.needWriting;
    }

    /**
     * @param needWriting
     *            the needWriting to set
     */
    public void setNeedWriting(boolean needWriting) {
        this.needWriting = needWriting;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "AbstractTextureTask [id=" + this.id + "]";
    }

}
