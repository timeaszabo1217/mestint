///Timi (JJMS9M),timeaszabo1217@gmail.hu

package game.snake.players;

///Timi,timeaszabo1217@gmail.hu

import game.snake.Direction;
import game.snake.SnakeGame;
import game.snake.SnakePlayer;
import game.snake.utils.Cell;
import game.snake.utils.SnakeGameState;

import java.util.*;

import static game.snake.SnakeGame.*;

public class Agent extends SnakePlayer {

    private final int[][] hamiltonCycle;
    private final int rows;
    private final int cols;

    public Agent(SnakeGameState gameState, int color, Random random) {
        super(gameState, color, random);
        this.rows = gameState.board.length;
        this.cols = gameState.board[0].length;
        this.hamiltonCycle = generateHamiltonCycle(rows, cols);
    }

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

    private int getFValue(Cell cell, Cell food) {
        int g = 0;
        int h = heuristic(cell, food);
        return g + h;
    }

    private int heuristic(Cell current, Cell goal) {
        return Math.abs(current.i - goal.i) + Math.abs(current.j - goal.j);
    }

    private boolean isSafeToFollowCycle(SnakeGameState gameState, int headIndex, int foodIndex) {
        int snakeLength = gameState.snake.size();
        int pathDistance = (foodIndex - headIndex + rows * cols) % (rows * cols);

        return pathDistance <= snakeLength;
    }

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

    private Direction reconstructPathAndGetDirection(Cell start, Cell goal, Map<Cell, Cell> parents) {
        Cell current = goal;
        while (parents.get(current) != null && !parents.get(current).equals(start)) {
            current = parents.get(current);
        }
        return getDirectionFromCells(start, current);
    }

    private boolean isValidCell(SnakeGameState gameState, Cell cell, Set<Cell> snakeBody) {
        if (cell == null) return false;
        return cell.i >= 0 && cell.j >= 0 &&
                cell.i < rows && cell.j < cols &&
                gameState.board[cell.i][cell.j] != SNAKE &&
                !snakeBody.contains(cell);
    }

    private Cell getNeighbor(Cell cell, Direction direction) {
        int x = cell.i + direction.i;
        int y = cell.j + direction.j;

        if (x >= 0 && x < rows && y >= 0 && y < cols) {
            return new Cell(x, y);
        }
        return null;
    }

    private Direction getDirectionFromCells(Cell current, Cell next) {
        if (next.i > current.i) return DOWN;
        if (next.i < current.i) return UP;
        if (next.j > current.j) return RIGHT;
        return LEFT;
    }

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