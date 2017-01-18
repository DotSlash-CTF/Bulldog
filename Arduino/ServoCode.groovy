//If your DyIO is using a lower voltage power source, you need to disable the brownout detect
dyio.setServoPowerSafeMode(false);
ServoChannel srv = new ServoChannel (dyio.getChannel(9));
ServoChannel serv = new ServoChannel (dyio.getChannel(10));
//Loop 10 times setting the position of the servo 
//the time the loop waits will be the time it takes for the servo to arrive
srv.SetPosition(15);
float time = 5;

System.out.println("Moving with time");
for (int i = 0; i < 4&&!Thread.interrupted(); i++) {
	// Set the value high every other time, exit if unsuccessful
	
	int target;
	int setter;

	srv.SetPosition((int)Math.random() * (172 - 16) + 16, 0);
	serv.SetPosition((int)Math.random() * (172 - 16) + 16, 0);
	
	Thread.sleep((long) (time*150));

	srv.SetPosition((int)Math.random() * (172 - 16) + 16, 0);
	serv.SetPosition((int)Math.random() * (172 - 16) + 16, 0);
     //This will move the servo from the position it is currentlly in
	
	// pause between cycles so that the changes are visible
	Thread.sleep((long) (time*150));

}

System.out.println("DONE");