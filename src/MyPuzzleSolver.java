import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
	private Integer[] rootState;
	private Node solution;
	private boolean foundSolution;
	private int expandedNodes = 0;
	private long start;

     /**
     * LÃ¶st ein Puzzle-Problem bzw. versucht dies.
     * @param heuristik Die hierbei zu verwendende Heuristik
     * @param problem Das zu lÃ¶sende Puzzle. Die Beschriftung der Steine wird in dem Array zeilenweise
     * hintereinander angegeben. D.h. beim 8-Puzzle geben die ersten 3 EintrÃ€ge die
     * Positionen (x,y): (0,0), (1,0), (2,0) an. Die folgenden 3 EintrÃ€ge
     * geben die Positionen (0,1), (1,1), (2,1) an usw.
     * FÃŒr den Stein mit der Beschriftung 1 erscheint bspw. eine 1 im Array.
     * Die leere Stelle wird durch eine 0 angegeben. Aus der GrÃ¶Ãe des Array kann man herausfinden,
     * ob es sich um ein 8-, ein 15- oder ein 24-Puzzle handelt (jeweils 9, 16 bzw. 25 EintrÃ€ge)
     * Beispiel: das gelÃ¶ste 8-Puzzle hat folgendes Array: 1,2,3,4,5,6,7,8,0
     * @return Ergebnis des LÃ¶sungsversuchs. D.h. entweder "ist unlÃ¶sbar" oder
     * LÃ¶sungszugfolge mit einer kleinen Statistik (expandierte Knoten und effektiver
     * Branchingfaktor)
     * @throws Exception Falls das ÃŒbergebene Problem kein gÃŒltiger Puzzle-Zustand ist,
     * weil bspw. eine andere Anzahl von EintrÃ€gen als 9, 16 oder 25 vorliegt oder
     * weil Ziffern mehrfach oder gar nicht auftauchen oder falls nur ein LÃ¶ser
     * fÃŒr das 8-Puzzle implementiert wurde, aber ein 15-Puzzle gelÃ¶st werden soll, kann hier eine
     * Exception geworfen werden.
     */
    public SolveErg solve(Heuristik heuristik, int[] problem) throws Exception
    {
		//make it global cause im lazy
		this.heuristic = heuristik;
		this.problem = problem;

        //1. zuerst testen, ob es sich um ein vernÃŒnftiges Puzzle-Problem handelt
        if(!checkPuzzle())
            throw new Exception("not a valid puzzle");

		this.rootState = new Integer[size*size];
		for(int i =0; i<problem.length; i++)
			rootState[i] = problem[i];

        //2. GrÃ¶Ãe des Puzzles herausfinden (9->3x3, 16->4x4, 25->5x5)
        //if(problem.length==9) make8Puzzle(...)
        //else if(problem.length==16) make15Puzzle(...)
        //else throw new Exception("so groÃe Puzzle habe ich nicht implementiert");
		System.err.println("Puzzle has size: "+size);

        //3. je nach Heuristik anders lÃ¶sen
        //if(heuristik==Heuristik.MissplacedTiles) solveMissplacedTiles(...)
        //else if(heuristik==Heuristik.Gaschnig) solveGaschnig(...)
        //else solveBlockDistance(...)
		start = System.currentTimeMillis();
		IDA();
		System.err.println("solved in (ms): "+(System.currentTimeMillis()-start));

        //4. je nach Ausgang ein Ergebnis zusammenstellen:
        //if(!isSolveable)
        //   return SolveErg.makeErgForUnsolvable();
        //else
        //{
        //     ArrayList<Direction> zugfolge=getLÃ¶sungszugfolge();
        //     int expandedNodes=getExpandedNodes();
        //     double effectiveBranchingFactor=getEffectiveBranchingfactor(...);
        //     return SolveErg.makeErgForSolvable(ArrayList<Direction> loesungsZuege, int expandedNodesCount, double effectiveBranchingFactor)
        //}
		if(foundSolution){
			ArrayList<Direction> way = calcWay();
			float branch = 1;
			float sum = 0;
			while(sum < expandedNodes){
				branch+=0.001;
				sum = 0;
				for(int i=0; i<=solution.getWayCost(); i++)
					sum+= Math.pow(branch, i);
			}
//			System.err.println("solution "+solution);
//			System.err.println("way "+way);
			return SolveErg.makeErgForSolvable(way, expandedNodes, branch);
		} else {
			return SolveErg.makeErgForUnsolvable();
		}
    }

	/**
	 * iterative deepening seach
	 */
	private void IDA(){
		//init
		List<Node> queue = new LinkedList<Node>();
		Map<Long, Integer> hash = new HashMap<Long, Integer>();

		//to be sure that we found something in the last iteration
		Boolean foundSomethingNew = true;
		//break if we find something
		foundSolution = false;
		Node root = new Node(null, rootState, null);
		hash.put(root.goodHash(), 0);
		//set the minimum, if heuristic always return 0 then the limit is 1
		currentCostLimit = root.getCost()+1;
		Boolean expand;

		while(!foundSolution && foundSomethingNew){
			System.err.println("currentCostLimit: "+currentCostLimit);
			System.err.println("current size of hash: "+hash.size());
			System.err.println("expanded Nodes: "+expandedNodes+" in (ms) "+(System.currentTimeMillis()-start));

			foundSomethingNew = false;
			queue.add(root);

			while(!queue.isEmpty()){
//				System.err.println(queue);
//				System.err.println(hash);
				expand = true;
				Node node = queue.remove(0);

				if(node.isSolution(node.getState())){
					solution = node;
					foundSolution = true;
				}

//				System.err.println("look at "+node+" in hash: "+hash.containsKey(node.goodHash())+" "+node.goodHash());
				if(hash.containsKey(node.goodHash())){
					Integer hashValue = hash.get(node.goodHash());
					//found shorter way
					//test whether we found a shorter way
					if( (currentCostLimit-node.getCost()) > hashValue){
						hash.put(node.goodHash(), currentCostLimit-node.getCost());
						//search
					} else {
						//we have already seen this from a shorter or equal way
						expand = false;
					}
				} else {
						//found new node
					foundSomethingNew = true;
					if((currentCostLimit-node.getCost()) < 1){
						expand = false;
					} else {
						hash.put(node.goodHash(), currentCostLimit-node.getCost());
					}
				}
				
				//look at this in the next round
				if(expand && (node.getCost() >= currentCostLimit))
					foundSomethingNew = true;

				if(expand && (node.getCost() < currentCostLimit)){
//					System.err.println(node);
					expandedNodes++;
					List<Direction> moves = node.getMoves();
					for(Direction move : moves){
						Node newNode = getNextNode(node, move);
						queue.add(0,newNode);
					}
				}
			}
			currentCostLimit++;
		}
		System.err.println("\nlast size of hash: "+hash.size());
		System.err.println("used memory: (KiB) "+(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1024);
	}

	private ArrayList<Direction> calcWay() {
		Node node = solution;
		//calc the way of the nodes
		List<Node> way = new LinkedList<Node>();
		while(node != null){
			way.add(0, node);
			node = node.getParent();
		}
		//find out the way we took
		ArrayList<Direction> result = new ArrayList<Direction>();
		while(way.size() > 1){
			node = way.remove(0);
			List<Direction> moves = node.getMoves();
			for(Direction move : moves){
				if(getNextNode(node, move).equals(way.get(0)))
					result.add(move);
			}
		}
		return result;
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

		if( sizeDouble == 0f)
			return false;

		if( (sizeDouble-sizeDouble.intValue()) != 0)
			return false;

		size = sizeDouble.intValue();
		return true;
	}

	private Node getNextNode(Node parent, Direction move){
		Integer[] state = new Integer[size*size];
		System.arraycopy(parent.getState(), 0, state, 0, size*size);
		int pos0 = parent.getPos0();
		int posOld = 0;
		switch(move){
			case Up:	posOld = pos0 + size;	break;
			case Down:	posOld = pos0 - size;	break;
			case Right:	posOld = pos0 - 1;		break;
			case Left:	posOld = pos0 + 1;		break;
		}
		//switch
		state[pos0] = parent.getState()[posOld];
		state[posOld] = 0;

		//System.err.println(move+" "+parent.getState()[posOld]+" "+posOld+" "+Arrays.asList(state));
		return new Node(parent, state, posOld);
	}

	/**
	 * datastructure used for the search
	 */
	private class Node implements Comparable{
		private Node parent;
		private int wayCost;
		private int heuristicCost;
		private Integer[] state;
		private Integer pos0;

		Node(Node parent, Integer[] state, Integer pos0){
			this.state = state;
	//		System.err.println(state);
			if (pos0 == null)
				calcPos0();
			else
				this.pos0 = pos0;
			this.parent = parent;
			if(parent == null)
				this.wayCost = 0;
			else
				this.wayCost = parent.getWayCost() +1;
			//TODO
			this.heuristicCost = calcHeuristic();
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

		Integer[] getState(){
			return state;
		}

		int getWayCost(){
			return wayCost;
		}

		Boolean isSolution(Integer[] thing){
			for(int i=1; i<size*size; i++){
				if(i != thing[i-1])
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

		private List<Direction> getMoves() {
			List<Direction> moves = new LinkedList<Direction>();
			//if not at the bottom i can move up
			if(!(pos0 >= size*(size-1)))
				moves.add(Direction.Up);
			//if not at the top i can move down
			if(!(pos0 < size))
				moves.add(Direction.Down);
			//if not on the left border i can move right
			if(!((pos0 % size) == 0))
				moves.add(Direction.Right);
			//of not on the right border i can move left
			if(!((pos0 % size) == (size-1) ))
				moves.add(Direction.Left);
			//System.err.println("calculated moves: "+moves);
			return moves;
		}

		private void calcPos0(){
			int i = 0;
			while(pos0 == null){
				if(state[i]==0)
					pos0 = i;
				i++;
			}
		}

		private int calcHeuristic(){
			int heuristicValue = 0;
			switch(heuristic){
				case MissplacedTiles:
					int a;
					for(a=1; a < size*size; a++){
						//add if this is not the empty field and there is the wrong number
						if((state[a-1] != 0)&&(a != state[a-1])){
							heuristicValue++;
						}
					}
					break;
				case Gaschnig:
					Integer[] tmpState = new Integer[size*size];
					Integer tmpPos0 = this.pos0;
					System.arraycopy(state, 0, tmpState, 0, size*size);
					//calc how long until right
					while(!isSolution(tmpState)){
						heuristicValue++;
						//there is seomthing wrong, find the piece that can go to 0
						int wrongPiece = 0;
						while((wrongPiece <= (size*size-1))&& (tmpState[wrongPiece] != (tmpPos0+1))){
							wrongPiece++;
						}
						//System.err.println(Arrays.asList(tmpState)+" "+wrongPiece+" "+tmpPos0);
						//if the 0 is at the last place
						//System.err.println(wrongPiece+" == "+(size*size-1));
						if(wrongPiece == (size*size)){
							//must place the first not right piece on 8
							int i=0;
							while(tmpState[i] == (i+1))
								i++;
							tmpState[size*size-1] = tmpState[i];
							tmpState[i] = 0;
							tmpPos0 = i;
						} else {
							//switch the wrong piece with the 0
							tmpState[tmpPos0] = tmpState[wrongPiece];
							//System.err.println("swapped "+tmpState[wrongPiece]);
							tmpState[wrongPiece] = 0;
							tmpPos0 = wrongPiece;
						}
					}
					//System.err.println("-");
					break;
				case BlockDistance:
						for(int src=0; src<(size*size); src++){
							int dst = state[src]-1;
							//if state[src]==0 then do nothing
							// -> the 0  is not a movable piece !
							if(dst != -1){
								heuristicValue+=Math.abs((dst % size)-(src % size));
								heuristicValue+=Math.abs(Math.floor(dst / size)-Math.floor(src / size));
							}
						}
					break;

			}
			return heuristicValue;
		}

		int getPos0(){
			if(pos0 == null)
				calcPos0();
			return pos0;
		}

		@Override
		public boolean equals(Object obj) {
			return (obj instanceof Node)
				&&	Arrays.equals(state,((Node) obj).getState());
		}

		public Long goodHash() {
			Long value = 0L;
			for(int i=0; i<size*size; i++){
				value+=Math.round(state[i]*Math.pow(16, i));
			}
			return value;
		}

		@Override
		public String toString() {
			return "node "+Arrays.asList(state)+" cost: "+getCost();
		}
	}
}
