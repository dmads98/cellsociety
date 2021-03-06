package GridCell;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.Random;

/**
 * Individual simulation created by inheriting from the Grid superclass. This simulation involves sharks, fish, and empty space. Sharks move about the grid looking for fish, and eat when they find them. Sharks reproduce after a certain number of cycles but can also die if they don't find a fish to eat in a certain number of cycles. Fish swim around and reproduce after a certain number of cycles. The most important part of this class is the updateCells() method which is broken into many different methods to make it more readable.
 * @author Connor Ghazaleh
 */

public class PredatorPrey extends Grid {
    public static final int EMPTY = 0;
    public static final int FISH = 1;
    public static final int SHARK = 2;

    private double fishBreedingAge;
    private double sharkBreedingAge;
    private double energyPerFish;
    private double sharkEnergy;
    private HashMap<Integer, Color> stateColorMap;

    /**
     * Create a PredatorPrey
     * @param gridSize
     * @param screenSize
     */
    public PredatorPrey(int gridSize, double screenSize) {
        super(gridSize, screenSize);
    }

    /**
     * Initialize the map assigning colors to states
     */
    @Override
    public void initStateColorMap(){
        HashMap<Integer, Color > colorMap = new HashMap<Integer,Color>();
        colorMap.put(EMPTY,Color.WHITE); //empty
        colorMap.put(FISH,Color.GREEN); //fish
        colorMap.put(SHARK,Color.BLUE); //shark
        stateColorMap = colorMap;
        setStateColorMap(colorMap);
    }

    /**
     * initializes the sliders relevant to the simulation
     */
    @Override
    public void initSliderMap(){

    }

    /**
     * Set additional parameters specific to the current simulation
     * @param params array of doubles specifying parameter values
     */
    @Override
    public void setAdditionalParams(Double[] params){
        fishBreedingAge = params[0];
        sharkBreedingAge = params[1];
        energyPerFish = params[2];
        sharkEnergy = params[3];
        Cell[][] currentGrid = getGrid();
        for (Cell[] row : currentGrid){
            for (Cell cell : row){
                if (cell.getState() == SHARK){
                    cell.setEnergy(sharkEnergy);
                }
            }
        }
        setGrid(currentGrid);
    }

    /**
     * Updates properties of cells to run simulation
     */
    @Override
    public void updateCells(){
        Cell[][] currentGrid = getGrid();
        updateEnergies(currentGrid);
        updateAges(currentGrid);
        ArrayList<Integer[]> sharks = findCellsWithState(currentGrid,SHARK);
        for (Integer[] shark : sharks) {
            updateSharkProperties(currentGrid,shark);
        }
        ArrayList<Integer[]> fish = findCellsWithState(currentGrid,FISH);
        for (Integer[] fishy : fish) {
            updateFishProperties(currentGrid,fishy);
        }
        setGrid(currentGrid);

    }

    private void updateSharkProperties(Cell[][] currentGrid, Integer[] shark){
        boolean didKillShark = killShark(currentGrid, shark);
        if (!didKillShark){
            ArrayList<Integer[]> neighbors = getNeighbors(shark[0],shark[1]);
            ArrayList<Integer[]> emptyNeighbors = new ArrayList<Integer[]>();
            findEmptyNeighbors(neighbors,emptyNeighbors,currentGrid);
            boolean didReproduce = reproduce(shark, currentGrid, emptyNeighbors, neighbors,SHARK,sharkBreedingAge);
            if (!didReproduce){
                feedMoveOrKillShark(neighbors,emptyNeighbors,currentGrid,shark);
            }
        }
    }

    private void updateFishProperties(Cell[][] currentGrid, Integer[] fishy){
        ArrayList<Integer[]> neighbors = getNeighbors(fishy[0],fishy[1]);
        ArrayList<Integer[]> emptyNeighbors = new ArrayList<Integer[]>();
        findEmptyNeighbors(neighbors,emptyNeighbors,currentGrid);
        boolean didReproduce = reproduce(fishy, currentGrid, emptyNeighbors, neighbors,FISH,fishBreedingAge);
        if (!didReproduce){
            moveFish(emptyNeighbors,currentGrid,fishy);
        }
    }


    private void moveFish(ArrayList<Integer[]> emptyNeighbors, Cell[][] currentGrid, Integer[] fishy){
        if (!emptyNeighbors.isEmpty()){
            findEmptyAndSwitch(emptyNeighbors,currentGrid,fishy,FISH);
        }
    }

    private void findEmptyNeighbors(ArrayList<Integer[]> neighbors, ArrayList<Integer[]> emptyNeighbors, Cell[][] currentGrid){
        for (Integer[] neighbor : neighbors){
            if (currentGrid[neighbor[0]][neighbor[1]].getState() == EMPTY) {
                emptyNeighbors.add(neighbor);
            }
        }
    }


    private void feedMoveOrKillShark(ArrayList<Integer[]> neighbors, ArrayList<Integer[]> emptyNeighbors, Cell[][] currentGrid, Integer[] shark){
        boolean foundFish = false;
        for (Integer[] neighbor : neighbors){
            if (currentGrid[neighbor[0]][neighbor[1]].getState() == FISH){
                //eat fish and replenish energy
                currentGrid[shark[0]][shark[1]].setEnergy(currentGrid[shark[0]][shark[1]].getEnergy()+energyPerFish);
                currentGrid[neighbor[0]][neighbor[1]].setAge(0);
                switchSpots(currentGrid,neighbor[0],neighbor[1],SHARK,shark[0],shark[1],EMPTY);
                foundFish = true;
            }else if (currentGrid[neighbor[0]][neighbor[1]].getState() == EMPTY){
                emptyNeighbors.add(neighbor);
            }
        }
        //set new age and move
        if (!foundFish){
            killOrMoveShark(currentGrid,shark,emptyNeighbors);
        }
    }

    private void killOrMoveShark(Cell[][] currentGrid, Integer[] shark, ArrayList<Integer[]> emptyNeighbors){
        if (currentGrid[shark[0]][shark[1]].getEnergy() <= 0){
            setCellState(currentGrid,shark,EMPTY,0,0);
        }else {
            if (!emptyNeighbors.isEmpty()){
                findEmptyAndSwitch(emptyNeighbors,currentGrid,shark,SHARK);
            }
        }
    }

    private void findEmptyAndSwitch(ArrayList<Integer[]> emptyNeighbors, Cell[][] currentGrid, Integer[] pos, int state){
        Random rand = new Random();
        Integer[] emptyCell = emptyNeighbors.get(rand.nextInt(emptyNeighbors.size()));
        switchSpots(currentGrid,emptyCell[0],emptyCell[1],state,pos[0],pos[1],EMPTY);
    }

    private void setCellState(Cell[][] currentGrid, Integer[] pos, int state, double energy, int age){
        currentGrid[pos[0]][pos[1]].setState(state);
        currentGrid[pos[0]][pos[1]].setColor(stateColorMap.get(state));
        currentGrid[pos[0]][pos[1]].setEnergy(energy);
        currentGrid[pos[0]][pos[1]].setAge(age);
    }

    private boolean killShark(Cell[][] currentGrid, Integer[] shark){
        boolean didKillShark = false;
        if (currentGrid[shark[0]][shark[1]].getEnergy() <= 0){
            didKillShark = true;
            setCellState(currentGrid,shark,EMPTY,0,0);
        }
        return didKillShark;
    }

    private ArrayList<Integer[]> findCellsWithState(Cell[][] grid, int state){
        ArrayList<Integer[]> cells = new ArrayList<>();
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid.length; j++) {
                int myState = grid[i][j].getState();
                if (myState == state) {
                    Integer[] cell = {i, j, myState};
                    cells.add(cell);
                }
            }
        }
        return cells;
    }

    private boolean reproduce(Integer[] animal, Cell[][] currentGrid, ArrayList<Integer[]> emptyNeighbors, ArrayList<Integer[]> neighbors, int state, double breedingAge){
        boolean didReproduce = false;
        if (currentGrid[animal[0]][animal[1]].getAge() >= breedingAge){
            didReproduce = true;
            for (Integer[] neighbor : neighbors){
                if (currentGrid[neighbor[0]][neighbor[1]].getState() == EMPTY){
                    emptyNeighbors.add(neighbor);
                }
            }
            if (!emptyNeighbors.isEmpty()){
                Random rand = new Random();
                Integer[] spawnLocation = emptyNeighbors.get(rand.nextInt(emptyNeighbors.size()));
                double energy = 0;
                if (state == SHARK){
                    energy = sharkEnergy;
                }
                setCellState(currentGrid,spawnLocation,state,energy,0);
                emptyNeighbors.remove(spawnLocation);
                currentGrid[animal[0]][animal[1]].setAge(0);
            }
        }
        return didReproduce;
    }

    private void switchSpots(Cell[][] grid, int y1, int x1, int state1, int y2, int x2, int state2){
        //switch energies
        double cell1Energy = grid[y1][x1].getEnergy();
        double cell2Energy = grid[y2][x2].getEnergy();
        //switch ages
        int cell1Age = grid[y1][x1].getAge();
        int cell2Age = grid[y2][x2].getAge();
        setCellState(grid,new Integer[]{y1,x1},state1,cell2Energy,cell2Age);
        setCellState(grid,new Integer[]{y2,x2},state2,cell1Energy,cell1Age);
    }

    private void updateEnergies(Cell[][] temp){
        for (Cell[] row : temp){
            for (Cell cell : row){
                if (cell.getState() == SHARK){
                    cell.setEnergy(cell.getEnergy()-1);
                }
            }
        }
    }

    private void updateAges(Cell[][] temp){
        for (Cell[] row : temp){
            for (Cell cell : row){
                if (cell.getState() == SHARK || cell.getState() == FISH){
                    cell.setAge(cell.getAge()+1);
                }
            }
        }
    }

    public void printEnergies(Cell[][] temp){
        for (Cell[] row : temp){
            for (Cell cell : row){
                System.out.print(cell.getEnergy()+",");
            }
            System.out.println();
        }
        System.out.println();

    }


    private void printAges(Cell[][] temp){
        for (Cell[] row : temp){
            for (Cell cell : row){
                System.out.print(cell.getAge()+",");
            }
            System.out.println();
        }
//        System.out.println();

    }
}

