package server;

import game.Ship;
import game.World;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class Httpserver extends NanoHTTPD {

	public Httpserver(int port) throws IOException {
		super(port, null);

	}
	
	public Response serve( String uri, String method, Properties header, Properties parms, Properties files )
	{
		String msg = "";
		if (uri.equalsIgnoreCase("/accelerate")) {
			try {
				double dx = Double.parseDouble(parms.getProperty("dx"));
				double dy = Double.parseDouble(parms.getProperty("dy"));
				double energy = Double.parseDouble(parms.getProperty("energy"));
				int shipid = Integer.parseInt(parms.getProperty("id"));
				game.World.w.accelerate_ship(new Point2D.Double(dx, dy), energy, shipid);
			}
			catch (Exception e) {
				// send error on out of energy exception
				msg = "";
			}
		}
		else if (uri.equalsIgnoreCase("/shoot")) {
			try {
				double dx = Double.parseDouble(parms.getProperty("dx"));
				double dy = Double.parseDouble(parms.getProperty("dy"));
				double energy = Double.parseDouble(parms.getProperty("energy"));
				double mass = Double.parseDouble(parms.getProperty("mass"));
				int shipid = Integer.parseInt(parms.getProperty("id"));
				int p = game.World.w.shoot(new Point2D.Double(dx, dy), energy, mass, shipid);
				msg = "{\"id\" : " + p + " }";
			}
			catch (Exception e) {
				// send error on out of energy exception
				msg = "";
			}
		}
		else if (uri.equalsIgnoreCase("/login")) {
			Ship s = World.w.new_ship();
			if (s != null) {
				msg = "{\"id\" : " + s.id + " }";
			}
		}
		else if (uri.equalsIgnoreCase("/reset")) {
			World.reset();
			msg="World Reset";
			System.out.println("World Reset");
		}
		else if (uri.equalsIgnoreCase("/testinterface")) {
			try {
				msg = Httpserver.readFileAsString("action.html");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if (uri.equalsIgnoreCase("/testinterface_")) {
			try {
				msg = Httpserver.readFileAsString("actioninput.html");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			msg = game.World.w.json().WorldState();
		}
		return new NanoHTTPD.Response( HTTP_OK, MIME_HTML, msg );
	}
	
	private static String readFileAsString(String filePath)
		    throws java.io.IOException{
		        StringBuffer fileData = new StringBuffer(1000);
		        BufferedReader reader = new BufferedReader(
		                new FileReader(filePath));
		        char[] buf = new char[1024];
		        int numRead=0;
		        while((numRead=reader.read(buf)) != -1){
		            String readData = String.valueOf(buf, 0, numRead);
		            fileData.append(readData);
		            buf = new char[1024];
		        }
		        reader.close();
		        return fileData.toString();
		    }

}
