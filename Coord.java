public class Coord{
	public double x;
	public double y;

	/**
	 * Constructor for the Coord class
	 **/
	public Coord() {
		this.x = 0;
		this.y = 0;
	}

	/**
	 * Constructor for the Coord class
	 * @param x the x coordinate to add
	 * @param y the y coordinate to add
	 **/
	public Coord(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Adds a coordinate to this coordinate
	 * @param other the other coordinate to add
	 **/
	public void add(Coord other) {
		this.x += other.x;
		this.y += other.y;
	}

	/**
	 * Adds an x and y value to this coordinate
	 * @param x the x coordinate to add
	 * @param y the y coordinate to add
	 **/
	public void add(double x, double y) {
		this.x += x; 
		this.y += y;
	}

	/**
	 * Adds a coordinate from another coordinate and returns the result
	 * @param target the coord to add to
	 * @param other the coord to add
	 **/
	public static Coord add(Coord target, Coord other) {
		return new Coord(target.x+other.x, target.y+other.y);
	}

	/**
	 * Subtracts a coordinate from this coordinate
	 * @param other the other coordinate to subtract
	 **/
	public void sub(Coord other) {
		this.x -= other.x;
		this.y -= other.y;
	}

	/**
	 * Subtracts a coordinate from another coordinate and returns the result
	 * @param target the coord to subtract from
	 * @param other the coord to subtract
	 **/
	public static Coord sub(Coord target, Coord other) {
		return new Coord(target.x-other.x, target.y-other.y);
	}

	/**
	 * Multiplies the coordinate by a factor of d
	 * @param d the number to multiply by
	 **/
	public void multiply(double d) {
		this.x *= d;
		this.y *= d;
	}

	/**
	 * Sets the coordinate to the specified x and y
	 * @param x the new x coordinate
	 * @param y the new y coordinate
	 **/
	public void set(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public void normalize(){
		double len = Math.sqrt(x*x + y*y);
		if(len != 0){
			x /= len;
			y /= len;
		}
	}

	public void limit(double max){
		double len = Math.sqrt(x*x + y*y);
		if(max < len){
			normalize();
			multiply(max);
		}
	}

	public double heading() {
		return Math.atan2(x, y);
	}

	public String toString() {
		return x+" "+y;
	}

}
