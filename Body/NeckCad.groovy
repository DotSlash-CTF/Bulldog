import eu.mihosoft.vrl.v3d.parametrics.*;
import com.neuronrobotics.bowlerstudio.vitamins.Vitamins;
import eu.mihosoft.vrl.v3d.Transform;
import eu.mihosoft.vrl.v3d.Extrude;
import java.nio.file.Paths;
import eu.mihosoft.vrl.v3d.FileUtil;

class Neck {
	ArrayList<CSG> makeNeck() {
		ArrayList<CSG> fullHead = new ArrayList<CSG>()

		CSG base = createBase()
		
		fullHead.add(base)
		return [fullHead]
	}

	private CSG createBase() {
		int xSize = 40
		int ySize = 70
		int zSize = 35
		
		CSG base = new Cube(xSize, ySize, zSize).toCSG()
		base = base.movez(zSize/2).movey(100).movex(5)
		
		CSG channel = Vitamins.get("vexCchannel", "2x20")
		CSG panServo = Vitamins.get( "hobbyServo","hv6214mg")
		panServo = panServo.movey(110).movez(30)

		base = base.difference(panServo)
		base = base.union(channel)
		
		return base
	}

	//public void parameterChanged(String name, Parameter p){}
}
ArrayList<CSG> fullHead = new Neck().makeNeck()

return fullHead