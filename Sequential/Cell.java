/**
 * Object representation of a cell in the Sudoku puzzle
 */

import java.util.ArrayList;
import java.util.Arrays;

public class Cell {
	
	/** The array of possible values for this cell */
	private ArrayList< Integer > hints = new ArrayList< Integer >( 9 );
	
	private int
		value, // the cell's  value
		x, y, // cell's global coordinate
		myPos; // representation of which 3x3 matrix it is
	
	/**
	 * Cell constructor
	 * 
	 * @param x
	 * @param y
	 * @param value
	 */
	public Cell(int x, int y, int value){
		this.x = x;
		this.y = y;
		this.myPos = ( ( x / 3 ) * 3 ) + ( y / 3 );
		if (value == 0) {
			hints = new ArrayList< Integer >(Arrays.asList( 1, 2, 3, 4, 5, 6, 7, 8, 9 ));
		} else {
			setValue( value );
		}
	}
	
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
		this.hints.clear();
		SudokuPuzzle.count--;
	}

	/**
	 * Return current hints for this cell
	 * 
	 * @return list of possible values for this cell
	 */
	public ArrayList<Integer> getHints() {
		return hints;
	}
	
	/**
	 * Remove a number from this cell's possible list of value
	 * 
	 * @param the number that is not possible for this cell to be
	 * @return whether or not the number was removed
	 */
	public boolean removeHint(int num){
		boolean removed = false;
		if (hints.contains(num)) {
			hints.remove( hints.indexOf(num) );
			removed = true;
			if (hints.size() == 1) {
				setValue(hints.get(0));
			}
		}
		return removed;
	}
	
	/**
	 * Remove some numbers from this cell's possible list of value
	 * 
	 * @param an array of numbers that is not possible for this cell to be
	 */
	public void removeHints(int[] nums){
		int numCount = nums.length;
		for(int i =0; i<numCount; i++)
			removeHint(i);
	}


	/**
	 * @return the x
	 */
	public int getX() {
		return x;
	}

	/**
	 * @return the y
	 */
	public int getY() {
		return y;
	}

	/**
	 * @return the myPos
	 */
	public int getPos() {
		return myPos;
	}
	
	/**
	 * @return String representation of the Cell
	 */
	public String toString(){
		return "(" + this.x + ", " + this.y + ") = "+ this.value;
	}

}
