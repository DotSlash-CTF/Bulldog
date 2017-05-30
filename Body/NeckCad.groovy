import eu.mihosoft.vrl.v3d.parametrics.*;
import com.neuronrobotics.bowlerstudio.vitamins.Vitamins;
import eu.mihosoft.vrl.v3d.Transform;
import eu.mihosoft.vrl.v3d.Extrude;
import java.nio.file.Paths;
import eu.mihosoft.vrl.v3d.FileUtil;

class Neck implements ICadGenerator {
	ArrayList<CSG> makeNeck() {
		ArrayList<CSG> fullHead = new ArrayList<CSG>()
		CSG channel = generateBody()

		int xSize = 70
		int ySize = (channel.getMaxY() - channel.getMinY())
		int zSize = 37

		CSG base = new Cube(xSize, ySize, zSize).toCSG()
		CSG screw0 = Vitamins.get("capScrew","8#32").makeKeepaway(-1.0).scalez(2).movez(0.3)
		CSG screw1 = screw0.movey(12.7*2).movex(6.5)
		CSG screw2 = screw0.movey(12.7*2).movex(6.5+12.7*2)
		CSG screw3 = screw0.movey(-12.7*2).movex(6.5+12.7*2)
		screw0 = screw0.movey(-12.7*2).movex(6.5)
		CSG screw = screw0.union(screw1.union(screw2.union(screw3)))

		CSG servo = Vitamins.get("hobbyServo","hv6214mg")
		//CSG horn = Vitamins.get("hobbyServo","hv6214mg_1")

		servo = servo.rotz(90).toZMin().movex((servo.getMaxX()-servo.getMinX())/2)

		base = base.movez((zSize / 2) + 0.8 - 2)
		base = base.difference(servo.movez(-2)).difference(servo.movez(-5))
		base = base.difference(new Cube(10, 21, 32).toCSG().movex(-24).toZMin().movez(-2)).difference(new Cube(10, 21, 32).toCSG().movex(24).toZMin().movez(-2))
		base = base.toXMin().movex(channel.getMaxX() - ((base.getMaxX()-base.getMinX())/2)).movez(2).difference(channel)
		
		for (int i = 0; i < 20; i++) {
			base = base.difference(channel)
		}

		base = base.difference(screw.roty(180).movex(channel.getMaxX()))
		
		//fullHead.add(channel)
		//fullHead.add(servo)
		fullHead.add(base)
		return [fullHead.collect{it.toXMin()}]
	}

	@Override
	public ArrayList<CSG> generateCad(DHParameterKinematics d, int linkIndex) {
		return makeNeck()
	}
	
	@Override 
	public ArrayList<CSG> generateBody(MobileBase b ) {
		return new ArrayList<CSG>()
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
return new Neck()

//474 in LegMethods