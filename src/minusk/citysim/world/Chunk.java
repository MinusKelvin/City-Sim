package minusk.citysim.world;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import minusk.render.math.Vec2;

public class Chunk {
	public final boolean useable;
	public final int x,y;
	private final ArrayList<MapStructures.Road> roads = new ArrayList<>();
	
	public Chunk(int x, int y) {
		this.x = x;
		this.y = y;
		File f = new File("map");
		if (!f.exists())
			throw new IllegalStateException("No /map/ folder");
		if (!f.isDirectory())
			throw new IllegalStateException("/map/ is not a directory");
		
		f = new File("map/c"+Integer.toHexString(x).toUpperCase());
		if (!f.exists() || !f.isDirectory()) {
			useable = false;
			System.err.println("Cannot find directory /map/c"+Integer.toHexString(x).toUpperCase());
			return;
		}
		
		f = new File("map/c"+Integer.toHexString(x).toUpperCase()+"/c"+Integer.toHexString(y).toUpperCase()+".cnk");
		if (!f.exists() || !f.isFile()) {
			useable = false;
			System.err.println("Cannot find file /map/c"+Integer.toHexString(x).toUpperCase()+
					"/c"+Integer.toHexString(y).toUpperCase()+".cnk");
			return;
		}
		
		try (Scanner s = new Scanner(new BufferedInputStream(new FileInputStream(f)))) {
			while (s.hasNext()) {
				String input = s.next();
				switch (input) {
				case "ROAD":
					float x1 = s.nextFloat();
					float y1 = s.nextFloat();
					float idirx = Float.NaN;
					float idiry = Float.NaN;
					String in = s.next();
					if (in.equals("DIR")) {
						idirx = s.nextFloat();
						idiry = s.nextFloat();
						in = s.next();
					}
					ArrayList<Vec2> controls = new ArrayList<>();
					while (in.equals("CURVEPOINT")) {
						controls.add(new Vec2(s.nextFloat(), s.nextFloat()));
						in = s.next();
					}
					float x2 = Float.parseFloat(in);
					float y2 = s.nextFloat();
					in = s.next();
					float endx = Float.NaN;
					float endy = Float.NaN;
					if (in.equals("DIR")) {
						endx = s.nextFloat();
						endy = s.nextFloat();
						in = s.next();
					}
					float width = Float.parseFloat(in);
					MapStructures.LaneLine[] lanes = new MapStructures.LaneLine[s.nextInt()];
					float[] dists = new float[lanes.length];
					for (int i = 0; i < lanes.length; i++)
						lanes[i] = new MapStructures.LaneLine(s.nextFloat(), s.next());
					roads.add(new MapStructures.Road(x1,y1,idirx!=idirx?null:new Vec2(idirx,idiry),
							x2,y2,endx!=endx?null:new Vec2(endx,endy),width,lanes,dists,controls));
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			useable = false;
			return;
		}
		useable = true;
	}
	
	public void render(MapRenderer renderer) {
		if (!useable)
			return;
		
		for (MapStructures.Road r : roads)
			renderer.drawRoad(r);
	}
}
