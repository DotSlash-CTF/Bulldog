import com.neuronrobotics.bowlerstudio.vitamins.Vitamins;
import eu.mihosoft.vrl.v3d.parametrics.*;
import com.neuronrobotics.bowlerstudio.creature.ICadGenerator;
import com.neuronrobotics.bowlerstudio.creature.CreatureLab;
import org.apache.commons.io.IOUtils;
import com.neuronrobotics.bowlerstudio.vitamins.*;
import javafx.scene.paint.Color;
import eu.mihosoft.vrl.v3d.Transform;
import com.neuronrobotics.bowlerstudio.physics.TransformFactory;

class legPiece{
public ArrayList<CSG> createShoulder(CSG servo, int xLength){
HashMap<String, Object>  vitaminData = Vitamins.getConfiguration( "hobbyServo","towerProMG91") //replace with servo type 
println vitaminData

ArrayList<CSG> parts = new ArrayList<CSG>()

/*
 * First part of the leg (the shoulder) is created below
 */

//these numbers can be used as universal reference numbers
int servoX = vitaminData.get("flangeLongDimention")//32
int servoY = vitaminData.get("servoThinDimentionThickness")//11.8
int servoZ = vitaminData.get("servoShaftSideHeight")//31.5

servo = servo								.scalex(1.08)
										.rotz(90)
										.rotx(180)
										.movez(-7)
										.movez(1.4)
										.movex(servoX + (xLength - 80)/2)

//create the main part of the leg that will have an indent in the shape of the servo
CSG sub1 = new Cube(servoX+3, servoY+1.75, servoZ).toCSG()

CSG mainLeg = new Cube(xLength, servoY*2, servoZ+1).toCSG().movez(1).movex(11) 
mainLeg =mainLeg.difference(servo)
mainLeg =mainLeg.difference(sub1.movex(servoX-2.7 + (xLength - 80)/2).movez(7.9))


//union barriers that will stop the cap (below) from moving onto the main leg
CSG barrier1 = new Cube(2, servoY*2, 2) .toCSG()
								.movez(servoZ/2+2)
								.movex(10.8 + (xLength - 80)/2)
CSG barrier2 = new Cube(2, servoY*2, 2) .toCSG()
								.movez(servoZ/2+2)
								.movex(47.8 + (xLength - 80)/2)
mainLeg = mainLeg.union(barrier1).union(barrier2)

//create the cap that will encapsulate the servo
CSG cap = new Cube(34.8, servoY*8/3.5, 5).toCSG()
							.movez(servoZ/2+2.5)
							.movex(28.0 + (xLength - 80)/2)
CSG capSide1 = new Cube(34.8, 5, servoZ/2).toCSG()
								.movex(28.0 + (xLength - 80)/2)
								.movey(servoY+2.5)
								.movez(servoZ/4+5)
CSG capSide2 = new Cube(34.8, 5, servoZ/2).toCSG()
								.movex(28.0 + (xLength - 80)/2)
								.movey(-servoY-2.5)
								.movez(servoZ/4+5)
cap = cap.union(capSide1)
cap = cap.union(capSide2)
					
//move for visibility
cap = cap.movez(100)
//servo = servo.movez(4)

//add parts to the arraylist of parts
parts.add(mainLeg);
//parts.add(servo)
parts.add(cap)
//parts.add(sub1)

return parts

}


/*
 * Create second part of the leg below
 * (uses connectorLength from above so that the distance corrects itself with a longer connector length
 */
public ArrayList<CSG> createThigh(CSG servo, CSG hornRef, int xLength){

	/*
	 * Recreation of the CSGs from the first part, mainLeg
	 */

	ArrayList<CSG> shoulderParts = createShoulder(servo, xLength)
	CSG mainThigh = shoulderParts.get(0)
	CSG cap2 = shoulderParts.get(1)
	CSG connector = createConnector(servo, hornRef, xLength).movez(18)
	
	HashMap<String, Object>  vitaminData = Vitamins.getConfiguration( "hobbyServo","towerProMG91")
	int servoX = vitaminData.get("flangeLongDimention")
	int servoY = vitaminData.get("servoThinDimentionThickness")
	int servoZ = vitaminData.get("servoShaftSideHeight")
	
	LengthParameter connectorLength = new LengthParameter("Length of Leg",70,[150,60])
	//using a value here because it doesnt work to put this in the movex method -- bring up an issue
	ArrayList<CSG> parts = new ArrayList<CSG>()
	mainThigh = mainThigh.movex(xLength + 25*xLength/80)
						   		.movez(-13)
						   		.difference(connector)
						   		.movez(13)
						   		
	//replicates parts from mainLeg part to have a similar thigh
	cap2 = cap2.movex((connectorLength.getMM()+33.5) + (xLength - 80)/2)
	

	CSG bottomCap = new Cube(33, servoY*8/3.5, 9).toCSG()
							.movez(servoZ/2+2.5)
							.movex(27.5 + (xLength - 80)/2)
	CSG capSide1 = new Cube(33, 5, servoZ/2).toCSG()
								.movex(27.5 + (xLength - 80)/2)
								.movey(servoY*8/7+2.5)
								.movez(servoZ/4+7)
	CSG capSide2 = new Cube(33, 5, servoZ/2).toCSG()
								.movex(27.5 + (xLength - 80)/2)
								.movey(-servoY*8/7-2.5)
								.movez(servoZ/4+7)
	bottomCap = bottomCap.union(capSide1)
	bottomCap = bottomCap.union(capSide2)

	//see original value declaration above
	int value = -(27.5 + (xLength - 80)/2)
	println(value)
	int value2 = (connectorLength.getMM()+21.5) + (xLength - 80)/2
	bottomCap = bottomCap.movex(value)
	bottomCap = bottomCap
				.movez(-60)
				.rotx(180)
				.movez(-73.5)
	bottomCap = bottomCap
				.movex(xLength/2+11 + 25*xLength/80+32.0/2)//figure this out
	bottomCap = bottomCap
				.difference(connector)

	//visibility
	bottomCap = bottomCap.movez(-50)
	
 	parts.add(mainThigh)
 	parts.add(cap2)
 	parts.add(bottomCap)
 	//parts.add(connector)

	return parts

}

public CSG createConnector(CSG servo, CSG hornRef, int xLength){

	/*
	 * Recreation of the CSGs from the first part, mainLeg
	 */
	HashMap<String, Object>  vitaminData = Vitamins.getConfiguration( "hobbyServo","towerProMG91")
	int servoX = vitaminData.get("flangeLongDimention")//32
	int servoY = vitaminData.get("servoThinDimentionThickness")
	int servoZ = vitaminData.get("servoShaftSideHeight")
	
	LengthParameter connectorLength = new LengthParameter("Length of Leg",70,[150,60])

	int thickness = 10
								
	CSG connector = new Cube((xLength - 80)/2 + 61, servoY*8/7,thickness).toCSG()
								   .movez(-18.5+thickness/5)
				 				   .toXMin()
				 				   .movex(20)
				 				   
	//fancifying
	CSG decor1 = new Cylinder(8,8,thickness,(int)50).toCSG()
								.movez(-21.5)
								.movex(24)
	CSG decor2 = new Cylinder(8,8,thickness,(int)50).toCSG()
								.movez(-21.5)
								.movex(39)
	CSG decor3 = new Cylinder(8,8,thickness,(int)50).toCSG()
								.movez(-21.5)
								.movex(54)
	connector = connector.union(decor1)
				 .union(decor2)
				 .union(decor3)
				 
	//keyHole is to be subtracted: connHole subtracted from horn
	int cylVal = 2
	CSG connHole = new Cylinder(cylVal,cylVal,4.5,(int)50).toCSG()
								.movez(-19.5)
								.movex(34)
								
	//subtracting the correct horn from the connector
	CSG hornCube = new Cube(10,10,10).toCSG()
	CSG halfHorn = hornRef.intersect(hornCube)
	halfHorn = halfHorn.rotz(90).movex(servoX).movez(-17)
	hornRef = hornRef.rotz(90).movex(servoX).movez(-17)
	CSG keyHole = connHole.union(hornRef).union(halfHorn.movez(5)).makeKeepaway(2)
					.movex(-(8))
	
	connector = connector
				 .difference(keyHole)
				 .movez(-10)

	connHole = connHole.movez(-12)
				.movex(-(8.5)) 
				
	connector = connector.difference(connHole)

	int endLength = (xLength - 80)/2 + 61 +20
	
	CSG connectorEnd1 = new Cylinder(2,2,thickness+2,(int)50).toCSG()
									.movex(endLength)
									.movey(6)
						   			.movez(-32.5)
	CSG connectorEnd2 = new Cylinder(2,2,thickness+2,(int)50).toCSG()
									.movex(endLength)
									.movey(-6)
						   			.movez(-32.5)
	CSG connectorEnds = connectorEnd1.hull(connectorEnd2)
							.movez(1)
	
	CSG endCyl = new Cylinder(1,1,14,(int)40).toCSG()
			.rotx(90)
			.movez(-20)
			.movex(endLength)
			.movey(-7)
			 connectorEnds = connectorEnds.hull(endCyl)
	connector = connector.union(connectorEnds)
	connector = connector
		 connector.setManufactuing({CSG arg0 ->
 				return arg0.toZMin()
 })
 
	connector.setParameter(connectorLength)// add any parameters that are not used to create a primitive
		    .setRegenerate({ createConnector(Vitamins.getConfiguration( "hobbyServo","towerProMG91"))})
		    
	connector = connector.movez(-20).toXMin().movex(servoX-10 + (xLength - 80)/2)

	
	return connector
}
public ArrayList<CSG> rotatedLegLink(CSG servo, CSG hornRef, int xLength){

	/*
	 * Recreation of the CSGs from the first part, mainLeg
	 */
	HashMap<String, Object>  vitaminData = Vitamins.getConfiguration( "hobbyServo","towerProMG91")
	int servoX = vitaminData.get("flangeLongDimention")//32
	int servoY = vitaminData.get("servoThinDimentionThickness")
	int servoZ = vitaminData.get("servoShaftSideHeight")

	ArrayList<CSG> shoulderParts = createShoulder(servo, xLength)
	ArrayList<CSG> thighParts = createThigh(servo, hornRef, xLength)

	CSG cap = thighParts.get(2)
					.rotx(-90)
	
	CSG rotatedLink = shoulderParts.get(0)
	rotatedLink = rotatedLink.rotx(90)
						.toYMin()
						
	rotatedLink = rotatedLink.movex(xLength + 25*xLength/80)
	
	CSG connector = createConnector(servo, hornRef, xLength).toZMin()

	connector = connector.movez(-servoY*8/14)//connector hight = servoY*8/7

	rotatedLink = rotatedLink.difference(connector)

	ArrayList<CSG> parts = new ArrayList<CSG>()

	parts.add(rotatedLink.movez(50))
	parts.add(cap.movez(50))
	parts.add(connector.movez(50))

	return parts
}

public CSG theUnion(){
	CSG test1 = new Cube(11,8,3).toCSG()
	
	CSG test2 = new Cube(11,8,3).toCSG()
						   
	CSG shoulder = createShoulder(test1)
	CSG thigh = createThigh(test1)
	CSG connector = createConnector(test1,test2)
	CSG united = shoulder.union(thigh).union(connector)

	united.setRegenerate({theUnion()})
	return united
}

public CSG theUnionNoConnector(){
	CSG test1 = new Cube(11,8,3).toCSG()
	
	CSG test2 = new Cube(11,8,3).toCSG()
	CSG shoulder = createShoulder(test1)
	CSG thigh = createThigh(test1)
	CSG united = shoulder.union(thigh)
	united.setRegenerate({theUnionNoConnector()})
	return united
}

}

return new legPiece()
