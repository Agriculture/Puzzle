import puzzlelib.*;

public class Main
{
    public static void main(String[] args)
    {
        //1. Problem definieren, hier mal ein paar Beispiele
        //einfaches 8-Puzzle in 17 Zügen zu lösen
        int[] simple8Puzzle=new int[]{5,6,7,
                           0,2,3,
                           1,4,8};
        //komplizierteres 8-Puzzle, in 31 Zügen zu lösen
        int[] complex8Puzzle=new int[]{6,4,7,
                                       8,5,0,
                                       3,2,1};

        //ein zufällig generiertes 8-Puzzle, das garantiert lösbar ist
        int[] zufalls8PuzzleLoesbar=Helper.generateRandom8Puzzle(true);

        //ein zufällig generiertes 8-Puzzle, wobei einfach die Steine permutiert wurden
        //die Hälfte aller so erzeugbaren Zustände ist nicht lösbar
        int[] zufalls8Puzzle=Helper.generateRandom8Puzzle(false);

        //einfaches 15-Puzzle, in 25 Zügen zu lösen
        int[] simple15Puzzle=new int[]{1,14,3,4,
                                       5,10,2,8,
                                       9,0,15,11,
                                       13,7,6,12};

        //kompliziertes 15-Puzzle, in 51 Zügen zu lösen
        int[] complex15Puzzle=new int[]{9,15,12,6,
                                        1,5,0,7,
                                        14,13,11,4,
                                        3,10,8,2};

        //ein zufällig generiertes 15-Puzzle, das garantiert lösbar ist
        int[] zufalls15PuzzleLoesbar=Helper.generateRandom15Puzzle(true);

        //ein zufällig generiertes 15-Puzzle, wobei einfach die Steine permutiert wurden, nicht unbedingt lösbar
        int[] zufalls15Puzzle=Helper.generateRandom15Puzzle(false);
        //2. Unseren Solver erstellen
        MyPuzzleSolver solver=new MyPuzzleSolver();

        //3. Heuristik festlegen
        Heuristik h1=Heuristik.MissplacedTiles;
        Heuristik h2=Heuristik.Gaschnig;
        Heuristik h3=Heuristik.BlockDistance;

        //4. Lösen und Lösung ausgeben
        //das muss in einen try-catch-Block, weil die oben angegebenen Puzzle
        //zwar wohldefinierte Puzzleprobleme sind,
        //dies gilt jedoch nicht für alle int-Arrays,
        //sowohl der Solver als auch manche der Hilfsmethoden bringen in diesem
        //Fall Fehler
        try
        {
            //4.1. welches Puzzle nehmen wir denn nun?
            int[] problem=simple8Puzzle;
            //4.2. Heuristik festlegen
            Heuristik h=h1;

            //4.3. Das Puzzle anzeigen (Console)
            System.out.println(Helper.puzzleToString(problem));

            //4.4. Der Solver soll mal eine Lösung versuchen
            SolveErg erg=solver.solve(h, problem);

            //4.5. Das Lösungsergebnis ausgeben
            System.out.println(erg);

            //4.6. Falls wir eine Lösung gefunden haben, testen, ob
            //1.) die Zugfolge gültig ist (d.h. es könnten ja auch Züge enthalten sein,
            //    wie eine Rechts-Verschiebung im simple8Puzzle oben, die ist nicht
            //    möglich, links vom leeren Feld gibt es keinen Stein, der nach rechts
            //    gerückt werden kann.
            //    In diesem Fall wirft der Test eine Exception
            //2.) nach Ausführen der Zugfolge das Puzzle wirklich gelöst ist
            if(!erg.isUnsolvable())
            {
                if(Helper.isSolution(problem, erg.getSolution()))
                    System.out.println("f�hrt wirklich zum Ziel");
                else
                    System.out.println("Fehler: f�hrt nicht zum Ziel");
            }
        }
        catch(Exception ex)
        {
            System.out.println("Fehler: "+ex.toString()+"\n"+ex.getStackTrace());
        }

    }
}
