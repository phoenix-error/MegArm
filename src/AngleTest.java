
public class AngleTest {
	public static double lowerArmLen = 10.5;
	public static double upperArmLen = 7;
	
	public static void moveTo(double x, double y, double z) {
		double baseAngle = Math.atan2(y, x)*(180/Math.PI);
		double l = Math.sqrt(x*x + y*y);
		double elbowAngle =  Math.acos((l*l + z*z - lowerArmLen*lowerArmLen - upperArmLen*upperArmLen)/ (2*upperArmLen*lowerArmLen))*(180/Math.PI);
		
		double gamma = Math.atan2(z, l)*(180/Math.PI);
		double r = Math.sqrt(l*l + z*z);
		
		double beta = Math.acos((lowerArmLen*lowerArmLen+r*r-upperArmLen*upperArmLen)/(2*lowerArmLen*r))*(180/Math.PI);
		double shoulderAngle = beta + gamma;

		System.out.println("baseAngle: " + baseAngle);
		System.out.println("r: " + l);
		System.out.println("elbowAngle: " + elbowAngle);
		System.out.println("shoulderAngle: " + shoulderAngle);
		System.out.println("beta: " + beta);
	}
	
	public static void main(String[] args) {
		moveTo(0,  9,  9);
	}
}
