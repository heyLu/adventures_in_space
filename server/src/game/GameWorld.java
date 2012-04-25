package game;

import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author maweki
 */
public class GameWorld {
	public Lock WorldLock;
	
	
	/**
	 * Framecounter between messages
	 */
	private int framecount = 0;
	
	/**
	 * Time of last message
	 */
	private long last_message;
	
	/**
	 * Cleanup Frame Counter
	 */
	private long cleanup_counter;
	
	/**
	 * Message every 10 Seconds (10.000ms)
	 */
	private static final long message_every = 10000;
	
	/**
	 * Cleanup every 10000 Frames
	 */
	private static final long cleanup_every = 10000;
	
	/**
	 * Last set object id
	 */
    private int lastid = 0;
    
    /**
     * factory
     */
    private GameWorldJsonFactory json;
    
    /**
     * Number of Ships in the game world
     */
    private int number_of_ships = 0;
    
	/**
	 * Minimum distance to other ships for spawning ships (m)
	 */
    private static final double starting_distance = 750.0;
    
    /**
     * Default Ship size (m)
     */
    private static final double ship_size = 25.0;
    
    /**
     * Default ship starting mass (kg)
     */
    private static final double starting_mass = 160000.0;
    
    /**
     * Default ship starting removable mass (kg)
     */
    private static final double starting_removable_mass = 20000.0;
    
    /**
     * Constructor
     */
    public GameWorld() {
		super();
		this.forces = new HashSet<Energy>();
		this.objects = new HashSet<GameObject>();
		this.json = new GameWorldJsonFactory(this);
		this.last_message = System.currentTimeMillis();
		this.WorldLock = new ReentrantLock();
	}

    /**
     * Set of Forces/Energies
     */
	private Set<Energy> forces;
	
	/**
	 * Set of GameObjects
	 */
    private Set<GameObject> objects;
    
    /**
     * Returns the Json-Factory
     * @return
     */
    public GameWorldJsonFactory json() {
    	return this.json;
    }
    
    /**
     * Returns the Iterator of the GameObject set
     * @return
     */
    public Iterator<GameObject> ObjectsIter() {
    	return this.objects.iterator();
    }
    
    /**
     * Adds a force to the set of Forces
     * @param energy
     */
    public void add_force(Energy energy) {
        forces.add(energy);
    }
    
    /**
     * Returns the number of ships
     * @return
     */
    public int number_of_ships() {
    	return this.number_of_ships;
    }
    
    /**
     * Adds an object to the set of GameObjects
     * @param object
     */
    public void add_object(GameObject object) {
        lastid = lastid + 1;
        object.id = lastid;
        objects.add(object);
        if (object instanceof Ship) {
        	this.number_of_ships = this.number_of_ships + 1;
        }
    }
    
    
    public double get_max_duration() {
        return 60.0;
    }
    
    /**
     * Simulates the World over a timeframe
     * @param time Timeframe (s)
     */
    public void simulate_world(double time) {

		apply_forces(time);
		
        move_objects(time);
        
        check_collision();
        
        this.cleanup_counter = this.cleanup_counter + 1;
        if (cleanup_counter == GameWorld.cleanup_every) {
        	remove_outset_projectiles();
        	cleanup_counter = 0;
        }
        
        nextframe();
    }

    /**
     * Removes projectiles that are too far out
     */
	private void remove_outset_projectiles() {
		/* Maximum distance from a projectile to its owner is double the maximum
		 * of the distances between any two ships
		 */
		Double d = Math.max(this.max_ship_distance() * 2.0, GameWorld.starting_distance);
		Iterator<GameObject> iter = this.ObjectsIter();
		while (iter.hasNext()) {
			GameObject o = iter.next();
			if (o instanceof Projectile) {
				Projectile p = (Projectile) o;
				if (p.distance_to(p.owner()) > d) {
					iter.remove();
				}
			}
		}
		
	}

	/**
	 * Move objects over time (calls the move-methods of the objects)
	 * @param time Timeframe (s) 
	 */
	private void move_objects(double time) {
		Iterator<GameObject> iter;
        iter = this.objects.iterator();
        while (iter.hasNext()) {
            GameObject object = (GameObject) iter.next();
            if (object != null) {
                object.move(time);
            }
        }
	}

	/**
	 * Applies all the forces to the objects over time (calls the apply method of the forces)
	 * @param time Timeframe (s)
	 */
	private void apply_forces(double time) {
		Iterator<Energy> iter = this.forces.iterator();
        while (iter.hasNext()) {
            Energy force = (Energy) iter.next();
            if (force != null) {
                if (force.apply(time) == null) {
                	iter.remove();
                }
            }
        }
	}

	/**
	 * Checks for collision of Ships with other objects
	 */
	private void check_collision() {
		/* Get list of ships */
		Vector<Ship> ships = new Vector<Ship>();
		Iterator<GameObject> iter = this.ObjectsIter();
		while (iter.hasNext()) {
			GameObject o = iter.next();
			if (o instanceof Ship) {
				ships.add((Ship) o);
			}
		}
		
		/* Check for specific ship collision */
		Iterator<Ship> ship_iter = ships.iterator();
		while (ship_iter.hasNext()) {
			GameObject obj = ship_iter.next();
			if (obj instanceof Ship) {
				check_collision((Ship)obj);
			}
		}
		
	}

	/**
	 * Checks for collision between a specific ship and any other game objects 
	 * @param ship
	 */
	private void check_collision(Ship ship) {
		Iterator<GameObject> projectile_iter = this.objects.iterator();
		while (projectile_iter.hasNext()) {
			GameObject obj1 = projectile_iter.next();
			if (obj1 instanceof Projectile) {
				/* Is projectile */
				Projectile proj = (Projectile) obj1;
				if (proj.is_owned_by(ship)) {
					/* don't shoot yourself */
					continue;
				}
				if (ship.is_impacted_by(proj)) {
					/* ship is impacted by projectile */
					ship.projectile_impact(proj);
					projectile_iter.remove();
				}
				
			}
			if ((obj1 instanceof Ship) && (obj1 != ship)) {
				if (ship.is_impacted_by(obj1)) {
					/* Ship is impacted by other ship */
					/* The other ship is removed in its collision routine */
					projectile_iter.remove();
					this.number_of_ships = this.number_of_ships - 1;
				}
			}
		}
		
	}

	/**
	 * Adds a Frame to the framecount
	 */
	private void nextframe() {
		this.framecount = this.framecount + 1;
		/* Print stats if the time is righti */
		if (System.currentTimeMillis() - this.last_message > GameWorld.message_every) {
			print_stats();
		}
	}

	/**
	 * Print server stats
	 */
	private void print_stats() {
		long duration = System.currentTimeMillis() - this.last_message;
		double fps = Math.floor(this.framecount / (duration / 1000.0));
		System.out.println(this.framecount + " Frames in " + duration + "ms : " + fps + "FPS");
		System.out.println(this.forces.size()+ " Forces : " + this.objects.size() + " Objects : Lastid=" +  this.lastid);
		this.last_message = System.currentTimeMillis();
		this.framecount = 0;
	}
	
	/**
	 * Shoot a projectile
	 * @param direction Direction
	 * @param energy Energy that is used (J)
	 * @param mass Mass of the projectile (kg)
	 * @param id ID of the ship that shoots
	 * @return id of the projectile
	 * @throws OutOfEnergyException Is thrown when available mass/energy was insufficient
	 */
	public int shoot(Point2D.Double direction, double energy, double mass, int id) throws OutOfEnergyException {
		Iterator<GameObject> iter = this.ObjectsIter();
		while (iter.hasNext()) {
			GameObject o = iter.next();
			if ((o.id == id) && (o instanceof Ship)) {
				Ship s = (Ship) o;
				if (s.action_possible()) {
					s.reset_action_timeout();
					Projectile p = s.create_projectile(direction, mass, energy);
					return p.id;
				}
				return 0;
			}
		}
		return 0;
	}
	
	/**
	 * Accelerate a ship
	 * @param direction Direction
	 * @param energy Energy that is used (J)
	 * @param id ID of the ship that accelerates
	 * @throws OutOfEnergyException Is thrown when available mass/energy was insufficient
	 */
	public void accelerate_ship(Point2D.Double direction, double energy, int id) throws OutOfEnergyException {
		Iterator<GameObject> iter = this.ObjectsIter();
		while (iter.hasNext()) {
			GameObject o = iter.next();
			if ((o.id == id) && (o instanceof Ship)) {
				Ship s = (Ship) o;
				if (s.action_possible()) {
					s.reset_action_timeout();
					s.accelerate_ship(direction, energy, 1.0);
				}
			}
		}
	}

	/**
	 * Creates new ship and positions it
	 * @return the new ship
	 */
	public Ship new_ship() {
		/* Create the ship with default characteristics */
		Ship s = new Ship(GameWorld.starting_mass, GameWorld.ship_size, GameWorld.starting_removable_mass);
		if (this.number_of_ships == 0) {
			/* One ship has no special routines*/
			this.add_object(s);
			return s;
		}
		if (this.number_of_ships == 1) {
			/* second ship will be positioned at a random angle 'starting_distance' away */
			
			double dx, dy;
			/* Random x and corresponding y so that vector is normalized */
			dx = Math.random();
			dy = Math.sqrt(1 - dx*dx);
			
			/* Flip signs for the four sectors */
		    Random generator = new Random();
		    if (generator.nextBoolean()) {
		    	dx = -1.0 * dx;
		    }
		    if (generator.nextBoolean()) {
		    	dy = -1.0 * dy;
		    }
		    
			s.set_position(new Point2D.Double(dx * GameWorld.starting_distance, dy * GameWorld.starting_distance));
			this.add_object(s);
			return s;
		}
		if (this.number_of_ships == 2) {
			/* Third ship will be positioned  'starting_distance' away from both other ships */
			
			/* Find both ships */
			Vector<Ship> ships = new Vector<Ship>();
			Iterator<GameObject> iter = this.ObjectsIter();
			while (iter.hasNext()) {
				GameObject o = iter.next();
				if (o instanceof Ship) {
					ships.add((Ship) o);
				}
			}
			Ship s1 = ships.get(0);
			Ship s2 = ships.get(1);
			
			/* get vector between both ships */
			Double dx = s2.position().x - s1.position().x;
			Double dy = s2.position().y - s1.position().y;
			
			/* get center point between ships */
			Point2D.Double center = new Point2D.Double(
					dx / 2 + s1.position().x,
					dy / 2 + s1.position().y );
			
			Double step = GameWorld.starting_distance / 10;
			
			/* get orthogonal vector */
			Point2D.Double vec;
			Random generator = new Random();
		    if (generator.nextBoolean()) {
		    	vec = new Point2D.Double(dy, -1.0 * dx);
		    }
		    else {
		    	vec = new Point2D.Double(-1.0 * dy, dx);
		    }
		    /* normalize orthogonal vector */
			Double vec_l = vec.distance(0.0, 0.0); 
			Point2D.Double vec_normalized = new Point2D.Double(vec.x / vec_l , vec.y / vec_l);
			
			/* add normalized vector to center position until distance is sufficient */
			s.set_position(center);
			while ((s.distance_to(s1) < GameWorld.starting_distance) &&
					(s.distance_to(s2) < GameWorld.starting_distance)) {
				center.x = center.x + vec_normalized.x * step;
				center.y = center.y + vec_normalized.y * step;
				s.set_position(center);
			}
			this.add_object(s);
			return s;
		}
		if (this.number_of_ships >= 3) {
			/* ever other ship will be created by calculating the center between
			 * all ships and randomly choosing an angle from there to create the
			 * ship at the sufficient distance*/

			/* get the corner positions of the rectangle around all ships */
			Double x_min = Double.MAX_VALUE,
					x_max = -1.0 * Double.MAX_VALUE,
					y_min = Double.MAX_VALUE , 
					y_max = -1.0 * Double.MAX_VALUE,
					step = GameWorld.starting_distance / 10.0;
			Vector<Ship> ships = new Vector<Ship>();			
			Iterator<GameObject> iter = this.ObjectsIter();
			while (iter.hasNext()) {
				GameObject o = iter.next();
				if (o instanceof Ship) {
					ships.add((Ship) o);
					x_min = Math.min(x_min, o.position().x);
					x_max = Math.max(x_max, o.position().x);
					y_min = Math.min(y_min, o.position().y);
					y_max = Math.max(y_max, o.position().y);
				}
			}
			
			/* get center position */
			Point2D.Double center = new Point2D.Double(
					(x_max - x_min)/2.0 + x_min ,
					(y_max - y_min)/2.0 + y_min);
			
			/* get random vector */
			Point2D.Double direction = new Point2D.Double(Math.random(), Math.random());
			Random generator = new Random();
		    if (generator.nextBoolean()) {
		    	direction.x = -1.0 * direction.x;
		    }
		    if (generator.nextBoolean()) {
		    	direction.y = -1.0 * direction.y;
		    }
		    
		    /* add the random vector to the center point until sufficient distance is reached */
		    Double mindistance = 0.0;
		    while (mindistance < GameWorld.starting_distance) {
		    	center.x = center.x + direction.x * step;
		    	center.y = center.y + direction.y * step;
		    	s.set_position(center);
		    	mindistance = Double.MAX_VALUE;
		    	Iterator<Ship> it = ships.iterator();
		    	while (it.hasNext()) {
		    		Ship o = it.next();
		    		mindistance = Math.min(mindistance, o.distance_to(s));
		    	}
		    }
			
			this.add_object(s);
			return s;
		}
		
		return null;
	}
	
	
	/**
	 * Removes a ship from the list of game objects (with all its forces and projectiles)
	 * @param s Ship to remove
	 */
	public void remove_ship(Ship s) {
		Iterator<GameObject> iter = this.ObjectsIter();
		while (iter.hasNext()) {
			GameObject go = iter.next();
			if (go == s) {
				this.remove_forces_of(s);
				iter.remove();
			}
			if ((go instanceof Projectile) && (((Projectile) go).owner() == s)) {
				this.remove_forces_of(go);
				iter.remove();
			}
		}
	}
	
	/**
	 * Removes a ship from the list of game objects (with all its forces and projectiles)
	 * @param id Ship to remove
	 */
	public void remove_ship(int id) {
		Iterator<GameObject> iter = this.ObjectsIter();
		while (iter.hasNext()) {
			GameObject go = iter.next();
			if (go.id == id) {
				this.remove_ship((Ship) go);
				break;
			}
		}
	}
	
	/**
	 * Removes a game object from the list of game objects (with all its forces)
	 * @param o Game Object to remove
	 */
	public void remove_game_object(GameObject o) {
		this.remove_forces_of(o);
		Iterator<GameObject> iter = this.ObjectsIter();
		while (iter.hasNext()) {
			GameObject go = iter.next();
			if (o == go) {
				iter.remove();
				break;
			}
		}
	}
	
	/**
	 * Remove all forces from a game object
	 * @param o GameObject
	 */
	public void remove_forces_of(GameObject o) {
		Iterator<Energy> f_iter = this.forces.iterator();
		while (f_iter.hasNext()) {
			Energy f = f_iter.next();
			if (f.applies_to() == o) {
				f_iter.remove();
			}
		}
	}
	
	/**
	 * Return the maximum of the distances between any two ships
	 * @return Distance (m)
	 */
	public Double max_ship_distance() {
		// TODO das sollte man optimieren!
		Double max = 0.0;
		Iterator<GameObject> iter = this.ObjectsIter();
		while (iter.hasNext()) {
			GameObject o = iter.next();
			if (o instanceof Ship) {
				Iterator<GameObject> iter2 = this.ObjectsIter();
				while (iter2.hasNext()) {
					GameObject o2 = iter2.next();
						if (o2 instanceof Ship) {
							max = Math.max(max, o.distance_to(o2));
						}
				}
			}
		}
		return max;
	}
}
