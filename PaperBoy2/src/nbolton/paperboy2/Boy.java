package nbolton.paperboy2;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;

public class Boy {
	
	final float legTopAngle = (float)Math.toRadians(60);
	final float legBottomAngle = (float)Math.toRadians(80);
	final float armTopAngle = (float)Math.toRadians(60);
	final float headAngle = (float)Math.toRadians(30);
	
	World world;
	
	RevoluteJoint leftArmTopJoint, rightArmTopJoint, 
		leftLegTopJoint, rightLegTopJoint,
		leftLegBottomJoint, rightLegBottomJoint;
	
	ArrayList<DistanceJoint> supportJoints = new ArrayList<DistanceJoint>();
	
	Body torso, torsoSupport1, torsoSupport2;
	Body head;
	Body leftArmTop;
	Body rightArmTop;
	Body leftLegTop;
	Body rightLegTop;
	Body leftLegBottom;
	Body rightLegBottom;
	
	Action lastAction = Action.None;
	Action action = Action.WalkRight; //Action.None;
	Vector2 position;
	
	long walkStartMillis;
	
	enum Action
	{
		None,
		WalkRight,
		WalkLeft,
		Jump,
		Grab
	};
	
	public Boy(World world) {
		this.world = world;
	}

	public void createStickManSideOn(float x, float y) {

		torso = createRectangleBodyPart(x, y, 0.1f, 0.8f);		
		head = createRoundBodyPart(x, y + 1.2f, 0.4f);
		
		leftArmTop = createRectangleBodyPart(x, y, 0.1f, 0.65f);
		rightArmTop = createRectangleBodyPart(x, y, 0.1f, 0.65f);
		
		leftLegTop = createRectangleBodyPart(x, y - 1.1f, 0.1f, 0.5f);
		rightLegTop = createRectangleBodyPart(x, y - 1.1f, 0.1f, 0.5f);
		
		leftLegBottom = createRectangleBodyPart(x, y - 1.9f, 0.1f, 0.5f);
		rightLegBottom = createRectangleBodyPart(x, y - 1.9f, 0.1f, 0.5f);

		torsoSupport1 = createSupportBody(position.x - 2, position.y);
		torsoSupport2 = createSupportBody(position.x + 2, position.y);
		
		joinBodyParts(torso, head, new Vector2(0, 0.8f), headAngle);
		
		leftArmTopJoint = joinBodyParts(torso, leftArmTop, new Vector2(0, 0.6f), -armTopAngle * 0.7f, armTopAngle * 0.3f);
		rightArmTopJoint = joinBodyParts(torso, rightArmTop, new Vector2(0, 0.6f), -armTopAngle * 0.7f, armTopAngle * 0.3f);
		
		leftLegTopJoint = joinBodyParts(torso, leftLegTop, new Vector2(0, -0.7f), legTopAngle * 0.1f, legTopAngle * 0.9f);
		rightLegTopJoint = joinBodyParts(torso, rightLegTop, new Vector2(0, -0.7f), legTopAngle * 0.1f, legTopAngle * 0.9f);
		
		leftLegBottomJoint = joinBodyParts(leftLegTop, leftLegBottom, new Vector2(0, -0.4f), -legBottomAngle, 0);
		rightLegBottomJoint = joinBodyParts(rightLegTop, rightLegBottom, new Vector2(0, -0.4f), -legBottomAngle, 0);

		supportJoints.add(joinSupportBody(torso, torsoSupport1, new Vector2(0, 0.7f)));
		supportJoints.add(joinSupportBody(torso, torsoSupport2, new Vector2(0, 0.7f)));
		supportJoints.add(joinSupportBody(torso, torsoSupport1, new Vector2(0, -0.7f)));
		supportJoints.add(joinSupportBody(torso, torsoSupport2, new Vector2(0, -0.7f)));
	}
	
	private Body createSupportBody(float x, float y)
	{
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(0.1f, 0.1f);
		
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.StaticBody;
		bodyDef.position.x = x;
		bodyDef.position.y = y;
		
		Body body = world.createBody(bodyDef);
		
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		
		// don't collide with anything
		fixtureDef.filter.groupIndex = -1;
		fixtureDef.filter.categoryBits = Game.FILTER_SUPPORT;
		fixtureDef.filter.maskBits = Game.FILTER_WALL;
		
		body.createFixture(fixtureDef);
		shape.dispose();
		
		return body;
	}

	private DistanceJoint joinSupportBody(Body body, Body support, Vector2 bodyAnchor) {
		
		DistanceJointDef jointDef = new DistanceJointDef();
		
		jointDef.initialize(
			body, support, body.getWorldPoint(bodyAnchor), support.getWorldCenter());
		
		return (DistanceJoint)world.createJoint(jointDef);
	}
	
	private RevoluteJoint joinBodyParts(Body a, Body b, Vector2 anchor, float angle) {
		
		return joinBodyParts(a, b, anchor, -angle, angle);
	}
	
	private RevoluteJoint joinBodyParts(
		Body a, Body b, Vector2 anchor, float lowerAngle, float upperAngle) {

		RevoluteJointDef jointDef = new RevoluteJointDef();
		
		jointDef.initialize(a, b, a.getWorldPoint(anchor));
		
		//jointDef.enableLimit = true;
		//jointDef.lowerAngle = lowerAngle;
		//jointDef.upperAngle = upperAngle;
		
		jointDef.enableMotor = true;
		jointDef.maxMotorTorque = 10;
		
		return (RevoluteJoint)world.createJoint(jointDef);
	}

	private Body createRoundBodyPart(float x, float y, float radius) {

		CircleShape shape = new CircleShape();
		shape.setRadius(radius);

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.x = x;
		bodyDef.position.y = y;
		bodyDef.allowSleep = false;
		
		Body body = world.createBody(bodyDef);
		
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 1f;
		
		// -1 means no body parts collide
		fixtureDef.filter.groupIndex = -1;
		fixtureDef.filter.categoryBits = Game.FILTER_BOY;
		fixtureDef.filter.maskBits = Game.FILTER_STUFF | Game.FILTER_WALL;

		// add the boxPoly shape as a fixture
		body.createFixture(fixtureDef);
		shape.dispose();
		
		return body;
	}

	private Body createRectangleBodyPart(float x, float y, float width, float height) {
		
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(width, height);
		
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.y = y;
		bodyDef.position.x = x;
		bodyDef.allowSleep = false;
		
		Body body = world.createBody(bodyDef);
		
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 1;
		
		// -1 means no body parts collide
		fixtureDef.filter.groupIndex = -1;
		fixtureDef.filter.categoryBits = Game.FILTER_BOY;
		fixtureDef.filter.maskBits = Game.FILTER_STUFF | Game.FILTER_WALL;
		
		body.createFixture(fixtureDef);
		shape.dispose();
		
		return body;
	}
	
	private long getWalkTimeElapsedMillis() {
		return System.currentTimeMillis() - walkStartMillis;
	}
	
	public void render() {
		
		//float delta = Gdx.graphics.getDeltaTime();
		
		// action has been toggled
		if (lastAction != action) {
			
			System.out.println(lastAction.toString() + " => " + action.toString());
			
			// boy has started walking - reset delta
			if (action == Action.WalkLeft || action == Action.WalkRight)
				walkStartMillis = System.currentTimeMillis();
		}

		
		if (action == Action.WalkRight) {
			
			rightLegTopJoint.setMotorSpeed(getLegTopSpeed(0));
			//leftLegTopJoint.setMotorSpeed(getLeftLegTopSpeed());
			
			rightLegBottomJoint.setMotorSpeed(getLegBottomSpeed(0));
			//leftLegBottomJoint.setMotorSpeed(getLeftLegBottomSpeed());

			//System.out.println(f.format(getLegTopSpeed()));
			//System.out.println(f.format(getWalkTimeElapsedMillis()));
			
		} else {
			
			standStill();
		}
		
		lastAction = action;
	}

	private float getLegTopSpeed(float offset) {
		return getWalkMotion(offset) * 5;
	}
	
	private float getLegBottomSpeed(float offset) {
		return -getWalkMotion(offset) * 15;
	}

	private float getWalkMotion(float offset) {
		return (float)Math.cos((getWalkTimeElapsedMillis() + offset) * 0.008f);
	}
	
	private void standStill() {
		
		rightLegBottomJoint.setMotorSpeed(0);
		leftLegBottomJoint.setMotorSpeed(0);
	}
	
	public void die() {
		
		for (DistanceJoint joint : supportJoints) {
			world.destroyJoint(joint);
		}

		leftArmTopJoint.enableMotor(false);
		rightArmTopJoint.enableMotor(false);
		
		rightLegTopJoint.enableMotor(false);
		leftLegTopJoint.enableMotor(false);
		
		rightLegBottomJoint.enableMotor(false);
		leftLegBottomJoint.enableMotor(false);
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public Action getAction() {
		return action;
	}

	public void setPosition(Vector2 position) {
		this.position = position;
	}

	public Vector2 getPosition() {
		return position;
	}
}
