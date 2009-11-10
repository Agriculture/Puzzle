import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import puzzlelib.*;

/**
 * Lösungsklasse
 */
public class MyPuzzleSolver implements IPuzzleSolver
{
	//the size of the border
	// 1 -> 0-puzzle, 4 -> 3-puzzle, 9 -> 8-puzzle, 16 -> 15-puzzle, ...
	private int size;
	//iterating limit for iterative deepening search
	private int currentWayLimit;
	//global input for solve(..)
	private Heuristik heuristic;
	private int[] problem;
     /**
     * Löst ein Puzzle-Problem bzw. versucht dies.
     * @param heuristik Die hierbei zu verwendende Heuristik
     * @param problem Das zu lösende Puzzle. Die Beschriftung der Steine wird in dem Array zeilenweise
     * hintereinander angegeben. D.h. beim 8-Puzzle geben die ersten 3 Einträge die
     * Positionen (x,y): (0,0), (1,0), (2,0) an. Die folgenden 3 Einträge
     * geben die Positionen (0,1), (1,1), (2,1) an usw.
     * Für den Stein mit der Beschriftung 1 erscheint bspw. eine 1 im Array.
     * Die leere Stelle wird durch eine 0 angegeben. Aus der Größe des Array kann man herausfinden,
     * ob es sich um ein 8-, ein 15- oder ein 24-Puzzle handelt (jeweils 9, 16 bzw. 25 Einträge)
     * Beispiel: das gelöste 8-Puzzle hat folgendes Array: 1,2,3,4,5,6,7,8,0
     * @return Ergebnis des Lösungsversuchs. D.h. entweder "ist unlösbar" oder
     * Lösungszugfolge mit einer kleinen Statistik (expandierte Knoten und effektiver
     * Branchingfaktor)
     * @throws Exception Falls das übergebene Problem kein gültiger Puzzle-Zustand ist,
     * weil bspw. eine andere Anzahl von Einträgen als 9, 16 oder 25 vorliegt oder
     * weil Ziffern mehrfach oder gar nicht auftauchen oder falls nur ein Löser
     * für das 8-Puzzle implementiert wurde, aber ein 15-Puzzle gelöst werden soll, kann hier eine
     * Exception geworfen werden.
     */
    public SolveErg solve(Heuristik heuristik, int[] problem) throws Exception
    {
		//make it global cause im lazy
		this.heuristic = heuristik;
		this.problem = problem;

        //1. zuerst testen, ob es sich um ein vernünftiges Puzzle-Problem handelt
        if(!checkPuzzle())
            throw new Exception("not a valid puzzle");

        //2. Größe des Puzzles herausfinden (9->3x3, 16->4x4, 25->5x5)
        //if(problem.length==9) make8Puzzle(...)
        //else if(problem.length==16) make15Puzzle(...)
        //else throw new Exception("so große Puzzle habe ich nicht implementiert");
		System.err.println("Puzzle has size: "+size);

        //3. je nach Heuristik anders lösen
        //if(heuristik==Heuristik.MissplacedTiles) solveMissplacedTiles(...)
        //else if(heuristik==Heuristik.Gaschnig) solveGaschnig(...)
        //else solveBlockDistance(...)
		IDA();

        //4. je nach Ausgang ein Ergebnis zusammenstellen:
        //if(!isSolveable)
        //   return SolveErg.makeErgForUnsolvable();
        //else
        //{
        //     ArrayList<Direction> zugfolge=getLösungszugfolge();
        //     int expandedNodes=getExpandedNodes();
        //     double effectiveBranchingFactor=getEffectiveBranchingfactor(...);
        //     return SolveErg.makeErgForSolvable(ArrayList<Direction> loesungsZuege, int expandedNodesCount, double effectiveBranchingFactor)
        //}
        return SolveErg.makeErgForUnsolvable();
    }

	/**
	 * iterative deepening seach
	 * @param heuristik
	 */
	private void IDA(){
		//init
		currentWayLimit = 1;
		List<Node> queue = new LinkedList<Node>();
		Map<Integer, Integer> visitedStates = new HashMap<Integer, Integer>();
		Node root = new Node(0, problem);
		queue.add(root);
		visitedStates.put(root.getState().hashCode(), root.getWayCost());

		while(!queue.isEmpty()){
			//TODO

		}

	}

	/**
	 * check puzzle before doing anything and determin the size
	 * @param problem
	 * @return true if the problem is valid
	 */
	private boolean checkPuzzle() {
		if(problem == null)
			return false;

		Double sizeDouble = java.lang.Math.sqrt(problem.length);

		if( sizeDouble == 0)
			return false;

		if( sizeDouble.floatValue() != 0)
			return false;

		sizeDouble = java.lang.Math.pow(sizeDouble, 2);
		size = sizeDouble.intValue();
		return true;
	}

	/**
	 * datastructure used for the search
	 */
	private class Node implements Comparable{
		private Node parent;
		private int wayCost;
		private int heuristicCost;
		private int[] state;

		Node(int wayCost, int[] state){
			this.parent = null;
			this.wayCost = wayCost;
			//TODO
			this.heuristicCost = 0;
			this.state = state;
		}

		void setParent(Node parent){
			this.parent = parent;
		}

		int getCost(){
			return wayCost+heuristicCost;
		}

		int getWayCost(){
			return wayCost;
		}

		Node getParent(){
			return parent;
		}

		int[] getState(){
			return state;
		}

		public int compareTo(Object arg0) {
			Node o = (Node) arg0;
			if(getCost() < o.getCost())
				return -1;
			else if(getCost() == o.getCost())
					return 0;
				else
					return 1;
		}
	}
}
