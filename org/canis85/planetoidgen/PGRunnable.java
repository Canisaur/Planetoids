package org.canis85.planetoidgen;

/**
 * Keeps the time frozen at night
 *
 * @author Canis85
 */
public class PGRunnable implements Runnable {

   public void run() {
      PlanetoidGen.planetoids.setTime(13801L);
   }
}
