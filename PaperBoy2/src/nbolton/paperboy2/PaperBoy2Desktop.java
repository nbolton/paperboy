package nbolton.paperboy2;

import com.badlogic.gdx.backends.jogl.JoglApplication;

public class PaperBoy2Desktop {
	public static void main (String[] argv) {
		new JoglApplication(new Game(), "Paper Boy", 320, 480, false);
	}
}
