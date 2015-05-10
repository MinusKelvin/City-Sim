package minusk.citysim.entities.friendly;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;

import minusk.citysim.Main;
import minusk.citysim.entities.Car;
import minusk.citysim.entities.Entity;
import minusk.render.graphics.draw.SpriteDrawPass;

import org.jbox2d.dynamics.Body;

public class Player extends Entity {
	private Car car;
	
	public void init() {
		car = new Car();
	}
	
	@Override
	public void update() {
		boolean left = Main.game.getInput().isKeyDown(GLFW_KEY_A);
		boolean right = Main.game.getInput().isKeyDown(GLFW_KEY_D);
		if (!Boolean.logicalXor(left, right))
			car.turn(0);
		else if (left)
			car.turn(0.6f);
		else
			car.turn(-0.6f);
		
		boolean forward = Main.game.getInput().isKeyDown(GLFW_KEY_W);
		boolean backward = Main.game.getInput().isKeyDown(GLFW_KEY_S);
		if (!Boolean.logicalXor(forward, backward))
			car.drive(0);
		else if (forward)
			car.drive(7000);
		else
			car.drive(-5000);
		
		car.handbrake(Main.game.getInput().isKeyDown(GLFW_KEY_SPACE));
		
		car.update();
		
		float mps = car.getChassis().getLinearVelocity().length();
		System.out.println("Speed: " + mps*3600/1000 + "km/h");
	}
	
	public void render(SpriteDrawPass carDraws) {
		car.render(carDraws);
	}
	
	public Body getPhysicsObject() {
		return car.getChassis();
	}
}
