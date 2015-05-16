package minusk.citysim.entities;

import org.jbox2d.common.Vec2;

import minusk.render.interfaces.Renderable;
import minusk.render.interfaces.Updateable;

public abstract class Entity implements Updateable, Renderable {
	protected int layer;
	
	public int getLayer() {
		return layer;
	}
	
	public abstract Vec2 getCenter();
}
