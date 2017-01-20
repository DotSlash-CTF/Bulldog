import com.neuronrobotics.sdk.addons.gamepad.IJInputEventListener;
import com.neuronrobotics.sdk.addons.gamepad.BowlerJInputDevice;
import net.java.games.input.Component;
import net.java.games.input.Event;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

BowlerJInputDevice g=null;// Create a variable to store the device
//Check if the device already exists in the device Manager
if(DeviceManager.getSpecificDevice(BowlerJInputDevice.class, "jogController")==null){
	BowlerStudio.speak("I did not find a device named jogController. Select a port to connect to the device.");
	//If the device does not exist, prompt for the connection
	g = new BowlerJInputDevice(ControllerEnvironment.getDefaultEnvironment().getControllers()[0]); // This is the DyIO to talk to.
	g.connect(); // Connect to it.
	// add the device to the maager
	DeviceManager.addConnection(g,"jogController");
}else{
	//the device is already present on the system, load the one that exists.
  g=(BowlerJInputDevice)DeviceManager.getSpecificDevice(BowlerJInputDevice.class, "jogController")
}
//Set the DyIO into cached mode
//dyio.setCachedMode(true);

ServoChannel pan = new ServoChannel(dyio.getChannel(9))
ServoChannel tilt = new ServoChannel(dyio.getChannel(10))

IJInputEventListener listener = new IJInputEventListener() {
	@Override public void onEvent(Component comp, Event event1,float value, String eventString) {
		int val = (int)(93+77*value)
		try{
			if(comp.getName().equals("x")){
				//System.out.println(comp.getName()+" is value= "+value);
				System.out.println(val);
				pan.SetPosition(val);
			}
			if(comp.getName().equals("y")){
				//System.out.println(comp.getName()+" is value= "+value);
				tilt.SetPosition(val);
			}
		}catch(Exception e){
			e.printStackTrace(System.out)
		}
		System.out.println(comp.getName()+" is value= "+value);
	}
}
g.clearListeners()
// gamepad is a BowlerJInputDevice
g.addListeners(listener);
// wait while the application is not stopped
while(!Thread.interrupted()){
	ThreadUtil.wait(20)
	//dyio.flush(0)
}

System.out.println("DONE");
