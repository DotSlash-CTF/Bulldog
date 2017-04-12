import eu.mihosoft.vrl.v3d.parametrics.*;
import com.neuronrobotics.bowlerstudio.vitamins.Vitamins;
import eu.mihosoft.vrl.v3d.Transform;
import eu.mihosoft.vrl.v3d.Extrude;
import java.nio.file.Paths;
import eu.mihosoft.vrl.v3d.FileUtil;

class Neck {
	ArrayList<CSG> makeNeck() {
		ArrayList<CSG> fullHead = new ArrayList<CSG>()
		CSG channel = generateBody()

		int xSize = 40
		int ySize = 50
		int zSize = 35

		CSG base = new Cube(xSize, ySize, zSize).toCSG()
		CSG screw = Vitamins.get("capScrew","8#32").makeKeepaway(1.0)

		base = base.movex(188).movez(zSize / 2)
		base = base.toXMin().movex(channel.getMaxX() - ((base.getMaxX()-base.getMinX())/2)).difference(channel)
		for (int i = 0; i < 20; i++) {
			base = base.difference(channel)
		}

		base = base.union(screw.roty(180).movex(channel.getMaxX()))
		
		fullHead.add(channel)
		fullHead.add(base)
		return [fullHead]
	}

	public CSG generateBody()
	{
		CSG crossChannel = centerOnX(Vitamins.get("vexCchannel", "5x20").roty(180).rotz(90))
	}

	private CSG centerOnX(CSG start)
	{
		double yWidth = start.getMaxY() - start.getMinY();
		return start.toYMin().movey(-(yWidth / 2));
	}
}
ArrayList<CSG> fullHead = new Neck().makeNeck()

return fullHead