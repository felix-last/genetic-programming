package programElements;

public class Square extends Operator {

	private static final long serialVersionUID = 7L;

	public Square() {
		super(1);
	}

	public double performOperation(double... arguments) {
		return Math.pow(arguments[0], 2);
	}

	public String toString() {
		return "Square";
	}
}
