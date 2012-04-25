package game;

import java.awt.geom.Point2D;

/**
 *
 * @author maweki
 */
public class Energy {
    @Override
	public String toString() {
		return "Energy [duration_remaining=" + duration_remaining
				 + ", energy_per_second="
				+ energy_per_second + ", direction=" + direction + "]";
	}

    /**
     * Remaining duration of time the force will be applied (s)
     */
	private double duration_remaining = 1.0;
	
	/**
	 * GameObject the force is applied to
	 */
    private GameObject apply_to;
    
    /**
     * Energy to apply per second (J/s)
     */
    private double energy_per_second = 0.0;
    
    /**
     * Direction in which the force is applied
     */
    private Point2D.Double direction; 
    
    /**
     * Constructor
     * @param apply_to Object to apply force to
     * @param direction Direction in which to apply
     * @param duration Duration (s)
     * @param energy_per_second Energy per second to apply (J/s)
     */
    public Energy(GameObject apply_to, Point2D.Double direction, double duration, double energy_per_second) {
        this.apply_to = apply_to;
        this.direction = this.normalize_vector((Point2D.Double) direction.clone());
        this.duration_remaining = duration; /* s */
        this.energy_per_second = energy_per_second; /*kg*m²/s³*/
    }
    
    /**
     * Normalizes a vector (so that it has length 1)
     * @param vector Vector to normalize
     * @return The normalized vector (same object as input)
     */
    private Point2D.Double normalize_vector(Point2D.Double vector) {
        double distance = vector.distance(0.0, 0.0);
        vector.x = vector.x / distance;
        vector.y = vector.y / distance;
        return vector;
    }
    
    /**
     * Applies the force
     * @param time Timeframe in which the force is applied (s)
     * @return Null if the remaining time is over and the Energy if it is not
     */
    public Energy apply(double time) {
        if (time >= this.duration_remaining) {
        	/* Last frame in which force is applied */
            time = this.duration_remaining;
            this.duration_remaining = 0.0;
        }
        else {
        	/* time remaining so force will also be applied in the next frame */
            this.duration_remaining = this.duration_remaining - time;
        }
        
        /* Get absolute changed velocity m/s */
        double undirected_dv = time * Math.sqrt(this.energy_per_second /* implicit multiplication with 1 second */ * 2.0 / this.apply_to.mass());
        
        /* Multiply by normalized vector to get actual changed velocity*/
        Point2D.Double dv = new Point2D.Double();
        dv.x = direction.x * undirected_dv;
        dv.y = direction.y * undirected_dv;
        
        /* apply changed velocity to vector of movement*/
        this.apply_to.change_velocity(dv);
        
        if (this.duration_remaining == 0.0) {
            return null;
        }
        else {
            return this;
        }
    }
    
    /**
     * Returns the GameObject force is applied to
     * @return
     */
    public GameObject applies_to() {
    	return this.apply_to;
    }
}
