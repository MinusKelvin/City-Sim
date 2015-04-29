package minusk.citysim;

import minusk.citysim.world.Map;
import minusk.render.core.Game;
import minusk.render.core.Input;

public class Main extends Game {
	public static final Main game = new Main();
	
	private Map map;
	
	public Main() {
		super(1024, 576, "City Simulator Thing", 8);
	}

	@Override
	public void update() {
		if (window.input.closeRequested())
			endLoop();
	}

	@Override
	public void render() {
		map.render();
	}

	@Override
	protected void initialize() {
		map = new Map();
	}

	@Override
	protected void cleanUp() {
		
	}
	
	public Input getInput() {
		return window.input;
	}
	
	public static void main(String[] args) {
		game.gameloop(60,10);
	}
}
