///Timi,timeaszabo1217@gmail.hu

import game.snake.Direction;
import game.snake.SnakeGame;
import game.snake.SnakePlayer;
import game.snake.utils.Cell;
import game.snake.utils.SnakeGameState;

import java.util.*;

import static game.snake.SnakeGame.*;

/**
 * <font color="purple">
 * Az Agent osztály egy mesterséges intelligencia (AI) vezérlő egység a kígyó játékhoz.
 *
 * <p>
 * <u>A felhasznált algoritmusok.</u>
 * <ul>
 *     <li><b>A* algoritmus:</b> Az ételhez való közelítéshez, ha az elérhető.</li>
 *     <li><b>BFS algoritmus:</b> Ha az A* nem talál utat.</li>
 *     <li><b>Hamilton-kör:</b> Ha az étel elérhetősége nem biztosítható az A* vagy BFS segítségével.</li>
 * </ul>
 * </p>
 *
 * <font color="green">
 *     @author Szabó Tímea (JJMS9M)
 * @contact <a href="mailto:timeaszabo1217@gmail.hu">timeaszabo1217@gmail.hu</a>
 * @version 1.0
 */
public class Agent extends SnakePlayer {
    /** <font color="purple">A Hamilton-kör, amely meghatározza a kígyó lehetséges mozgásait. */
    private final int[][] hamiltonCycle;
    /** <font color="purple">A játék táblájának sorainak, oszlopainak száma. */
    private final int rows;
    private final int cols;

    /**
     * <font color="purple">
     * Az Agent osztály konstruktorja, amely inicializálja a Hamilton-kört és beállítja a játék táblájának méretét.
     * <font color="green">
     * @param gameState A játék állapota, amely tartalmazza a tábla és a kígyó aktuális helyzetét.
     * @param color A játékos színe.
     * @param random A véletlenszerű számok generálásához használt objektum.
     */
    public Agent(SnakeGameState gameState, int color, Random random) {
        super(gameState, color, random);
        this.rows = gameState.board.length;
        this.cols = gameState.board[0].length;
        this.hamiltonCycle = generateHamiltonCycle(rows, cols);
    }

    /**
     * <font color="purple">
     * A következő lépés irányának meghatározása.
     * <font color="green">
     * @param remainingTime A hátralévő idő.
     * @return A kiválasztott irány.
     */
    @Override
    public Direction getAction(long remainingTime) {
        SnakeGameState gameState = this.gameState;
        Cell head = gameState.snake.peekFirst();
        Cell food = findFood(gameState);

        if (food == null) {
            return SnakeGame.RIGHT;
        }
        return decideDirection(gameState, head, food);
    }

    /**
     * <font color="purple">
     * Az ételt kereső metódus, amely végigiterál a játéktáblán és visszaadja az étel helyét.
     * <font color="green">
     * @param gameState A játék aktuális állapota.
     * @return Az étel helyét tartalmazó Cell objektum, vagy null, ha nem található étel.
     */
    private Cell findFood(SnakeGameState gameState) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (gameState.board[i][j] == FOOD) {
                    return new Cell(i, j);
                }
            }
        }
        return null;
    }

    /**
     * <font color="purple">
     * A legjobb irány meghatározása az étel eléréséhez.
     * Az AI különböző algoritmusokat alkalmaz, mint A* és BFS, hogy megtalálja a legjobb irányt.
     * <font color="green">
     * @param gameState A játék aktuális állapota.
     * @param head A kígyó fejének helye.
     * @param food Az étel helye.
     * @return Az irány, amelyet a kígyó követni fog.
     */
    private Direction decideDirection(SnakeGameState gameState, Cell head, Cell food) {
        Set<Cell> snakeBody = new HashSet<>(gameState.snake);
        Direction aStarDirection = aStarToFood(gameState, head, food, snakeBody);
        if (aStarDirection != null) {
            return aStarDirection;
        }

        if (food == null) {
            return followHamiltonCycle(head);
        }

        int headIndex = hamiltonCycle[head.i][head.j];
        int foodIndex = hamiltonCycle[food.i][food.j];

        if (!isSafeToFollowCycle(gameState, headIndex, foodIndex)) {
            Direction bfsDirection = bfsToFood(gameState, head, food, snakeBody);
            if (bfsDirection != null) {
                return bfsDirection;
            }
        }
        return followHamiltonCycle(head);
    }

    /**
     * <font color="purple">
     * A A* algoritmus alkalmazása az ételhez vezető legjobb irány meghatározásához.
     * <font color="green">
     * @param gameState A játék aktuális állapota.
     * @param head A kígyó fejének helye.
     * @param food Az étel helye.
     * @param snakeBody A kígyó testének helyei.
     * @return Az A* algoritmus által meghatározott irány, vagy null, ha nincs elérhető út.
     */
    private Direction aStarToFood(SnakeGameState gameState, Cell head, Cell food, Set<Cell> snakeBody) {
        PriorityQueue<Cell> openList = new PriorityQueue<>(Comparator.comparingInt(cell -> getFValue(cell, food)));
        Set<Cell> closedList = new HashSet<>();

        Map<Cell, Integer> gValues = new HashMap<>();
        Map<Cell, Cell> parents = new HashMap<>();

        openList.add(head);
        gValues.put(head, 0);
        parents.put(head, null);

        while (!openList.isEmpty()) {
            Cell currentCell = openList.poll();

            if (currentCell.equals(food)) {
                return reconstructPathAndGetDirection(head, currentCell, parents);
            }

            closedList.add(currentCell);

            for (Direction dir : DIRECTIONS) {
                Cell neighbor = getNeighbor(currentCell, dir);

                if (isValidCell(gameState, neighbor, snakeBody) && !closedList.contains(neighbor)) {
                    int newG = gValues.get(currentCell) + 1;
                    if (!gValues.containsKey(neighbor) || newG < gValues.get(neighbor)) {
                        gValues.put(neighbor, newG);
                        parents.put(neighbor, currentCell);
                        openList.add(neighbor);
                    }
                }
            }
        }
        return null;
    }

    /**
     * <font color="purple">
     * Kiszámítja a F értéket az A* algoritmus számára, amely az aktuális cella és az étel közötti távolságot tartalmazza.
     * <font color="green">
     * @param cell Az aktuális cella.
     * @param food Az étel cellája.
     * @return Az F érték, amely a G és H értékek összegéből áll.
     */
    private int getFValue(Cell cell, Cell food) {
        int g = 0;
        int h = heuristic(cell, food);
        return g + h;
    }

    /**
     * <font color="purple">
     * Kis számítja a Manhattan-távolságot két cella között, amely a legrövidebb távolságot jelzi, ha csak függőleges
     * és vízszintes mozgásokat engedünk.
     * <font color="green">
     * @param current Az aktuális cella.
     * @param goal A cél cella.
     * @return A Manhattan-távolság.
     */
    private int heuristic(Cell current, Cell goal) {
        return Math.abs(current.i - goal.i) + Math.abs(current.j - goal.j);
    }

    /**
     * <font color="purple">
     * Ellenőrzi, hogy biztonságos-e követni a Hamilton-kört a jelenlegi helyzet alapján.
     * A metódus megvizsgálja, hogy az ételhez való távolság elég rövid-e ahhoz, hogy a kígyó ne ütközzön a saját testével,
     * figyelembe véve a kígyó hosszát és a Hamilton-körben található cellák távolságát.
     * <font color="green">
     * @param gameState A játék aktuális állapota, amely tartalmazza a kígyó és a játék tábla helyzetét.
     * @param headIndex A kígyó fejének aktuális pozíciója a Hamilton-körben.
     * @param foodIndex Az étel aktuális pozíciója a Hamilton-körben.
     * @return Igaz, ha a kígyó biztonságosan követheti a Hamilton-kört, különben hamis.
     */
    private boolean isSafeToFollowCycle(SnakeGameState gameState, int headIndex, int foodIndex) {
        int snakeLength = gameState.snake.size();
        int pathDistance = (foodIndex - headIndex + rows * cols) % (rows * cols);
        return pathDistance <= snakeLength;
    }

    /**
     * <font color="purple">
     * A Hamilton-kör követése, ha az étel nem érhető el közvetlenül.
     * <font color="green">
     * @param head A kígyó fejének aktuális helye.
     * @return A következő lépés iránya a Hamilton-kör szerint.
     */
    private Direction followHamiltonCycle(Cell head) {
        int currentIndex = hamiltonCycle[head.i][head.j];
        for (Direction dir : DIRECTIONS) {
            Cell neighbor = getNeighbor(head, dir);
            if (neighbor != null && hamiltonCycle[neighbor.i][neighbor.j] == (currentIndex + 1) % (rows * cols)) {
                return dir;
            }
        }
        return SnakeGame.RIGHT;
    }

    /**
     * <font color="purple">
     * A BFS algoritmus alkalmazása, hogy megtaláljuk az ételhez vezető legjobb irányt.
     * <font color="green">
     * @param gameState A játék aktuális állapota.
     * @param start A kígyó fejének helye.
     * @param food Az étel helye.
     * @param snakeBody A kígyó testének aktuális helye.
     * @return A BFS által meghatározott irány.
     */
    private Direction bfsToFood(SnakeGameState gameState, Cell start, Cell food, Set<Cell> snakeBody) {
        Queue<Cell> queue = new LinkedList<>();
        Map<Cell, Cell> parentMap = new HashMap<>();
        queue.add(start);
        parentMap.put(start, null);

        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            if (current.equals(food)) {
                return reconstructPathAndGetDirection(start, current, parentMap);
            }

            for (Direction dir : DIRECTIONS) {
                Cell neighbor = getNeighbor(current, dir);
                if (isValidCell(gameState, neighbor, snakeBody) && !parentMap.containsKey(neighbor)) {
                    queue.add(neighbor);
                    parentMap.put(neighbor, current);
                }
            }
        }
        return followHamiltonCycle(start);
    }

    /**
     * <font color="purple">
     * A célból kiindulva rekonstruálja az útvonalat és meghatározza a következő irányt.
     * <font color="green">
     * @param start A kezdő hely.
     * @param goal A célhely.
     * @param parents Az útvonal szülő-celláit tartalmazó térkép.
     * @return A következő lépés iránya.
     */
    private Direction reconstructPathAndGetDirection(Cell start, Cell goal, Map<Cell, Cell> parents) {
        Cell current = goal;
        while (parents.get(current) != null && !parents.get(current).equals(start)) {
            current = parents.get(current);
        }
        return getDirectionFromCells(start, current);
    }

    /**
     * <font color="purple">
     * Megállapítja, hogy egy adott cella érvényes-e a játék állapota alapján.
     * <font color="green">
     * @param gameState A játék aktuális állapota.
     * @param cell A vizsgált cella.
     * @param snakeBody A kígyó testének helyei.
     * @return Igaz, ha a cella érvényes (nem a kígyó testén vagy a falon van), különben hamis.
     */
    private boolean isValidCell(SnakeGameState gameState, Cell cell, Set<Cell> snakeBody) {
        if (cell == null) return false;
        return cell.i >= 0 && cell.j >= 0 &&
                cell.i < rows && cell.j < cols &&
                gameState.board[cell.i][cell.j] != SNAKE &&
                !snakeBody.contains(cell);
    }

    /**
     * <font color="purple">
     * A szomszédos cella meghatározása az aktuális cella és az irány alapján.
     * <font color="green">
     * @param cell Az aktuális cella.
     * @param direction Az irány.
     * @return A szomszédos cella, vagy null, ha nem található.
     */
    private Cell getNeighbor(Cell cell, Direction direction) {
        int x = cell.i + direction.i;
        int y = cell.j + direction.j;

        if (x >= 0 && x < rows && y >= 0 && y < cols) {
            return new Cell(x, y);
        }
        return null;
    }

    /**
     * <font color="purple">
     * Meghatározza a következő irányt két Cell objektum alapján.
     * <font color="green">
     * @param current Az aktuális cella.
     * @param next A következő cella.
     * @return Az irány, amely a két cella közötti mozgást jelzi.
     */
    private Direction getDirectionFromCells(Cell current, Cell next) {
        if (next.i > current.i) return DOWN;
        if (next.i < current.i) return UP;
        if (next.j > current.j) return RIGHT;
        return LEFT;
    }

    /**
     * <font color="purple">
     * Létrehozza a Hamilton-kört a játéktábla méretének megfelelően.
     * A Hamilton-kör azokat a cellákat tartalmazza, amelyeket a kígyónak követnie kell egy
     * meghatározott sorrendben.
     * <font color="green">
     * @param rows A tábla sorainak száma.
     * @param cols A tábla oszlopainak száma.
     * @return A generált Hamilton-kör.
     */
    private int[][] generateHamiltonCycle(int rows, int cols) {
        int[][] cycle = new int[rows][cols];
        int counter = 0;

        for (int i = 0; i < rows; i++) {
            if (i % 2 == 0) {
                for (int j = 0; j < cols; j++) {
                    cycle[i][j] = counter++;
                }
            } else {
                for (int j = cols - 1; j >= 0; j--) {
                    cycle[i][j] = counter++;
                }
            }
        }
        return cycle;
    }
}