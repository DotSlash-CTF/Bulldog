import eu.mihosoft.vrl.v3d.parametrics.*;
import com.neuronrobotics.bowlerstudio.vitamins.Vitamins;
import eu.mihosoft.vrl.v3d.Transform;
import eu.mihosoft.vrl.v3d.Extrude;
import java.nio.file.Paths;
import eu.mihosoft.vrl.v3d.FileUtil;

class Neck {
	ArrayList<CSG> makeNeck() {
		ArrayList<CSG> fullHead = new ArrayList<CSG>()
		
		CSG channel = Vitamins.get("vexCchannel", "2x20")
		CSG panServo = Vitamins.get( "hobbyServo","hv6214mg")

		CSG base = createBase()

		fullHead.add(channel)
		fullHead.add(panServo)
		fullHead.add(base)
		return [fullHead]
	}

	private CSG createBase() {
		CSG base = new Cube(40, 70, 20).toCSG()
		base = base.movez(10).movey(100)
		
		return base
	}

	//public void parameterChanged(String name, Parameter p){}
}
ArrayList<CSG> fullHead = new Neck().makeNeck()

return fullHead