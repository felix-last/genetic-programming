package programElements;

public class Exponential extends Operator {

	private static final long serialVersionUID = 7L;

	public Exponential() {
		super(2);
	}

	public double performOperation(double... arguments) {
		double res = Math.pow(arguments[0], arguments[1]);
		if(Double.isNaN(res) || Double.isInfinite(res)) {
			return arguments[0];
		}
		else {
			return res;
		}
	}

	public String toString() {
		return "^";
	}
}
