
public class SequentialSudoku {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if (args.length != 2) {
			System.out.println("Usage: java SequentialSudoku N infile");
			System.exit(0);
		}
		
		int n = Integer.parseInt(args[0]);
		String filename = args[1];
		
		SudokuPuzzle puzzle = new SudokuPuzzle(filename, n);
		puzzle.printPuzzle();
		long start = System.currentTimeMillis();
		puzzle.solveSeq();
		long stop = System.currentTimeMillis();
		
		System.out.println((stop - start) + " msec");
	}

}
