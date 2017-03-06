import com.neuronrobotics.bowlerstudio.vitamins.Vitamins;
import eu.mihosoft.vrl.v3d.parametrics.*;
import com.neuronrobotics.bowlerstudio.creature.ICadGenerator;
import com.neuronrobotics.bowlerstudio.creature.CreatureLab;
import org.apache.commons.io.IOUtils;
import com.neuronrobotics.bowlerstudio.vitamins.*;
import javafx.scene.paint.Color;
import eu.mihosoft.vrl.v3d.Transform;
import com.neuronrobotics.bowlerstudio.physics.TransformFactory;

	//Recreation of the CSGs from the first part, mainLeg
	HashMap<String, Object>  vitaminData = Vitamins.getConfiguration( "hobbyServo","towerProMG91")
	HashMap<String, Object>  vitaminData2 = Vitamins.getConfiguration( "hobbyServoHorn","hv6214mg_1")
	CSG hornRef = Vitamins.get("hobbyServoHorn","hv6214mg_1")
	//print("horn" + vitaminData2)
	int hornRad = vitaminData2.get("hornBaseDiameter")//13.48
	int hornThick = vitaminData2.get("hornThickness")//6.6
	int hornLeng = vitaminData2.get("hornLength")//23.0
	int servoX = vitaminData.get("flangeLongDimention")//32
	int servoY = vitaminData.get("servoThinDimentionThickness")//11.8

	int thickness = 10
								
	CSG connector = new Cube(100, servoY*8/7,thickness).toCSG()
								   .movez(-18.5+thickness/5)
				 				   .toXMin()
				 				   .movex(20)
				 				   
	//fancifying, could be removed if we want the connectors shorter
	CSG decor1 = new Cylinder(hornRad+1,thickness,(int)50).toCSG()
								.movez(-21.5)
								.movex(24+hornRad-7.7)
	CSG decor2 = new Cylinder(hornRad/2+2,hornRad/2+2,thickness,(int)50).toCSG()
								.movez(-21.5)
								.movex(37+hornRad-7.7)
	connector = connector.union(decor1)
				 .union(decor2)
				 
	//keyHole is to be subtracted: connHole subtracted from horn (the cylinder hole)
	int cylVal = 4
	CSG connHole = new Cylinder(cylVal,cylVal,4.5,(int)50).toCSG()
								.movez(-19.5)
								.movex(33)
	
	connector = connector
				 .movez(-10)

	connHole = connHole.movez(-12)
				.movex(-(8.5)) 
				
	connector = connector.difference(connHole)

	int endLength = 100
	
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
	
	CSG endCyl = new Cylinder(2,2,14,(int)40).toCSG()
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

	
	return connector