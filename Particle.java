import java.awt.*;
import java.util.Random;

public class Particle{

  	double initForce = 200;
	double initSpeed = 200;
	double maxForce;
	double maxSpeed;
	double impForce = 20;
	double impSpeed = 20;
	Coord force;
	Coord acceleration;
	Coord velocity;
	Coord position;
	boolean seek = true;
	boolean wander = false;
	boolean orbit = false;
	double wanderTheta = 0;

	Color color;
	int size;


	public Particle() {
		this.force = new Coord();
		this.acceleration = new Coord();
		this.velocity = new Coord();
		this.position = new Coord();

		color = new Color(0,0,0);
		size = 20;
		Random rand = new Random();
		setForceVariation();
		setSpeedVariation();
	}

	public Particle(int x, int y) {
		this.force = new Coord();
		this.acceleration = new Coord();
		this.velocity = new Coord();
		this.position = new Coord(x, y);

		color = new Color(0,0,0);
		size = 20;

		setForceVariation();
		setSpeedVariation();
	}

	public Particle setForceVariation() {
	  	Random rand = new Random();
		maxForce = initForce + (initForce*rand.nextDouble()*impForce+100)/100;
		return this;
	}

	public Particle setSpeedVariation() {
	  	Random rand = new Random();
		maxSpeed = initSpeed + (initSpeed*rand.nextDouble()*impSpeed+100)/100;
		return this;
	}

	public Particle setInitForce(double f) {
		initForce = f;
		maxForce = initForce + impForce;
		return this;
	}
	
	public Particle setInitSpeed(double f) {
		initSpeed = f;
		maxSpeed = initSpeed + impSpeed;
		return this;
	}

	/**
	 * Makes sure the values are appropriate for color values
	 * @param r the red component
	 * @param g the green component
	 * @param b the blue component
	 * @return the validated color
	 **/
	public Color validateColor(int r, int g, int b) {
		int red = color.getRed() + r;
		int green = color.getGreen() + g;
		int blue = color.getBlue() + b;

		if (red > 255) {
		  	red = 255;
		} else if (red < 0) {
			red = 0;
		}
		if (green > 255) {
		  	green = 255;
		} else if (green < 0) {
			green = 0;
		}
		if (blue > 255) {
		  	blue = 255;
		} else if (blue < 0) {
			blue = 0;
		}

		return new Color(r, g, b);
	}
	
	/**
	 * Sets the color of the particle by RGB
	 * @param r the red component
	 * @param g the green component
	 * @param b the blue component
	 **/
	public void setColor(int r, int g, int b) {
		this.color = validateColor(r, g, b);
	}

	/**
	 * Sets the color of the particle by Color
	 * @param c the color to set
	 **/
	public void setColor(Color c) {
		this.color = c;
	}

	/**
	 * Adds a value to the particle color.
	 * This method accepts negative values
	 * @param r the red value to add
	 * @param g the green value to add
	 * @param b the blue component to add
	 **/
	public void addColor(int r, int g, int b) {
	  	Color newCol = validateColor(r, g, b);
		int nr = newCol.getRed() + color.getRed();
		int ng = newCol.getGreen() + color.getGreen();
		int nb = newCol.getBlue() + color.getBlue();
		setColor(nr, ng, nb);
	}
	
	/**
	 * Caps the position to the provided values
	 * @param x the maximum x value
	 * @param y the maximum y value
	 **/
	public void inBounds(double x, double y) {
	  	int damp = 4;
		if (this.position.x < 0) {
			this.position.x = 0;
			this.velocity.set(-this.velocity.x/damp, this.velocity.y);
		}
		if (this.position.y < 0) {
			this.position.y = 0;
			this.velocity.set(this.velocity.x, -this.velocity.y/damp);
		}
		if (this.position.x > x) {
			this.position.x = x;
			this.velocity.set(-this.velocity.x/damp, this.velocity.y);
		}
		if (this.position.y > y) {
			this.position.y = y;
			this.velocity.set(this.velocity.x, -this.velocity.y/damp);
		}
	}


	//does the old point mass physics
	//dt is the change in time -- it should be in seconds! ie a fraction of a second
	public void update(double dt){
        acceleration.add(force);
        acceleration.multiply(dt);
        velocity.add(acceleration);
        position.add(velocity.x*dt,velocity.y*dt);
        force.set(0,0);
        acceleration.set(0,0);
	}

	public void seek(Coord target){
		Coord end = Coord.sub(target, position);
		end.normalize();
		end.multiply(maxForce);
		end.sub(velocity);
		end.limit(maxSpeed);
		force.add(end);
	}

	/**
	 * Opposite of seek
	 * @param target point to flee from
	 **/
	public void flee(Coord target) {
		Coord end = Coord.sub(position, target);
		end.normalize();
		end.multiply(maxForce);
		end.sub(velocity);
		end.limit(maxSpeed);
		force.add(end);
	}

	public void wander() {
		wanderTheta += (Math.random()*0.2)-0.1;

		Coord circle = new Coord(velocity.x, velocity.y);
		circle.normalize();
		circle.multiply(size);
		circle.add(position);

		double heading = Math.sqrt(velocity.x*velocity.x + velocity.y*velocity.y);

		Coord pointOnCircle = new Coord((10*size)*Math.cos(wanderTheta+heading),(10*size)*Math.sin(wanderTheta+heading));

		pointOnCircle.add(circle);
		seek(pointOnCircle);
	}

	/**
	 * Causes the particle to circle around the target
	 **/
	public void orbit() {
		wanderTheta += (Math.random()*0.2)-0.1;

		Coord circle = new Coord(velocity.x, velocity.y);
		circle.normalize();
		circle.multiply(800);
		circle.add(position);

		double heading = Math.sqrt(velocity.x*velocity.x + velocity.y*velocity.y);
		Coord pointOnCircle = new Coord(100*Math.cos(wanderTheta+heading),100*Math.sin(wanderTheta+heading));

		pointOnCircle.add(circle);
		seek(pointOnCircle);
	}

	/**
	 * Seeks or flees depending on particle state
	 * @param target point to move to or from
	 **/
	public void move(Coord target) {

		if (wander) {
			orbit = false;
		  	wander();
		} else if (orbit) {
		  	wander = false;
			orbit();
		}
		if (seek) {
			seek(target);
		} else {
			flee(target);
		}
	}

	public String toString() {
		String result = "PARTICLE\n";
		result += position+"\n";
		result += force+"\n";
		result += acceleration+"\n";
		result += velocity+"\n";
		result += color.getRed()+" "+color.getGreen()+" "+ color.getBlue()+" "+color.getAlpha()+"\n";
		result += size+"\n";
		result += maxForce+"\n";
		result += maxSpeed+"\n";
		result += impForce+"\n";
		result += impSpeed+"\n";
		result += initForce+"\n";
		result += initSpeed+"\n";
		result += seek+"\n";
		result += wander+"\n";
		result += orbit+"\n";
		return result;
	}


}
