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
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
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
	
	protected void createWorld() {
		
		groundBody = createWall(50, 1, 0);
		createWall(1, 50, -23);
		createWall(1, 50, 23);

		createStickManFaceForward();
	}

	private void createStickManFaceForward() {

		Body torso = createRectangleBodyPart(0, 15, 1, 3, 0);
		Body head = createRoundBodyPart(0, 20, 2);
		Body leftLeg = createRectangleBodyPart(-2, 11, 1, 4, -40 * MathUtils.degreesToRadians);
		Body rightLeg = createRectangleBodyPart(2, 11, 1, 4, 40 * MathUtils.degreesToRadians);
		Body leftArm = createRectangleBodyPart(-2, 16.5f, 1, 3, -80 * MathUtils.degreesToRadians);
		Body rightArm = createRectangleBodyPart(2, 16.5f, 1, 3, 80 * MathUtils.degreesToRadians);
		
		joinBodyParts(torso, head, new Vector2(0, 3));
		joinBodyParts(torso, leftLeg, new Vector2(0, -2));
		joinBodyParts(torso, rightLeg, new Vector2(0, -2));
		joinBodyParts(torso, leftArm, new Vector2(0, 1.5f));
		joinBodyParts(torso, rightArm, new Vector2(0, 1.5f));
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
		fixtureDef.filter.groupIndex = -1;

		// add the boxPoly shape as a fixture
		body.createFixture(fixtureDef);
		shape.dispose();
		
		return body;
	}

	private void joinBodyParts(Body a, Body b, Vector2 anchor) {
		
		RevoluteJointDef jointDef = new RevoluteJointDef();
		jointDef.initialize(a, b, a.getWorldPoint(anchor));
		jointDef.lowerAngle = -10 * MathUtils.degreesToRadians;
		jointDef.upperAngle = 10 * MathUtils.degreesToRadians;
		jointDef.enableLimit = true;
		world.createJoint(jointDef);
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
		fixtureDef.filter.groupIndex = -1;
		
		body.createFixture(fixtureDef);
		shape.dispose();
		
		return body;
	}

	/*private void createCircles(World world) {
		// next we add a few more circles
		CircleShape circleShape = new CircleShape();
		circleShape.setRadius(1);

		for (int i = 0; i < 10; i++) {
			BodyDef circleBodyDef = new BodyDef();
			circleBodyDef.type = BodyType.DynamicBody;
			circleBodyDef.position.x = -24 + (float)(Math.random() * 48);
			circleBodyDef.position.y = 10 + (float)(Math.random() * 100);
			Body circleBody = world.createBody(circleBodyDef);

			// add the boxPoly shape as a fixture
			circleBody.createFixture(circleShape, 10);
		}
		circleShape.dispose();
	}

	private void createBoxes(World world) {
		// next we create 50 boxes at random locations above the ground
		// body. First we create a nice polygon representing a box 2 meters
		// wide and high.
		PolygonShape boxPoly = new PolygonShape();
		boxPoly.setAsBox(1, 1);

		// next we create the 50 box bodies using the PolygonShape we just
		// defined. This process is similar to the one we used for the ground
		// body. Note that we reuse the polygon for each body fixture.
		for (int i = 0; i < 20; i++) {
			// Create the BodyDef, set a random position above the
			// ground and create a new body
			BodyDef boxBodyDef = new BodyDef();
			boxBodyDef.type = BodyType.DynamicBody;
			boxBodyDef.position.x = -24 + (float)(Math.random() * 48);
			boxBodyDef.position.y = 10 + (float)(Math.random() * 100);
			Body boxBody = world.createBody(boxBodyDef);

			// add the boxPoly shape as a fixture
			boxBody.createFixture(boxPoly, 10);
		}

		// we are done, all that's left is disposing the boxPoly
		boxPoly.dispose();
	}*/

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

		// finally we add a fixture to the body using the polygon
		// defined above. Note that we have to dispose PolygonShapes
		// and CircleShapes once they are no longer used. This is the
		// only time you have to care explicitely for memomry managment.
		body.createFixture(groundPoly, 10);
		groundPoly.dispose();
		
		return body;
	}

	/** temp vector **/
	protected Vector2 tmp = new Vector2();

	@Override public void render () {
		// update the world with a fixed time step
		world.step(Gdx.app.getGraphics().getDeltaTime(), 8, 3);

		// clear the screen and setup the projection matrix
		GL10 gl = Gdx.app.getGraphics().getGL10();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		camera.setMatrices();

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
		world = new World(new Vector2(0, -10), true);

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
