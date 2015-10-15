package mule.model.resources;

/**
 * Created by The Boat on 9/22/2015.
 */
public class Resource {
    private int value;

    public Resource(int value) {
        this.value = value;
    }

    public int getCost() {
        return value;
    }

    public int hashCode() {
        return 31 * value + 11;
    }

}