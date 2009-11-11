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
	// 1 -> 0-puzzle, 2 -> 3-puzzle, 3 -> 8-puzzle, 4 -> 15-puzzle, ...
	private int size;
	//iterating limit for iterative deepening search
	private int currentCostLimit;
	//global input for solve(..)
	private Heuristik heuristic;
	private int[] problem;
	private Node solution;

	private enum Move{Up, Down, Left, Right};
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
	 */
	private void IDA(){
		//init
		currentCostLimit = 1;
		List<Node> queue = new LinkedList<Node>();
		Map<Integer, Integer> visitedStates = new HashMap<Integer, Integer>();

		//to be sure that we found something in the last iteration
		Boolean newWays = true;
		//break if we find something
		Boolean foundSolution = true;

		while(!foundSolution && newWays){
			newWays = false;
			Node root = new Node(null, problem);
			queue.add(root);
			visitedStates.put(root.getState().hashCode(), root.getCost());

			while(!queue.isEmpty()){
				//TODO entweder sortierte queue oder pr�fen ob kleinstm�gliches
				Node node = queue.remove(0);
				if(node.isSolution()){
					solution = node;
					foundSolution = true;
				}

				if(node.getCost() < currentCostLimit){
					List<Move> moves = node.getMoves();
					for(Move move : moves){
						Node newNode = getNextNode(node, move);
//                                                if(!hash)
						queue.add(0, newNode);
					}
				}

			}
			currentCostLimit++;
			System.err.println("currentCostLimit: "+currentCostLimit);
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

		size = sizeDouble.intValue();
		return true;
	}

	private Node getNextNode(Node parent, Move move){
		int[] state = parent.getState();
		int pos0 = parent.getPos0();
		int posOld = 0;
		switch(move){
			case Up:	posOld = pos0 + size;	break;
			case Down:	posOld = pos0 - size;	break;
			case Right:	posOld = pos0 - 1;		break;
			case Left:	posOld = pos0 + 1;		break;
		}
		int old = state[posOld-1];
		state[posOld-1] = 0;
		state[pos0-1]	= old;
		return new Node(parent, state);
	}

	/**
	 * datastructure used for the search
	 */
	private class Node implements Comparable{
		private Node parent;
		private int wayCost;
		private int heuristicCost;
		private int[] state;
		private Integer pos0;

		Node(Node parent, int[] state){
			this.parent = parent;
			if(parent == null)
				this.wayCost = 0;
			else
				this.wayCost = parent.getWayCost() +1;
			//TODO
			this.heuristicCost = 0;
			this.state = state;
			this.pos0 = null;
		}

		void setParent(Node parent){
			this.parent = parent;
		}

		int getCost(){
			return wayCost+heuristicCost;
		}

		Node getParent(){
			return parent;
		}

		int[] getState(){
			return state;
		}

		int getWayCost(){
			return wayCost;
		}

		Boolean isSolution(){
			for(int i=1; i<size; i++){
				if(i != state[i-1])
					return false;
			}
			return true;
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

		private List<Move> getMoves() {
			List<Move> moves = new LinkedList<Move>();
			if(pos0 == null)
				calcPos0();
			if(!(pos0 >= size*(size-1)))
				moves.add(Move.Up);
			if(!(pos0 <= size))
				moves.add(Move.Down);
			if(!((pos0 % size) == 1))
				moves.add(Move.Left);
			if(!((pos0 % size) == (size-1) ))
				moves.add(Move.Right);
			return moves;
		}

		private void calcPos0(){
			int i = 0;
			while(pos0 == null){
				if(state[i]==0)
					pos0 = i+1;
			}
		}

		int getPos0(){
			if(pos0 == null)
				calcPos0();
			return pos0;
		}
	}
}
