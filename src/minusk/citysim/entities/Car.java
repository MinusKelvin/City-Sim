package minusk.citysim.entities;

import minusk.citysim.Main;
import minusk.render.graphics.draw.SpriteDrawPass;
import minusk.render.util.Util;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.joints.RevoluteJointDef;
import org.jbox2d.dynamics.joints.WeldJointDef;

public class Car extends Entity {
	private Body chassis, tire0, tire1, tire2, tire3;
	private float[] prevTurning = new float[10];
	private float turning = 0, enginePower = 0;
	private boolean skidding0, skidding1, skidding2, skidding3, handbrakeApplied;
	
	public Car() {
		// Chassis
		BodyDef bDef = new BodyDef();
		bDef.position.x = 25;
		bDef.position.y = 25;
		bDef.type = BodyType.DYNAMIC;
		chassis = Main.game.getMap().physics.createBody(bDef);
		
		PolygonShape shape = new PolygonShape();
		FixtureDef fDef = new FixtureDef();
		shape.setAsBox(1.8f/2, 4.8f/2);
		fDef.shape = shape;
		fDef.density = 210;
		chassis.createFixture(fDef);
		chassis.setUserData(this);
		
		// Front-left Tire
		RevoluteJointDef rjDef = new RevoluteJointDef();
		rjDef.collideConnected = false;
		rjDef.bodyA = chassis;
		bDef.position.x = 25-0.775f;
		bDef.position.y = 25+1.5f;
		tire0 = Main.game.getMap().physics.createBody(bDef);
		shape.setAsBox(0.125f, 0.375f);
		fDef.shape = shape;
		fDef.density = 240;
		fDef.filter.maskBits = 0;
		tire0.createFixture(fDef);
		rjDef.bodyB = tire0;
		rjDef.localAnchorA.x = -0.775f;
		rjDef.localAnchorA.y = 1.5f;
		Main.game.getMap().physics.createJoint(rjDef);
		tire0.setUserData(this);

		// Front-Right Tire
		bDef.position.x = 25+0.775f;
		tire1 = Main.game.getMap().physics.createBody(bDef);
		tire1.createFixture(fDef);
		rjDef.bodyB = tire1;
		rjDef.localAnchorA.x = 0.775f;
		Main.game.getMap().physics.createJoint(rjDef);
		tire1.setUserData(this);

		// Back-Left Tire
		WeldJointDef wjDef = new WeldJointDef();
		rjDef.collideConnected = false;
		wjDef.bodyA = chassis;
		bDef.position.x = 25-0.775f;
		bDef.position.y = 25-1.5f;
		tire2 = Main.game.getMap().physics.createBody(bDef);
		tire2.createFixture(fDef);
		wjDef.bodyB = tire2;
		wjDef.localAnchorA.x = -0.775f;
		wjDef.localAnchorA.y = -1.5f;
		Main.game.getMap().physics.createJoint(wjDef);
		tire2.setUserData(this);

		// Back-Right Tire
		bDef.position.x = 25+0.775f;
		tire3 = Main.game.getMap().physics.createBody(bDef);
		tire3.createFixture(fDef);
		wjDef.bodyB = tire3;
		wjDef.localAnchorA.x = 0.775f;
		Main.game.getMap().physics.createJoint(wjDef);
		tire3.setUserData(this);
	}
	
	@Override
	public void update() {
		prevTurning[9] = prevTurning[8];
		prevTurning[8] = prevTurning[7];
		prevTurning[7] = prevTurning[6];
		prevTurning[6] = prevTurning[5];
		prevTurning[5] = prevTurning[4];
		prevTurning[4] = prevTurning[3];
		prevTurning[3] = prevTurning[2];
		prevTurning[2] = prevTurning[1];
		prevTurning[1] = prevTurning[0];
		prevTurning[0] = turning;
		
		float angle = Util.sum(prevTurning) / prevTurning.length;
		tire0.setTransform(tire0.getPosition(),angle*1/Math.max(tire0.getLinearVelocity().length()/10,1)+chassis.getAngle());
		tire1.setTransform(tire1.getPosition(),angle*1/Math.max(tire1.getLinearVelocity().length()/10,1)+chassis.getAngle());
		
		tire0.applyForceToCenter(tire0.getWorldVector(new Vec2(0, 1)).mulLocal(enginePower));
		tire1.applyForceToCenter(tire1.getWorldVector(new Vec2(0, 1)).mulLocal(enginePower));
	
		recalcVel(tire0,0);
		recalcVel(tire1,1);
		recalcVel(tire2,2);
		recalcVel(tire3,3);
		
		float speed = chassis.getLinearVelocity().length();
		if (speed > 28)
			chassis.setLinearVelocity(chassis.getLinearVelocity().mul(28/speed));
	}
	
	private void recalcVel(Body tire, int val) {
		Vec2 lateral;
		if (val > 1 && handbrakeApplied) {
			lateral = new Vec2(tire.getLinearVelocity());
			skidding2 = true;
			skidding3 = true;
		} else {
			lateral = tire.getWorldVector(new Vec2(1, 0));
			lateral.mulLocal(Vec2.dot(lateral, tire.getLinearVelocity()));
			
			Vec2 forward = tire.getWorldVector(new Vec2(0, 1));
			forward.mulLocal(Vec2.dot(forward, tire.getLinearVelocity()));
			float forwardSpeed = forward.normalize();
			float drag = -2 * forwardSpeed * tire.getMass();
			tire.applyForceToCenter(forward.mulLocal(drag));
		}
		if (lateral.length() > 2.5f) {
			lateral.mulLocal(2.5f/lateral.length());
			switch (val) {
			case 0: skidding0=true; break;
			case 1: skidding1=true; break;
			case 2: skidding2=true; break;
			case 3: skidding3=true; break;
			}
		} else if (!(val > 1 && handbrakeApplied)) {
			switch (val) {
			case 0: skidding0=false; break;
			case 1: skidding1=false; break;
			case 2: skidding2=false; break;
			case 3: skidding3=false; break;
			}
		}
		lateral.mulLocal(-tire.getMass());
		tire.applyLinearImpulse(lateral, tire.getWorldCenter(), true);
	}

	public void render(SpriteDrawPass carDraws) {
		drawPoly(chassis, carDraws, false);
		drawPoly(tire0, carDraws, skidding0);
		drawPoly(tire1, carDraws, skidding1);
		drawPoly(tire2, carDraws, skidding2);
		drawPoly(tire3, carDraws, skidding3);
	}
	
	private void drawPoly(Body body, SpriteDrawPass carDraws, boolean highlight) {
		Vec2[] verticies = ((PolygonShape)body.getFixtureList().getShape()).getVertices();
		Vec2 p1 = Transform.mul(body.getTransform(),verticies[0]);
		for (int i = 2; i < ((PolygonShape)body.getFixtureList().getShape()).getVertexCount(); i++) {
			Vec2 p2 = Transform.mul(body.getTransform(),verticies[i-1]);
			Vec2 p3 = Transform.mul(body.getTransform(),verticies[i]);
			carDraws.drawTriangle(p1.x, p1.y, 0, 0, p2.x, p2.y, 0, 0, p3.x, p3.y, 0, 0, highlight?1:0);
		}
	}
	
	public void turn(float dir) {
		turning = dir;
	}
	
	public void drive(float power) {
		enginePower = power;
	}
	
	public void handbrake(boolean applied) {
		handbrakeApplied = applied;
	}
	
	public Body getChassis() {
		return chassis;
	}
	
	public Body getFrontLeftTire() {
		return tire0;
	}
	
	public Body getFrontRightTire() {
		return tire1;
	}
	
	public Body getBackLeftTire() {
		return tire2;
	}
	
	public Body getBackRightTire() {
		return tire3;
	}
}
