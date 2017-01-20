
import eu.mihosoft.vrl.v3d.parametrics.*;
import com.neuronrobotics.bowlerstudio.vitamins.*;

LengthParameter bodyLength 		= new LengthParameter("Body Length",100,[200,0])
LengthParameter bodyWidth 		= new LengthParameter("Body Width",50,[200,0])
LengthParameter matThickness 		= new LengthParameter("Material Thickness",10,[200,0])
CSG ribs = new CSG();
double[][] ribVals = [[200, 100, -20], [200, 100, 20], [200, 100, 30]];


void getBodyOffsetLength(double bodyLength)
{
	
}

CSG makeRib(double width, double height, double materialThickness, CSG spine)
{
	CSG base = new Cylinder(1, 1, materialThickness, (int) 30).toCSG();
	CSG center = new Cube(width, 2.5, materialThickness).toCSG().toZMin();
	return base.scalex(width / 2).scaley(height / 2).difference(center).roty(90).difference(spine);
}

CSG makeRibCage(double[][] ribVals, double matThickness, CSG spine)
{
	CSG ribs = makeRib(ribVals[0][0], ribVals[0][1], matThickness, spine).movex(ribVals[0][2]); 
	println("Rib with dimensions " + ribVals[0][0] + " " + ribVals[0][1] + " " + ribVals[0][2]);
	for(int i = 1; i < ribVals.length; i++)
	{
		ribs = ribs.union(makeRib(ribVals[i][0], ribVals[i][1], matThickness, spine).movex(ribVals[i][2]));
		println("Rib with dimensions " + ribVals[i][0] + " " + ribVals[i][1] + " " + ribVals[i][2]);
	}
	return ribs;
}

CSG mainBody = new Cube(bodyLength, bodyWidth, matThickness).toCSG()

mainBody = mainBody	.setParameter(bodyLength)
				.setParameter(bodyWidth)
				.setParameter(matThickness)
				.setRegenerate({new Cube(bodyLength, bodyWidth, matThickness).toCSG()})

ribs = ribs		.setParameter(matThickness)
				.setRegenerate({makeRibCage(ribVals, matThickness, mainBody).toCSG()})

CSG cChannel = Vitamins.get("vexCchannel","5x10").rotz(90)

ribs = makeRibCage(ribVals, matThickness.getMM(), mainBody)
//cChannel = cChannel.movey((cChannel.getMaxY()-cChannel.getMinY())/2)

CSG center = new Cube(5, 240, 5).toCSG().toZMin()
return [ribs, mainBody];
//eturn makeRib(160, 240, 5, mainBody);
//return [cChannel,mainBody]
