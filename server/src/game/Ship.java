package game;

import java.awt.geom.Point2D;
import game.OutOfEnergyException;

/**
 *
 * @author maweki
 */
public class Ship extends GameObject {
    @Override
	public String toString() {
		return "Ship [removable_mass=" + removable_mass + ", toString()="
				+ super.toString() + "]";
	}

	private double damage = 0.0;
	private double points = 0.0;
    private double removable_mass = 0.0;
    
    private String playername = "";
    
    
    public Ship(double mass, double size) {
        this(mass, size, 6000.0);
    }
    
    public Ship(double mass, double size, double removable_mass) {
        super(mass, size);
        this.add_removable_mass(removable_mass);
    }
    
    public double mass() {
        return super.mass() + this.removable_mass;
    }
    
    public double remove_removable_mass(double amount) throws OutOfEnergyException {
        amount = Math.max(amount, 0.0);
        if (amount > this.removable_mass) {
        	throw new OutOfEnergyException();
        }
        else {
        	this.removable_mass = this.removable_mass - amount;
        }
        return amount;
    }
    
    private double add_removable_mass(double amount) {
        this.removable_mass = this.removable_mass + amount;
        return this.mass();
    }
    
    public double damage() {
        return this.damage;
    }
    
    public double damage(double amount) {
        this.damage = Math.max(this.damage + amount, 0.0);
        return this.damage();
    }
    
    public double points() {
        return this.points;
    }
    
    public double points(double amount) {
        this.points = Math.max(this.points+ amount, 0.0);
        return this.points();
    }
    
    public String playername() {
        return this.playername;
    }
    
    public String playername(String playername) {
    	this.playername = playername;
        return this.playername();
    }
    
    /**
     * Removes mass from a ship (implicit conversion from energy)
     * @param energy Energy to remove from the ship
     * @return removed mass
     * @throws OutOfEnergyException 
     */
    public double remove_energy(double energy) throws OutOfEnergyException {
    	// TODO use high precision decimals
    	double mass_to_remove = energy / (89.9 * 1000000000000000.0);
    	this.remove_removable_mass(mass_to_remove);
    	return mass_to_remove;
    }      
    
    public Ship projectile_impact(Projectile projectile) {
        Double impact_energy = projectile.impact_energy_with(this);
    	this.damage(impact_energy);
    	projectile.owner().points(impact_energy);
        this.add_removable_mass(projectile.mass());
        this.accelerate_object(projectile.velocity(), impact_energy, 1.0);
        return this;
    }
      
    public Energy accelerate_ship(Point2D.Double direction, double energy_per_second, double duration) throws OutOfEnergyException {
        return this.accelerate_ship(direction, energy_per_second, duration, true);
    }
    
    public Energy accelerate_ship(Point2D.Double direction, double energy_per_second, double duration, boolean remove_energy) throws OutOfEnergyException {
        if (remove_energy) {
        	this.remove_energy(duration * energy_per_second);
        }
        return accelerate_object(direction, energy_per_second, duration);
    }
    
    public Projectile create_projectile(Point2D.Double direction, double mass, double energy) throws OutOfEnergyException {
        Projectile projectile = new Projectile(mass, this);
        projectile.change_velocity(this.velocity());
        projectile.set_position(this.position());
        
        Point2D.Double negative_vektor = new Point2D.Double(-direction.x,-direction.y);
        this.accelerate_ship(negative_vektor, energy/2, 1.0);
        projectile.accelerate_object(direction, energy/2, 1.0);
        this.remove_energy(energy/2);
        this.remove_removable_mass(mass);
        
        World.w.add_object(projectile);
        
        return projectile;
    }
    
    public String Json() {	
    	return "{\"mass\" : " + this.mass() + ", "+
    			"\"rem_mass\"  : " + removable_mass + ", "+
    			"\"damage\"    : " + this.damage() + ", "+
    			"\"points\"    : " + this.points() + ", "+
    			"\"size\"  : " + this.size() + ", " +
    			"\"playername\"  : \"" + this.playername() + "\", " + //Escape playername with slashes
    			"\"position\"  : " + GameWorldJsonFactory.PointToJson(this.position(), "x", "y") + ", " +
				"\"velocity\"  : " + GameWorldJsonFactory.PointToJson(this.velocity(), "dx", "dy") + ", " +
    			"\"id\"        : " + id + "}";
    }
    
    
}
