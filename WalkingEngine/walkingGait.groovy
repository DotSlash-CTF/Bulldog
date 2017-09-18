import java.time.Duration;

import java.util.ArrayList;

import javafx.application.Platform;

import org.reactfx.util.FxTimer;

import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.util.ThreadUtil;
import com.neuronrobotics.sdk.addons.kinematics.IDriveEngine;


double stepOverHeight=5;
long stepOverTime=100;
Double zLock=-15;
Closure calcHome = { DHParameterKinematics leg -> 
		TransformNR h=leg.calcHome() 

		TransformNR tr = leg.forwardOffset(new TransformNR())
		tr.setZ(zLock)
		
		return tr;

}
boolean usePhysicsToMove = true;

/*
return ScriptingEngine.inlineGistScriptRun("bcb4760a449190206170", 
"GenericWalkingEngine.groovy" , 
[stepOverHeight,stepOverTime,zLock,calcHome,usePhysicsToMove]
);
*/
return ScriptingEngine.gitScriptRun(
	"https://github.com/DotSlash-CTF/Bulldog.git",
	"WalkingEngine/bulldogWalkingEngine.groovy",
	[stepOverHeight, stepOverTime, zLock, calcHome, usePhysicsToMove]
	);
