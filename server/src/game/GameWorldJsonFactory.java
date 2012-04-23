package game;

import java.awt.geom.Point2D;
import java.util.Iterator;

public class GameWorldJsonFactory {
	private GameWorld parent;
	
	public GameWorldJsonFactory(GameWorld parent) {
		super();
		this.parent = parent;
	}
	
	public String WorldState() {
		Iterator<GameObject> iter = this.parent.ObjectsIter();
		String projectiles = "";
		String ships = "";
		while (iter.hasNext()) {
			GameObject o = iter.next();
			if (o instanceof Ship) {
				if (ships.equals("")) {
					ships = o.Json();
				}
				else {
					ships = ships.concat(", \n");
					ships = ships.concat(o.Json());
				}
			}
			else if (o instanceof Projectile) {
				if (projectiles.equals("")) {
					projectiles = o.Json();
				}
				else {
					projectiles = projectiles.concat(", \n");
					projectiles = projectiles.concat(o.Json());
				}
			}
		}
		return "{\"ships\" : \n [ " + ships + " ], \n \"projectiles\" : \n [ " + projectiles  + " ]\n}";
	}
	
	public static String PointToJson(Point2D.Double point, String namex, String namey) {
		return "{" + "\""+ namex +"\" : " + point.x + "," +
				"\"" + namey +"\" : " + point.y + "}" ;
		
	}
}
