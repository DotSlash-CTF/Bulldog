import eu.mihosoft.vrl.v3d.parametrics.*;
import com.neuronrobotics.bowlerstudio.vitamins.*;

LengthParameter bodyLength 		= new LengthParameter("Body Length",100,[200,0])
LengthParameter bodyWidth 		= new LengthParameter("Body Width",50,[200,0])
LengthParameter matThickness 		= new LengthParameter("Material Thickness",2.5,[200,0])
double[][] ribVals = [[150, 100, -20]]//,  [200, 100, 30]];

CSG solidRectFromCSG(CSG start)
{
	double xDist = start.getMaxX() - start.getMinX();
	double yDist = start.getMaxY() - start.getMinY();
	double zDist = start.getMaxZ() - start.getMinZ();

	return new Cube(xDist, yDist, zDist).toCSG()
}

CSG centerOnX(CSG start)
{
	double yWidth = start.getMaxY() - start.getMinY();
	return start.toYMin().movey(-(yWidth / 2));
}
CSG centerOnY(CSG start)
{
	double xWidth = start.getMaxX() - start.getMinX();
	return start.toXMin().movex(-(xWidth / 2));
}
CSG centerOnZ(CSG start)
{
	double zWidth = start.getMaxZ() - start.getMinZ();
	return start.toZMin().movez(-(zWidth / 2));
}
CSG centerOnAxes(CSG start)
{
	return centerOnY(centerOnX(centerOnZ(start)));
}

CSG makeRib(double height, double width, double materialThickness, CSG spine)
{
	CSG base = new Cylinder(1, 1, materialThickness, (int) 30).toCSG();
	CSG center = new Cube(height, 2.5, materialThickness).toCSG().toZMin();
	return base.scalex(height / 2).scaley(width / 2).difference(center).roty(90).difference(spine);
}

//FOR RIB TO ATTACH TO C CHANNELS
CSG makeVexRib(double height, double width, double materialThickness, CSG spine)
{
	CSG screwType = Vitamins.get("capScrew","8#32").makeKeepaway(1.0);
	
	CSG base = new Cylinder(1, 1, materialThickness, (int) 30).toCSG();
	CSG center = new Cube(2.5, height, 50 + materialThickness).toCSG().toZMin();
	base = base.scalex(height / 2).scaley(width / 2).difference(center).roty(90).difference(spine);
	//Making attach point for vex parts
	double outerRadius = Vitamins.getConfiguration("capScrew","8#32").outerDiameter / 2;
	CSG attachPoint = new Cylinder(outerRadius * 2, 3, 30).toCSG().difference(screwType.movez(2.5));
	CSG attachPoint2 = attachPoint.clone();

	attachPoint = attachPoint.movey(- (spine.getMaxY() - spine.getMinY()) * (89 / 224) ).movez( (spine.getMaxZ() - spine.getMinZ()) / 2).movex(outerRadius) //NOT PERFECTLY PARAMETERIZED
	attachPoint2 = attachPoint2.movey( (spine.getMaxY() - spine.getMinY()) * (89 / 224) ).movez( (spine.getMaxZ() - spine.getMinZ()) / 2).movex(outerRadius) //DITTO

	for(int i = 0; i < 6; i++)
	{
		base = base.difference(attachPoint.hull().movez(2 * i)).difference(attachPoint2.hull().movez(2 * i))
	}

	//base = base.difference(attachPoint.hull()).difference(attachPoint.hull().scalez(5)).difference(attachPoint2.hull()).difference(attachPoint2.hull().movez(3))
	attachPoint = attachPoint.difference(new Cylinder(outerRadius, 5, 30).toCSG().movey(- (spine.getMaxY() - spine.getMinY()) * (89 / 224)).movez( (spine.getMaxZ() - spine.getMinZ()) / 2).movex(outerRadius))
	attachPoint2 = attachPoint2.difference(new Cylinder(outerRadius, 5, 30).toCSG().movey( (spine.getMaxY() - spine.getMinY()) * (89 / 224)).movez( (spine.getMaxZ() - spine.getMinZ()) / 2).movex(outerRadius))
	
	base = base.union(attachPoint).union(attachPoint2);
	return base.difference(new Cube(100, 100, 100).toCSG().toZMin().movez(-100))
}

CSG makeRibCage(double[][] ribVals, double matThickness, CSG spine)
{
	spine = solidRectFromCSG(spine)
	CSG ribs = makeRib(ribVals[0][0], ribVals[0][1], matThickness, spine).movex(ribVals[0][2]); 
	println("Rib with dimensions " + ribVals[0][0] + " " + ribVals[0][1] + " " + matThickness + " " + ribVals[0][2]);
	for(int i = 1; i < ribVals.length; i++)
	{
		ribs = ribs.union(makeRib(ribVals[i][0], ribVals[i][1], matThickness, spine).movex(ribVals[i][2]));
		println("Rib with dimensions " + ribVals[i][0] + " " + ribVals[i][1] + " " + matThickness + " " + ribVals[i][2]);
	}
	return ribs;
}

CSG makeVexRibCage(double[][] ribVals, double matThickness, CSG spine)
{
	spine = solidRectFromCSG(spine)
	CSG ribs = makeVexRib(ribVals[0][0], ribVals[0][1], matThickness, spine).movex(ribVals[0][2]); 
	println("Rib with dimensions " + ribVals[0][0] + " " + ribVals[0][1] + " " + matThickness + " " + ribVals[0][2]);
	for(int i = 1; i < ribVals.length; i++)
	{
		ribs = ribs.union(makeVexRib(ribVals[i][0], ribVals[i][1], matThickness, spine).movex(ribVals[i][2]));
		println("Rib with dimensions " + ribVals[i][0] + " " + ribVals[i][1] + " " + matThickness + " " + ribVals[i][2]);
	}
	return ribs;
}

//CSG mainBody = new Cube(bodyLength, bodyWidth, matThickness).toCSG()
CSG mainBody = centerOnAxes(Vitamins.get("vexCchannel","5x10").rotz(90))
.union(centerOnAxes(Vitamins.get("vexCchannel","5x10").rotz(90).rotx(180)).movez(75))

/*
mainBody = mainBody	.setParameter(bodyLength)
				.setParameter(bodyWidth)
				.setParameter(matThickness)
				.setRegenerate({new Cube(bodyLength, bodyWidth, matThickness).toCSG()})
				*/

ribs = makeVexRibCage(ribVals, matThickness.getMM(), mainBody.hull())

ribs = ribs		.setParameter(matThickness)
				//.setParameter(bodyWidth)
				//.setParameter(bodyLength)
				.setRegenerate({makeVexRibCage(ribVals, matThickness.getMM(), mainBody.hull())})
				//.setManufactuing({CSG arg0 -> arg0.difference(new Cube(100, 100, 100).toMinZ().movez(-100))}) //typo on purpose
				.setManufactuing({CSG arg0 -> arg0.toZMin().makeKeepaway(1)})
//cChannel = cChannel.movey((cChannel.getMaxY()-cChannel.getMinY())/2)

CSG center = new Cube(5, 240, 5).toCSG().toZMin()
//return [ribs, centerOnAxes(mainBody)];
return ribs.difference(new Cube(100, 100, 100).toCSG().toZMin().movez(-100));
//return makeRib(160, 240, 5, mainBody);
//return [cChannel,mainBody]
