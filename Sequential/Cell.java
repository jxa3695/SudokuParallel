import java.util.ArrayList;

/**
 * Object representation of a cell in the Sudoku puzzle
 */

public class Cell {

	/** This cell's value */
	private int value;

	/** The array of possible values for this cell */
	private ArrayList< Integer > hints = new ArrayList< Integer >();
	
	private int myX, myY, myPos;

	/**
	 * Access the value at this cell
	 * 
	 * @return the cell value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Modify this cell's value
	 * 
	 * @param value the new value to be used
	 */
	public void setValue( int value ) {
		this.value = value;
	}

	/**
	 * Current hints for this cell
	 * 
	 * @return list of possible values for this cell
	 */
	public ArrayList<Integer> getHints() {
		return hints;
	}
	
	/**
	 * 
	 * @TODO stuff
	 * @param x
	 * @param y
	 */
	public void setCoordinates( int x, int y ) {
		this.myX = x;
		this.myY = y;
		
		this.myPos = ( ( x / 3 ) * 3 ) + ( y / 3 );
	}

	/**
	 * @return the myX
	 */
	public int getX() {
		return myX;
	}

	/**
	 * @return the myY
	 */
	public int getY() {
		return myY;
	}

	/**
	 * @return the myPos
	 */
	public int getPos() {
		return myPos;
	}

}
