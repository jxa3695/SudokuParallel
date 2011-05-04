/**
 * Class to hold the puzzle data structure and methods to operate on it
 */
public class SudokuPuzzle {

	private Cell[][] _puzzle;
	
	/**
	 * Default constructor
	 */
	public SudokuPuzzle() {
		_puzzle = new Cell[9][9];
	}
	
	/**
	 * Get a specific cell
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public Cell getCell(int x, int y) {
		return _puzzle[x][y];
	}
	
	/**
	 * Function to get the specified row
	 * 
	 * @param row
	 * @return
	 */
	public Cell[] getRow(int row) {
		return _puzzle[row];
	}
	
	/**
	 * Function to get the specified column
	 * 
	 * @param col
	 * @return
	 */
	public Cell[] getCol(int col) {
		//TODO: return the actual column
		return _puzzle[col];
	}
	
	/**
	 * Solve the puzzle
	 */
	public void solve() {
		
	}
	
	/**
	 * Print the contents of the puzzle
	 */
	public void printPuzzle() {
		
	}
	
}
