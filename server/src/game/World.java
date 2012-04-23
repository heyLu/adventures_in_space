package game;

/**
*
* @author maweki
*/
public class World {
   public static GameWorld w;
   
   static public GameWorld reset() {
	   game.World.w = new GameWorld();
	   return game.World.w;
   }
}
