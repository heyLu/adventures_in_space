package game;

/**
*
* @author maweki
*/
public class Projectile extends GameObject {
	/**
	 * Ship that has fired the projectile
	 */
   private Ship owner;
   
   /**
    * Is the projectile owned by ship
    * @param ship Ship
    * @return
    */
   public boolean is_owned_by(Ship ship) {
       if (ship == null) {
           return false;
       }
       return (ship == this.owner);
   }
   
   /**
    * Returns the ship owner
    * @return
    */
   public Ship owner() {
	   return this.owner;
   }
   
   /**
    * Constructor
    * @param mass Projectile mass (kg)
    * @param owner Projectile owner
    */
   public Projectile(double mass, Ship owner) {
       super(mass,Math.sqrt(mass)/10);
       this.owner = owner;
   }
   
   /**
    * Returns the Json-String of the projectile
    */
   public String Json() {	
   		return "{\"emptymass\" : " + this.mass() + ", "+
   			"\"position\"  : " + GameWorldJsonFactory.PointToJson(this.position(), "x", "y") + ", " +
			"\"velocity\"  : " + GameWorldJsonFactory.PointToJson(this.velocity(), "dx", "dy") + ", " +
			"\"owner\"  : " + this.owner.id + ", " +
			"\"size\"  : " + this.size() + ", " +
   			"\"id\"        : " + id + "}";
   }
   
   

}