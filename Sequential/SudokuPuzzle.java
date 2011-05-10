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
import edu.rit.pj.reduction.SharedInteger;

import edu.rit.pj.Comm;
import edu.rit.util.Range;
import edu.rit.mp.IntegerBuf;
import edu.rit.mp.ObjectBuf;

public class SudokuPuzzle {

	public static int count;

	public static int N;

	public static int sqrtN;

	private static Cell[][] _puzzle;

	private static SharedBoolean sharedHintGen;

	private static SharedBoolean sharedChanged;

	public static SharedInteger sharedCount;

	/**
	 * Constructor to initialize with values from a given file
	 * 
	 * @param filename
	 */
	public SudokuPuzzle(String filename, int n) {
		N = n;
		sqrtN = (int) Math.sqrt(N);
		count = N * N;
		sharedCount = new SharedInteger(count);
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

	// ******************************SMP
	// Methods********************************
	/**
	 * Solve the puzzle using a SMP
	 */
	public void solveSmp() throws Exception {
		ParallelTeam pt = new ParallelTeam();

		// hint generation
		sharedHintGen = new SharedBoolean(true);
		sharedChanged = new SharedBoolean();

		int iterations = 0;

		while (true) {
			sharedChanged.set(false);
			iterations++;
			while (sharedHintGen.get()) {
				sharedHintGen.set(false);
				pt.execute(new ParallelRegion() {
					boolean hintGen_thread;

					public void run() throws Exception {
						for (int z = 0; z < 3; z++) {
							final int zz = z;
							execute(0, N - 1, new IntegerForLoop() {

								public void run(int first, int last) {
									for (int i = first; i <= last; i++) {
										hintGen_thread =
												hintGen_thread
														|| hintGeneratorSmp(
																zz, i);

									}
								}

								public void finish() {
									sharedHintGen.set(hintGen_thread
											|| sharedHintGen.get());
								}
							}, BarrierAction.WAIT);
						}
					}
				});

			}
			if (sharedCount.get() == 0) {
				break;
			}
			pt.execute(new ParallelRegion() {

				boolean changed_thread = false;

				public void run() throws Exception {
					for (int z = 0; z < 3; z++) {
						final int zz = z;
						execute(0, N - 1, new IntegerForLoop() {

							public void run(int first, int last) {
								for (int i = first; i <= last; i++) {
									changed_thread =
											changed_thread
													|| rowColChecker(
															zz, i);
								}
							}

							public void finish() {
								sharedChanged.set(changed_thread
										|| sharedChanged.get());
							}
						}, BarrierAction.WAIT);
					}
				}
			});

			if (sharedChanged.get()) {
				sharedHintGen.set(true);
				continue;
			} else {
				if (sharedCount.get() == 0) {
					break;
				} else {
					// No solution can be found using our algorithms, break
					// out
					// or use another method
					break;
				}
			}
		}
	}

	/**
	 * Hint Generator modified to work on SMP
	 * 
	 * @param type
	 *            - 0-row, 1-col, 2-quad
	 * @param num
	 *            - row/col/quad number
	 * @return whether something was changed
	 */
	public boolean hintGeneratorSmp(int type, int num) {
		boolean changed = false;

		Cell[] holder;
		if (type == 0) {
			holder = getRow(num);
		} else if (type == 1) {
			holder = getCol(num);
		} else {
			holder = getQuadrant(num);
		}
		for (int y = 0; y < N; y++) {
			if (holder[y].getValue() != 0) {
				for (int i = 0; i < holder.length; i++) {
					if (holder[i].getValue() == 0) {
						if (holder[i]
								.removeHint(holder[y].getValue())) {
							changed = true;
						}
					}
				}
			}
		}
		return changed;
	}

	// ***************************Cluster
	// Methods******************************
	/**
	 * Solve the puzzle using a cluster
	 */
	public void solveClu(Comm world, int rank, int size)
			throws Exception {

		// slice matrix based on size
		Range[] ranges = new Range(0, N - 1).subranges(size);

		Range range = ranges[rank];
		int lb = range.lb(), ub = range.ub();

		Range[] quadRange = new Range(0, N - 1).subranges(sqrtN);
		ObjectBuf<Cell>[] patchbufs =
				ObjectBuf.patchBuffers(_puzzle, quadRange, quadRange);
		// hintGEn!
		//int quadCount = ub - lb;
		//boolean[] quadComplete = new boolean[quadCount + 1];

		for (;;) {
			// process retires if all of its own ed cell is solved
			boolean changed = false;

			//if (quadComplete[rank])
			//	continue;
			ObjectBuf<Cell> mypatch = patchbufs[rank];

			// int plb = mypatch.lb(),
			
			/*
			 * start x should be your range lb and end x should be your range ub
			 * for the quadrants this process is responsible. rowColChecker should be 
			 * altered to a quadSolverCluster so that the same things happen in rowColChecker
			 * in quadSolverCluster, the difference being quadSolverCluster only alters
			 * the quad this process 'owns'
			 */

			int startX = rank / sqrtN * sqrtN, startY =
					rank % sqrtN * sqrtN, endX = startX + 2, endY =
					startY + 2;

			// deduce hints, and set values..
			for (int method = 0; method < 3; method++) {
				for (int i = startX; i < endX; i++)
					changed = changed || rowColChecker(method, i);
				if (changed) {
					hintGeneratorSeq();
					changed = false;
					method--;
				}
			}
			System.out.println("ready!");
			// share!
			world.allGather(mypatch, patchbufs);
			System.out.println(rank + " waiting..");
		}

	}

	// **************************Sequential
	// Methods****************************

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
			for (int z = 0; z < 3; z++) {
				for (int x = 0; x < N; x++) {
					changed = changed || rowColChecker(z, x);
				}
			}
			if (changed) {
				hintGen = true;
				continue;
			} else {
				if (count == 0) {
					break;
				} else {
					bruteForceIt();
					break;
				}
			}
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
							if (_puzzle[i][j].removeHint(row[k]
									.getValue())) {
								changed = true;
							}
						}
						if (col[k].getValue() != 0) {
							if (_puzzle[i][j].removeHint(col[k]
									.getValue())) {
								changed = true;
							}
						}
						if (qad[k].getValue() != 0) {
							if (_puzzle[i][j].removeHint(qad[k]
									.getValue())) {
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
	 * collect any hints that is not shared by any other. if hint is not
	 * shared by any other, it is the answer/Value for its owner.
	 */
	public boolean rowColChecker(int type, int num) {

		Cell[] holder;
		if (type == 0) {
			holder = getRow(num);
		} else if (type == 1) {
			holder = getCol(num);
		} else {
			holder = getQuadrant(num);
		}

		boolean changed = false;
		// index of these array represent the hint with the value of
		// index+1
		int[] hintCounter = new int[N]; // counter for hint
		Cell[] owner = new Cell[N]; // first encountered ownere for hint

		for (int i = 0; i < N; i++) { // iterate through the cells in col
			if (holder[i].getValue() > 0)
				continue; // skip cells with answer set already

			ArrayList<Integer> hints = holder[i].getHints(); // get the
			// list of
			// hints in

			for (int j = 0; j < hints.size(); j++) {
				int hintVal = hints.get(j) - 1;
				if (owner[hintVal] == null)
					owner[hintVal] = holder[i]; // record a first seen
				// hints
				// owner
				hintCounter[hintVal]++; // for every hint seen increment
				// its
				// counter
			}
		}

		// go through hintCounter for any singlely owned hint(s)
		for (int i = 0; i < N; i++) {
			if (hintCounter[i] == 1) {
				changed = true;
				owner[i].setValue(i + 1);
				// debugger
				// System.out.println(col[i].toString());
			}
		}

		return changed;
	}

	// ***********************Brute Force
	// Methods******************************
	/**
	 * Run the brute force method on the current puzzle
	 */
	public void bruteForceIt() {
		Cell cell = getNextEmptyCell(null);
		while (count > 0) {
			setCellTempHints(cell);
			if (cell.nextTempValue()) {
				// forwardtrack
				cell = getNextEmptyCell(cell);
				if (cell == null) {
					System.out.println("Brute Force Done, count = "
							+ count);
				}
			} else {
				// backtrack
				cell = getPrevEmptyCell(cell);
			}
		}
		fillInBruteForce();
	}

	/**
	 * Essentially the hint checker but looks at temp and normal values and
	 * only looks at empty cells
	 * 
	 * @param cell
	 */
	public void setCellTempHints(Cell cell) {
		cell.resetTempHints();
		Cell[] quad = getQuadrant(cell.getPos());
		Cell[] row = getRow(cell.getX());
		Cell[] col = getCol(cell.getY());
		for (int x = 0; x < row.length; x++) {
			if (row[x].isEmpty()) {
				cell.removeTempHint(row[x].getTempValue());
			} else {
				cell.removeTempHint(row[x].getValue());
			}
			if (col[x].isEmpty()) {
				cell.removeTempHint(col[x].getTempValue());
			} else {
				cell.removeTempHint(col[x].getValue());
			}
			if (quad[x].isEmpty()) {
				cell.removeTempHint(quad[x].getTempValue());
			} else {
				cell.removeTempHint(quad[x].getValue());
			}
		}
	}

	/**
	 * Change the temp values into the real values
	 */
	public void fillInBruteForce() {
		for (int x = 0; x < N; x++) {
			for (int y = 0; y < N; y++) {
				if (_puzzle[x][y].isEmpty()) {
					_puzzle[x][y].setValue(_puzzle[x][y]
							.getTempValue());
				}
			}
		}
	}

	/**
	 * Get the next cell that can hold a temp value
	 * 
	 * @param cell
	 * @return
	 */
	public Cell getNextEmptyCell(Cell cell) {
		if (cell == null) {
			cell = _puzzle[0][0];
		} else {
			cell = getNextCell(cell);
			if (cell == null) {
				return null;
			}
		}
		while (!cell.isEmpty()) {
			cell = getNextCell(cell);
			if (cell == null) {
				return null;
			}
		}
		return cell;
	}

	/**
	 * Get the last Cell that can hold a temp value
	 * 
	 * @param cell
	 * @return
	 */
	public Cell getPrevEmptyCell(Cell cell) {
		cell = getPrevCell(cell);
		while (!cell.isEmpty()) {
			cell = getPrevCell(cell);
			if (cell == null) {
				return null;
			}
		}
		return cell;
	}

	/**
	 * Get the previous cell in the matrix
	 * 
	 * @param cell
	 *            - previous cell
	 * @return
	 */
	public Cell getPrevCell(Cell cell) {
		int x = cell.getX();
		int y = cell.getY();
		if (x == 0 && y == 0) {
			return null;
		}
		if (y == 0) {
			y = 8;
			x--;
		} else {
			y--;
		}
		return _puzzle[x][y];
	}

	/**
	 * Get the next cell in the matrix
	 * 
	 * @param cell
	 * @return
	 */
	public Cell getNextCell(Cell cell) {
		int x = cell.getX();
		int y = cell.getY();
		if (x == 8 && y == 8) {
			return null;
		}
		if (y == 8) {
			y = 0;
			x++;
		} else {
			y++;
		}
		return _puzzle[x][y];
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
					System.out.println("You messed up the input at ("
							+ x + "," + y + ")");
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
