/**
 * Class to hold the puzzle data structure and methods to operate on it
 */
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import edu.rit.pj.BarrierAction;
import edu.rit.pj.IntegerForLoop;
import edu.rit.pj.ParallelRegion;
import edu.rit.pj.ParallelTeam;
import edu.rit.pj.reduction.SharedBoolean;

public class SudokuPuzzle {

	public static int count;
	public static int N;
	public static int sqrtN;
	private Cell[][] _puzzle;

	private static SharedBoolean sharedHintGen;
	private static SharedBoolean sharedChanged;

	/**
	 * Constructor to initialize with values from a given file
	 * 
	 * @param filename
	 */
	public SudokuPuzzle(String filename, int n) {
		N = n;
		sqrtN = (int) Math.sqrt(N);
		count = N * N;
		_puzzle = new Cell[N][N];
		try {
			Scanner scan = new Scanner(new File(filename));
			scan.useDelimiter(",");
			for (int x = 0; x < N; x++) {
				for (int y = 0; y < N; y++) {
					if (scan.hasNextInt()) {
						int num = scan.nextInt();
						_puzzle[x][y] = new Cell(x, y, num);
					}
				}
				scan.nextLine();
			}
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	/**
	 * Function to get the specified row
	 * 
	 * @param row
	 * @return
	 */
	public Cell[] getRow(int row) {
		Cell[] r = new Cell[N];
		for (int x = 0; x < N; x++) {
			r[x] = _puzzle[row][x];
		}
		return r;
	}

	/**
	 * Function to get the specified column
	 * 
	 * @param col
	 * @return
	 */
	public Cell[] getCol(int col) {
		Cell[] column = new Cell[N];
		for (int x = 0; x < N; x++) {
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
		Cell[] quadrant = new Cell[N];
		int i = quad / sqrtN;
		int j = quad % sqrtN;
		int index = 0;
		for (int x = i * sqrtN; x < i * sqrtN + sqrtN; x++) {
			for (int y = j * sqrtN; y < j * sqrtN + sqrtN; y++) {
				quadrant[index] = _puzzle[x][y];
				index++;
			}
		}
		return quadrant;
	}

	// *****************SMP Methods****************************

	public void solveSmp() throws Exception {

		// hint generation
		sharedHintGen = new SharedBoolean(true);
		sharedChanged = new SharedBoolean(false);

		int iterations = 0;

		while (true) {
			iterations++;
			while (sharedHintGen.get()) {
				sharedHintGen.set(false);
				new ParallelTeam().execute(new ParallelRegion() {
					boolean hintGen_thread;

					public void run() throws Exception {
						hintGen_thread = hintGeneratorSmp(getThreadIndex());
					}

					public void finish() {
						sharedHintGen.set(hintGen_thread || sharedHintGen.get());
					}
				});
			}

			new ParallelTeam().execute(new ParallelRegion() {

				boolean changed_thread = false;

				public void run() throws Exception {

					execute(0, N - 1, new IntegerForLoop() {

						public void run(int first, int last) {
							for (int i = first; i <= last; i++) {
								changed_thread = changed_thread
										|| rowColChecker(getRow(i));
							}
						}
					}, new BarrierAction() {

						@Override
						public void run() throws Exception {
							sharedChanged.set(changed_thread
									|| sharedChanged.get());
						}

					});
				}
			});
			if (sharedChanged.get()) {
				sharedHintGen.set(true);
				continue;
			}
			new ParallelTeam().execute(new ParallelRegion() {

				boolean changed_thread = false;

				public void run() throws Exception {

					execute(0, N - 1, new IntegerForLoop() {

						public void run(int first, int last) {
							for (int i = first; i <= last; i++) {
								changed_thread = changed_thread
										|| rowColChecker(getCol(i));
							}
						}
					}, new BarrierAction() {

						@Override
						public void run() throws Exception {
							sharedChanged.set(changed_thread
									|| sharedChanged.get());
						}

					});
				}
			});
			if (sharedChanged.get()) {
				sharedHintGen.set(true);
				continue;
			}
			new ParallelTeam().execute(new ParallelRegion() {

				boolean changed_thread = false;

				public void run() throws Exception {

					execute(0, N - 1, new IntegerForLoop() {

						public void run(int first, int last) {
							for (int i = first; i <= last; i++) {
								changed_thread = changed_thread
										|| rowColChecker(getQuadrant(i));
							}
						}
					}, new BarrierAction() {

						@Override
						public void run() throws Exception {
							sharedChanged.set(changed_thread
									|| sharedChanged.get());
						}

					});
				}
			});
			if (sharedChanged.get()) {
				sharedHintGen.set(true);
				continue;
			} else {
				if (count == 0) {
					break;
				} else {

				}
			}
			
		}
	}

	public boolean hintGeneratorSmp(int rank) {
		rank++;
		boolean changed = false;

		for (int i = 0; i < _puzzle.length; i++) {
			for (int j = 0; j < _puzzle[0].length; j++) {
				if (_puzzle[i][j].getValue() == 0) {
					Cell[] row = getRow(_puzzle[i][j].getX());
					Cell[] col = getCol(_puzzle[i][j].getY());
					Cell[] qad = getQuadrant(_puzzle[i][j].getPos());
					for (int k = 0; k < row.length; k++) {
						if (row[k].getValue() == rank) {
							if (_puzzle[i][j].removeHint(rank)) {
								changed = true;
							}
						}
						if (col[k].getValue() == rank) {
							if (_puzzle[i][j].removeHint(rank)) {
								changed = true;
							}
						}
						if (qad[k].getValue() == rank) {
							if (_puzzle[i][j].removeHint(rank)) {
								changed = true;
							}
						}
					}
				}
			}
		}
		return changed;
	}


	// *****************Sequential Methods****************************

	/**
	 * Solve the puzzle
	 */
	public void solveSeq() {

		// hint generation
		boolean hintGen = true;
		boolean changed = false;
		int iterations = 0;

		while (true) {
			changed = false;
			iterations++;
			while (hintGen) {
				hintGen = hintGeneratorSeq();
			}
			for (int x = 0; x < N; x++) {
				changed = changed || rowColChecker(getRow(x));
			}
			if (changed == true) {
				hintGen = true;
				continue;
			}
			for (int x = 0; x < N; x++) {
				changed = changed || rowColChecker(getCol(x));
			}
			if (changed == true) {
				hintGen = true;
				continue;
			}
			for (int x = 0; x < N; x++) {
				changed = changed || rowColChecker(getQuadrant(x));
			}
			if (changed == true) {
				hintGen = true;
				continue;
			} else {
				if (count == 0) {
					break;
				} else {

				}
			}
		}

		printPuzzle();
		System.out.println(iterations + " iterations performed");

		if (count == 0) {
			System.out.println("Win!");
		}
	}

	public boolean hintGeneratorSeq() {

		boolean changed = false;

		for (int i = 0; i < _puzzle.length; i++) {
			for (int j = 0; j < _puzzle[0].length; j++) {
				if (_puzzle[i][j].getValue() == 0) {
					Cell[] row = getRow(_puzzle[i][j].getX());
					Cell[] col = getCol(_puzzle[i][j].getY());
					Cell[] qad = getQuadrant(_puzzle[i][j].getPos());
					for (int k = 0; k < row.length; k++) {
						if (row[k].getValue() != 0) {
							if (_puzzle[i][j].removeHint(row[k].getValue())) {
								changed = true;
							}
						}
						if (col[k].getValue() != 0) {
							if (_puzzle[i][j].removeHint(col[k].getValue())) {
								changed = true;
							}
						}
						if (qad[k].getValue() != 0) {
							if (_puzzle[i][j].removeHint(qad[k].getValue())) {
								changed = true;
							}
						}
					}
				}
			}
		}
		return changed;
	}

	/**
	 * Row and Column Checker
	 * 
	 * collect any hints that is not shared by any other. if hint is not shared
	 * by any other, it is the answer/Value for its owner.
	 */
	public boolean rowColChecker(Cell[] col) {
		boolean changed = false;
		// index of these array represent the hint with the value of index+1
		int[] hintCounter = new int[N]; // counter for hint
		Cell[] owner = new Cell[N]; // first encountered ownere for hint

		for (int i = 0; i < N; i++) { // iterate through the cells in col
			if (col[i].getValue() > 0)
				continue; // skip cells with answer set already

			ArrayList<Integer> hints = col[i].getHints(); // get the list of
															// hints in

			for (int j = 0; j < hints.size(); j++) {
				int hintVal = hints.get(j) - 1;
				if (owner[hintVal] == null)
					owner[hintVal] = col[i]; // record a first seen hints owner
				hintCounter[hintVal]++; // for every hint seen increment its
										// counter
			}
		}

		// go through hintCounter for any singlely owned hint(s)
		for (int i = 0; i < N; i++) {
			if (hintCounter[i] == 1) {
				changed = true;
				owner[i].setValue(i + 1);
				// debugger
				System.out.println(col[i].toString());
			}
		}

		return changed;
	}

	/**
	 * Print the contents of the puzzle
	 */
	public void printPuzzle() {
		StringBuilder sb = new StringBuilder();
		for (int x = 0; x < N; x++) {
			for (int y = 0; y < N; y++) {
				try {
					sb.append(_puzzle[x][y].getValue());
				} catch (Exception e) {
					System.out.println("You messed up the input at (" + x + ","
							+ y + ")");
				}
				sb.append(" ");
				if (y % sqrtN == (sqrtN - 1)) {
					sb.append(" ");
				}
			}
			sb.append("\n");
			if (x % sqrtN == (sqrtN - 1)) {
				sb.append("\n");
			}
		}
		System.out.print(sb.toString());
	}

}
