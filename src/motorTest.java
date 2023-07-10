import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.utility.Delay;
import lejos.hardware.Button;


public class motorTest {
	public static EV3LargeRegulatedMotor base;
	public static EV3LargeRegulatedMotor shoulder;
	public static EV3LargeRegulatedMotor elbow;
	public static EV3MediumRegulatedMotor gripper;
	public static double upperArmLen = 19;
	public static double lowerArmLen = 17.5;
	public static double shoulderGearRatio = 125;
	public static double elbowGearRatio = 25;
	
	public static void moveTo(double x, double y, double z) {
		double baseAngle = Math.atan2(y, x)*(180/Math.PI);
		double l = Math.sqrt(x*x + y*y);
		double elbowAngle =  Math.acos((l*l + z*z - lowerArmLen*lowerArmLen - upperArmLen*upperArmLen)/ (2*upperArmLen*lowerArmLen))*(180/Math.PI);		
		double gamma = Math.atan2(z, l)*(180/Math.PI);
		double r = Math.sqrt(l*l + z*z);
		double c= Math.cos(elbowAngle/180*Math.PI);
		double an= c  * upperArmLen;
//		System.out.println("cos(elbowangle): " + c);
//		System.out.println("an: " + an);

		double beta = Math.acos((lowerArmLen+an)/r)*(180/Math.PI);
		double shoulderAngle = beta + gamma;
		
//		base.rotateTo((int)Math.round(baseAngle)*3);
//		elbow.rotateTo((int)(Math.round(elbowAngle)*elbowGearRatio));
//		shoulder.rotateTo(-(int)(Math.round(shoulderAngle)*shoulderGearRatio));
//		while(base.isMoving() || elbow.isMoving() || shoulder.isMoving()){ /*wait*/ } 
		
		System.out.println("B " + (int)baseAngle+ " s "+ (int)shoulderAngle*shoulderGearRatio + " e " + (int)elbowAngle*elbowGearRatio);
	}
	
	public static void main(String[] args) {		
		base = new EV3LargeRegulatedMotor(MotorPort.A);
		shoulder = new EV3LargeRegulatedMotor(MotorPort.B);
		elbow = new EV3LargeRegulatedMotor(MotorPort.C);
		gripper = new EV3MediumRegulatedMotor(MotorPort.D);
		base.setSpeed(90);
		shoulder.setSpeed(90);
		elbow.setSpeed(90);
		
		System.out.println("shoulder pos: " + shoulder.getPosition());
		shoulder.resetTachoCount();
		System.out.println("shoulder pos a. reset: " + shoulder.getPosition());
		shoulder.rotateTo(90);
		System.out.println("shoulder pos x90x: " + shoulder.getPosition());
		shoulder.rotateTo(120);
		System.out.println("shoulder pos x120x: " + shoulder.getPosition());
		shoulder.rotateTo(0);
		System.out.println("shoulder pos x0x: " + shoulder.getPosition());
		System.out.println("------------------------");

		shoulder.resetTachoCount();
		System.out.println("shoulder pos: " + shoulder.getPosition());
		shoulder.resetTachoCount();
		System.out.println("shoulder pos a. reset: " + shoulder.getPosition());
		shoulder.rotate(90);
		System.out.println("shoulder pos x90x: " + shoulder.getPosition());
		shoulder.rotate(120);
		System.out.println("shoulder pos x120x: " + shoulder.getPosition());
		shoulder.rotateTo(0);
		System.out.println("shoulder pos x0x: " + shoulder.getPosition());
		
		while(Button.getButtons() == 0) {/*wait*/}

		
	}

}
