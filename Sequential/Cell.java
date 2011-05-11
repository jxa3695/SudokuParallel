/**
 * Object representation of a cell in the Sudoku puzzle
 */

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class Cell implements Serializable {

	/** The array of possible values for this cell */
	private ArrayList<Integer> hints;

	private int value, // the cell's value
			x, y, // cell's global coordinate
			myPos; // representation of which SudokuPuzzle.sqrtN x
					// SudokuPuzzle.sqrtN matrix it is

	private ArrayList<Integer> tempHints;
	private int tempValue;

	/**
	 * Cell constructor
	 * 
	 * @param x
	 * @param y
	 * @param value
	 */
	public Cell(int x, int y, int value) {
		this.x = x;
		this.y = y;
		this.myPos = ((x / SudokuPuzzle.sqrtN) * SudokuPuzzle.sqrtN)
				+ (y / SudokuPuzzle.sqrtN);
		hints = new ArrayList<Integer>(SudokuPuzzle.N);

		tempHints = new ArrayList<Integer>(SudokuPuzzle.N); // used in the brute
															// force method

		if (value == 0) {
			for (int i = 1; i <= SudokuPuzzle.N; i++) {
				hints.add(i);
				tempHints.add(i);
			}
		} else {
			setValue(value);
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
	 * @param value
	 *            the new value to be used
	 */
	public void setValue(int value) {
		this.value = value;
		this.hints.clear();
		this.tempHints.clear();

		// TODO: figure a way to tell if it is being run in parallel
		SudokuPuzzle.count--;
		SudokuPuzzle.sharedCount.decrementAndGet();
	}

	/**
	 * Find the next possible temp value in the temp hints list and remove if
	 * there is a possible one
	 * 
	 * @return false if no higher valid values exist, true if one does
	 */
	public boolean nextTempValue(boolean parallel) {
		tempValue++;
		while (!tempHints.contains(tempValue)) {
			tempValue++;
			if (tempValue > SudokuPuzzle.N) {
				break;
			}
		}
		if (tempValue <= SudokuPuzzle.N) {
			tempHints.remove(tempHints.indexOf(tempValue));
			if (parallel) {
				SudokuPuzzle.sharedCount.decrementAndGet();
			} else {
				SudokuPuzzle.count--;
			}
			return true;
		} else {
			tempValue = 0;
			if (parallel) {
				SudokuPuzzle.sharedCount.incrementAndGet();
			} else {
				SudokuPuzzle.count++;
			}
			return false;
		}
	}

	/**
	 * Removes a temp hint from the tempHint List
	 * 
	 * @param num
	 */
	public void removeTempHint(int num) {
		if (tempHints.contains(num)) {
			tempHints.remove(tempHints.indexOf(num));
		}
	}

	/**
	 * Adds a temp hint to the tempHint list
	 * 
	 * @param num
	 */
	public void addTempHint(int num) {
		if (!tempHints.contains(num)) {
			tempHints.add(num);
		}
	}

	/**
	 * Refills the tempHint list with numbers 1-N
	 */
	public void resetTempHints() {
		for (int x = 1; x <= SudokuPuzzle.N; x++) {
			addTempHint(x);
		}
	}

	public ArrayList<Integer> getTempHints() {
		return tempHints;
	}

	public int getTempValue() {
		return tempValue;
	}

	public void setTempValue(int value) {
		tempValue = value;
	}

	/**
	 * @return true - if the cell has not been solved yet
	 */
	public boolean isEmpty() {
		return value == 0;
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
	 * @param the
	 *            number that is not possible for this cell to be
	 * @return whether or not the number was removed
	 */
	public boolean removeHint(int num) {
		boolean removed = false;
		if (hints.contains(num)) {
			hints.remove(hints.indexOf(num));
			tempHints.remove(tempHints.indexOf(num));
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
	 * @param an
	 *            array of numbers that is not possible for this cell to be
	 */
	public void removeHints(int[] nums) {
		int numCount = nums.length;
		for (int i = 0; i < numCount; i++)
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
	public String toString() {
		return "(" + this.x + ", " + this.y + ") = " + this.value;
	}

}
