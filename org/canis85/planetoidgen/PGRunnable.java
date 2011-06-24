package org.canis85.planetoidgen;

/**
 *
 * @author canis85
 */
public class PGRunnable implements Runnable {

   public void run() {
      PlanetoidGen.planetoids.setTime(13801L);
   }
}
