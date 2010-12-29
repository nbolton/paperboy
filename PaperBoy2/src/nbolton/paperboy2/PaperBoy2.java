package nbolton.paperboy2;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.utils.MathUtils;

public class PaperBoy2 implements ApplicationListener, InputProcessor {
	
	/** the camera **/
	protected OrthographicCamera camera;

	/** the renderer **/
	protected Box2DDebugRenderer renderer;

	/** our box2D world **/
	protected World world;

	/** ground body to connect the mouse joint to **/
	protected Body groundBody;
	protected Body leftWall;
	protected Body rightWall;

	/** our mouse joint **/
	protected MouseJoint mouseJoint = null;

	/** a hit body **/
	protected Body hitBody = null;
	
	private Vector2 gravity = new Vector2(0, -20);

	final short FILTER_NONE = 0x0000;
	final short FILTER_SUPPORT = 0x0001;
	final short FILTER_WALL = 0x0002;
	final short FILTER_BOY = 0x0004;
	final short FILTER_STUFF = 0x0008;
	
	protected void createWorld() {
		
		groundBody = createWall(50, 1, 0);
		createWall(1, 50, -23);
		createWall(1, 50, 23);

		createCircles();
		createBoxes();
		
		createStickManSideOn(0, -8);
	}
	
	RevoluteJoint leftArmJoint, rightArmJoint, leftLegJoint, rightLegJoint;
	Body torso, support;
	Vector2 supportFirstPos;

	private void createStickManSideOn(float x, float y) {
		
		torso = createRectangleBodyPart(x + 0, y + 15, 1, 3, 0);
		Body head = createRoundBodyPart(x + 0, y + 20, 2);
		Body leftLeg = createRectangleBodyPart(x + -0.5f, y + 11, 1, 3, -5 * MathUtils.degreesToRadians);
		Body rightLeg = createRectangleBodyPart(x + 0.5f, y + 11, 1, 3, 5 * MathUtils.degreesToRadians);
		Body leftArm = createRectangleBodyPart(x + -0.5f, y + 16.5f, 1, 1.7f, -20 * MathUtils.degreesToRadians);
		Body rightArm = createRectangleBodyPart(x + 0.5f, y + 16.5f, 1, 1.7f, 20 * MathUtils.degreesToRadians);

		float legAngle = 60 * MathUtils.degreesToRadians;
		float armAngle = 70 * MathUtils.degreesToRadians;
		float headAngle = 20 * MathUtils.degreesToRadians;
		
		joinBodyParts(torso, head, new Vector2(0, 3), headAngle);
		leftLegJoint = joinBodyParts(torso, leftLeg, new Vector2(0, -2), legAngle);
		rightLegJoint = joinBodyParts(torso, rightLeg, new Vector2(0, -2), legAngle);
		leftArmJoint = joinBodyParts(torso, leftArm, new Vector2(0, 2.5f), armAngle);
		rightArmJoint = joinBodyParts(torso, rightArm, new Vector2(0, 2.5f), armAngle);
		
		supportFirstPos = new Vector2(x, y + 25);
		support = createSupportBody(supportFirstPos);
		joinSupportBody(torso, support);
	}

	private DistanceJoint joinSupportBody(Body torso, Body support) {
		
		DistanceJointDef jointDef = new DistanceJointDef();
		
		jointDef.initialize(
			torso, 
			support, 
			torso.getWorldPoint(new Vector2(0, 2.5f)), 
			support.getWorldCenter());
		
		jointDef.frequencyHz = 16f;
		jointDef.dampingRatio = 0f;
		
		return (DistanceJoint)world.createJoint(jointDef);
	}

	private RevoluteJoint joinBodyParts(Body a, Body b, Vector2 anchor, float angle) {
		
		RevoluteJointDef jointDef = new RevoluteJointDef();
		
		jointDef.initialize(a, b, a.getWorldPoint(anchor));

		jointDef.enableLimit = true;
		jointDef.lowerAngle = -angle;
		jointDef.upperAngle = angle;

		jointDef.enableMotor = true;
		jointDef.maxMotorTorque = 1000000;
		
		return (RevoluteJoint)world.createJoint(jointDef);
	}

	private Body createRoundBodyPart(float x, float y, float radius) {

		CircleShape shape = new CircleShape();
		shape.setRadius(radius);

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.x = x;
		bodyDef.position.y = y;
		Body body = world.createBody(bodyDef);
		
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 10;
		
		// -1 means no body parts collide
		fixtureDef.filter.groupIndex = -1;
		fixtureDef.filter.categoryBits = FILTER_BOY;
		fixtureDef.filter.maskBits = FILTER_STUFF | FILTER_WALL;

		// add the boxPoly shape as a fixture
		body.createFixture(fixtureDef);
		shape.dispose();
		
		return body;
	}

	private Body createRectangleBodyPart(float x, float y, float width, float height, float angle) {
		
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(width, height);
		
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.y = y;
		bodyDef.position.x = x;
		bodyDef.angle = angle;
		
		Body body = world.createBody(bodyDef);
		
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 10;
		
		// -1 means no body parts collide
		fixtureDef.filter.groupIndex = -1;
		fixtureDef.filter.categoryBits = FILTER_BOY;
		fixtureDef.filter.maskBits = FILTER_STUFF | FILTER_WALL;
		
		body.createFixture(fixtureDef);
		shape.dispose();
		
		return body;
	}
	
	private Body createSupportBody(Vector2 position)
	{
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(1, 1);
		
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.x = position.x;
		bodyDef.position.y = position.y;
		
		Body body = world.createBody(bodyDef);
		
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 10;
		
		// don't collide with anything
		fixtureDef.filter.categoryBits = FILTER_SUPPORT;
		fixtureDef.filter.maskBits = FILTER_WALL;
		
		body.createFixture(fixtureDef);
		shape.dispose();
		
		return body;
	}

	private void createCircles() {
		// next we add a few more circles
		CircleShape circleShape = new CircleShape();
		circleShape.setRadius(1);

		for (int i = 0; i < 5; i++) {
			BodyDef circleBodyDef = new BodyDef();
			circleBodyDef.type = BodyType.DynamicBody;
			circleBodyDef.position.x = -24 + (float)(Math.random() * 48);
			circleBodyDef.position.y = 10 + (float)(Math.random() * 100);
			Body circleBody = world.createBody(circleBodyDef);

			FixtureDef fixtureDef = new FixtureDef();
			fixtureDef.shape = circleShape;
			fixtureDef.density = 10;
			
			fixtureDef.filter.categoryBits = FILTER_STUFF;
			fixtureDef.filter.maskBits = FILTER_STUFF | FILTER_BOY | FILTER_WALL;
			
			// add the boxPoly shape as a fixture
			circleBody.createFixture(fixtureDef);
		}
		circleShape.dispose();
	}

	private void createBoxes() {
		// next we create 50 boxes at random locations above the ground
		// body. First we create a nice polygon representing a box 2 meters
		// wide and high.
		PolygonShape boxPoly = new PolygonShape();
		boxPoly.setAsBox(1, 1);

		// next we create the 50 box bodies using the PolygonShape we just
		// defined. This process is similar to the one we used for the ground
		// body. Note that we reuse the polygon for each body fixture.
		for (int i = 0; i < 5; i++) {
			// Create the BodyDef, set a random position above the
			// ground and create a new body
			BodyDef boxBodyDef = new BodyDef();
			boxBodyDef.type = BodyType.DynamicBody;
			boxBodyDef.position.x = -24 + (float)(Math.random() * 48);
			boxBodyDef.position.y = 10 + (float)(Math.random() * 100);
			Body boxBody = world.createBody(boxBodyDef);

			FixtureDef fixtureDef = new FixtureDef();
			fixtureDef.shape = boxPoly;
			fixtureDef.density = 10;

			fixtureDef.filter.categoryBits = FILTER_STUFF;
			fixtureDef.filter.maskBits = FILTER_STUFF | FILTER_BOY | FILTER_WALL;
			
			// add the boxPoly shape as a fixture
			boxBody.createFixture(fixtureDef);
		}

		// we are done, all that's left is disposing the boxPoly
		boxPoly.dispose();
	}

	private Body createWall(float width, float height, float xOffset) {
		// next we create a static ground platform. This platform
		// is not moveable and will not react to any influences from
		// outside. It will however influence other bodies. First we
		// create a PolygonShape that holds the form of the platform.
		// it will be 100 meters wide and 2 meters high, centered
		// around the origin
		PolygonShape groundPoly = new PolygonShape();
		groundPoly.setAsBox(width, height);

		// next we create the body for the ground platform. It's
		// simply a static body.
		BodyDef groundBodyDef = new BodyDef();
		groundBodyDef.type = BodyType.StaticBody;
		groundBodyDef.position.x = xOffset;
		
		Body body = world.createBody(groundBodyDef);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = groundPoly;
		fixtureDef.density = 10;
		
		fixtureDef.filter.categoryBits = FILTER_WALL;
		fixtureDef.filter.maskBits = FILTER_BOY | FILTER_STUFF | FILTER_SUPPORT;
		
		body.createFixture(fixtureDef);
		groundPoly.dispose();
		
		return body;
	}

	float maxSpeed = 2;
	float motorSpeed = 1;
	int motorDirection = 1;
	
	/** temp vector **/
	protected Vector2 tmp = new Vector2();

	@Override public void render () {
		
		float delta = Gdx.app.getGraphics().getDeltaTime();
		
		// update the world with a fixed time step
		world.step(delta, 8, 3);

		// clear the screen and setup the projection matrix
		GL10 gl = Gdx.app.getGraphics().getGL10();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		camera.setMatrices();
		
		if (motorSpeed > maxSpeed)
			motorDirection = -1;
		
		if (motorSpeed < -maxSpeed)
			motorDirection = 1;
		
		motorSpeed += delta * motorDirection * 2;

		leftArmJoint.setMotorSpeed(motorSpeed);
		rightArmJoint.setMotorSpeed(-motorSpeed);
		leftLegJoint.setMotorSpeed(-motorSpeed);
		rightLegJoint.setMotorSpeed(motorSpeed);
		
		// ensure the support block stays on the same y plane, but follows the head.
		support.setTransform(new Vector2(torso.getPosition().x, supportFirstPos.y), 0);

		// render the world using the debug renderer
		renderer.render(world);			
	}

	@Override public void create () {
		// setup the camera. In Box2D we operate on a
		// meter scale, pixels won't do it. So we use
		// an orthographic camera with a viewport of
		// 48 meters in width and 32 meters in height.
		// We also position the camera so that it
		// looks at (0,16) (that's where the middle of the
		// screen will be located).
		camera = new OrthographicCamera();
		camera.setViewport(48, 32);
		camera.getPosition().set(0, 15, 0);

		// create the debug renderer
		renderer = new Box2DDebugRenderer();

		// create the world
		world = new World(gravity, true);

		// we also need an invisible zero size ground body
		// to which we can connect the mouse joint
		BodyDef bodyDef = new BodyDef();
		groundBody = world.createBody(bodyDef);

		// call abstract method to populate the world
		createWorld();
		
		Gdx.input.setInputProcessor(this);
	}

	@Override public void dispose () {		
		renderer.dispose();
		world.dispose();

		renderer = null;
		world = null;
		mouseJoint = null;
		hitBody = null;
	}

	@Override public boolean keyDown (int keycode) {
		return false;
	}

	@Override public boolean keyTyped (char character) {
		return false;
	}

	@Override public boolean keyUp (int keycode) {
		return false;
	}

	/** we instantiate this vector and the callback here so we don't irritate the GC **/
	Vector2 testPoint = new Vector2();
	QueryCallback callback = new QueryCallback() {
		@Override public boolean reportFixture (Fixture fixture) {
			// if the hit point is inside the fixture of the body
			// we report it
			if (fixture.testPoint(testPoint)) {
				hitBody = fixture.getBody();
				return false;
			} else
				return true;
		}
	};

	@Override public boolean touchDown (int x, int y, int pointer) {
		// translate the mouse coordinates to world coordinates
		camera.getScreenToWorld(x, y, testPoint);
		// ask the world which bodies are within the given
		// bounding box around the mouse pointer
		hitBody = null;
		world.QueryAABB(callback, testPoint.x - 0.0001f, testPoint.y - 0.0001f, testPoint.x + 0.0001f, testPoint.y + 0.0001f);

		if (hitBody == groundBody) hitBody = null;

		// ignore kinematic bodies, they don't work with the mouse joint
		if (hitBody != null && hitBody.getType() == BodyType.KinematicBody) return false;

		// if we hit something we create a new mouse joint
		// and attach it to the hit body.
		if (hitBody != null) {
			MouseJointDef def = new MouseJointDef();
			def.bodyA = groundBody;
			def.bodyB = hitBody;
			def.collideConnected = true;
			def.target.set(testPoint);
			def.maxForce = 1000.0f * hitBody.getMass();

			mouseJoint = (MouseJoint)world.createJoint(def);
			hitBody.setAwake(true);
		}

		return false;
	}

	/** another temporary vector **/
	Vector2 target = new Vector2();

	@Override public boolean touchDragged (int x, int y, int pointer) {
		// if a mouse joint exists we simply update
		// the target of the joint based on the new
		// mouse coordinates
		if (mouseJoint != null) {
			camera.getScreenToWorld(x, y, target);
			mouseJoint.setTarget(target);
		}
		return false;
	}

	@Override public boolean touchUp (int x, int y, int pointer) {
		// if a mouse joint exists we simply destroy it
		if (mouseJoint != null) {
			world.destroyJoint(mouseJoint);
			mouseJoint = null;
		}
		return false;
	}
	
	public void pause() {
		
	}
	
	public void resume() {
		
	}
	
	public void resize(int width, int height) {
		
	}
}
