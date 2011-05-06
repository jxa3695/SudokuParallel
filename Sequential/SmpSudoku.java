import edu.rit.pj.Comm;


public class SmpSudoku {
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Comm.init(args);
		
		if (args.length != 2) {
			System.out.println("Usage: java SmpSudoku N infile");
			System.exit(0);
		}
		
		int n = Integer.parseInt(args[0]);
		String filename = args[1];
		
		SudokuPuzzle puzzle = new SudokuPuzzle(filename, n);
		puzzle.printPuzzle();
		long start = System.currentTimeMillis();
		puzzle.solveSmp();
		long stop = System.currentTimeMillis();
		
		System.out.println((stop - start) + " msec");
	}
}
