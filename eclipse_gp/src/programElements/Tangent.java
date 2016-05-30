package programElements;

public class Tangent extends Operator {

	private static final long serialVersionUID = 7L;

	public Tangent() {
		super(1);
	}

	public double performOperation(double... arguments) {
		return Math.tan(arguments[0]);
	}

	public String toString() {
		return "Tan";
	}
}
