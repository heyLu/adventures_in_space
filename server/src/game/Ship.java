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

    /**
     * Damage the ship has received, equivalent to kinetic energy received on projectile impacts (J)
     */
	private double damage = 0.0;
	
	/**
	 * Points the ship has received, equivalent to kinetic energy dealt by own projectiles. (J)
	 */
	private double points = 0.0;
	
	/**
	 * Remaining mass that is usable for game moves (kg)
	 */
    private double removable_mass = 0.0;
    
    /**
     * Player's name, does not need to be set
     */
    private String playername = "";
    
    
    /**
     * Constructor - Constructs a ship with 6000kg of removable mass
     * @param mass Empty mass of the ship (kg)
     * @param size Size (diameter) of the ship (m)
     */
    public Ship(double mass, double size) {
        this(mass, size, 6000.0);
    }
    
    /**
     * Constructor - Constructs a ship
     * @param mass Empty mass of the ship (kg)
     * @param size (diameter) of the ship (m)
     * @param removable_mass Removable mass of the ship (kg)
     */
    public Ship(double mass, double size, double removable_mass) {
        super(mass, size);
        this.add_removable_mass(removable_mass);
    }
    
    /**
     * Returns the mass of the ship (kg)
     */
    public double mass() {
        return super.mass() + this.removable_mass;
    }
    
    /**
     * Removes removable mass from ship
     * @param amount Amount to be removed (kg)
     * @return amount that has been removed (kg)
     * @throws OutOfEnergyException If the ship has had insufficient mass
     */
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
    
    /**
     * Adds removable mass to ship
     * @param amount Amount to be added (kg)
     * @return new ship mass (including empty mass) (kg)
     */
    private double add_removable_mass(double amount) {
        this.removable_mass = this.removable_mass + amount;
        return this.mass();
    }
    
    /**
     * Returns damage taken by the ship (J)
     * @return 
     */
    public double damage() {
        return this.damage;
    }
    
    /**
     * Adds/Removes damage taken by the ship
     * @param amount Amount to be added/removed (J)
     * @return accumulated damage taken by ship (J)
     */
    public double damage(double amount) {
        this.damage = Math.max(this.damage + amount, 0.0);
        return this.damage();
    }
    
    /**
     * Returns damage dealt by the ship's projectiles (J)
     * @return
     */
    public double points() {
        return this.points;
    }
    
    /**
     * Adds/Removes damage dealt by the ship's projectiles
     * @param amount Amount to be added/removed (J)
     * @return accumulated damage dealt by ship's projectiles (J)
     */
    public double points(double amount) {
        this.points = Math.max(this.points+ amount, 0.0);
        return this.points();
    }
    
    /**
     * Returns the playername
     * @return
     */
    public String playername() {
        return this.playername;
    }
    
    /**
     * Sets the playername
     * @param playername Playername to be set
     * @return The set playername
     */
    public String playername(String playername) {
    	this.playername = playername;
        return this.playername();
    }
    
    /**
     * Removes mass from a ship (implicit conversion from energy)
     * @param energy Energy to remove from the ship (J)
     * @return removed mass (kg)
     * @throws OutOfEnergyException If the ship has had insufficient mass
     */
    public double remove_energy(double energy) throws OutOfEnergyException {
    	// TODO use high precision decimals
    	double mass_to_remove = energy / (89.9 * 1000000000000000.0);
    	this.remove_removable_mass(mass_to_remove);
    	return mass_to_remove;
    }      
    
    /**
     * Execute events of projectile impact on ship (collision is not checked)
     * @param projectile Impacted projectile 
     * @return own ship
     */
    public Ship projectile_impact(Projectile projectile) {
        Double impact_energy = projectile.impact_energy_with(this);
        
        /* Add damage/points to players */
    	this.damage(impact_energy);
    	projectile.owner().points(impact_energy);
    	
    	/* Add removable mass to hit player (reuse of projectile mass) */
        this.add_removable_mass(projectile.mass());
        
        /* Accelerate hit player with impact energy */
        this.accelerate_object(projectile.velocity(), impact_energy, 1.0);
        
        return this;
    }
      
    /**
     * Accelerate ship and removes energy used to accelerate from ship
     * @param direction Direction to accelerate
     * @param energy_per_second Energy with which to accelerate (J/s)
     * @param duration Duration of Acceleration (s)
     * @return Force that accelerates the ship
     * @throws OutOfEnergyException If the ship has had insufficient mass/energy
     */
    public Energy accelerate_ship(Point2D.Double direction, double energy_per_second, double duration) throws OutOfEnergyException {
        return this.accelerate_ship(direction, energy_per_second, duration, true);
    }
    
    /**
     * Accelerate ship
     * @param direction Direction to accelerate
     * @param energy_per_second Energy with which to accelerate (J/s)
     * @param duration Duration of Acceleration (s)
     * @param remove_energy Should energy used be removed from ship's reservoir/mass
     * @return Force that accelerates the ship
     * @throws OutOfEnergyException If the ship has had insufficient mass/energy
     */
    public Energy accelerate_ship(Point2D.Double direction, double energy_per_second, double duration, boolean remove_energy) throws OutOfEnergyException {
        if (remove_energy) {
        	this.remove_energy(duration * energy_per_second);
        }
        return accelerate_object(direction, energy_per_second, duration);
    }
    
    /**
     * Execute events of shooting projectiles from this ship
     * @param direction Direction to shoot
     * @param mass Mass the projectile should have (kg)
     * @param energy Energy with which the projectile should be accelerated (J)
     * @return The projectile shot
     * @throws OutOfEnergyException If the ship has had insufficient mass/energy
     */
    public Projectile create_projectile(Point2D.Double direction, double mass, double energy) throws OutOfEnergyException {
    	/* Create the projectile and set its position and velocity relative to the ship */
        Projectile projectile = new Projectile(mass, this);
        projectile.change_velocity(this.velocity());
        projectile.set_position(this.position());
        
        /* Calculate negative vector (opposite direction) and accelerate the ship in that direction */
        Point2D.Double negative_vector = new Point2D.Double(-direction.x,-direction.y);
        this.accelerate_ship(negative_vector, energy/2, 1.0);
        
        /* Accelerate projectile and remove mass/energy from ship's reservoir */
        projectile.accelerate_object(direction, energy/2, 1.0);
        this.remove_energy(energy/2); // the other half was removed by accelerate_ship()
        this.remove_removable_mass(mass);
        
        World.w.add_object(projectile);
        
        return projectile;
    }
    
    /**
     * Returns Json-String of the ship
     */
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
