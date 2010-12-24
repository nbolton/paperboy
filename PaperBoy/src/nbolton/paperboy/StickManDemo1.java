package nbolton.paperboy;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.primitive.Line;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.shape.Shape;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.ui.activity.BaseGameActivity;

import android.hardware.SensorManager;
import android.util.Log;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class StickManDemo1 extends BaseGameActivity {

	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;

	private static final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
	
	PhysicsWorld physicsWorld;
	Line leftLeg;
	Line rightLeg;
	Line torso;
	
	@Override
	public void onLoadComplete() {
	}

	@Override
	public Engine onLoadEngine() {
		
		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		
		return new Engine(
			new EngineOptions(true, ScreenOrientation.LANDSCAPE, 
				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera));
	}
	
	@Override
	public void onLoadResources() {
		
	}

	private Vector2 touchDown;
	private Vector2 firstLegPos;
	
	@Override
	public Scene onLoadScene() {
		
		mEngine.registerUpdateHandler(new FPSLogger());
		physicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);
		
		Scene scene = new Scene(1)
		{
			@Override
			public boolean onSceneTouchEvent(TouchEvent event) {
				
				if (event.getAction() == TouchEvent.ACTION_DOWN)
				{
					touchDown = new Vector2(event.getX(), event.getY());
					firstLegPos = new Vector2(rightLeg.getX2(), rightLeg.getY2());
				}
				
				Vector2 touchDelta = new Vector2(
					touchDown.x - event.getX(),
					touchDown.y - event.getY());
				
				rightLeg.setPosition(
					rightLeg.getX1(),
					rightLeg.getY1(),
					firstLegPos.x - touchDelta.x,
					firstLegPos.y - touchDelta.y);
				
				Log.d("PaperBoy", "touch delta: " + touchDelta.x + "," + touchDelta.y);
				return true;
			}
		};
		
		final Shape ground = new Rectangle(0, CAMERA_HEIGHT - 2, CAMERA_WIDTH, 2);
		final Shape roof = new Rectangle(0, 0, CAMERA_WIDTH, 2);
		final Shape left = new Rectangle(0, 0, 2, CAMERA_HEIGHT);
		final Shape right = new Rectangle(CAMERA_WIDTH - 2, 0, 2, CAMERA_HEIGHT);
		
		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
		PhysicsFactory.createBoxBody(physicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(physicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(physicsWorld, left, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(physicsWorld, right, BodyType.StaticBody, wallFixtureDef);
		
		scene.getBottomLayer().addEntity(ground);
		scene.getBottomLayer().addEntity(roof);
		scene.getBottomLayer().addEntity(left);
		scene.getBottomLayer().addEntity(right);
		
		scene.setBackground(new ColorBackground(1, 1, 1));
		scene.registerUpdateHandler(physicsWorld);

		leftLeg = createLeftLeg(100, 200);
		rightLeg = createRightLeg(100, 200);
		torso = createTorso(100, 100);

		addBodyPart(scene, leftLeg);
		addBodyPart(scene, rightLeg);
		addBodyPart(scene, torso);
		
		Rectangle ballRect = new Rectangle(100, 100, 20, 20);
		Body ballBody = PhysicsFactory.createCircleBody(physicsWorld, ballRect, BodyType.DynamicBody, FIXTURE_DEF);
		
		scene.getTopLayer().addEntity(ballRect);
		physicsWorld.registerPhysicsConnector(new PhysicsConnector(ballRect, ballBody, true, true, false, false));
		
		return scene;
	}
	
	private Line createLeftLeg(float x, float y)
	{
		Line line = new Line(x, y, x - 60, y + 100);
		line.setColor(0.1f, 0.1f, 0.1f);
		return line;
	}
	
	private Line createRightLeg(float x, float y)
	{
		Line line = new Line(x, y, x + 60, y + 100);
		line.setColor(0.1f, 0.1f, 0.1f);
		return line;
	}
	
	private Line createTorso(float x, float y)
	{
		Line line = new Line(x, y, x, y + 100);
		line.setColor(0.1f, 0.1f, 0.1f);
		return line;
	}
	
	private void addBodyPart(Scene scene, Line line)
	{
		scene.getTopLayer().addEntity(line);
		//Body body = PhysicsFactory.createLineBody(physicsWorld, line, FIXTURE_DEF);
		//physicsWorld.registerPhysicsConnector(new PhysicsConnector(line, body, true, true, false, false));
	}
}
