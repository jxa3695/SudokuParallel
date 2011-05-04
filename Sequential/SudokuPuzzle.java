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
	
	
	
	
	/* Row and Column checker */
	// collect any hints that is not shared by any other.
	// if hint is not shared by any other, it is the answer/Value for its owner.
	void RCChecker( Cell [] col ){
		// index of these array represent the hint with the value of index+1
		int [] hintCounter = new int[9]; // counter for hint
		Cell [] owner = new Cell[9]; // first encountered ownere for hint

		for (int cell=0; cell<9; cell++){ //iterate through the cells in col
			if ( col[i].getAnswer()>0 ) next; // skip cells with answer set already

			int [] hints = col[i].getHints(); // get the list of hints in 

			for(int j =0; j<hints.length; j++){ 
				hintVal = hints[j] - 1;
				if( owner[hintVal] == 0 ) owner[hintVal] = cell ; // record a first seen hints owner
				hintCounter[ hintVal ]++ ;  // for every hint seen increment its counter

			}
		}
		
		// go through hintCounter for any singlely owned hint(s)
		for (int i=0; i<9; i++){
			if (hintCounter[i] == 1) {
				cell[i].setvalue( i );
				//debugger
				System.out.println(cell[i].toString())
			}
		}
	}
	
}
