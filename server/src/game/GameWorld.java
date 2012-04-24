package game;

import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

/**
 *
 * @author maweki
 */
public class GameWorld {
	private int framecount = 0;
	private long last_message;
	private long cleanup_counter;
	private static final long message_every = 10000;
	private static final long cleanup_every = 10000;
    private int lastid = 0;
    private GameWorldJsonFactory json;
    private int number_of_ships = 0;
    
	
    private static final double starting_distance = 750.0;
    private static final double ship_size = 25.0;
    private static final double starting_mass = 160000.0;
    private static final double starting_removable_mass = 20000.0;
    
    public GameWorld() {
		super();
		this.forces = new HashSet<Energy>();
		this.objects = new HashSet<GameObject>();
		this.json = new GameWorldJsonFactory(this);
		this.last_message = System.currentTimeMillis();
	}

	private Set<Energy> forces;
    private Set<GameObject> objects;
    
    public GameWorldJsonFactory json() {
    	return this.json;
    }
    
    public Iterator<GameObject> ObjectsIter() {
    	return this.objects.iterator();
    }
    
    public void add_force(Energy energy) {
        forces.add(energy);
    }
    
    public int number_of_ships() {
    	return this.number_of_ships;
    }
    
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

	private void remove_outset_projectiles() {
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

	private void check_collision() {
		Vector<Ship> ships = new Vector<Ship>();
		Iterator<GameObject> iter = this.ObjectsIter();
		while (iter.hasNext()) {
			GameObject o = iter.next();
			if (o instanceof Ship) {
				ships.add((Ship) o);
			}
		}
		
		Iterator<Ship> ship_iter = ships.iterator();
		while (ship_iter.hasNext()) {
			GameObject obj = ship_iter.next();
			if (obj instanceof Ship) {
				check_collision((Ship)obj);
			}
		}
		
	}

	private void check_collision(Ship ship) {
		Iterator<GameObject> projectile_iter = this.objects.iterator();
		while (projectile_iter.hasNext()) {
			GameObject obj1 = projectile_iter.next();
			if (obj1 instanceof Projectile) {
				Projectile proj = (Projectile) obj1;
				if (proj.is_owned_by(ship)) {
					continue;
				}
				if (ship.is_impacted_by(proj)) {
					ship.projectile_impact(proj);
					projectile_iter.remove();
				}
				
			}
			if ((obj1 instanceof Ship) && (obj1 != ship)) {
				if (ship.is_impacted_by(obj1)) {
					// crash
					projectile_iter.remove();
					this.number_of_ships = this.number_of_ships - 1;
				}
			}
		}
		
	}

	private void nextframe() {
		this.framecount = this.framecount + 1;
		if (System.currentTimeMillis() - this.last_message > GameWorld.message_every) {
			print_stats();
		}
	}

	private void print_stats() {
		long duration = System.currentTimeMillis() - this.last_message;
		double fps = Math.floor(this.framecount / (duration / 1000.0));
		System.out.println(this.framecount + " Frames in " + duration + "ms : " + fps + "FPS");
		System.out.println(this.forces.size()+ " Forces : " + this.objects.size() + " Objects : Lastid=" +  this.lastid);
	/*	Iterator<GameObject> iter = this.objects.iterator();
		while (iter.hasNext()) {
			GameObject obj = iter.next();
			System.out.println(obj);
		}*/
		this.last_message = System.currentTimeMillis();
		this.framecount = 0;
	}
	
	public int shoot(Point2D.Double direction, double energy, double mass, int id) throws OutOfEnergyException {
		Iterator<GameObject> iter = this.ObjectsIter();
		while (iter.hasNext()) {
			GameObject o = iter.next();
			if ((o.id == id) && (o instanceof Ship)) {
				Ship s = (Ship) o;
				Projectile p = s.create_projectile(direction, mass, energy);
				return p.id;
			}
		}
		return 0;
	}
	
	public void accelerate_ship(Point2D.Double direction, double energy, int id) throws OutOfEnergyException {
		Iterator<GameObject> iter = this.ObjectsIter();
		while (iter.hasNext()) {
			GameObject o = iter.next();
			if ((o.id == id) && (o instanceof Ship)) {
				Ship s = (Ship) o;
				s.accelerate_ship(direction, energy, 1.0);
			}
		}
	}

	public Ship new_ship() {
		Ship s = new Ship(GameWorld.starting_mass, GameWorld.ship_size, GameWorld.starting_removable_mass);
		if (this.number_of_ships == 0) {
			this.add_object(s);
			return s;
		}
		if (this.number_of_ships == 1) {
			double dx, dy;
			dx = Math.random();
			dy = Math.sqrt(1 - dx*dx);
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
			
			
			Double dx = s2.position().x - s1.position().x;
			Double dy = s2.position().y - s1.position().y;
			Point2D.Double center = new Point2D.Double(
					dx / 2 + s1.position().x,
					dy / 2 + s1.position().y );
			System.out.println(center);
			Double step = GameWorld.starting_distance / 10;
			
			Point2D.Double vec;
			Random generator = new Random();
		    if (generator.nextBoolean()) {
		    	vec = new Point2D.Double(dy, -1.0 * dx);
		    }
		    else {
		    	vec = new Point2D.Double(-1.0 * dy, dx);
		    }
			Double vec_l = vec.distance(0.0, 0.0); 
			Point2D.Double vec_normalized = new Point2D.Double(vec.x / vec_l , vec.y / vec_l);
			
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
			
			Point2D.Double center = new Point2D.Double(
					(x_max - x_min)/2.0 + x_min ,
					(y_max - y_min)/2.0 + y_min);
			Point2D.Double direction = new Point2D.Double(Math.random(), Math.random());
			Random generator = new Random();
		    if (generator.nextBoolean()) {
		    	direction.x = -1.0 * direction.x;
		    }
		    if (generator.nextBoolean()) {
		    	direction.y = -1.0 * direction.y;
		    }
		    
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
	
	public void remove_forces_of(GameObject o) {
		Iterator<Energy> f_iter = this.forces.iterator();
		while (f_iter.hasNext()) {
			Energy f = f_iter.next();
			if (f.applies_to() == o) {
				f_iter.remove();
			}
		}
	}
	
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
