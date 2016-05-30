package programElements;

public class NaturalLogarithm extends Operator {

	private static final long serialVersionUID = 7L;

	public NaturalLogarithm() {
		super(1);
	}

	public double performOperation(double... arguments) {
		double res = Math.log(arguments[0]);
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
