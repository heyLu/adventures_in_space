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

	private double emptymass = 0.0;
    private Point2D.Double position;
    private Point2D.Double velocity;
    private double size = 10.0;
    public int id = 0;
    
    public GameObject(double mass, double size) {
        this.add_empty_mass(mass);
        this.size = size;
        this.position = new Point2D.Double();
        this.velocity = new Point2D.Double();
    }
    
    public double mass() {
        return this.emptymass;
    }
    
    public final double add_empty_mass(double mass) {
        this.emptymass = this.emptymass + mass;
        if (this.emptymass <= 0.0) {
            this.emptymass = 1.0;
        }
        return this.mass();
    }
    
    public final double remove_empty_mass(double mass) {
        return this.add_empty_mass(-mass);
    }
    
    public double size() {
        return this.size;
    }
    
    public double change_size(double amount) {
        this.size = this.size + amount;
        if (this.size < 0.0) {
            this.size = Math.abs(this.size);
        }
        return this.size();
    }
    
    public Point2D.Double position(){
        return (Point2D.Double)this.position.clone();
    }
    
    public void set_position(Point2D.Double position){
        this.position = (Point2D.Double) position.clone();
    }
    
    public Point2D.Double change_position(Point2D.Double change) {
        this.position.x = this.position.x + change.x;
        this.position.y = this.position.y + change.y;
        return this.position();
    }
    
    public double distance_to(GameObject object) {
        return Math.max(this.position.distance(object.position) - (this.size() + object.size()) / 2 ,0.0);
    }
    
    public boolean is_impacted_by(GameObject object) {
        return (distance_to(object) == 0.0);
    }
    
    public Point2D.Double velocity() {
        return (Point2D.Double) this.velocity.clone();
    }
    
    public double speed() {
        return velocity.distance(0.0, 0.0);
    }
    
    public double impact_energy_with(GameObject object) {
        Point2D.Double diff_vel = new Point2D.Double(
                this.velocity.x - object.velocity.x,
                this.velocity.y - object.velocity.y
                );
        
        return this.mass() * diff_vel.distanceSq(0.0, 0.0) / 2;
    }
    
    public double kinetic_energy() {
        return this.velocity.distanceSq(0.0,0.0) * this.mass() / 2;
    }
    
    public void change_velocity(Point2D.Double amount) {
        this.velocity.x = this.velocity.x + amount.x;
        this.velocity.y = this.velocity.y + amount.y;
    }
    
    public Point2D.Double move(double time) {
        this.position.x = this.position.x + this.velocity.x * time;
        this.position.y = this.position.y + this.velocity.y * time;
        return this.position();
    }    
    
    public Energy accelerate_object(Point2D.Double direction, double energy_per_second, double duration) {
        duration = Math.min(duration, World.w.get_max_duration());
        Energy force = new Energy(this, direction, duration, energy_per_second);
        World.w.add_force(force);
        return force;
    }
    
    public String Json() {	
    	return "{\"emptymass\" : " + this.mass() + ", "+
    			"\"position\"  : " + GameWorldJsonFactory.PointToJson(this.position(), "x", "y") + ", " +
				"\"velocity\"  : " + GameWorldJsonFactory.PointToJson(this.velocity(), "dx", "dy") + ", " +
    			"\"id\"        : " + id + "}";
    }    
}