import eu.mihosoft.vrl.v3d.parametrics.*;
import com.neuronrobotics.bowlerstudio.vitamins.Vitamins;
import eu.mihosoft.vrl.v3d.Transform;
import eu.mihosoft.vrl.v3d.Extrude;
import java.nio.file.Paths;
import eu.mihosoft.vrl.v3d.FileUtil;

class Neck {
	ArrayList<CSG> makeNeck() {
		ArrayList<CSG> fullHead = new ArrayList<CSG>()
		ArrayList<CSG> channel = createBase()

		int xSize = 40
		int ySize = 50
		int zSize = 35

		CSG base = new Cube(xSize, ySize, zSize).toCSG()
		CSG screw = Vitamins.get("capScrew","8#32").makeKeepaway(1.0)

		base = base.movex(188).movez(88.3)
		base = base.union(new Cube(xSize/2, ySize, zSize/2).toCSG().movex(164.2).movez(78)).setColor(javafx.scene.paint.Color.WHITE)
		base = base.difference(channel)
		for (int i = 0; i < 20; i++) {
			base = base.difference(channel.get(1).movey(i))
		}

		base = base.union(screw.roty(180).movex(186).movez(70))
		
		fullHead.add(channel)
		fullHead.add(base)
		return [fullHead]
	}

	private ArrayList<CSG> createBase() {
		ArrayList<CSG> base = ScriptingEngine.gitScriptRun("https://github.com/DotSlash-CTF/Bulldog.git", "FullDog/laserCutCad.groovy", null).generateBody(new MobileBase());//Vitamins.get("vexCchannel", "2x20")
		
		return base
	}
}
ArrayList<CSG> fullHead = new Neck().makeNeck()

return fullHead