package mule.model.resources;

public class Energy extends Resource {

    private static final int VALUE = 25;

	public Energy() {
		super(VALUE);
	}

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (this == o) {
            return true;
        } else if (o instanceof Energy) {
            return true;
        }
        return false;
    }

    public String toString() {
        return "Energy";
    }

}