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
	public ArrayList<CSG> generateBody(MobileBase base ){
	//println base.getInstance()
	println "Generating body"

	int numPanels = 4;
	CSG cChannelRef = centerOnAxes(createCChannel(numPanels)).rotz(90); //double long
	CSG mainBody    = cChannelRef.union(cChannelRef.rotx(180).movez(62.5)) //two, sandwich style

	//Messy way of populating corners... no real good way to fix
	ArrayList<ArrayList<Double>> corners = [ [62.5 * numPanels/2, 31.25], [62.5 * numPanels/2, -31.25], [-62.5 * numPanels/2, -31.25], [-62.5 * numPanels/2, 31.25] ]; //x, y
	ArrayList<CSG> topLinks = new ArrayList<CSG>();
	
	ArrayList<CSG> bodyParts = new ArrayList<CSG>()
	ArrayList<CSG> attachmentParts = new ArrayList<CSG>()

	double maxZ = 0;
	
	def remoteLegPiece = ScriptingEngine.gitScriptRun("https://github.com/DotSlash-CTF/Bulldog.git", "LegLinks/LegMethods.groovy", null);
	for(DHParameterKinematics l:base.getLegs()){
		TransformNR position = l.getRobotToFiducialTransform();
		Transform csgTrans = TransformFactory.nrToCSG(position);

		double thisZ = position.getZ();
		if(thisZ > maxZ)
			maxZ = thisZ;

		DHParameterKinematics sourceLimb=l
		LinkConfiguration conf = sourceLimb.getLinkConfiguration(0);
		ArrayList<DHLink> dhLinks=sourceLimb.getChain().getLinks();
		DHLink dh = dhLinks.get(0);
		HashMap<String, Object> servoMeasurments = Vitamins.getConfiguration(conf.getElectroMechanicalType(),conf.getElectroMechanicalSize())
		CSG servoReference=   Vitamins.get(conf.getElectroMechanicalType(),conf.getElectroMechanicalSize())
								.rotz(180+Math.toDegrees(dh.getTheta()))
		CSG horn = Vitamins.get(conf.getShaftType(),conf.getShaftSize())	
		
		double servoNub = servoMeasurments.tipOfShaftToBottomOfFlange - servoMeasurments.bottomOfFlangeToTopOfBody
		double servoTop = servoReference.getMaxZ()-servoNub
		for(CSG attachment:	generateCad(l,0)){
			//println "attach:" + attachment.toString()
			//CSG movedCorner = attachment
			//	.transformed(csgTrans)// this moves the part to its placement where it will be in the final model
			CSG movedCorner1 = new Cube(25, 25, 25).toCSG().transformed(csgTrans);
			//CSG movedCorner = remoteLegPiece.createBaseLink2(CSG servo, CSG hornRef, 80)
			CSG movedCorner2 = remoteLegPiece.createBaseLink2(servoReference, horn, 80 )
			attachmentParts.add(movedCorner2.transformed(csgTrans).setColor(javafx.scene.paint.Color.AQUA))
			topLinks.add(movedCorner2.transformed(csgTrans));
		}
	}

	for(ArrayList<Double> coords : corners)
	{
		CSG cornerBlock = new Cube(25, 25, 25).toCSG().movex(coords.get(0)).movey(coords.get(1));
		attachmentParts.add(cornerBlock)
	}
	print "in generateBody"
	for(ArrayList<Double> coords : corners)
	{
		print coords[0] + " : x"
		println coords[1] + " : y"
	}
	

	add(bodyParts, makeVexRibCage(ribVals, matThickness.getMM(), mainBody.hull()).movez(maxZ), base.getRootListener());
	add(bodyParts, mainBody.movez(100), 	  base.getRootListener())
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

	private CSG createCChannel(int secLength) //Number of 5x panels - width of 62.5, length of 62.5 * secLength
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
		println("Rib with dimensions " + ribVals[0][0] + " " + ribVals[0][1] + " " + matThickness + " " + ribVals[0][2]);
		for(int i = 1; i < ribVals.length; i++)
		{
			ribs = ribs.union(makeRib(ribVals[i][0], ribVals[i][1], matThickness, spine).movex(ribVals[i][2]));
			println("Rib with dimensions " + ribVals[i][0] + " " + ribVals[i][1] + " " + matThickness + " " + ribVals[i][2]);
		}
		return ribs;
	}

	private CSG makeVexRibCage(double[][] ribVals, double matThickness, CSG spine)
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
};
