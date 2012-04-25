package game;

import java.awt.geom.Point2D;

/**
 *
 * @author maweki
 */
public class GameObject {
    @Override
	public String toString() {
		return "GameObject [emptymass=" + emptymass + ", position=" + position
				+ ", velocity=" + velocity.distance(0.0, 0.0) + ", id=" + id + "]";
	}

    /**
     * Empty mass of the object (kg)
     */
	private double emptymass = 0.0;
	
	/**
	 * Position of the object (m)
	 */
    private Point2D.Double position;
    
    /**
     * Velocity of the object (m/s)
     */
    private Point2D.Double velocity;
    
    /**
     * Size (diameter) of the object (m)
     */
    private double size = 10.0;
    
    /**
     * id of the object
     */
    public int id = 0;
    
    /**
     * Constructor
     * @param mass empty mass (kg)
     * @param size Size (Diameter) (m)
     */
    public GameObject(double mass, double size) {
        this.add_empty_mass(mass);
        this.size = size;
        this.position = new Point2D.Double();
        this.velocity = new Point2D.Double();
    }
    
    /**
     * Returns mass of object
     * @return Mass (kg)
     */
    public double mass() {
        return this.emptymass;
    }
    
    /**
     * Adds empty mass to object
     * @param mass Mass to add (kg)
     * @return full object mass (kg)
     */
    public final double add_empty_mass(double mass) {
        this.emptymass = this.emptymass + mass;
        if (this.emptymass <= 0.0) {
            this.emptymass = 1.0;
        }
        return this.mass();
    }
    
    /**
     * Removes empty mass to object
     * @param mass Mass to remove (kg)
     * @return full object mass (kg)
     */
    public final double remove_empty_mass(double mass) {
        return this.add_empty_mass(-mass);
    }
    
    /**
     * Returns the size (Diameter) of the object (m)
     * @return
     */
    public double size() {
        return this.size;
    }
    
    /**
     * Changes the size (Diameter of the object)
     * @param amount Amount of diameter change (m)
     * @return The new diameter (m)
     */
    public double change_size(double amount) {
        this.size = this.size + amount;
        if (this.size < 0.0) {
            this.size = Math.abs(this.size);
        }
        return this.size();
    }
    
    /**
     * Position of the object (m)
     * @return
     */
    public Point2D.Double position(){
        return (Point2D.Double)this.position.clone();
    }
    
    /**
     * Sets position of the object
     * @param position Position (m)
     */
    public void set_position(Point2D.Double position){
        this.position = (Point2D.Double) position.clone();
    }
    
    /**
     * Changes the object position
     * @param change Position delta (m)
     * @return new current Position (m)
     */
    public Point2D.Double change_position(Point2D.Double change) {
        this.position.x = this.position.x + change.x;
        this.position.y = this.position.y + change.y;
        return this.position();
    }
   
    /**
     * Calculates distance between this and another game object (surfaces, not center points)
     * @param object The other object
     * @return Distance (m)
     */
    public double distance_to(GameObject object) {
        return Math.max(this.position.distance(object.position) - (this.size() + object.size()) / 2 ,0.0);
    }
    
    /**
     * Returns whether two objects are collided
     * @param object The other object
     * @return
     */
    public boolean is_impacted_by(GameObject object) {
        return (distance_to(object) == 0.0);
    }
    
    /**
     * Returns the object's velocity (m/s)
     * @return
     */
    public Point2D.Double velocity() {
        return (Point2D.Double) this.velocity.clone();
    }
    
    /**
     * Returns the object's undirected velocity (speed) (m/s) 
     * @return
     */
    public double speed() {
        return velocity.distance(0.0, 0.0);
    }
    
    /**
     * Calculated the impact energy between this and another object
     * @param object The other object
     * @return Kinetic Energy (J)
     */
    public double impact_energy_with(GameObject object) {
    	/* Velocity difference */
        Point2D.Double diff_vel = new Point2D.Double(
                this.velocity.x - object.velocity.x,
                this.velocity.y - object.velocity.y
                );
        
        /* Kinetic energy with velocity difference and object mass */
        return this.mass() * diff_vel.distanceSq(0.0, 0.0) / 2;
    }
    
    /**
     * Absolute kinetic energy (World as Frame of Reference)
     * @return Kinetic Energy (J)
     */
    public double kinetic_energy() {
        return this.velocity.distanceSq(0.0,0.0) * this.mass() / 2;
    }
    
    /**
     * Change objects velocity
     * @param amount Amount of velocity change (m/s)
     */
    public void change_velocity(Point2D.Double amount) {
        this.velocity.x = this.velocity.x + amount.x;
        this.velocity.y = this.velocity.y + amount.y;
    }
    
    /**
     * Move object over time
     * @param time Timeframe (s)
     * @return new position (m)
     */
    public Point2D.Double move(double time) {
        this.position.x = this.position.x + this.velocity.x * time;
        this.position.y = this.position.y + this.velocity.y * time;
        return this.position();
    }    
    
    /**
     * Accelerate object
     * @param direction Direction of acceleration
     * @param energy_per_second Energy per second (m/s)
     * @param duration Duration of acceleration (s)
     * @return Force that accelerates the object
     */
    public Energy accelerate_object(Point2D.Double direction, double energy_per_second, double duration) {
        duration = Math.min(duration, World.w.get_max_duration());
        Energy force = new Energy(this, direction, duration, energy_per_second);
        World.w.add_force(force);
        return force;
    }
    
    /**
     * Returns the Json-String of the object
     * @return
     */
    public String Json() {	
    	return "{\"emptymass\" : " + this.mass() + ", "+
    			"\"position\"  : " + GameWorldJsonFactory.PointToJson(this.position(), "x", "y") + ", " +
				"\"velocity\"  : " + GameWorldJsonFactory.PointToJson(this.velocity(), "dx", "dy") + ", " +
    			"\"id\"        : " + id + "}";
    }    
}