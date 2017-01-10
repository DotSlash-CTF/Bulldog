CSG body = new Cube(10, 10, 77.5).toCSG().toZMin(); //Starter code
rib = Extrude.bezier(new Cylinder(2.5, 2.5, 10).toCSG(), [10,-60,0], [60,-60,0], [65,-20,0], 10) //index finger
int numRibs = 4;
for(int i = 0; i < numRibs; i++)
{
	body = body.union(rib.collect{it.movez(25 * i)});
	body = body.union(rib.collect{it.rotx(180).toZMin().movez(25 * i)})
}
return body.roty(-90).toZMin()