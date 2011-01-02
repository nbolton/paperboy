package nbolton.paperboy2;

import com.badlogic.gdx.Gdx;

public class PaperBoy2 extends StickManDemo1 {
	
	@Override
	public void create() {
		super.create();
		
		Gdx.input.setInputProcessor(this);
	}
}
