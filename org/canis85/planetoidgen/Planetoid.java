package org.canis85.planetoidgen;

import java.io.Serializable;
import org.bukkit.Material;

/**
 * Holder class for an individual planetoid.
 *
 * @author Canis85
 */
public class Planetoid implements Serializable {

   public Material coreBlk;
   public Material shellBlk;
   public int shellThickness;
   public int radius;
   
   //Position, local to the chunk.
   public int xPos;
   public int yPos;
   public int zPos;

   public Planetoid() {}

   public Planetoid(Material coreID, Material shellID, int shellThick, int radius, int x, int y, int z) {
      this.coreBlk = coreID;
      this.shellBlk = shellID;
      this.shellThickness = shellThick;
      this.radius = radius;
      
      this.xPos = x;
      this.yPos = y;
      this.zPos = z;
   }

}
