import com.neuronrobotics.bowlerstudio.creature.ICadGenerator;
import com.neuronrobotics.bowlerstudio.creature.CreatureLab;
import org.apache.commons.io.IOUtils;
import com.neuronrobotics.bowlerstudio.vitamins.*;
import eu.mihosoft.vrl.v3d.parametrics.*;
import com.neuronrobotics.bowlerstudio.vitamins.Vitamins;
import javafx.scene.paint.Color;
import eu.mihosoft.vrl.v3d.Transform;
import com.neuronrobotics.bowlerstudio.physics.TransformFactory;

return new ICadGenerator(){
	HashMap<String , HashMap<String,ArrayList<CSG>>> map =  new HashMap<>();
	HashMap<String,ArrayList<CSG>> bodyMap =  new HashMap<>();
	LengthParameter thickness 		= new LengthParameter("Material Thickness",3.15,[10,1])
	LengthParameter headDiameter 		= new LengthParameter("Head Dimeter",100,[200,50])
	LengthParameter snoutLen 		= new LengthParameter("Snout Length",63,[200,50])
	LengthParameter jawHeight 		= new LengthParameter("Jaw Height",32,[200,10])
	LengthParameter eyeCenter 		= new LengthParameter("Eye Center Distance",headDiameter.getMM()/2,[headDiameter.getMM(),headDiameter.getMM()/2])
	StringParameter servoSizeParam 			= new StringParameter("hobbyServo Default","towerProMG91",Vitamins.listVitaminSizes("hobbyServo"))
	StringParameter boltSizeParam 			= new StringParameter("Bolt Size","M3",Vitamins.listVitaminSizes("capScrew"))
	LengthParameter matThickness = new LengthParameter("Material Thickness",2.5,[200,0])

	HashMap<String, Object>  boltMeasurments = Vitamins.getConfiguration( "capScrew",boltSizeParam.getStrValue())
	HashMap<String, Object>  nutMeasurments = Vitamins.getConfiguration( "nut",boltSizeParam.getStrValue())
	//println boltMeasurments.toString() +" and "+nutMeasurments.toString()
	double boltDimeMeasurment = boltMeasurments.get("outerDiameter")
	double nutDimeMeasurment = nutMeasurments.get("width")
	double nutThickMeasurment = nutMeasurments.get("height")
	double[][] ribVals = [[150, 100, -20],  [200, 100, 30]];
	DHParameterKinematics neck=null;
	/**
	 * Gets the all dh chains.
	 *
	 * @return the all dh chains
	 */
	public ArrayList<DHParameterKinematics> getLimbDHChains(MobileBase base) {
		ArrayList<DHParameterKinematics> copy = new ArrayList<DHParameterKinematics>();
		for(DHParameterKinematics l:base.getLegs()){
			copy.add(l);	
		}
		for(DHParameterKinematics l:base.getAppendages() ){
			copy.add(l);	
		}
		return copy;
	}
	
	@Override 
	public ArrayList<CSG> generateBody(MobileBase base )
	{
		println "Generating body"
	
		//Final values
		double 	numPanels		= 6;
		double 	unitLength 	= 62.5;
		//double[] 	localMaxZ 	= {0, 0, 0, 0}; //Front right, back right, back left, front left (Clockwise from right front)
		double[][]	topLinkCoords = new double[4][3];
	
		//Body CSGs 
		CSG cChannelRef 	= centerOnAxes(createCChannel(numPanels)).rotz(90); //double long
		CSG crossChannel 	= centerOnAxes(Vitamins.get("vexCchannel", "2x20").roty(180)).movex(numPanels * unitLength/2).movez(-1.5 * unitLength - 12.5).movey(6.25);
		CSG spine 		= cChannelRef.movez(-1.5 * unitLength).union(cChannelRef.rotx(180).movez(-0.5 * unitLength));
		CSG mainBody    	= spine.union(crossChannel.movex(0.1 * -unitLength-0.25).movey(-6)).union(crossChannel.movex(-numPanels * unitLength + 0.1 * unitLength-0.25).movey(-6));
	
		//Utilities
		def remoteLegPiece = ScriptingEngine.gitScriptRun("https://github.com/DotSlash-CTF/Bulldog.git", "LegLinks/LegMethods.groovy", null);
	
		//Arrays
		ArrayList<ArrayList<Double>> corners = [ [-unitLength * numPanels/2, unitLength/2], [unitLength * numPanels/2, -unitLength/2], [unitLength * numPanels/2, unitLength/2], [-unitLength * numPanels/2, -unitLength/2] ]; //x, y
		ArrayList<CSG> topLinks = new ArrayList<CSG>();
		ArrayList<CSG> bodyParts = new ArrayList<CSG>();
		ArrayList<CSG> attachmentParts = new ArrayList<CSG>();
		ArrayList<CSG> immobileLinks = new ArrayList<CSG>();
	
		//12 Total links per leg -> we want coords for the highest link on each leg
		int round = 0; //Three rounds per leg
		int loc = 0; //leg -> corresponds to index for localMaxZ (back left, front right, front left, back right)
		for(DHParameterKinematics l:base.getLegs()){
			
			TransformNR position = l.getRobotToFiducialTransform();
			Transform csgTrans = TransformFactory.nrToCSG(position);
	
			LinkConfiguration conf = l.getLinkConfiguration(0);
			ArrayList<DHLink> dhLinks=l.getChain().getLinks();
			DHLink dh = dhLinks.get(0);
			HashMap<String, Object> servoMeasurments = Vitamins.getConfiguration(conf.getElectroMechanicalType(),conf.getElectroMechanicalSize())
			CSG servoReference=   Vitamins.get(conf.getElectroMechanicalType(),conf.getElectroMechanicalSize())
									.rotz(180+Math.toDegrees(dh.getTheta()))
			CSG horn = Vitamins.get(conf.getShaftType(),conf.getShaftSize())	

			
			for(CSG attachment:	generateCad(l,0)){ //3x per leg, total of 12
				CSG immobileLink = remoteLegPiece.createBaseLink2(servoReference, horn, 80);

				//attachmentParts.add(attachment.transformed(csgTrans).movez(300).setColor(javafx.scene.paint.Color.AQUA))
				
				//println "attach:" + attachment.toString()
				//attachmentParts.add(immobileLink.transformed(csgTrans).setColor(javafx.scene.paint.Color.AQUA));
				topLinks.add(immobileLink.transformed(csgTrans));
	
				if(attachment.getBounds().getMax().z > topLinkCoords[loc][2])
				{
					Vector3d topLink = attachment.transformed(csgTrans).getBounds().getMax()
					topLinkCoords[loc][0] = topLink.x
					topLinkCoords[loc][1] = topLink.y
					topLinkCoords[loc][2] = topLink.z

					immobileLinks.add(immobileLink.transformed(csgTrans));
		
					println "loc: " + loc + " new coords: " + topLinkCoords[loc][0] + " " + topLinkCoords[loc][1] + " " + topLinkCoords[loc][2]
				}
				else
					println attachment.getBounds().getMax().z + " failed @ loc " + loc;
			}
			loc++;
			/*
			if(++round % 2 == 0)
			{
				loc++;
				round = 0;
				//print "round is " + round + " and loc is " + loc;
			}
			*/
			println "end of getLegs"
		}
	
		for(double[] e : topLinkCoords)
		{
			println "x: " + e[0] + ", y: " + e[1] + ", z: " + e[2]
		}

		for(CSG e : immobileLinks)
		{
			System.out.println(e.toString());
		}
		
		for(int i = 0; i < 4; i++)
		{
			ArrayList<CSG> coords = corners.get(i);
			CSG cornerBlock = new Cube(25, 25, 25).toCSG().movex(coords.get(0)).movey(coords.get(1) + Math.signum(coords.get(1)) * unitLength);
			
			if(i == 0) //back left
			{
				cornerBlock = cornerBlock/*.movey(2)*/.movex(4.5).difference(crossChannel);
			}
			else if(i == 1) //front right
			{
				cornerBlock = cornerBlock/*.movey(-9)*/.movex(-4.7).difference(crossChannel);
			}
			else if(i == 2) //front left
			{
				cornerBlock = cornerBlock/*.movey(9)*/.movex(-4.7).difference(crossChannel);
			}
			else if(i == 3) //back right
			{
				cornerBlock = cornerBlock/*.movey(-2)*/.movex(4.5).difference(crossChannel);
			}
			
			/*
			int minDist = 99999;
			int minJ;
			int currDist = 0;
			for(int j = 0; j < 4; j++)
			{
				currDist = Math.sqrt( Math.pow(coords.get(0) - topLinkCoords[j][0], 2) + Math.pow(coords.get(1) - topLinkCoords[j][1], 2) )
				if(currDist < minDist)
				{
					minDist = currDist;
					minJ = j;
				}
			}
			*/
			
			attachmentParts.add(cornerBlock.movez(topLinkCoords[0][2] - 1.5 * unitLength).union(immobileLinks.get(i)).hull()); 
			
		}
		
		//print "in generateBody"
		for(ArrayList<Double> coords : corners)
		{
			print coords[0] + " : x"
			println coords[1] + " : y"
		}
	
	
		add(bodyParts, makeVexRibCage(ribVals, matThickness.getMM(), spine.hull()).movez(topLinkCoords[0][2]), base.getRootListener());
		add(bodyParts, mainBody.movez(topLinkCoords[0][2]), 	  base.getRootListener())
		add(bodyParts, attachmentParts, base.getRootListener())
		
			
		
		return bodyParts;
	}


	@Override 
	public ArrayList<CSG> generateCad(DHParameterKinematics sourceLimb, int linkIndex) {
		
		String legStr = sourceLimb.getXml()
		LinkConfiguration conf = sourceLimb.getLinkConfiguration(linkIndex);

		String linkStr =conf.getXml()
		ArrayList<CSG> csg = null;
		HashMap<String,ArrayList<CSG>> legmap=null;
		if(map.get(legStr)==null){
			map.put(legStr, new HashMap<String,ArrayList<CSG>>())	
			// now load the cad and return it. 
		}
		legmap=map.get(legStr)
		if(legmap.get(linkStr) == null ){
			legmap.put(linkStr,new ArrayList<CSG>())
		}
		csg = legmap.get(linkStr)
		if(csg.size()>linkIndex){
			// this link is cached
			println "This link is cached"
			return csg;
		}
		//Creating the horn
		ArrayList<DHLink> dhLinks=sourceLimb.getChain().getLinks();
		DHLink dh = dhLinks.get(linkIndex);
		HashMap<String, Object> shaftmap = Vitamins.getConfiguration(conf.getShaftType(),conf.getShaftSize())
		double hornOffset = 	shaftmap.get("hornThickness")	
		
		// creating the servo
		CSG servoReference=   Vitamins.get(conf.getElectroMechanicalType(),conf.getElectroMechanicalSize())
		.transformed(new Transform().rotZ(90))
		
		double servoTop = servoReference.getMaxZ()
		CSG horn = Vitamins.get(conf.getShaftType(),conf.getShaftSize())	
		
		servoReference=servoReference
			.movez(-servoTop)

		
		
		if(linkIndex==0){
			add(csg,servoReference.clone(),sourceLimb.getRootListener())
			add(csg,servoReference,dh.getListener())
		}else{
			if(linkIndex<dhLinks.size()-1)
				add(csg,servoReference,dh.getListener())
			else{
				// load the end of limb
			}
			
		}
		
		
		add(csg,moveDHValues(horn,dh),dh.getListener())

		if(neck ==sourceLimb ){
			
		}
		
		
		return csg;
	}

	private CSG reverseDHValues(CSG incoming,DHLink dh ){
		println "Reversing "+dh
		TransformNR step = new TransformNR(dh.DhStep(0))
		Transform move = TransformFactory.nrToCSG(step)
		return incoming.transformed(move)
	}
	
	private CSG moveDHValues(CSG incoming,DHLink dh ){
		TransformNR step = new TransformNR(dh.DhStep(0)).inverse()
		Transform move = TransformFactory.nrToCSG(step)
		return incoming.transformed(move)
	}


	private add(ArrayList<CSG> csg ,CSG object, Affine dh ){
		object.setManipulator(dh);
		csg.add(object);
		BowlerStudioController.addCsg(object);
	}
	private add(ArrayList<CSG> csg, ArrayList<CSG> objects, Affine dh)
	{
		for(CSG e : objects)
		{
			e.setManipulator(dh);
			csg.add(e);
			BowlerStudioController.addCsg(e);
		}
	}

	private CSG solidRectFromCSG(CSG start)
	{
		double xDist = start.getMaxX() - start.getMinX();
		double yDist = start.getMaxY() - start.getMinY();
		double zDist = start.getMaxZ() - start.getMinZ();

		return new Cube(xDist, yDist, zDist).toCSG()
	}

	private CSG centerOnX(CSG start)
	{
		double yWidth = start.getMaxY() - start.getMinY();
		return start.toYMin().movey(-(yWidth / 2));
	}
	private CSG centerOnY(CSG start)
	{
		double xWidth = start.getMaxX() - start.getMinX();
		return start.toXMin().movex(-(xWidth / 2));
	}
	private CSG centerOnZ(CSG start)
	{
		double zWidth = start.getMaxZ() - start.getMinZ();
		return start.toZMin().movez(-(zWidth / 2));
	}
	private CSG centerOnAxes(CSG start)
	{
		return centerOnY(centerOnX(centerOnZ(start)));
	}

	private CSG createCChannel(double secLength) //Number of 5x panels - width of 62.5, length of 62.5 * secLength
	{
		CSG toReturn = new Cube(0.1, 0.1, 0.1).toCSG();
		for(int i = 0; i < secLength; i++)
		{
			toReturn = toReturn.union(Vitamins.get("vexCchannel", "5x5").movey(i * 62.5))
		}
		return toReturn;
	}

	private CSG makeRib(double height, double width, double materialThickness, CSG spine)
	{
		CSG base = new Cylinder(1, 1, materialThickness, (int) 30).toCSG();
		CSG center = new Cube(height, 2.5, materialThickness).toCSG().toZMin();
		return base.scalex(height / 2).scaley(width / 2).difference(center).roty(90).difference(spine);
	}

	//FOR RIB TO ATTACH TO C CHANNELS
	private CSG makeVexRib(double height, double width, double materialThickness, CSG spine)
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

	private CSG makeRibCage(double[][] ribVals, double matThickness, CSG spine)
	{
		spine = solidRectFromCSG(spine)
		CSG ribs = makeRib(ribVals[0][0], ribVals[0][1], matThickness, spine).movex(ribVals[0][2]); 
		//println("Rib with dimensions " + ribVals[0][0] + " " + ribVals[0][1] + " " + matThickness + " " + ribVals[0][2]);
		for(int i = 1; i < ribVals.length; i++)
		{
			ribs = ribs.union(makeRib(ribVals[i][0], ribVals[i][1], matThickness, spine).movex(ribVals[i][2]));
			//println("Rib with dimensions " + ribVals[i][0] + " " + ribVals[i][1] + " " + matThickness + " " + ribVals[i][2]);
		}
		return ribs;
	}

	private CSG makeVexRibCage(double[][] ribVals, double matThickness, CSG spine)
	{
		spine = solidRectFromCSG(spine)
		CSG ribs = makeVexRib(ribVals[0][0], ribVals[0][1], matThickness, spine).movex(ribVals[0][2]); 
		//println("Rib with dimensions " + ribVals[0][0] + " " + ribVals[0][1] + " " + matThickness + " " + ribVals[0][2]);
		for(int i = 1; i < ribVals.length; i++)
		{
			ribs = ribs.union(makeVexRib(ribVals[i][0], ribVals[i][1], matThickness, spine).movex(ribVals[i][2]));
			//println("Rib with dimensions " + ribVals[i][0] + " " + ribVals[i][1] + " " + matThickness + " " + ribVals[i][2]);
		}
		return ribs;
	}
};