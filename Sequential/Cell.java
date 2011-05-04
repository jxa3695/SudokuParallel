/**
 * Object representation of a cell in the Sudoku puzzle
 */

import java.util.ArrayList;

public class Cell {
	
	/** The array of possible answers for this cell */
	private ArrayList< Integer > hints = new ArrayList< Integer >();
	
	private int
		answer, // the cell's  answer
		x, y, // cell's global coordinate
		myPos; // representation of which 3x3 matrix it is
	
	
	/** Constructor **/
	public Cell(int x, int y){
		this.x = x;
		this.y = y;
		this.myPos = ( ( x / 3 ) * 3 ) + ( y / 3 );
	}
	
	/**
	 * Access the answer at this cell
	 * 
	 * @return the cell answer
	 */
	public int getAnswer() {
		return answer;
	}

	/**
	 * Modify this cell's answer
	 * 
	 * @param answer the new answer to be used
	 */
	public void setAnswer( int answer ) {
		this.answer = answer;
	}

	/**
	 * Return current hints for this cell
	 * 
	 * @return list of possible answers for this cell
	 */
	public ArrayList<Integer> getHints() {
		return hints;
	}
	
	/**
	 * Remove a number from this cell's possible list of value
	 * 
	 * @param the number that is not possible for this cell to be
	 */
	public void removeHint(int num){
		hints.remove( hints.indexOf(num) );
		if (hints.size() == 1)
			setAnswer(hints.get(0));
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

}
