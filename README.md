# Particle-Simulator
This is a Java application for simulating particles on a 1280x720 canvas. Features include adding an explorer/sprite that navigates the canvas and renders the periphery of the explorer in terms of the other particles moving nearby, adding particles individually or in batches, real-time interaction through a GUI, and efficient parallel processing.  STDISCM Group 4.

## How to Use: Run an ExplorerClient instance by jar file (if local) or run .java (if connecting to another machine)
### Connection to a different machine
Input your ipv4 address being used by the Main.jar aka the Main server, and then run the ExplorerClient.java file with this specific change

### By default, it's localhost
By default, it's set to localhost for both the code and the jar file, so you just need to change this specific line and then you're good to go

### Introducing the Explorer Mode
This mode is used for activating the sprite and navigating to wherever desired in the canvas. The sprite is centered within the screen and uses a grid perspective in terms of rendering nearby particles in a zoomed layout.

### Movement of Sprite
The movement of the Sprite is simply done by using the arrow keys in your keyboard.

## Getting Started

### Prerequisites
Ensure you have Java installed on your machine. This application requires Java 8 or higher. If Java is not installed, you can download it from the [Oracle Website](https://www.oracle.com/ph/java/technologies/downloads/).

#### Recommended Way to Run:
1. Download the `Main.jar` file from the repository.
2. Open a terminal or command prompt.
3. Navigate to the directory where the `Main.jar` file is located.
4. Execute the following command: java -jar Main.jar
5. When connecting locally, just do everything from 1-4 but using the ExplorerClient.jar
6. **When connecting with different machines,** change the line within the ExplorerClient.java file and replace 'localhost' parameter to your actual Main server's ipv4 address
