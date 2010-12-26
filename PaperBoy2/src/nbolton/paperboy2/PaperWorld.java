package nbolton.paperboy2;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.World;

public class PaperWorld implements ContactListener {

	World world;
	
	public void initialize()
	{
		Vector2 gravity = new Vector2(0.0f, -1.0f);
		boolean doSleep = true;
		
		world = new World(gravity, doSleep);
		world.setContactListener(this);
		
		
	}
	
	@Override
	public void beginContact(Contact arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void endContact(Contact arg0) {
		// TODO Auto-generated method stub
		
	}

}
