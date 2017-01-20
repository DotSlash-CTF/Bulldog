import com.neuronrobotics.bowlerstudio.creature.ICadGenerator;
import com.neuronrobotics.bowlerstudio.creature.CreatureLab;
import org.apache.commons.io.IOUtils;
import com.neuronrobotics.bowlerstudio.vitamins.*;
import eu.mihosoft.vrl.v3d.parametrics.*;
import javafx.scene.paint.Color;
import com.neuronrobotics.bowlerstudio.threed.BowlerStudio3dEngine;


class Feet implements ICadGenerator, IParameterChanged{
	//First we load teh default cad generator script 
	ICadGenerator defaultCadGen=(ICadGenerator) ScriptingEngine
	                    .gitScriptRun(
                                "https://github.com/DotSlash-CTF/Bulldog.git", // git location of the library
	                              "FullDog/laserCutCad.groovy" , // file to load
	                              null
                        )
	LengthParameter thickness 		= new LengthParameter("Material Thickness",3.15,[10,1])
	LengthParameter headDiameter 		= new LengthParameter("Head Dimeter",100,[200,50])
	LengthParameter snoutLen 		= new LengthParameter("Snout Length",63,[200,50])
	LengthParameter jawHeight 		= new LengthParameter("Jaw Height",32,[200,10])
	LengthParameter leyeDiam 		= new LengthParameter("Left Eye Diameter",35,[headDiameter.getMM()/2,29])
	LengthParameter reyeDiam 		= new LengthParameter("Right Eye Diameter",35,[headDiameter.getMM()/2,29])
	LengthParameter eyeCenter 		= new LengthParameter("Eye Center Distance",headDiameter.getMM()/2,[headDiameter.getMM(),headDiameter.getMM()/2])
	StringParameter servoSizeParam 			= new StringParameter("hobbyServo Default","towerProMG91",Vitamins.listVitaminSizes("hobbyServo"))
	StringParameter boltSizeParam 			= new StringParameter("Bolt Size","M3",Vitamins.listVitaminSizes("capScrew"))

	HashMap<String, Object>  boltMeasurments = Vitamins.getConfiguration( "capScrew",boltSizeParam.getStrValue())
	HashMap<String, Object>  nutMeasurments = Vitamins.getConfiguration( "nut",boltSizeParam.getStrValue())
	//println boltMeasurments.toString() +" and "+nutMeasurments.toString()
	double boltDimeMeasurment = boltMeasurments.get("outerDiameter")
	double nutDimeMeasurment = nutMeasurments.get("width")
	double nutThickMeasurment = nutMeasurments.get("height")
	// private TransformNR offset =BowlerStudio3dEngine.getOffsetforvisualization().inverse();
	ArrayList<CSG> headParts =null
	@Override 
	public ArrayList<CSG> generateCad(DHParameterKinematics d, int linkIndex) {
		ArrayList<CSG> allCad=defaultCadGen.generateCad(d,linkIndex);
		ArrayList<DHLink> dhLinks=d.getChain().getLinks();
		DHLink dh = dhLinks.get(linkIndex)

		//The link configuration
		LinkConfiguration conf = d.getLinkConfiguration(linkIndex);
		// creating the servo
		CSG servoReference=   Vitamins.get(conf.getElectroMechanicalType(),conf.getElectroMechanicalSize())
		.transformed(new Transform().rotZ(90))
		//Creating the horn
		double servoTop = servoReference.getMaxZ()
		CSG horn = Vitamins.get(conf.getShaftType(),conf.getShaftSize())	
		
			//variable setup
			HashMap<String, Object>  vitaminData = Vitamins.getConfiguration(conf.getElectroMechanicalType(),conf.getElectroMechanicalSize())
			ArrayList<CSG> parts = new ArrayList<CSG>()
			int servoX = vitaminData.get("flangeLongDimention")//32
			int servoY = vitaminData.get("servoThinDimentionThickness")//11.8
			int servoZ = vitaminData.get("servoShaftSideHeight")//31.5
			int xLength = dh.getR() * (16.0/21.0);

			//creating servo cutout
			CSG servo = servoReference
							.scalex(1.08)
							.rotz(90)
							.rotx(180)
							.movez(-7)
							.movez(1.4)
							.movex(servoX + (xLength - 80)/2)

			//creating servo hole and main leg piece
			CSG sub1 = new Cube(servoX+3, servoY+1.75, servoZ).toCSG()
			CSG mainLeg = new Cube(xLength, servoY*2, servoZ+1).toCSG().movez(1).movex(11) 
			mainLeg =mainLeg.difference(servo)
			mainLeg =mainLeg.difference(sub1.movex(servoX-2.7 + (xLength - 80)/2).movez(7.9))

			//creating barrier to keep cap from moving onto main leg piece
			CSG barrier1 = new Cube(2, servoY*2, 2) .toCSG()
								.movez(servoZ/2+2)
								.movex(10.8 + (xLength - 80)/2)
			CSG barrier2 = new Cube(2, servoY*2, 2) .toCSG()
								.movez(servoZ/2+2)
								.movex(47.8 + (xLength - 80)/2)
			mainLeg = mainLeg.union(barrier1).union(barrier2)

			//creating cap to hold servo
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

			//adding link pieces to parts list
			parts.add(mainLeg)
			parts.add(cap)

			//create conector base piece
			int thickness = 10						
			CSG connector = new Cube((xLength - 80)/2 + 61, servoY*8/7,thickness).toCSG()
								   .movez(-18.5+thickness/5)
				 				   .toXMin()
				 				   .movex(20)

			//making pretty bumpy thingies
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
			//making holes
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

			//create connector end key
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
			CSG endCyl = new Cylinder(2,2,14,(int)40).toCSG()
									.rotx(90)
									.movez(-20)
									.movex(endLength)
									.movey(-7)
			connectorEnds = connectorEnds.hull(endCyl)
			connector = connector.union(connectorEnds)

			//add connector to parts list
			parts.add(connector)	

		//add parts to cad generator list
		for(int i = 0; i < parts.size(); i++)
		{
			CSG part = parts.get(i).toXMax()
			defaultCadGen.add(allCad, part, dh.getListener())
		}
		return allCad;
	}
	@Override 
	public ArrayList<CSG> generateBody(MobileBase b ) {
		ArrayList<CSG> allCad=defaultCadGen.generateBody(b);
		//If you want you can add things here
		//allCad.add(myCSG);
		
		return allCad;
	}
	/**
	 * This is a listener for a parameter changing
	 * @param name
	 * @param p
	 */
	 
	public void parameterChanged(String name, Parameter p){
		//new RuntimeException().printStackTrace(System.out);
		println "headParts was set to null from "+name
		new Exception().printStackTrace(System.out)
		headParts=null
	}
};

System.out.println("please show")

return new Feet()//Your code here
