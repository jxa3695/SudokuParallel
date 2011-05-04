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
	 * Constructor to initialize with values
	 * 
	 * @param values
	 */
	public SudokuPuzzle(int[][] values) {
		
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
		Cell[] column = new Cell[9];
		for (int x=0; x < 9; x++) {
			column[x] = _puzzle[x][col];
		}
		return column;
	}

	/**
	 * 
	 * @param quad
	 * @return
	 */
	public Cell[] getQuadrant(int quad) {
		return new Cell[9];
	}
	
	/**
	 * Solve the puzzle
	 */
	public void solve() {
		
		// hint generation
		for( int i = 0; i < _puzzle.length; i++ ) {
			for( int j = 0; j < _puzzle[ 0 ].length; j++ ) {
				if( _puzzle[ i ][ j ].getAnswer() == 0 ) {
					Cell[] row = getRow( _puzzle[ i ][ j ].getX() );
					Cell[] col = getCol( _puzzle[ i ][ j ].getY() );
					Cell[] qad = getQuadrant( _puzzle[ i ][ j ].getPos() );
					for( int k = 0; k < row.length; k++ ) {
						_puzzle[ i ][ j ].removeHint( row[ k ].getAnswer() );
						_puzzle[ i ][ j ].removeHint( col[ k ].getAnswer() );
						_puzzle[ i ][ j ].removeHint( qad[ k ].getAnswer() );
					}
				}
			}
		}
	}
	
	/**
	 * Print the contents of the puzzle
	 */
	public void printPuzzle() {
		
	}
	
}
