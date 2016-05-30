package programElements;

public class Square extends Operator {

	private static final long serialVersionUID = 7L;

	public Square() {
		super(1);
	}

	public double performOperation(double... arguments) {
		double res = Math.pow(arguments[0], 2);
		if(Double.isNaN(res) || Double.isInfinite(res)) {
			return arguments[0];
		}
		else {
			return res;
		}
	}

	public String toString() {
		return "Square";
	}
}
