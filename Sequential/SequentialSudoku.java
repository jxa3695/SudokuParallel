
public class SequentialSudoku {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SudokuPuzzle puzzle = new SudokuPuzzle("hardest.txt");
		puzzle.printPuzzle();
		long start = System.currentTimeMillis();
		puzzle.solve();
		long stop = System.currentTimeMillis();
		
		System.out.println((stop - start) + " msec");
	}

}
