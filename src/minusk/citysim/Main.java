package minusk.citysim;

import minusk.citysim.world.Map;
import minusk.render.core.Game;
import minusk.render.core.Input;

public class Main extends Game {
	public static final Main game = new Main();
	
	private Map map;
	
	public Main() {
		super(1280, 720, "City Simulator Thing", 8);
	}

	@Override
	public void update() {
		if (window.input.closeRequested())
			endLoop();
		map.update();
	}

	@Override
	public void render() {
		map.render();
	}

	@Override
	protected void initialize() {
		map = new Map();
		map.init();
	}

	@Override
	protected void cleanUp() {
		
	}
	
	public Input getInput() {
		return window.input;
	}
	
	public Map getMap() {
		return map;
	}
	
	public int getResolutionX() {
		return window.input.getFramebufferWidth();
	}
	
	public int getResolutionY() {
		return window.input.getFramebufferHeight();
	}
	
	public static void main(String[] args) {
		game.gameloop(60,10);
	}
}
