import lejos.hardware.Button;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.utility.Delay;
import java.util.LinkedList;

import java.lang.Math;
import lejos.hardware.lcd.LCD;

class position {
	public int pos_x,pos_y,pos_z;
	position(int _pos_x, int _pos_y, int _pos_z) {
		pos_x = _pos_x;
		pos_y = _pos_y;
		pos_z = _pos_z;
	}
}

class MegArm {
	private EV3LargeRegulatedMotor base;
	private EV3LargeRegulatedMotor shoulder;
	private EV3LargeRegulatedMotor elbow;
	private EV3MediumRegulatedMotor gripper;
	private double upperArmLen = 19;
	private double lowerArmLen = 17.5;
	private double shoulderGearRatio = 125;//125
	private double elbowGearRatio = 25;//25
	private double baseGearRatio = 3;
	private int maxMotorSpeed = 900; 	// max Speet the shoulder motor can reach (max 100*battery voltage). elbowSpeed=1/5 of it.
	private double maxRange = 35; // max Range, the MegArm can reach
	private double minRange = 1; // min Range, the MegArm can reach
	private boolean open = true;
//	private double zOffset = 17.5; 
	
	// start Point: (0, upperArmLen, lowerArmLen)
	double currBaAngle = 90;
	double currShAngle = 90;
	double currElAngle = 90;

	// how far the gripper has rotated when it last tried closing
	// is initialized with -90 so it opens properly when openGripper() is called before closeGripper()
	// because we assume the gripper starts closed
	private int rotatedDistance_Gripper = -90;

	// the x,y and z coords of the last saved Position
	private LinkedList<position> saved_Positions = new LinkedList<position>();

	private int x,y,z;
	
	private void changeX(String plusminus) {
		if(plusminus == "+" && x < Math.sqrt(Math.pow(maxRange, 2) - Math.pow(y, 2) - Math.pow(z, 2))-1) { //sphere around the Base with radius maxRange
			x++;
		}
		else if(plusminus == "-" && x > Math.sqrt(Math.pow(minRange, 2) - Math.pow(y, 2) - Math.pow(z, 2))+1) { //sphere around the Base with radius minRange
			x--;
		}
	}
	private void changeY(String plusminus) {
		if(plusminus == "+" && y < Math.sqrt(Math.pow(maxRange, 2) - Math.pow(x, 2) - Math.pow(z, 2))-1) {
			y++;
		}
		else if(plusminus == "-" && y > Math.sqrt(Math.pow(minRange, 2) - Math.pow(x, 2) - Math.pow(z, 2))+1) {
			y--;
		}
	}
	private void changeZ(String plusminus) {
		if(plusminus == "+" && z < Math.sqrt(Math.pow(maxRange, 2) - Math.pow(y, 2) - Math.pow(x, 2))-1) {
			z++;
		}
		else if(plusminus == "-" && z > Math.sqrt(Math.pow(minRange, 2) - Math.pow(y, 2) - Math.pow(x, 2))+1) {
			z--;
		}
	}


	// closes the gripper
	// if it detects a stall it stops. Whether it stopped or not it remembers how far it rotated.
	public void closeGripper() {
		if(open) {
			gripper.resetTachoCount();
			gripper.rotate(-120, true);  //immidiate Return = true
			while(gripper.isMoving()) {
				if(gripper.isStalled()) {
					gripper.stop();
				}
				//TODO: gripper muss wissen, wie weit er wieder aufmachen soll, wenn er beim Schließen blockiert wurde
			}
			gripper.stop();

			rotatedDistance_Gripper = gripper.getTachoCount();
			open = false;
		}
	}
	// opens the gripper if it was closed i.e. it rotates rotatedDistance_Gripper back
	public void openGripper() {
//		if(!open) {
//			gripper.rotate(-rotatedDistance_Gripper);
//			open = true;
//		}
		gripper.rotate(5);
	}
	
	public void moveTo(double x, double y, double z) {
		double baseAngle = Math.atan2(y, x)*(180/Math.PI);
		double l = Math.sqrt(x*x + y*y);
		double elbowAngle =  Math.acos((l*l + z*z - lowerArmLen*lowerArmLen - upperArmLen*upperArmLen)/ (2*upperArmLen*lowerArmLen))*(180/Math.PI);		
		double gamma = Math.atan2(z, l)*(180/Math.PI);
		double r = Math.sqrt(l*l + z*z);
		double beta = Math.acos((lowerArmLen*lowerArmLen+r*r-upperArmLen*upperArmLen)/(2*lowerArmLen*r))*(180/Math.PI);
		double shoulderAngle = beta + gamma;
		
		base.rotate((int)(Math.round(baseAngle - currBaAngle)*baseGearRatio), true);		//immediateReturn = true
		elbow.rotate((int)(Math.round(elbowAngle - currElAngle)*elbowGearRatio), true);
		shoulder.rotate((int)(Math.round(shoulderAngle - currShAngle)*shoulderGearRatio), true);
		
		while(base.isMoving() || elbow.isMoving() || shoulder.isMoving()){ /*wait*/ }
		
		currBaAngle = baseAngle;
		currElAngle = elbowAngle;
		currShAngle = shoulderAngle;
	}
	
	public void btn2xyzControl() {
		LCD.clearDisplay();
		LCD.drawString("move to (0, 17.5, 19) ", 0, 0); //all angles at 90 degrees
		LCD.drawString("and press escape", 0, 1);
		btn2jointControl();
		
		while(Button.getButtons() != 0) {/*wait*/}
		currBaAngle = 90;
		currShAngle = 90;
		currElAngle = 90;
		
		LCD.clearDisplay();
		LCD.drawString("btn2xyzControl", 0, 0);
		x = 0; y = (int)upperArmLen; z = (int)lowerArmLen;
		
		int direction = 0;	//0 = x, 1 = y, 2 = z, 3 = gripper, 4 = saving/returning to position
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
						changeX("-");
						break;
					case 1:
						changeY("-");
						break;
					case 2: 
						changeZ("-");
						break;
					case 3:
						closeGripper();
						break;
						/*
					case 4:
						// setting the coords to the save ones, moving after button is no longer pressed
						x = savedPosition_X;
						y = savedPosition_Y;
						z = savedPosition_Z;
						System.out.println("release button to move to saved position");
						break;
					*/
					default: System.out.println("no direction");
				}
				Delay.msDelay(200);
				change = true;
			}
			while(Button.RIGHT.isDown()) {
				switch (direction) {
					case 0: 
						changeX("+");
						break;
					case 1: 
						changeY("+");
						break;
					case 2: 
						changeZ("+");
						break;
					case 3: 
						openGripper();
						break;
						/*
					case 4:
						if(!change) {
							savedPosition_X = x;
							savedPosition_Y = y;
							savedPosition_Z = z;
						} else {
							System.out.println("release button to move to saved position");
						}
						break;
						*/
					default: System.out.println("no direction");
				}
				Delay.msDelay(200);
				change = true;
			}
			if(change) {
				String[] xyz = {"|x: " + x + "| y: " + y + " z: " + z,
						"x: " + x + " |y: " + y + "| z: " + z,
						"x: " + x + " y: " + y + " |z: " + z + "|",
						"gripper",};
				LCD.clear(4);
				LCD.drawString(xyz[direction], 0, 4);
				moveTo(x, y, z);
			}
		}
	}
	
	public void teachMode() {
		// setup
		LCD.clear(4);
		LCD.drawString("teachmode", 0, 0);
		LCD.drawString("left to return",0,1);
		LCD.drawString("right to save",0,2);
		// initializing Button
		int button = 0;
		button = Button.getButtons();

		// main loop
		while(true) {

			if(button == Button.ID_ESCAPE) {
				break;
			}

			// press right to add new position
			// press left to return to saved positions
			if(Button.LEFT.isDown()) {
				if(saved_Positions.size() == 0) {
					LCD.drawString("no saved positions",0,3);
					break;
				}
				for (position p: saved_Positions){
					moveTo(p.pos_x,p.pos_y,p.pos_z);
					x = p.pos_x;
					y = p.pos_y;
					z = p.pos_z;
					LCD.drawString("moved to "+ x +" " + y +" " + " " + z, 0, 5 );
					Delay.msDelay(1000);
					LCD.clear();

				}
				LCD.clear(4);
				LCD.drawString("teachmode", 0, 0);
				LCD.drawString("left to return",0,1);
				LCD.drawString("right to save",0,2);
				Delay.msDelay(2000);
			} else if(Button.RIGHT.isDown()) {
				// drive to position in xyz mode
				btn2xyzControl();
				saved_Positions.addFirst(new position(x,y,z));
				LCD.clear(4);
				LCD.drawString("position saved",0,0);
				Delay.msDelay(2000);
				LCD.clear();
				LCD.drawString("teachmode", 0, 0);
				LCD.drawString("left to return",0,1);
				LCD.drawString("right to save",0,2);
			}
		}
	}

	public void btn2jointControl() {
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
	public void init() {
		base = new EV3LargeRegulatedMotor(MotorPort.A);
		shoulder = new EV3LargeRegulatedMotor(MotorPort.B);
		elbow = new EV3LargeRegulatedMotor(MotorPort.C);
		gripper = new EV3MediumRegulatedMotor(MotorPort.D);
		base.setSpeed(90);
		shoulder.setSpeed(maxMotorSpeed);
		elbow.setSpeed(maxMotorSpeed/5);
	}
}

public class motorControl {
	public static void main(String[] args) {		
		MegArm arm = new MegArm();
		arm.init();
		
//		debugPrint(shoulder.getSpeed());				//die Funktion wäre gut
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
				arm.btn2jointControl();
				break;
			case 2:
				arm.btn2xyzControl();
				break;
			case 3:
				arm.teachMode();
				break;
			default:
				break;
			}
		}
	}
}