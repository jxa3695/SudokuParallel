import edu.rit.pj.Comm;


public class SequentialSudoku {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Comm.init(args);
		
		if (args.length != 2) {
			System.out.println("Usage: java SequentialSudoku N infile");
			System.exit(0);
		}
		
		int n = Integer.parseInt(args[0]);
		String filename = args[1];
		
		SudokuPuzzle puzzle = new SudokuPuzzle(filename, n);
		System.out.println("Initial Puzzle");
		puzzle.printPuzzle();
		long start = System.currentTimeMillis();
		//puzzle.solveSeq();
		puzzle.bruteForceIt();
		long stop = System.currentTimeMillis();
		if (SudokuPuzzle.count > 0) {
			System.out.println("\nState of the puzzle when the algorithm failed");
		} else {
			System.out.println("\nSolved Puzzle");
		}
		puzzle.printPuzzle();
		
		System.out.println((stop - start) + " msec");
	}

}
