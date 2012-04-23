package game;

/**
*
* @author maweki
*/
public class Projectile extends GameObject {
   private Ship owner;
   
   public boolean is_owned_by(Ship ship) {
       if (ship == null) {
           return false;
       }
       return (ship == this.owner);
   }
   
   public Ship owner() {
	   return this.owner;
   }
   
   public Projectile(double mass, Ship owner) {
       super(mass,Math.sqrt(mass)/10);
       this.owner = owner;
   }
   
   public String Json() {	
   		return "{\"emptymass\" : " + this.mass() + ", "+
   			"\"position\"  : " + GameWorldJsonFactory.PointToJson(this.position(), "x", "y") + ", " +
			"\"velocity\"  : " + GameWorldJsonFactory.PointToJson(this.velocity(), "dx", "dy") + ", " +
			"\"owner\"  : " + this.owner.id + ", " +
			"\"size\"  : " + this.size() + ", " +
   			"\"id\"        : " + id + "}";
   }
   
   

}