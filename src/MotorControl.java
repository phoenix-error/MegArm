import lejos.hardware.Button;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.utility.Delay;

import java.lang.Math;
import lejos.hardware.lcd.LCD;

public class MotorControl {
	public static EV3LargeRegulatedMotor base;
	public static EV3LargeRegulatedMotor shoulder;
	public static EV3LargeRegulatedMotor elbow;
	public static EV3MediumRegulatedMotor gripper;
	public static double upperArmLen = 0;
	public static double lowerArmLen = 0;
	public static double shoulderGearRatio = 125;
	public static double elbowGearRatio = 25;
	public static boolean open = false;
	
	public static void closeGripper() {
		if(open) {
			gripper.rotate(-90, true);  //immidiate Return = true
			while(gripper.isMoving()) {
//				if(gripper.isStalled()) {
//					gripper.stop();
//				}
				//TODO: gripper muss wissen, wie weit er wieder aufmachen soll, wenn er beim Schließen blockiert wurde
			}
			open = false;
		}
	}
	public static void openGripper() {
		if(!open) {
			gripper.rotate(90);
			open = true;
		}
	}
	
	public static void moveTo(double x, double y, double z) {
		double baseAngle = Math.atan2(y, x)*(180/Math.PI);
		double l = Math.sqrt(x*x + y*y);
		double elbowAngle =  Math.acos((l*l + z*z - lowerArmLen*lowerArmLen - upperArmLen*upperArmLen)/ (2*upperArmLen*lowerArmLen))*(180/Math.PI);		
		double gamma = Math.atan2(z, l)*(180/Math.PI);
		double r = Math.sqrt(l*l + z*z);
		double c= Math.cos(elbowAngle/180*Math.PI);
		double an= c  * upperArmLen;
		System.out.println("cos(elbowangle): " + c);
		System.out.println("an: " + an);

		double beta = Math.acos((lowerArmLen+an)/r)*(180/Math.PI);
		double shoulderAngle = beta + gamma;
		
		base.rotateTo((int)Math.round(baseAngle));
		elbow.rotateTo((int)Math.round(elbowAngle));
		shoulder.rotateTo((int)Math.round(shoulderAngle));
		while(base.isMoving() || elbow.isMoving() || shoulder.isMoving()){ /*wait*/ } 
	}
	
	public static void btn2xyzControl() {
		LCD.clearDisplay();
		LCD.drawString("move to (0, 0, 0) ", 0, 0);
		LCD.drawString("and press escape", 0, 1);
		btn2jointControl();
		while(Button.getButtons() != 0) {/*wait*/}
		base.resetTachoCount();
		shoulder.resetTachoCount();
		elbow.resetTachoCount();
		
		LCD.clearDisplay();
		LCD.drawString("btn2xyzControl", 0, 0);
		int x, y, z;
		x = y = z = 0;
		int direction = 0;	//0 = x, 1 = y, 2 = z, 3 = gripper
		int button = 0;
		boolean change = false;
		while(true) {		// loop for choosing axis
			change = false;
			button = Button.getButtons();
			if(button == Button.ID_UP) {
				if(direction < 3) direction++;
				change = true;
				while(Button.UP.isDown()) {/*wait*/}
			}
			if(button == Button.ID_DOWN) {
				if(direction > 0) direction--;
				change = true;
				while(Button.DOWN.isDown()) {/*wait*/}
			}
			if(button == Button.ID_ESCAPE) {
				break;
			}
			
			while(Button.LEFT.isDown()) {	
				switch (direction) {
					case 0:
						x--;
						break;
					case 1:
						y--;
						break;
					case 2: 
						z--;
						break;
					case 3:
						closeGripper();
						break;
					default: System.out.println("no direction");
				}
				Delay.msDelay(200);
				change = true;
			}
			while(Button.RIGHT.isDown()) {
				switch (direction) {
					case 0: 
						x++;
						break;
					case 1: 
						y++;
						break;
					case 2: 
						z++;
						break;
					case 3: 
						openGripper();
						break;
					default: System.out.println("no direction");
				}
				Delay.msDelay(200);
				change = true;
			}
			if(change) {
				String[] xyz = {"|x: " + x + "| y: " + y + " z: " + z,
						"x: " + x + " |y: " + y + "| z: " + z,
						"x: " + x + " y: " + y + " |z: " + z + "|",
						"gripper"};
				LCD.clear(4);
				LCD.drawString(xyz[direction], 0, 4);
				moveTo(x, y, z);
			}
		}
	}
	
	public static void teachMode() {
		return;
	}

	public static void btn2jointControl() {
		int mode = 0;	//0 = base, 1 = shoulder, 2 = elbow, 3 = gripper
		int button = 0;
		String[] joints = {
				"base",
				"shoulder",
				"elbow",
				"gripper"
		};
		while(true) {
			button = Button.getButtons();
					
			if(button == Button.ID_LEFT) {
				if (mode == 0) {
					LCD.clear(4);
					LCD.drawString("base left", 0, 4);
					base.forward();
					while(Button.LEFT.isDown() && !base.isStalled()) {/*wait*/}
					base.stop();
				} else if(mode == 1) {
					LCD.clear(4);
					LCD.drawString("shoulder up", 0, 4);
					shoulder.forward();
					while(Button.LEFT.isDown() && !base.isStalled()) {/*wait*/}
					shoulder.stop();
				} else if(mode == 2){
					LCD.clear(4);
					LCD.drawString("elbow up", 0, 4);
					elbow.forward();
					while(Button.LEFT.isDown() && !base.isStalled()) {/*wait*/}
					elbow.stop();
				} else if(mode == 3) {
					LCD.clear(4);
					LCD.drawString("gripper open", 0, 4);
					openGripper();
				}			
			}
			if(button == Button.ID_RIGHT) {
				if (mode == 0) {
					LCD.clear(4);
					LCD.drawString("base right", 0, 4);
					base.backward();
					while(Button.RIGHT.isDown() && !base.isStalled()) {/*wait*/}
					base.stop();
				} else if(mode == 1) {
					LCD.clear(4);
					LCD.drawString("shoulder down", 0, 4);
					shoulder.backward();
					while(Button.RIGHT.isDown() && !base.isStalled()) {/*wait*/}
					shoulder.stop();
				} else if(mode == 2){
					LCD.clear(4);
					LCD.drawString("elbow down", 0, 4);
					elbow.backward();
					while(Button.RIGHT.isDown() && !base.isStalled()) {/*wait*/}
					elbow.stop();
				} else if(mode == 3){
					LCD.clear(4);
					LCD.drawString("gripper close", 0, 4);
					closeGripper();
				}
			}
			
			if(button == Button.ID_UP) {
				if(mode < 3) mode++;
				LCD.clear(2);
				LCD.drawString(joints[mode], 0, 2);
				while(Button.UP.isDown()) {/*wait*/}
			}
			if(button == Button.ID_DOWN) {
				if(mode > 0) mode--;
				LCD.clear(2);
				LCD.drawString(joints[mode], 0, 2);
				while(Button.DOWN.isDown()) {/*wait*/}
			}
			if(button == Button.ID_ESCAPE) {
				break;
			}
		}
	}

	public static void main(String[] args) {		
		base = new EV3LargeRegulatedMotor(MotorPort.A);
		shoulder = new EV3LargeRegulatedMotor(MotorPort.B);
		elbow = new EV3LargeRegulatedMotor(MotorPort.C);
		gripper = new EV3MediumRegulatedMotor(MotorPort.D);
		base.setSpeed(90);
		debugPrint(shoulder.getSpeed());				//die Funktion wäre gut
		String [] header = {"quit",
							"btn2jointControl",
							"btn2xyzControl",
							"teachMode"};
		boolean quit = false;
		int button = 0;
		
		// choose the mode from the menu
		while(!quit) {
			int menu = 0;		//0 = quit, 1 = btn2jointControl, 2 = btn2xyzControl, 3 = teachMode
			while(true) {
				button = Button.getButtons();
				if(button == Button.ID_UP) {
					if(menu < 3) menu++;
					LCD.clear();
					LCD.drawString(header[menu], 0, 0);
					LCD.drawString("Enter to choose", 0, 4);
					while(Button.UP.isDown()) {/*wait*/}
				}
				if(button == Button.ID_DOWN) {
					if(menu > 0) menu--;
					LCD.clear();
					LCD.drawString(header[menu], 0, 0);
					LCD.drawString("Enter to choose", 0, 4);
					while(Button.DOWN.isDown()) {/*wait*/}
				}
				if(button == Button.ID_ENTER) {
					break;
				}
			}
			
			// evaluate the chosen menu mode
			switch (menu) {
			case 0:
				quit = true;
				break;
			case 1: 
				btn2jointControl();
				break;
			case 2:
				btn2xyzControl();
				break;
			case 3:
				teachMode();
				break;
			default:
				break;
			}
		}
	}
}
