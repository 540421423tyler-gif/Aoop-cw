public class Tile {
    private int value;
    private final int solutionValue;
    private final boolean isFixed;

    public Tile(int value, int solutionValue, boolean isFixed) {
        this.value = value;
        this.solutionValue = solutionValue;
        this.isFixed = isFixed;
    }

    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }
    public int getSolutionValue() { return solutionValue; }
    public boolean isFixed() { return isFixed; }
}