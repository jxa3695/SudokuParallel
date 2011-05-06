
public class SequentialSudoku {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SudokuPuzzle puzzle = new SudokuPuzzle("hard.txt", 9);
		puzzle.printPuzzle();
		long start = System.currentTimeMillis();
		puzzle.solveSeq();
		long stop = System.currentTimeMillis();
		
		System.out.println((stop - start) + " msec");
	}

}
