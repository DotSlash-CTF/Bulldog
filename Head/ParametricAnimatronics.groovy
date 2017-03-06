import eu.mihosoft.vrl.v3d.parametrics.*;
import com.neuronrobotics.bowlerstudio.vitamins.Vitamins;
import eu.mihosoft.vrl.v3d.Transform;
import eu.mihosoft.vrl.v3d.Extrude;

class Headmaker implements IParameterChanged{
	boolean makeCutsheetStorage = false
	HashMap<Double,CSG> eyeCache=new HashMap<>();
	HashMap<String,Double> previousValue = new HashMap<>();
	ArrayList<CSG> cachedParts = null;
	double eyeGearSpacing = 6
	double eyeLidPinDiam = 3
	LengthParameter thickness 		= new LengthParameter("Material Thickness",3.15,[10,1])
	LengthParameter headDiameter 		= new LengthParameter("Head Dimeter",140,[200,140])
	LengthParameter snoutLen 		= new LengthParameter("Snout Length",headDiameter.getMM()*0.55,[headDiameter.getMM()*2,headDiameter.getMM()/2])
	LengthParameter jawHeight 		= new LengthParameter("Jaw Height",32,[200,10])
	LengthParameter JawSideWidth 		= new LengthParameter("Jaw Side Width",20,[40,10])
	LengthParameter boltDiam 		= new LengthParameter("Bolt Diameter",3.0,[8,2])
	LengthParameter boltLength		= new LengthParameter("Bolt Length",10,[18,10])
	LengthParameter nutDiam 		 	= new LengthParameter("Nut Diameter",5.42,[10,3])
	LengthParameter nutThick 		= new LengthParameter("Nut Thickness",2.4,[10,3])
	LengthParameter upperHeadDiam 	= new LengthParameter("Upper Head Height",20,[300,0])
	LengthParameter leyeDiam 		= new LengthParameter("Left Eye Diameter",56,[headDiameter.getMM()/2,56])
	LengthParameter reyeDiam 		= new LengthParameter("Right Eye Diameter",56,[headDiameter.getMM()/2,56])
	LengthParameter eyeCenter 		= new LengthParameter("Eye Center Distance",headDiameter.getMM()/2+thickness.getMM()*2,[headDiameter.getMM(),leyeDiam.getMM()*1.5])
	LengthParameter ballJointPin		= new LengthParameter("Ball Joint Pin Size",8,[50,8])
	LengthParameter centerOfBall 		= new LengthParameter("Center Of Ball",18.5,[50,8])
	LengthParameter printerOffset		= new LengthParameter("printerOffset",0.5,[2,0.001])
	LengthParameter eyemechRadius		= new LengthParameter("Eye Mech Linkage",16,[20,5])
	LengthParameter eyemechWheelHoleDiam	= new LengthParameter("Eye Mech Wheel Center Hole Diam",7.25,[8,3])
	LengthParameter wireDiam			= new LengthParameter("Connection Wire Diameter",1.6,[boltDiam.getMM(),1])
	StringParameter servoSizeParam 			= new StringParameter("hobbyServo Default","towerProMG91",Vitamins.listVitaminSizes("hobbyServo"))
	StringParameter hornSizeParam 			= new StringParameter("hobbyServoHorn Default","standardMicro1",Vitamins.listVitaminSizes("hobbyServoHorn"))
	StringParameter boltSizeParam 			= new StringParameter("Bolt Size","8#32",Vitamins.listVitaminSizes("capScrew"))

	HashMap<String, Object>  boltMeasurments = Vitamins.getConfiguration( "capScrew",boltSizeParam.getStrValue())
	HashMap<String, Object>  nutMeasurments = Vitamins.getConfiguration( "nut",boltSizeParam.getStrValue())
	/**
	 * This script is used to make a parametric anamatronic creature head.
	 * change the default values in LengthParameters to make changes perminant
	 */
	ArrayList<CSG> makeHead(boolean makeCutSheet){
		makeCutsheetStorage= makeCutSheet
		if(cachedParts==null){
			println "All Parts was null"
			//Set up some parameters to use
			eyeLidPinDiam = (thickness.getMM()+2)*Math.sqrt(2)
			println boltMeasurments.toString() +" and "+nutMeasurments.toString()
			double boltDimeMeasurment = boltMeasurments.get("outerDiameter")
			double nutDimeMeasurment = nutMeasurments.get("width")
			double nutThickMeasurment = nutMeasurments.get("height")
			boltDiam.setMM(boltDimeMeasurment)
			nutDiam.setMM(nutDimeMeasurment)
			nutThick.setMM(nutThickMeasurment)
			
			ballJointPin.setMM(4)
			ArrayList<CSG> ballJointParts= (ArrayList<CSG>)ScriptingEngine.gitScriptRun(
		                                "https://github.com/madhephaestus/cablePullServo.git", // git location of the library
			                              "ballJointBall.groovy" , // file to load
			                              null// no parameters (see next tutorial)
		                        )
		     CSG ballJoint = ballJointParts.get(0)
		     CSG ballJointKeepAway = ballJointParts.get(1)
		     
			String jawServoName = servoSizeParam.getStrValue()
			
			double jawAttachOffset =  (headDiameter.getMM()/2
						-thickness.getMM()/2 
						-thickness.getMM()*2)
		     HashMap<String, Object> shaftmap = Vitamins.getConfiguration("hobbyServoHorn",hornSizeParam.getStrValue())
			double hornOffset = shaftmap.get("hornThickness")
			HashMap<String,Object> jawServoConfig = Vitamins.getConfiguration("hobbyServo",jawServoName)
			double servoHeightFromMechPlate = Double.parseDouble(jawServoConfig.get("servoThinDimentionThickness").toString())/2
			double servoJawMountPlateOffset = Double.parseDouble(jawServoConfig.get("tipOfShaftToBottomOfFlange").toString())
			double servoWidth = Double.parseDouble(jawServoConfig.get("flangeLongDimention").toString())
			double servoCentering  = Double.parseDouble(jawServoConfig.get("shaftToShortSideFlandgeEdge").toString())
			double flangeMountOffset =  Double.parseDouble(jawServoConfig.get("tipOfShaftToBottomOfFlange").toString())
			double flangeThickness =  Double.parseDouble(jawServoConfig.get("flangeThickness").toString())
			double servoShaftSideHeight =  Double.parseDouble(jawServoConfig.get("servoShaftSideHeight").toString())	
			double bottomOfFlangeToTopOfBody =  Double.parseDouble(jawServoConfig.get("bottomOfFlangeToTopOfBody").toString())
			double jawHingeSlotScale = 8
			double thicknessHoleRadius =  Math.sqrt(2*(thickness.getMM()/2)* (thickness.getMM()/2))
			double servoLongSideOffset = servoWidth-servoCentering
			CSG horn = Vitamins.get("hobbyServoHorn",hornSizeParam.getStrValue())	
			CSG jawServo = Vitamins.get("hobbyServo",jawServoName)
		                        .toZMax()
		                        .roty(90)
		                        .rotz(90)
		
		                        
		                        
		                        
			CSG smallServo = Vitamins.get("hobbyServo",jawServoName)
		
			CSG baseHead =new Cylinder(	headDiameter.getMM()/2,
									headDiameter.getMM()/2,
									thickness.getMM(),(int)30).toCSG() // a one line Cylinder
															
			CSG mechPlate = baseHead.scalex(2*snoutLen.getMM() / headDiameter.getMM())
									.intersect(new Cube(
										snoutLen.getMM()+JawSideWidth.getMM(),
										headDiameter.getMM(),
										thickness.getMM()*2)
										.noCenter()
										.toCSG()
										.movey(- headDiameter.getMM()/2)
										.movex(- JawSideWidth.getMM())
										.union(baseHead)
										.hull()
					)
			
			CSG mechPlateJaw = baseHead.scalex((2*snoutLen.getMM() + 15) / headDiameter.getMM())
									.intersect(new Cube(
										snoutLen.getMM()+JawSideWidth.getMM() + 100,
										headDiameter.getMM(),
										thickness.getMM()*2)
										.noCenter()
										.toCSG()
										.movey(- headDiameter.getMM()/2)
										.movex(- JawSideWidth.getMM())
										.union(baseHead)
										.hull()
					)

			CSG bezSlice = new Cube(5, 200, 5).toCSG().movex(-20)
			ArrayList<CSG> parts = new ArrayList<CSG>()
			int numParts = 100
			for(int i=0;i<numParts;i++){
				parts.add(bezSlice)
			}
						
			ArrayList<CSG> testing = Extrude.bezier(parts,[0,0,0],[100,0,0],[140,0,30]).collect{
				it.movez(0)
			}
			
			CSG bottomJaw = testing.get(0);
			for (int i = 1; i < testing.size(); i++) {
				bottomJaw = bottomJaw.union(testing.get(i));
			}
	
			bottomJaw = bottomJaw.intersect(mechPlateJaw.difference(
					new Cylinder(	headDiameter.getMM()/2 - thickness.getMM()*4,
								headDiameter.getMM()/2- thickness.getMM()*4,
								thickness.getMM(),(int)30).toCSG()
									.scalex(2*snoutLen.getMM()/headDiameter.getMM())
										,
					new Cube(
						snoutLen.getMM()+JawSideWidth.getMM(),
						headDiameter.getMM(),
						thickness.getMM()*2)
						.noCenter()
						.toCSG()
						.toXMax()
						.movey(- headDiameter.getMM()/2)
						.movex(- JawSideWidth.getMM())
				).scalez(100).movez(-5))
							
			BowlerStudioController.setCsg([bottomJaw]);
			mechPlate=mechPlate 
				.movez(jawHeight.getMM())
			
			
			CSG sideJaw = new Cube(
					JawSideWidth.getMM()+thickness.getMM(),
					thickness.getMM(),
					jawHeight.getMM()+thickness.getMM()
					+servoHeightFromMechPlate
					).toCSG()
					.toZMin()
					.union(new Cylinder(	JawSideWidth.getMM()/2,
										JawSideWidth.getMM()/2,
										thickness.getMM(),(int)30).toCSG()
							.movez(-thickness.getMM()/2)
							.rotx(90)
							.movez(jawHeight.getMM() +thickness.getMM()+servoHeightFromMechPlate )
									)
			sideJaw=	tSlotPunch(sideJaw.rotz(90)).rotz(-90)
		
		
			horn=	horn
					.roty(90)
					.rotz(-90)
					
					.movey(-thickness.getMM()/2)

			horn = horn
					.union(horn
							.movey(thickness.getMM())
					)
					.hull()
			def servoBrackets  =generateServoBracket(jawServoName)
			
			def allJawServoParts = [horn,jawServo,servoBrackets.get(0),servoBrackets.get(1)].collect { 
				it.movez(	jawHeight.getMM() 
		                       		 	+thickness.getMM()
		                       		 	+servoHeightFromMechPlate
		                        )
		                        .movey(jawAttachOffset-thickness.getMM()/2+hornOffset/2)
							.setColor(javafx.scene.paint.Color.CYAN)
				} 
			double servoBraceRad = JawSideWidth.getMM()
			CSG servoBrace = new Cylinder(	-horn.getMinX()+thickness.getMM(),
										-horn.getMinX()+thickness.getMM(),
										thickness.getMM(),(int)30).toCSG()
						.rotx(90)
						.movey(-thickness.getMM()/2)
						.movez(jawHeight.getMM() +servoBraceRad/2)
			CSG LeftSideJaw =sideJaw
					.union(servoBrace)
					.movey(jawAttachOffset) 
					.difference(
						allJawServoParts
					)
			.setColor(javafx.scene.paint.Color.CYAN)
			CSG RightSideJaw =sideJaw
					.difference(new Cylinder(thicknessHoleRadius,thicknessHoleRadius,thickness.getMM()*2,(int)30).toCSG()
								.movez(-thickness.getMM())
								.rotx(-90)
								.movez(jawHeight.getMM()+thickness.getMM()+servoHeightFromMechPlate)
								)
					
					.movey(-jawAttachOffset) 
					.setColor(javafx.scene.paint.Color.CYAN)
			BowlerStudioController.addCsg(LeftSideJaw);		
			BowlerStudioController.addCsg(RightSideJaw);	
			def upperHead = generateUpperHead(mechPlate)
			/**
			 * Setting up the eyes
			 */
			double eyeHeight = jawHeight.getMM()+thickness.getMM()
			double minKeepaway =0;
			//double flangeThickness =  Double.parseDouble(jawServoConfig.get("flangeThickness"))
			//double servoShaftSideHeight =  Double.parseDouble(jawServoConfig.get("servoShaftSideHeight"))	
			//double bottomOfFlangeToTopOfBody =  Double.parseDouble(jawServoConfig.get("bottomOfFlangeToTopOfBody"))
			double bracketClearence = servoShaftSideHeight-bottomOfFlangeToTopOfBody+flangeThickness*2+thickness.getMM()
			if(leyeDiam.getMM()>reyeDiam.getMM()){
				minKeepaway=leyeDiam.getMM()/2
			}else
				minKeepaway=reyeDiam.getMM()/2
			
			if(	bracketClearence>	minKeepaway){
				minKeepaway=bracketClearence
			}
			if(boltLength.getMM()*2+thickness.getMM()>minKeepaway){
				minKeepaway = boltLength.getMM()*2+thickness.getMM()
			}
			eyeHeight+=minKeepaway
			double eyePlateHeight = eyeHeight - thickness.getMM()/2
		
			double eyeStockBoltDistance = boltDiam.getMM()*2+	thickness.getMM()*4		
			double eyestockStandoffDistance= bottomOfFlangeToTopOfBody+thickness.getMM()/2
			eyeHeight +=eyestockStandoffDistance
			double eyeStockThickness = ballJointPin.getMM()
			double maxRad = Math.sqrt(Math.pow(headDiameter.getMM()/2,2)-Math.pow(eyeCenter.getMM()/2,2))
			double firstEyeBoltDistance = (maxRad
									-centerOfBall.getMM()
									+eyemechRadius.getMM()
									)
			
			double eyeLinkageLength = eyemechRadius.getMM()+(boltDiam.getMM()/2)
			
			double titlServoPlacement = -(eyeLinkageLength+boltDiam.getMM()*2)
			double panServoPlacement  = (eyeLinkageLength+boltDiam.getMM()*2)
			if(headDiameter.getMM()>190){
				//titlServoPlacement = -(eyeLinkageLength*4+boltDiam.getMM()*2)
				//panServoPlacement  = -(eyeLinkageLength+boltDiam.getMM()*2)
				titlServoPlacement =-maxRad +eyeLinkageLength*2
				panServoPlacement=titlServoPlacement+eyeLinkageLength*3+boltDiam.getMM()*2
			}
			double tiltWheelheight = eyePlateHeight+smallServo.getMaxZ()+	thickness.getMM()
			double tiltLinkageHeight = eyeHeight+eyemechRadius.getMM();
			double panWheelheight = eyePlateHeight+smallServo.getMaxZ()-	flangeThickness - thickness.getMM()
			double eyeXdistance  =headDiameter.getMM()/2
			double eyeBoltDistance =eyeCenter.getMM()/2-servoLongSideOffset+thickness.getMM()
			CSG bolt =new Cylinder(
								boltDiam.getMM()/2,
								boltDiam.getMM()/2,
								firstEyeBoltDistance*2,
								(int)15).toCSG()
								.movez(-firstEyeBoltDistance)	
			CSG printedBolt =new Cylinder(
								(boltDiam.getMM()+printerOffset.getMM())/2,
								(boltDiam.getMM()+printerOffset.getMM())/2,
								firstEyeBoltDistance*2,
								(int)15).toCSG()
								.movez(-firstEyeBoltDistance)	
								
			CSG wire = new Cylinder(wireDiam.getMM()/2+0.25,
								wireDiam.getMM()/2+0.25
								,firstEyeBoltDistance*2,(int)15).toCSG()
								.movez(-firstEyeBoltDistance)	
			double boltDistance = 	nutDiam.getMM();
			double attachmentOffset = nutDiam.getMM()*2
			CSG bolts =	bolt.union(
								bolt
								.movey(-boltDistance	)	)
			CSG printedBolts =	printedBolt.union(
								printedBolt
								.movey(-boltDistance	)	)				
								
				
			CSG eyeStockAttach = new Cube(nutDiam.getMM()*1.5,
									attachmentOffset,
									eyestockStandoffDistance-thickness.getMM()/2).toCSG()
								.toXMax()
								//.movex(-centerOfBall.getMM()+nutDiam.getMM()/4)
								.toZMax()
								.movex(boltDiam.getMM()*2)
			CSG eyeStockanchor = new Cube(thickness.getMM(),
									ballJointPin.getMM()*2,
									eyeStockThickness).toCSG()
								.toXMax()
								.movex(-centerOfBall.getMM()+thickness.getMM()/2)
								.toZMax()
			double eyeStockMountLocation = (eyeCenter.getMM()/2)-eyeBoltDistance+boltDiam.getMM()*1.5
			CSG rigtStockAttach = eyeStockanchor
								.movex(headDiameter.getMM()/2)
								.union(
									eyeStockAttach
									.toYMax()
									.toXMin()
									.movex(firstEyeBoltDistance)
									.movey(( eyeCenter.getMM()/2)-eyeBoltDistance)
									.movex(-boltDistance)
									.movey(boltDistance*2/3)
									)
								.hull()
			CSG leftStockAttach = eyeStockanchor
								.movex(headDiameter.getMM()/2)
								.union(
									eyeStockAttach
									.toYMin()
									.toXMin()
									.movex(firstEyeBoltDistance)
									.movey(-( eyeCenter.getMM()/2)+eyeBoltDistance)
									.movex(-boltDistance)
									.movey(-boltDistance*2/3)
									)
								.hull()				
			CSG eyestockRight = ballJoint
							.rotz(180)
							.rotx(180)
						.movex(headDiameter.getMM()/2)
						
						
						.union(rigtStockAttach)
						//change back to difference
						.difference(printedBolts
								.movex(firstEyeBoltDistance)
								.movey(( eyeCenter.getMM()/2)-eyeBoltDistance)
								)
						.movez(eyeHeight)
			
			CSG eyestockLeft = ballJoint
						.rotz(180)
						.rotx(180)
						.movex(headDiameter.getMM()/2)
						.union(leftStockAttach)
						//change back to difference
						.difference(
								printedBolts
								.rotz(180)
								.movex(firstEyeBoltDistance)
								.movey(-( eyeCenter.getMM()/2)+eyeBoltDistance)
								)
						.movez(eyeHeight)
			
		
			CSG eyeKeepAwayr =new Sphere(reyeDiam.getMM()/2+1)// Spheres radius
							.toCSG()
						.movey(-eyeCenter.getMM()/2)		
						.movex(headDiameter.getMM()/2)
						.movez(eyePlateHeight-thickness.getMM())
			eyeKeepAwayr= eyeKeepAwayr.union(eyeKeepAwayr.movez(	thickness.getMM()*2)).hull()
		
			CSG eyeKeepAwayl =new Sphere(leyeDiam.getMM()/2+1)// Spheres radius
							.toCSG()
						.movey(eyeCenter.getMM()/2)		
						.movex(headDiameter.getMM()/2)
						.movez(eyePlateHeight-thickness.getMM())
			eyeKeepAwayl= eyeKeepAwayl.union(eyeKeepAwayl.movez(	thickness.getMM()*2)).hull()
		
			CSG eyeKeepAway = eyeKeepAwayl.union(eyeKeepAwayr)
							.difference(new Cube(headDiameter.getMM())
							.toCSG()
							.toXMax()
							.movex(firstEyeBoltDistance+boltDiam.getMM())
							.movez(eyePlateHeight-thickness.getMM())
							)
				
			CSG leftBallJoint =  eyestockLeft.movey(  eyeCenter.getMM()/2)
							.setColor(javafx.scene.paint.Color.GREEN)
			CSG rightBallJoint = eyestockRight.movey( -eyeCenter.getMM()/2)
							.setColor(javafx.scene.paint.Color.BLUE)
			BowlerStudioController.addCsg(leftBallJoint)
			BowlerStudioController.addCsg(rightBallJoint)
			CSG upperHeadPart = upperHead.get(0)
			CSG eyePlate=baseHead
						.movez(eyePlateHeight)
						eyemechWheelHoleDiam
			CSG eyeMechWheel= new Cylinder(
								eyeLinkageLength+boltDiam.getMM(),
								eyeLinkageLength+boltDiam.getMM(),
								thickness.getMM(),
								(int)15).toCSG()
			//Generate Linkages					
			CSG mechLinkageCore = new Cylinder(boltDiam.getMM(),
								boltDiam.getMM(),
								thickness.getMM(),
								(int)15).toCSG()
			CSG mechLinkageAttach = new Cylinder(boltDiam.getMM()*1.5,
								boltDiam.getMM()*1.5,
								thickness.getMM(),
								(int)15).toCSG()
			CSG mechLinkage =mechLinkageCore
				.movey(eyeCenter.getMM()/2)
				.union(mechLinkageCore.movey(-eyeCenter.getMM()/2))
				.hull()
				.movex(-boltDiam.getMM()*1.75)
				.union(mechLinkageAttach.movey(eyeCenter.getMM()/2))
				.union(mechLinkageAttach.movey(-eyeCenter.getMM()/2))
				.difference(bolt.movey(eyeCenter.getMM()/2))
				.difference(bolt.movey(-eyeCenter.getMM()/2))

				//MAYBE HERE 1
		
			
				
			CSG mechLinkage2 = mechLinkage
							.movex(panServoPlacement-eyeLinkageLength)
							.movez(panWheelheight+thickness.getMM())
			mechLinkage=mechLinkage
							.movex(titlServoPlacement-eyeLinkageLength)
							.movey(eyeLinkageLength)
							.movez(tiltWheelheight+thickness.getMM())
							//.union(mechLinkage2)
			//keepaway for the linkages				
			CSG mechKeepaway=	mechLinkage
							.union(mechLinkage2)
							.movex(-eyeLinkageLength+boltDiam.getMM())
							.union(mechLinkage
								.movex(eyeLinkageLength))
							.hull()	
							.movez(thickness.getMM())
							mechKeepaway=mechKeepaway
										.union(mechKeepaway
												.movez(-thickness.getMM()*2))
												
										.hull()
			mechKeepaway = mechKeepaway
						.union(mechKeepaway.movex(thickness.getMM()+eyeLinkageLength))
						.union(mechKeepaway.movex(-headDiameter.getMM()))
						.hull()	
			//Eye to wheel linkage
			double tiltLinkagelength = -titlServoPlacement +eyeXdistance - boltDiam.getMM()*2-1
			double panLinkagelength = -panServoPlacement +eyeXdistance - boltDiam.getMM()*2-1
			
			CSG tiltEyeLinkage  = 	mechLinkageCore
				.union(mechLinkageCore.movex(tiltLinkagelength))
				.hull()
				.union(mechLinkageAttach)
				.difference(bolt)
				.difference(wire.movex(tiltLinkagelength))
				.movez(tiltLinkageHeight)
				.movex(titlServoPlacement)
				.movey(-eyeCenter.getMM()/2)
			CSG tiltEyeLinkage2 = tiltEyeLinkage
				.movey(eyeCenter.getMM())
			
			CSG panEyeLinkageBase  = 	mechLinkageCore
				.union(mechLinkageCore.movex(panLinkagelength))
				.hull()
				.union(mechLinkageAttach)
				.difference(bolt)
				.difference(wire.movex(panLinkagelength))
			CSG panEyeLinkageKeepaway = panEyeLinkageBase
									.hull()
									.makeKeepaway(printerOffset.getMM())
									.movez(eyeHeight-thickness.getMM()/2)
									.movex(panServoPlacement)
									.movey(-eyeCenter.getMM()/2-eyeLinkageLength)
			panEyeLinkageKeepaway=panEyeLinkageKeepaway
								.union(
									panEyeLinkageKeepaway
										.movey(eyeCenter.getMM()+eyeLinkageLength*2)
								)
			CSG panEyeLinkage=	panEyeLinkageBase
				.movez(eyeHeight-thickness.getMM()/2)
				.movex(panServoPlacement)
				.movey(-eyeCenter.getMM()/2-eyeLinkageLength)
			CSG panEyeLinkage2 = panEyeLinkage
				.movey(eyeCenter.getMM()+eyeLinkageLength*2)
			
			
			BowlerStudioController.addCsg(mechLinkage)
			BowlerStudioController.addCsg(mechLinkage2)
			BowlerStudioController.addCsg(tiltEyeLinkage)
			BowlerStudioController.addCsg(tiltEyeLinkage2)		
			BowlerStudioController.addCsg(panEyeLinkage)
			BowlerStudioController.addCsg(panEyeLinkage2)
			CSG slot = wire.union(wire.movex(eyeLinkageLength/3)).hull()
			// Make the linkage wheels
			for(int i=0;i<4;i++){
				eyeMechWheel=eyeMechWheel
							.difference(
								bolt
									.movex(eyeLinkageLength)
									.rotz(90*i)
									)
				if(i%2==0){
					eyeMechWheel=eyeMechWheel
							.difference(
								slot
									.movex(eyeLinkageLength/2)
									.rotz(90*i-45)
									)
				}
			}
			CSG eyeMechWheel1 = eyeMechWheel
								.difference(
									new Cylinder(
										eyemechWheelHoleDiam.getMM()/2,
										eyemechWheelHoleDiam.getMM()/2,
										thickness.getMM(),
										(int)15).toCSG())
								.movey(-eyeCenter.getMM()/2)
								.movex(panServoPlacement)	
			CSG eyeMechWheel2 = eyeMechWheel
								.difference(
									new Cylinder(
										eyemechWheelHoleDiam.getMM()/2,
										eyemechWheelHoleDiam.getMM()/2,
										thickness.getMM(),
										(int)15).toCSG()
										)
								.movey(-eyeCenter.getMM()/2+eyeLinkageLength)
								.movex(titlServoPlacement)
			CSG eyeMechWheel3 = eyeMechWheel
								.difference(bolt)
								.movey(eyeCenter.getMM()/2+eyeLinkageLength)
								.movex(titlServoPlacement)
			CSG eyeMechWheel4 = eyeMechWheel
								.difference(bolt)
								.movey(eyeCenter.getMM()/2)
								.movex(panServoPlacement)	
			CSG eyeBoltPan1 =bolt.movez(eyePlateHeight)
								.movey(eyeCenter.getMM()/2+eyeLinkageLength)
								.movex(titlServoPlacement)
			CSG eyeBoltPan2 =bolt.movez(eyePlateHeight)
								.movey(eyeCenter.getMM()/2)
								.movex(panServoPlacement)
			
			CSG eyeMechWheelPan = eyeMechWheel1.union(eyeMechWheel4)	
						.movez(panWheelheight+thickness.getMM()*2)
			CSG eyeMechWheelTilt = eyeMechWheel2.union(eyeMechWheel3)	
						.movez(tiltWheelheight)
						
			BowlerStudioController.addCsg(eyeMechWheelPan)		
			BowlerStudioController.addCsg(eyeMechWheelTilt)							
			// Cut the slot for the eye mec from the upper head
			upperHeadPart = upperHeadPart
						.difference(eyePlate
						.movex(-headDiameter.getMM()*2/3))
						.difference(mechKeepaway)
			BowlerStudioController.addCsg(upperHeadPart)		
			CSG eyePan = smallServo
						.rotz(180)
						.movez(eyePlateHeight-flangeThickness)
						.movey(-eyeCenter.getMM()/2)
						.movex(panServoPlacement)
			BowlerStudioController.addCsg(eyePan)
			CSG eyeTilt = smallServo.clone()
						.movez(eyePlateHeight+thickness.getMM())
						.movey(-eyeCenter.getMM()/2+eyeLinkageLength)
						.movex(titlServoPlacement)
			//BowlerStudioController.addCsg(eyeTilt)
			def jawHingeParts =generateServoHinge(jawServoName,eyePlateHeight-jawHeight.getMM()).collect { 
									it.movez(	jawHeight.getMM() 
				                       		 	)
						                        .movey(-jawAttachOffset+thickness.getMM()/2)
											.setColor(javafx.scene.paint.Color.BLUE)
									}
			double supportBracketDistance= eyeBoltDistance+nutDimeMeasurment*3
			def leftSupport =generateServoHinge(jawServoName,eyePlateHeight-jawHeight.getMM()).collect { 
										it  .movez(	jawHeight.getMM() )
										    .rotz(180)
						                        .movex(firstEyeBoltDistance - nutDimeMeasurment*2)
										    .movey(supportBracketDistance)
										    .setColor(javafx.scene.paint.Color.GREEN)
									}
			def rightSupport =generateServoHinge(jawServoName,eyePlateHeight-jawHeight.getMM()).collect { 
										it	.movez(	jawHeight.getMM() )
					                        		.movex(firstEyeBoltDistance-nutDimeMeasurment*2)
											.movey(-supportBracketDistance)
											.setColor(javafx.scene.paint.Color.GREEN)
									}					
			/**			
			 * 			Building the main plates
			 * 			
			 */
			//cut a matching slot from the eye plate 	
			def jawKeepaway = [	LeftSideJaw.movex(0.5)
											 .movez(-thickness.getMM()*2)
											.union(
												LeftSideJaw.movex(-0.5)
											 		.movez(-thickness.getMM()*2)
												)
											 .hull(),
									RightSideJaw.movex(jawHingeSlotScale),
									RightSideJaw.movex(-jawHingeSlotScale)	
											 	].collect{
											 		return it.movez(thickness.getMM()*4)
											 				.union(it)
											 				.hull()
											 	}
			eyePlate = eyePlate	.difference(upperHeadPart,upperHeadPart.movex(10))
							.difference(bolts
										.movex(firstEyeBoltDistance)
										.movey(-eyeBoltDistance)
										.movez(eyeHeight))
							.difference(bolts
										.rotz(180)
										.movex(firstEyeBoltDistance)
										.movey(eyeBoltDistance)
										.movez(eyeHeight))
							.difference(eyePan,eyeTilt,eyeBoltPan1,eyeBoltPan2,eyeKeepAway)
							//.union(eyeBoltPan1,eyeBoltPan2)
							.difference(jawHingeParts)
							.difference(leftSupport)
							.difference(rightSupport)
							.difference(jawKeepaway)
			BowlerStudioController.addCsg(eyePlate)	
			mechPlate = mechPlate.difference(jawKeepaway)// scale forrro for the jaw to move
			mechPlate = mechPlate.difference(allJawServoParts)
			mechPlate = mechPlate.difference(jawHingeParts)
			mechPlate = mechPlate.difference(leftSupport)
			mechPlate = mechPlate.difference(rightSupport)
			mechPlate = mechPlate.difference(upperHead)
			if(mechPlate.touching(eyePan))
				mechPlate = mechPlate.difference(eyePan)
			if(mechPlate.touching(eyeTilt))
				mechPlate = mechPlate.difference(eyeTilt)
			BowlerStudioController.addCsg(mechPlate)	
			bottomJaw = bottomJaw.difference(
								LeftSideJaw,
								RightSideJaw,
								tSlotTabsWithHole()
									.rotz(90)
									.movey(jawAttachOffset), 
								tSlotTabsWithHole()
									.rotz(90)
									.movey(-jawAttachOffset) 	
								)
			ArrayList<CSG> washers = new ArrayList<CSG>()
			/*
				double eyeLinkageLength = eyemechRadius.getMM()
			double titlServoPlacement = -(eyeLinkageLength+boltDiam.getMM()*2)
			double panServoPlacement  = (eyeLinkageLength+boltDiam.getMM()*2)
			double tiltWheelheight = eyePlateHeight+smallServo.getMaxZ()+	thickness.getMM()
			double panWheelheight = eyePlateHeight+smallServo.getMaxZ()-	flangeThickness - thickness.getMM()
			double eyeXdistance  =headDiameter.getMM()/2
			double eyeBoltDistance =eyeCenter.getMM()/2-servoLongSideOffset+thickness.getMM()
			*/
			int numTiltWashers = 4;
			int numPanWashers = 3;
			int numExtraWashers = 3;
			int totalWashers = numTiltWashers+numPanWashers+numExtraWashers;
			for(int i=0;i<totalWashers;i++){
				CSG newWash = washer();
				if(i<numTiltWashers){
					newWash=newWash
							.movex(titlServoPlacement)
							.movey(eyeCenter.getMM()/2+eyeLinkageLength)
							.movez(tiltWheelheight-(thickness.getMM()*(i+1))-nutThick.getMM())
							.setColor(javafx.scene.paint.Color.color(i%2?1:0,1,1))
							
				}
				if(i>=numTiltWashers && i<totalWashers-numExtraWashers){
					newWash=newWash
							.movex(panServoPlacement)
							.movey(eyeCenter.getMM()/2)
							.movez(panWheelheight-(thickness.getMM()*(i-numTiltWashers-1))-nutThick.getMM())
							.setColor(javafx.scene.paint.Color.color(i%2?1:0,1,1))
							
				}
				int myIndex=i;
				newWash.setManufactuing({incoming ->
					return 	incoming
								.toXMin()
								.toYMin()
								.toZMin()
								.movey(-headDiameter.getMM()-upperHeadDiam.getMM() - boltDiam.getMM()*myIndex*4)
								.movex( headDiameter.getMM()/4 + snoutLen.getMM() )
				})
				BowlerStudioController.addCsg(newWash)	
				washers.add(newWash)
			}
			def mechLinks = [mechLinkage,
			mechLinkage2,
			tiltEyeLinkage,
			tiltEyeLinkage2,
			panEyeLinkage,
			panEyeLinkage2,
			]
			for (int i=0;i<6;i++){
				int index = i;
			
				mechLinks[index].setManufactuing({incoming -> 
						CSG tmp= 	incoming.toZMin()
										.toXMin()
										.toYMin()
						if(index <2){	
							tmp=	tmp.rotz(90)
							.movey(boltDiam.getMM()*2)
						}
						tmp=	tmp.movey(-headDiameter.getMM()-upperHeadDiam.getMM()-boltDiam.getMM()*(5.5*index) -boltDiam.getMM()*4-thickness.getMM()*2 )
						return tmp
					})
				BowlerStudioController.addCsg(mechLinks[i].prepForManufacturing())			
			}
			
	
				print "\nLoading eyes..."
			Transform lEyeLocation = new Transform().translate(eyeXdistance,
													eyeCenter.getMM()/2,
													eyeHeight)
			Transform rEyeLocation=	new Transform().translate(eyeXdistance,
													-eyeCenter.getMM()/2,
													eyeHeight)	
										// creating the ball socket cup	
			CSG cup = getEyeLinkageCup()
						.roty(180)
			//eyeLinkageLength = eyemechRadius.getMM()+(boltDiam.getMM()/2)
			CSG outerLeftCup = cup
							.transformed(lEyeLocation)
							.movey(eyeLinkageLength)	
							.difference(panEyeLinkageKeepaway)	
			BowlerStudioController.addCsg(outerLeftCup)					
			CSG leftEye = getEye(leyeDiam.getMM(),ballJointKeepAway)
						.transformed(lEyeLocation)
						.setColor(javafx.scene.paint.Color.WHITE)
						
			CSG rightEye = getEye(reyeDiam.getMM(),ballJointKeepAway)	
						.transformed(rEyeLocation)
						.setColor(javafx.scene.paint.Color.WHITE)
			print "Done with Eyes\n"			
			BowlerStudioController.addCsg(leftEye)
			BowlerStudioController.addCsg(rightEye)					
			CSG jawServoBracket = allJawServoParts.get(2)
			CSG jawHingePin = jawHingeParts.get(0)
			CSG leftSupportPin =  leftSupport.get(0)
			CSG rightSupportPin =  rightSupport.get(0)
			

			def eyeRings  = generateEyeRings(upperHeadPart,eyeXdistance-eyeLidPinDiam*3/2,eyeHeight)
			mechPlate = mechPlate
						.difference(eyeRings)
			eyePlate = eyePlate
						.difference(eyeRings.collect{
							CSG slice = eyePlate.intersect(it)
							if(slice. getPolygons().size()>0)
								return slice.union(
									slice.movez(thickness.getMM()),
									slice.movez(-thickness.getMM())
									).hull()
							return it
						})
									
			eyePlate.setColor(javafx.scene.paint.Color.CYAN)
			upperHeadPart=upperHeadPart
						.difference(eyeRings.collect{
							CSG slice = upperHeadPart.intersect(it)
							if(slice. getPolygons().size()>0)
								return slice.union(
									slice.movez(upperHeadDiam.getMM())
									).hull()
							return it
						})
						.difference(new Cube(100, 10, 30).toCSG().movez(130))

						//MAYBE HERE 2
			
			CSG eyeRingPlate = eyeRings.get(0)
			/*
			def eyeLids = [eyeLid(leyeDiam.getMM())
						.transformed(lEyeLocation),
						eyeLid(leyeDiam.getMM())
						.rotx(180)
						.transformed(lEyeLocation),
						eyeLid(reyeDiam.getMM())
						.transformed(rEyeLocation),
						eyeLid(reyeDiam.getMM())
						.rotx(180)
						.transformed(rEyeLocation)]
			*/
			outerLeftCup.setManufactuing({incoming ->
				return 	incoming.toZMin()
							.toXMin()
							.toYMin()
							
			})	
			eyeRingPlate.setManufactuing({incoming ->
				return 	incoming.roty(90)
							.toZMin()
							.toXMin()
							.toYMin()
							.movey(headDiameter.getMM())
							.movex( -headDiameter.getMM()*5/4)
							
			})			
			rightEye.setManufactuing({incoming ->
				return 	incoming.roty(90)
							.toZMin()
							.toXMin()
							.toYMin()
							.movey(headDiameter.getMM())
							.movex( -headDiameter.getMM())
							
			})
			leftEye.setManufactuing({incoming ->
				return 	incoming.roty(90)
							.toZMin()
							.toXMin()
							.toYMin()
							.movey(headDiameter.getMM())
							.movex( -headDiameter.getMM())
							
			})
			rightBallJoint.setManufactuing({incoming ->
				return 	incoming.roty(180)
							.toZMin()
							.toXMin()
							.toYMin()
							.movey(headDiameter.getMM())
							.movex( -headDiameter.getMM())
							
			})
			leftBallJoint.setManufactuing({incoming ->
				return 	incoming.roty(180)
							.toZMin()
							.toXMin()
							.toYMin()
							.movey(headDiameter.getMM())
							.movex( -headDiameter.getMM())
							
			})
			eyeMechWheelPan.setManufactuing({incoming ->
				return 	incoming.toZMin()
							.toXMax()
							.toYMin()
							.movex( -headDiameter.getMM()+eyeLinkageLength)
							.movey(- headDiameter.getMM()*2)
							
			})
			eyeMechWheelTilt.setManufactuing({incoming ->
				return 	incoming.toZMin()
							.toXMin()
							.toYMin()
							.movex( -headDiameter.getMM()+eyeLinkageLength+1)
							.movey(- headDiameter.getMM()*2)
							
			})
	
			eyePlate.setManufactuing({incoming ->
				return 	incoming.toZMin()
							.toXMax()
							.movex( -headDiameter.getMM()/5)
							
			})
			upperHeadPart.setManufactuing({incoming ->
				return 	incoming
							
							.toZMin()
							.rotx(-90)
							.toZMin()
							.toYMax()
							.movey(- headDiameter.getMM()/2-1)
							
			})
			jawHingePin.setManufactuing({incoming ->
				return 	incoming
							.roty(90)
							.rotz (90)
							.toZMin()
							.toXMin()						
							.movey(-jawHeight.getMM()-2)							
							.movex((- headDiameter.getMM()*3/2)-thickness.getMM()*2)	
							
			})
			leftSupportPin.setManufactuing({incoming ->
				return 	incoming
							.roty(90)
							.rotz (90)
							.toZMin()
							.toXMin()						
							.movey(jawHeight.getMM()+2)							
							.movex((- headDiameter.getMM()*3/2)-thickness.getMM()*2)	
							
			})
			rightSupportPin.setManufactuing({incoming ->
				return 	incoming
							.roty(90)
							.rotz (90)
							.toZMin()
							.toXMin()						
							.movey(jawHeight.getMM()*2+4)							
							.movex((- headDiameter.getMM()*3/2)-thickness.getMM()*2)	
							
			})
			
			jawServoBracket.setManufactuing({incoming ->
				return 	incoming
							.rotx(90)
							.rotz (90)
							.toZMin()
							.toXMin()
							.toYMin()
							.movex(snoutLen.getMM()+JawSideWidth.getMM()+6+thickness.getMM())
							
			})
			
			RightSideJaw.setManufactuing({incoming ->
				return 	incoming
							.rotx(90)
							.toZMin()
							.toXMin()
							.movex(snoutLen.getMM()+1)
							
			})
			LeftSideJaw.setManufactuing({incoming ->
				return 	incoming
							.rotx(-90)
							.toZMin()
							.movey(-1)
							.toXMin()
							.movex(snoutLen.getMM()+1)
							
			})
			mechPlate.setManufactuing({incoming ->
				return 	incoming
							.toZMin()
							.movey(headDiameter.getMM())
			})
			
			
			def returnValues = 	[bottomJaw,
							RightSideJaw,
							LeftSideJaw,
							mechPlate,
							upperHeadPart, 							
							eyePlate,
							eyeRingPlate,
							jawServoBracket,
							jawHingePin,
							leftSupportPin,
							rightSupportPin,
							//eyePan,
							//eyeTilt,
							eyeMechWheelTilt,eyeMechWheelPan,				
							]
			returnValues.addAll(washers)	
			returnValues.addAll(mechLinks)
			CSG cutSheet;
			if(makeCutSheet){
				print "\nBuilding cut sheet... "
				def allParts = 	returnValues.collect { it.prepForManufacturing() } 
				cutSheet = allParts.get(0).union(allParts)
			}
			returnValues.add(leftEye)
			returnValues.add(rightEye)
			returnValues.add(leftBallJoint)
			returnValues.add(rightBallJoint)
			returnValues.add(eyePan)
			returnValues.add(eyeTilt)
			returnValues.add(outerLeftCup)
			//returnValues.addAll(eyeLids)
			for (int i=0;i<returnValues.size();i++){
				int index = i
				returnValues[i].getMapOfparametrics().clear()
				returnValues[i] = returnValues[i]
				.setParameter(thickness)
				.setParameter(headDiameter)
				.setParameter(snoutLen)
				.setParameter(jawHeight)
				//.setParameter(boltDiam)
				//.setParameter(nutDiam)
				//.setParameter(nutThick)
				.setParameter(upperHeadDiam)
				.setParameter(leyeDiam)
				.setParameter(reyeDiam)
				.setParameter(eyeCenter)
				.setParameter(printerOffset)
				.setParameter(servoSizeParam)
				.setParameter(boltSizeParam)
				.setParameter(hornSizeParam)
				//.setParameter(ballJointPinSize)
				//.setParameter(centerOfBall)
				//.setParameter(ballJointPinSize)
				.setParameter(boltLength)
				//.setParameter(eyemechRadius)
				//.setParameter(eyemechWheelHoleDiam)
				.setRegenerate({ makeHead(makeCutsheetStorage).get(index)})			
				for(String p:returnValues[i] .getParameters()){
					CSGDatabase.addParameterListener(p,this);
				}
			}
			if(makeCutSheet)
				returnValues.add(cutSheet)
			BowlerStudioController.setCsg(returnValues)	
			print "Done!\n"
			cachedParts = returnValues
			//println cachedParts
			return cachedParts
		}else{
			println "Returning cached Parts"
			return cachedParts
		}
	}

	/**
	 * This is a listener for a parameter changing
	 * @param name
	 * @param p
	 */
	 
	public void parameterChanged(String name, Parameter p){
		//new RuntimeException().printStackTrace(System.out);
		//println "All Parts was set to null"
		cachedParts=null
	}
	
	CSG tSlotTabs(){
		LengthParameter boltDiam 		= new LengthParameter("Bolt Diameter",2.5,[8,2])
		LengthParameter nutDiam 		= new LengthParameter("Nut Diameter",4,[10,3])
		LengthParameter thickness 		= new LengthParameter("Material Thickness",3.5,[10,1])
		CSG tab =new Cube( thickness,
				thickness,
				thickness)
				.toCSG()
		double tabOffset  = boltDiam.getMM()+thickness.getMM()
		tab = tab
			.movey(-tabOffset)
			.union(tab
			.movey(tabOffset))
		return tab.toZMin()
	}
	
	CSG tSlotTabsWithHole(){
		LengthParameter boltDiam 		= new LengthParameter("Bolt Diameter",2.5,[8,2])
		LengthParameter thickness 		= new LengthParameter("Material Thickness",3.5,[10,1])
		
		return tSlotTabs()
				.union(new Cylinder(
					boltDiam.getMM()/2,
					boltDiam.getMM()/2,
					thickness.getMM(),
					(int)15).toCSG()
				.toZMin())
	}
	
	CSG tSlotKeepAway(){
		return tSlotTabs().hull()
	}
	CSG getEyeLinkageCup(){
		CSG cup = new Sphere((boltDiam.getMM()*2.2 )-
						printerOffset.getMM()
		).toCSG()
		CSG pin = new Sphere((boltDiam.getMM()*1.5)+
						printerOffset.getMM(),30,15).toCSG()
		
		CSG ringBox =new Cube(	boltDiam.getMM()*4,// X dimention
			boltDiam.getMM()*4,// Y dimention
			thickness.getMM()*2//  Z dimention
			).toCSG()// 
			.movex(boltDiam.getMM()*4/3)
		CSG linkage =new Cube(	boltDiam.getMM()*3,// X dimention
			boltDiam.getMM()*3,// Y dimention
			thickness.getMM()*2//  Z dimention
			).toCSG()// 
			.toXMin()
			.movex(boltDiam.getMM())
		cup = cup.intersect(ringBox)
				.union(linkage)
				.difference(pin)
		return cup
	}
	CSG getEye(double diameter,CSG ballJointKeepAway){
		if(eyeCache.get(diameter)!=null){
			println "getting Eye cached"
			return eyeCache.get(diameter).clone()
		}
		CSG fastEye = new Sphere(diameter/2).toCSG()
		eyeCache.put(diameter,fastEye)
		return fastEye
		double cupOffset = 4
		ballJointKeepAway= ballJointKeepAway
						.union(
							ballJointKeepAway
							.union(ballJointKeepAway.movex(-10))
							.hull()
							.difference(new Cube(diameter)
							.toCSG()
							.toZMin()
							)
							)
		CSG eye = new Sphere(diameter/2,30,15)// Spheres radius
					.toCSG()// convert to CSG to display
					.difference(new Cube(diameter).toCSG().toXMax().movex(-cupOffset))
					.difference(new Cube(diameter).toCSG().toXMin().movex(diameter/2-6))
					.difference(ballJointKeepAway)
		
		CSG slot = new Sphere(boltDiam.getMM()*2.2,30,15).toCSG()
		
		CSG pin = new Sphere(boltDiam.getMM()*1.5,30,15).toCSG()
				.union(
					new Cylinder(	boltDiam.getMM()/1.5,
								boltDiam.getMM()/1.5,,
								boltDiam.getMM()*4,(int)15)
					.toCSG() 
					.roty(-90)
					)
		
		slot = slot.movex(-cupOffset)
				.union(slot)
				.hull()
		slot=slot.difference(pin)
		for (int i=1;i<5;i++){
			eye=eye
			.difference(
				slot
				.movez(eyemechRadius.getMM()+boltDiam.getMM()/2))
				.rotx(90*i)
		}
		/*
		eye=eye.union( getEyeLinkageCup()
					.roty(180)
					.movez(eyemechRadius.getMM()+boltDiam.getMM()/2))
		*/			
		eyeCache.put(diameter,eye)
		return eye			
	}
	
	CSG tSlotNutAssembly(){
		LengthParameter boltDiam 		= new LengthParameter("Bolt Diameter",2.5,[8,2])
		LengthParameter nutDiam 		= new LengthParameter("Nut Diameter",4,[10,3])
		LengthParameter thickness 		= new LengthParameter("Material Thickness",3.5,[10,1])
		LengthParameter nutThick 		= new LengthParameter("Nut Thickness",2,[10,3])
		LengthParameter boltLength		= new LengthParameter("Bolt Length",10,[20,5])
		CSG bolt = new Cube( thickness.getMM(),
				boltDiam.getMM(),
				boltLength.getMM()+thickness.getMM())
				.toCSG()
				.toZMin()
		CSG nut =new Cube( thickness,
				nutDiam,
				nutThick)
				.toCSG()
				.toZMin()
				.movez(thickness.getMM()*2)
		return bolt.union(nut)		
	}
	
	CSG tSlotPunch(CSG allignedIncoming){
		return allignedIncoming
				.difference(tSlotKeepAway(),tSlotNutAssembly())
				.union(tSlotTabs())
				
		
	}
	ArrayList <CSG> tSlotPunchLocaton(ArrayList <CSG> allignedIncoming,Transform location){
		CSG part = allignedIncoming.get(0)
					.difference(tSlotKeepAway().transformed(location),
						tSlotNutAssembly().transformed(location))
					.union(tSlotTabs().transformed(location))
		CSG keepaway = allignedIncoming.get(1)
						.union(
						tSlotTabsWithHole()
						.transformed(location))
					
		return [part,keepaway]
		
	}
	
	ArrayList <CSG> generateServoBracket(String servoName){
		LengthParameter boltDiam 		= new LengthParameter("Bolt Diameter",2.5,[8,2])
		LengthParameter thickness 		= new LengthParameter("Material Thickness",3.5,[10,1])
		HashMap<String,Object> jawServoConfig = Vitamins.getConfiguration("hobbyServo",servoName)
		double servoHeightFromMechPlate = Double.parseDouble(jawServoConfig.get("servoThinDimentionThickness").toString())/2
		double servoJawMountPlateOffset = Double.parseDouble(jawServoConfig.get("tipOfShaftToBottomOfFlange").toString())
		double servoWidth = Double.parseDouble(jawServoConfig.get("flangeLongDimention").toString())
		double servoCentering  = Double.parseDouble(jawServoConfig.get("shaftToShortSideFlandgeEdge").toString())
		double flangeMountOffset =  Double.parseDouble(jawServoConfig.get("tipOfShaftToBottomOfFlange").toString())
		double leftOffset = servoCentering+thickness.getMM()*3+boltDiam.getMM()*2
		double rightOffset = servoWidth-leftOffset+thickness.getMM()*5+boltDiam.getMM()*2
		
		CSG jawServo = Vitamins.get("hobbyServo",servoName)
	                        .toZMax()
	                        .roty(90)
	                        .rotz(90)
	     CSG bracket =  new Cube(servoWidth+thickness.getMM()*9+boltDiam.getMM()*4,
							thickness.getMM(),
							servoHeightFromMechPlate*2+9
							).toCSG()
							.toZMin()
							.toXMax()
							.movex(servoCentering+thickness.getMM()*6+boltDiam.getMM()*2)	
							.movez(thickness.getMM())
		bracket=tSlotPunch(bracket
				.movex(rightOffset)
				.rotz(90)
				).rotz(-90).movex(-rightOffset)
		bracket=tSlotPunch(bracket
				.movex(-leftOffset)
				.rotz(90)
				).rotz(-90).movex(leftOffset)
		CSG bracketWithHoles = bracket
							.movex(-leftOffset)
							.rotz(90)
							.union( tSlotTabsWithHole())
							.rotz(-90)
							.movex(leftOffset)
		bracketWithHoles = bracketWithHoles
							.movex(rightOffset)
							.rotz(90)
							.union( tSlotTabsWithHole())
							.rotz(-90)
							.movex(-rightOffset)
		def bracketParts = [bracket,bracketWithHoles] .collect{
	   		it.movez(-servoHeightFromMechPlate) 
				.toYMax()
				.movey(-flangeMountOffset)
				.movez(-thickness.getMM())
				.difference(jawServo)                   
	   	}
		//bracketParts.add(jawServo)	            
	   	return bracketParts
	}

	CSG washer(){
		LengthParameter thickness 		= new LengthParameter("Material Thickness",3.5,[10,1])
		LengthParameter boltDiam 		= new LengthParameter("Bolt Diameter",2.5,[8,2])
		CSG bolt =new Cylinder(
							boltDiam.getMM()/2,
							boltDiam.getMM()/2,
							thickness.getMM()*2,
							(int)15).toCSG()
		return new Cylinder(
							boltDiam.getMM()*1.5,
							boltDiam.getMM()*1.5,
							thickness.getMM(),
							(int)15).toCSG()
							.difference(bolt)
							//.movex(-5)
	}
	
	ArrayList <CSG> generateServoHinge(String servoName, double eyePlateHeight){
		LengthParameter boltDiam 		= new LengthParameter("Bolt Diameter",2.5,[8,2])
		LengthParameter thickness 		= new LengthParameter("Material Thickness",3.5,[10,1])
		LengthParameter boltLength		= new LengthParameter("Bolt Length",10,[20,5])
		HashMap<String,Object> jawServoConfig = Vitamins.getConfiguration("hobbyServo",servoName)
		double servoHeightFromMechPlate = Double.parseDouble(jawServoConfig.get("servoThinDimentionThickness").toString())/2
		double widthOfTab = thickness.getMM()*4+boltDiam.getMM()
		CSG pinAssembly = new Cube(	thickness.getMM(),
								widthOfTab,
								eyePlateHeight-thickness.getMM()
		
			).toCSG()
			.toZMin()
			.movez(thickness.getMM())
			
		pinAssembly =tSlotPunch(	pinAssembly)
		pinAssembly =tSlotPunch(	pinAssembly.rotx(180).toZMin().movez(+thickness.getMM()))	
	
		pinAssembly=pinAssembly.toYMin()
					.union(new Cube(	thickness.getMM(),
									thickness.getMM()*2,
									thickness.getMM()
					).toCSG()
							.movez(servoHeightFromMechPlate+thickness.getMM())
							.movey(-thickness.getMM()/2)
							)
		def parts = [pinAssembly,pinAssembly
							.union(tSlotTabsWithHole()
								.movey(widthOfTab/2),
								tSlotTabsWithHole()
								.roty(180)
								.movez(eyePlateHeight+thickness.getMM())
								.movey(widthOfTab/2)
							)]
	
		return parts
	}

	ArrayList <CSG> generateEyeRings(CSG upperHead,double xdist,double height){
		double cheecWidth = headDiameter.getMM()/6
		double cheeckAttach = eyeCenter.getMM()/2 -cheecWidth/2
		double attachlevel =jawHeight.getMM()+thickness.getMM()-height
		
		CSG lring =new Cylinder(leyeDiam.getMM()/2,leyeDiam.getMM()/2,thickness.getMM(),(int)30).toCSG() // a one line Cylinde
					.toZMin()
					.roty(-90)
		CSG rring =new Cylinder(reyeDiam.getMM()/2,reyeDiam.getMM()/2,thickness.getMM(),(int)30).toCSG() // a one line Cylinder
					.toZMin()
					.roty(-90)
		double scale = 1.4
		CSG rKeepaway= rring
					.scaley(scale)
					.scalez(scale)
					.movey(-eyeCenter.getMM()/2)
		rring=rring
				.movey(-eyeCenter.getMM()/2)
		CSG lKeepaway= lring
					.scaley(scale)
					.scalez(scale)
					.movey(eyeCenter.getMM()/2)
		lring=lring
				.movey(eyeCenter.getMM()/2)		
		
		//boltDiam			
		CSG lug =new Cylinder(boltDiam.getMM(),boltDiam.getMM(),thickness.getMM(),(int)30).toCSG() // a one line Cylinder
					.toZMin()
					.roty(-90)

		CSG llug = lug
					.movez(leyeDiam.getMM()/2)
					.movey(eyeCenter.getMM()/2)
		CSG rlug = lug
					.movez(reyeDiam.getMM()/2)
					.movey(-eyeCenter.getMM()/2)
					
		CSG attach = new Cube(	thickness.getMM(),// X dimention
							cheecWidth,// Y dimention
							boltLength.getMM()+thickness.getMM()//  Z dimention
							).toCSG()
							.toZMin()
							.toXMin()
							.movez(attachlevel)
		CSG nose = attach
					.union(attach
							.toZMax()
							.movez(reyeDiam.getMM()/2))
					.movez(-attachlevel)
		CSG plate =lKeepaway
					.union(attach
								.movez(-attachlevel))
								.hull()
					.union(rKeepaway
							.union(attach
								.movez(-attachlevel))
								.hull())
					
		plate=plate
				.union(
					attach.movey(-cheeckAttach)
						.union(rring,rlug)
						.hull()
					)
		plate=plate
				.union(
					attach.movey(cheeckAttach)
						.union(lring,llug)
						.hull()
					)
		plate=plate.difference(lring.makeKeepaway(7))
		plate=plate.difference(rring.makeKeepaway(7))
		
		def lcheekLoc =  new Transform().translate(thickness.getMM()/2, 
											cheeckAttach,
											attachlevel-thickness.getMM())
		def rcheekLoc =  new Transform().translate(thickness.getMM()/2, 
											-cheeckAttach,
											attachlevel-thickness.getMM())
		def parts = tSlotPunchLocaton([plate,plate.clone()],lcheekLoc)
		parts = tSlotPunchLocaton(parts,rcheekLoc)

		return parts.collect{
			it.movex(xdist)	
				.movez(height)	
		}
	}
	
	ArrayList <CSG> generateUpperHead(CSG lowerHead){
		LengthParameter boltDiam 		= new LengthParameter("Bolt Diameter",4,[8,2])
		LengthParameter thickness 		= new LengthParameter("Material Thickness",3.5,[10,1])
		LengthParameter headDiameter 		= new LengthParameter("Head Dimeter",100,[200,50])
		LengthParameter snoutLen 		= new LengthParameter("Snout Length",headDiameter.getMM(),[200,50])
		LengthParameter upperHeadDiam 		= new LengthParameter("Upper Head Height",20,[300,0])
		LengthParameter jawHeight 		= new LengthParameter("Jaw Height",50,[200,10])
		CSG upperHead = new Cylinder(	headDiameter.getMM()/2,
								headDiameter.getMM()/2,
								thickness.getMM(),
								(int)30).toCSG()
								.difference(new Cube(headDiameter.getMM()+snoutLen.getMM())
								.toCSG()
								.toYMin()
								
								)
		upperHead=upperHead
			.union( 
				upperHead.union(upperHead.movey(-upperHeadDiam.getMM())).hull(),
				upperHead
				.scalex(2*snoutLen.getMM()/headDiameter.getMM())
				.difference(new Cube(upperHeadDiam.getMM()+snoutLen.getMM())
								.toCSG()
								.toYMax()
								.toXMax()))
				.rotx(90)	
				.movey( -thickness.getMM()/2)	
				.movez(thickness.getMM())
		double moutOffset = lowerHead.getMinX()+thickness.getMM()*3
		upperHead = upperHead.rotz(90)
		upperHead = tSlotPunch(	upperHead				
		.movey(moutOffset)
		)
		.movey(-moutOffset)	
		double backeHeadMount = 
		moutOffset = lowerHead.getMaxX()-thickness.getMM()*3
		upperHead = tSlotPunch(	upperHead				
		.movey(moutOffset)
		)
		.movey(-moutOffset)	
	
		upperHead = upperHead.rotz(-90)
		CSG 	upperHeadWithHoles = upperHead
								.union(tSlotTabsWithHole()
										.rotz(90)
										.movex(lowerHead.getMinX()+thickness.getMM()*3)	)
										.union(
											tSlotTabsWithHole()
											.rotz(90)
											.movex(lowerHead.getMaxX()-thickness.getMM()*3)	
											)
				
		def parts = [upperHead,upperHeadWithHoles].collect{
			it.movez(jawHeight.getMM())
		} as ArrayList<CSG>
	
		return parts
	}
	/**
	 * This function generated the eyelid
	 * Side effect is to set the pin distance between the rotation pins
	 */
	CSG eyeLid(double diameter){
		double lidThickness = 4
		
		eyeGearSpacing= eyeLidPinDiam*3/2
		double lidMaxAngle =60
		double outerDiameter = diameter/2.0+lidThickness
		double pinLength = thickness.getMM()*2+lidThickness/2
		double cutCubeSize = diameter*2+lidThickness*4+thickness.getMM()*4
		CSG lid  = new Sphere(diameter/2.0+lidThickness,40,20)
					.toCSG()
					.difference(new Sphere(diameter/2+1,40,20).toCSG())
					.difference(new Cube(diameter+lidThickness*2).toCSG()
								.toXMax()
								//.movex(eyeGearSpacing)
								)
					.difference(new Cube(diameter+lidThickness*2).toCSG().toZMax())
					.difference(new Cylinder(lidThickness/4,lidThickness/4,pinLength,(int)30).toCSG()
							.movex(outerDiameter-lidThickness/2)
					)
		CSG pin = new Cylinder(eyeLidPinDiam/2,eyeLidPinDiam/2,pinLength,(int)30).toCSG()
					.toZMin()
					
					.rotx(90)
					
					//.movex(eyeGearSpacing)
		CSG ring = new Cylinder(eyeLidPinDiam,eyeLidPinDiam,pinLength/2,(int)30).toCSG()
					.toZMin()
					.rotx(90)
					.difference(pin.makeKeepaway(printerOffset.getMM()))
		
		CSG upperlid=lid.union(pin
					.toYMin()
					.movey(outerDiameter-lidThickness/2))
			   .union(pin
					.toYMax()
					.movey(-outerDiameter+lidThickness/2))
			   .roty(lidMaxAngle)
			   .difference(new Cube(diameter+lidThickness*4+thickness.getMM()*4).toCSG()
								.toXMax()
								.movex(-eyeLidPinDiam/4)
								)
		
		BowlerStudioController.addCsg(upperlid)
		CSG brace = new Cylinder(eyeLidPinDiam,eyeLidPinDiam,pinLength/2+eyeLidPinDiam/2,(int)30).toCSG()
							.rotx(90)
				   			.toXMin()
				   			.toZMin()
				   			.movex(-eyeLidPinDiam/2)
				   			.difference(pin.makeKeepaway(printerOffset.getMM()))
		CSG lowerlid=lid
				  .difference(new Cube(cutCubeSize).toCSG()
									.toZMax()
									.movez(eyeLidPinDiam/2)
									)
				   .union(ring
						.toYMin()
						.movey(outerDiameter))
				   .union(ring
						.toYMax()
						.movey(-outerDiameter))
				   .roty(lidMaxAngle)
				   .union(brace
				   		.toYMin()
						.movey(outerDiameter-lidThickness/2)
				   )	
				    .union(brace
						.toYMax()
						.movey(-outerDiameter+lidThickness/2))
				   .difference(new Cube(cutCubeSize).toCSG()
									.toXMax()
									.movex(-eyeLidPinDiam/4)
									)
				   .rotx(180)
				   .difference(upperlid
				   				.roty(-lidMaxAngle*2)
				   				.makeKeepaway(printerOffset.getMM()),
				   			upperlid
				   				.roty(-lidMaxAngle*2+5)
				   				.makeKeepaway(printerOffset.getMM())	
				   				)
		
		BowlerStudioController.addCsg(lowerlid)
		
		return upperlid
	}
}
if(args!=null)
	return new Headmaker().makeHead(args.get(0))
CSGDatabase.clear()//set up the database to force only the default values in
ArrayList<CSG> ballJointParts= (ArrayList<CSG>)ScriptingEngine.gitScriptRun(
		                                "https://github.com/madhephaestus/cablePullServo.git", // git location of the library
			                              "ballJointBall.groovy" , // file to load
			                              null// no parameters (see next tutorial)
		                        )
CSG ballJoint = ballJointParts.get(0)
CSG ballJointKeepAway = ballJointParts.get(1)
//return new Headmaker().getEye(46,ballJointKeepAway)
//return new Headmaker().getEyeLinkageCup()
//
return new Headmaker().makeHead(false)	
//return new Headmaker().eyeLid(new LengthParameter("Left Eye Diameter",35,[200,29]).getMM())
