package orbit;

import java.util.*;

public class World
{
	public final int WORLD_SIZE = 6000;
	public final double MAX_SHIP_SPEED = 1000;

	private ArrayList<SpaceObject> spaceObjects;
	private ArrayList<SpaceObject> deadObjects;
	private ArrayList<Explosion> explosions;
	private Starfield starfield;
	private Spaceship spaceship;
	private BinaryInput binaryInput;
	private Rect viewport;
	private String explosionSprite;

	private Game game;

	public World(Game g)
	{
		game = g;
		spaceObjects=new ArrayList<SpaceObject>();
		deadObjects = new ArrayList<SpaceObject>();
		explosions = new ArrayList<Explosion>();
		//create the starfield
		starfield = new Starfield();
		//create the spaceship
		//spaceship = new Spaceship();

	}
	public void setSpaceship(Spaceship ship)
	{
		spaceship=ship;
	}
	public void setViewport(Rect view)
	{
		viewport=view;
	}
	public void setBinaryInput(BinaryInput binIn)
	{
		binaryInput=binIn;
	}
	public ArrayList<SpaceObject> getSpaceObjects()
	{
		return spaceObjects;
	}
	
	public ArrayList<Explosion> getExplosions() {
		return explosions;
	}
	
	/** Add a SpaceObject to the list
	 *
	 **/
	public void add(SpaceObject so)
	{
		spaceObjects.add(so);
	}

	public Starfield getStarfield() {
		return starfield;
	}

	/**
	 * main game loop. This updates the physics of the objects
	 */
	public void update(long timeElapsed) {

		//update the starfield
		starfield.update(timeElapsed,spaceship.getVel());

		//apply thrusters
		if(spaceship!=null)
		{
			spaceship.setThrusting(binaryInput.getButtonState()==1);

			//System.out.println(spaceship.getPos()+" , "+viewport);
		}

		for (SpaceObject obj : spaceObjects) {
			obj.update(timeElapsed);
			Vector2 pos = obj.getPos();
			
			if (pos.x < 0)pos.x = WORLD_SIZE;
			if (pos.y < 0) pos.y = WORLD_SIZE;
			if (pos.x >= WORLD_SIZE) pos.x = 0;
			if (pos.y >= WORLD_SIZE) pos.y = 0;
			
			obj.setPos(pos);

			if (obj instanceof Planet) {
				//check if the planet is within range
				Planet p = (Planet) obj;
				double dist = p.getPos().subVector(spaceship.getPos()).getLength();
				if (dist < 800*2) {
					spaceship.interact(p);

					//see if they collide
					if (dist < p.getRadius()) {
						//collision!
						//splode!
						Explosion e = new Explosion(spaceship.getPos(), explosionSprite, p.getWidth(), p.getHeight());
						explosions.add(e);
					}
				}
			}
		}

		//maximum speed for spaceship
		Vector2 v = spaceship.getVel();
		if (v.getLength() > MAX_SHIP_SPEED) {
			v = v.getNormalized().scale(MAX_SHIP_SPEED);
			spaceship.setVel(v);
		}

		if(viewport!=null)
			viewport.setCenter(spaceship.getPos());
		
		for(Explosion e : explosions) {
			if(e.getAlive()) {
				e.animate((int)timeElapsed);
			}
			else {
				deadObjects.add(e);
			}
		}

		//go through the garbage can
		for (SpaceObject obj : deadObjects) {
			spaceObjects.remove(obj);
		}

		if (deadObjects.size() > 0)
			deadObjects.clear();


	}
	/** Populates the world with planets and spaceship based on difficulty.
	 *
	 **/
	public void populate(int level)
	{
		//create the main ship, and add it
		Spaceship ship=new Spaceship(new Vector2(750,750),new Vector2(0,0),new Vector2(0,0),"spaceship",50,50);
		//set the ship
		setSpaceship(ship);
		add(ship);

		Random rand=new Random();
		for(int i=0;i<4;i++)
		{
			int size=rand.nextInt(3)+1;
			SpaceObject so=new Planet(new Vector2(rand.nextInt(1500),rand.nextInt(1500)),
				new Vector2(0,0),new Vector2(0,0),"planet"+(size==2?7:size),10000,50+size*1.2);
			add(so);
		}
	}
}