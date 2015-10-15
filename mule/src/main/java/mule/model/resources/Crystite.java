package mule.model.resources;

public class Crystite extends Resource {

    private static final int VALUE = 100;

	public Crystite() {
		super(VALUE);
	}

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (this == o) {
            return true;
        } else if (o instanceof Crystite) {
            return true;
        }
        return false;
    }

    public String toString() {
        return "Crystite";
    }

}