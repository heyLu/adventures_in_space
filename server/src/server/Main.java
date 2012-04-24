package server;

import java.io.IOException;

public class Main {

	public static final int FPScap =  100 /*Integer.MAX_VALUE*/;
	public static final double MAX_SIMU_FRAME =  0.5; /*Integer.MAX_VALUE*/;
	public static HttpServer server;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		game.World.reset();
		try {
			server = new HttpServer(8081);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(1);
		}

		
		double framelength = 0.0;
		long start_frame;
		long end_frame;
		while (true) {
			start_frame = System.currentTimeMillis();
			while (framelength > MAX_SIMU_FRAME) {
				framelength = framelength - MAX_SIMU_FRAME;
				game.World.w.simulate_world(MAX_SIMU_FRAME);
			}
			game.World.w.simulate_world(framelength);
			long calctime = System.currentTimeMillis() - start_frame;
			try {
				Thread.sleep(Math.max((long) Math.floor((1.0 / FPScap - calctime)*1000),0));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			end_frame = System.currentTimeMillis();
			framelength = (end_frame - start_frame) / 1000.0;
		}
	}

}
