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

	private double duration_remaining = 1.0;
    private GameObject apply_to;
    private double energy_per_second = 0.0;
    private Point2D.Double direction; 
    
    
    public Energy(GameObject apply_to, Point2D.Double direction, double duration, double energy_per_second) {
        this.apply_to = apply_to;
        this.direction = this.normalize_vektor((Point2D.Double) direction.clone());
        this.duration_remaining = duration; /* s */
        this.energy_per_second = energy_per_second; /*kg*m²/s³*/
        System.out.println(this);
    }
    
    private Point2D.Double normalize_vektor(Point2D.Double vektor) {
        double distance = vektor.distance(0.0, 0.0);
        vektor.x = vektor.x / distance;
        vektor.y = vektor.y / distance;
        return vektor;
    }
    
    public Energy apply(double time /* s */) {
        if (time >= this.duration_remaining) {
            time = this.duration_remaining;
            this.duration_remaining = 0.0;
        }
        else {
            this.duration_remaining = this.duration_remaining - time;
        }
        
        Point2D.Double dv = new Point2D.Double();
        double undirected_dv = time * Math.sqrt(this.energy_per_second * 2.0 / this.apply_to.mass());
        dv.x = direction.x * undirected_dv;
        dv.y = direction.y * undirected_dv;
        
        this.apply_to.change_velocity(dv);
        
        if (this.duration_remaining == 0.0) {
            return null;
        }
        else {
            return this;
        }
    }
    
    public GameObject applies_to() {
    	return this.apply_to;
    }
}
