package engine.physics.util;

public class Vector2D {
	
	private float x, y;
	
	public Vector2D() {this.x = 0; this.y = 0;}
	public Vector2D(float x, float y) {this.x = x; this.y = y;}
	
	public float dot(Vector2D other) {
		return x*other.getX() + y*other.getY();
	}
	
	public Vector2D getDifference(Vector2D other) {
		return new Vector2D(this.x - other.getX(), this.y - other.getY());
	}
	
	public Vector2D getNormal() {
		float hypot = x*x + y*y;
		if (hypot == 0) {return new Vector2D();}
		float overH = 1 / (float)Math.sqrt(hypot);
		return new Vector2D(x*overH, y*overH);
	}
	
	public void scale(float n) {this.x *= n; this.y *= n;}
	
	public void add(Vector2D other) {
		this.x += other.getX(); this.y += other.getY();
	}
	
	public float getSqrX() {return this.x * this.x;}
	public float getSqrY() {return this.y * this.y;}
	
	public float getX() {return this.x;}
	public float getY() {return this.y;}
	
	public void setX(float x) {this.x = x;}
	public void setY(float y) {this.y = y;}
	
	public void addX(float x) {this.x += x;}
	public void addY(float y) {this.y += y;}
	
	public void sclX(float x) {this.x *= x;}
	public void sclY(float y) {this.y *= y;}
	
	public Vector2D copy() {return new Vector2D(x, y);}
	
	@Override
	public String toString() {
		return "x = " + this.x + ", y = " + this.y;
	}

}
