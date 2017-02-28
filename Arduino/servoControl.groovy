import com.neuronrobotics.sdk.addons.gamepad.IJInputEventListener;
import com.neuronrobotics.sdk.addons.gamepad.BowlerJInputDevice;
import net.java.games.input.Component;
import net.java.games.input.Event;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import org.apache.commons.io.IOUtils;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.ui.ConnectionDialog;
import com.neuronrobotics.bowlerstudio.physics.*;
import com.neuronrobotics.bowlerstudio.threed.*;

//read here for instructions: top link is called "pan". Middle link called "mid"
//feel free to change stuff. You will need to plug in the controller for this to work
//lastly, if you get "nullPointerException", un-plug and re-plug the dyio
System.out.println("Starting");
DyIO d=null;
Object dyiodev = DeviceManager.getSpecificDevice(DyIO.class, "dyio");
if(dyiodev==null){
	d=new DyIO();
	if (!ConnectionDialog.getBowlerDevice(d)){
		System.err.println("Dialog failed");
		return;
	}
	DeviceManager.addConnection(d,"dyio");
	
}else{
	d = (DyIO) dyiodev
}

BowlerJInputDevice g=null;// Create a variable to store the device
//Check if the device already exists in the device Manager
if(DeviceManager.getSpecificDevice(BowlerJInputDevice.class, "jogController")==null){
	//BowlerStudio.speak("I did not find a device named jogController. Select a port to connect to the device.");
	//If the device does not exist, prompt for the connection
	Controller validController= null
	def controllerList = ControllerEnvironment.getDefaultEnvironment().getControllers()
	for(Controller  s:controllerList){
		
		if(	!s.toString().toLowerCase().contains("hid") &&
			!s.toString().toLowerCase().contains("keyboard") &&
			!s.toString().toLowerCase().contains("mouse")
		){
			validController = s
			println  "Found : " +s
		}
	}
	if(validController!=null){
		g = new BowlerJInputDevice(validController); // This is the DyIO to talk to.
		g.connect(); // Connect to it.
		// add the device to the maager
		DeviceManager.addConnection(g,"jogController");
	}
}else{
	//the device is already present on the system, load the one that exists.
  g=(BowlerJInputDevice)DeviceManager.getSpecificDevice(BowlerJInputDevice.class, "jogController")
}

MobileBase base;
Object dev = DeviceManager.getSpecificDevice(MobileBase.class, "DogRobot");
println "found: "+dev
//Check if the device already exists in the device Manager

if(dev==null){
	//Create the kinematics model from the xml file describing the D-H compliant parameters. 
	def file=["https://github.com/DotSlash-CTF/Bulldog.git","FullDog/Dog.xml"]as String[]
	String xmlContent = ScriptingEngine.codeFromGit(file[0],file[1])[0]
	MobileBase mb =new MobileBase(IOUtils.toInputStream(xmlContent, "UTF-8"))
	mb.setGitSelfSource(file)
	mb.connect()
	DeviceManager.addConnection(mb,mb.getScriptingName())
	base=mb
	println "Waiting for cad to generate"
}else{
	println "Robot found, runing code"
	//the device is already present on the system, load the one that exists.
  	base=(MobileBase)dev
}

//Set the DyIO into cached mode
d.setCachedMode(true);

ServoChannel pan = new ServoChannel(d.getChannel(11))
ServoChannel tilt = new ServoChannel(d.getChannel(1))
ServoChannel jaw = new ServoChannel(d.getChannel(8))
ServoChannel mid = new ServoChannel(d.getChannel(10))

double move=0;
double turn=0;
boolean direction = true;
int position = 80;
System.out.print(direction);
int joystickVal = 90;
int maximum = 165;
int minimum = 20;
int counter = 0;
int listenerCounter = 1;
int valuesPan = 50;
int valuesMid = 50;
boolean testBool = false;

int milisInASec = 1000;
long time = System.currentTimeMillis();


    

Timer timer = new Timer();
timer.schedule(new TimerTask() {
	boolean dir =false
    public void run() {
	//counter ++;
	if (counter < 5) {
	/*
		if (position > 164) {
			direction = false;
		}
		if (position < 22) {
			direction = true;
		}
		
		counter ++;
		values = WalkMotion(joystickVal, minimum, maximum, direction, position);
		*/
		//position = values;
		if (dir) {
			valuesPan= 160;
			valuesMid = 150;
		}
		else {
			valuesPan= 22;
			valuesMid = 22;
		}
		dir = !dir
		pan.getChannel().setCachedValue(valuesPan);
		mid.getChannel().setCachedValue(valuesMid);
		d.flush(0)
		counter ++;
	}
	else {
		testBool = true;
	}
	if (testBool && counter < 6) {
		pan.getChannel().setCachedValue(104);
		mid.getChannel().setCachedValue(90);
		counter ++;
		d.flush(0);
    }
    
    System.out.println(counter);
    }
}, 0, milisInASec);

// This will update for the current minute, it will be updated again in at most one minute.


IJInputEventListener listener = new IJInputEventListener() {
	@Override public void onEvent(Component comp, Event event1,float value, String eventString) {
		//if (listenerCounter % 10 == 0) {
		
		int val = (int)(93 + 77*value)
		if (val>169)
			val=169
		if(val<20)
			val=20
		try{
			if (comp.getName().equals("y") ){
				
				//if(val<20)
					//val=20
				System.out.println(val);
				
				joystickVal = val;
				//values = WalkMotion(joystickVal, minimum, maximum, direction, position);
				//position = valuesPan;
				//counter ++;
				//pan.getChannel().setCachedValue(values);
				//System.out.println(comp.getName()+" is value= "+values);
				//Thread.sleep(25);
				
				//Thread.sleep(600);
				//pan.getChannel().setCachedValue(values);
				
				
				
				
				
			}else
			if(comp.getName().equals("rz")){
				if(val<80)
					val=80
				System.out.println(comp.getName()+" is value= "+val);
				tilt.getChannel().setCachedValue(val);
			}else
			if(comp.getName().equals("Z Axis")){
				val=val-127+63
				System.out.println(comp.getName()+" is value= "+val);
				if (val>110)
					val=110
				if(val<63)
					val=63
				jaw.getChannel().setCachedValue(val);
			}else
			if(comp.getName().equals("Y Rotation")){
				//move=value
			}else 
			if(comp.getName().equals("X Rotation")){
				turn=value
			}
			else{
				//System.out.println("UNALLOCATED "+comp.getName()+" is value= "+val);
			}
		}catch(Exception e){
			e.printStackTrace(System.out)
		}
	//}
	//else {
		//listenerCounter ++;
		//System.out.println(listenerCounter);
	//}
	}
}
g.clearListeners()
// gamepad is a BowlerJInputDevice
g.addListeners(listener);
// wait while the application is not stopped
println "Running controller top level"


public int WalkMotion (int speed, int min, int max, boolean directionLoop, int position) {
	//Thread.sleep(600);
	this.position = position;
	int speeds = speed * 2;
	 //sets target to middle by default, can change later
	//System.out.println(directionLoop);
	if (directionLoop) {
		//System.out.println("We Made It")
		//System.out.println("the position is" + position)
		if (position <= max) {
			position = max; //sets target 1 above current thing
		//System.out.println("the New position is" + position)
			return position;
		}
		
		//Thread.sleep(20);  //sets wait time 
		 //sets value to servo
			
		}
	System.out.println(directionLoop);
	if (!directionLoop) {
	 if (position > min) {
		//System.out.println("We Made It")
		position = min;
		//Thread.sleep(20);	
		return position;
		
	}	
		
		}
	return position;	
}


double loopTime = 300;

try{
	while(!Thread.interrupted()){
		ThreadUtil.wait((int)loopTime)
		if(Math.abs(move)>0.05 || Math.abs(turn)>0.05){
			//println "Walking "+move+" turn "+turn
			// walk sideways 10 increments of 10 mm totalling 100 mm translation
			RotationNR rot = new RotationNR()
			if(Math.abs(turn)>0.01)
				rot=new RotationNR( 0,0, (2.0 * turn))
			TransformNR m = new TransformNR((10 * move),0,0, rot)
			base.DriveArc(m, loopTime/1000.0);
		}else{
			move=0
			turn=0
			//d.flush(0)
		}
	}
}catch (java.lang.InterruptedException ex){
	//ex.printStackTrace(System.out)
}catch (Exception e){
	e.printStackTrace(System.out)
}
println "Clearing controller top level"
//remove listener and exit
g.removeListeners(listener)
