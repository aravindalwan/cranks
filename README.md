
CRANKS 1.0 - README
===================

ABOUT CRANKS
------------

CRANKS is an Java application that uses the Swing toolkit to provide an interactive environment for the animation and kinematic synthesis of four-bar mechanisms created through a series of geometrical constructions.

CRANKS was written in order to address the lack of a robust software that enables a user to perform the animation and synthesis of mechanisms. Normally, mechanical engineers attack the issue of designing mechanisms through the traditional pen-paper route. Mechanisms are optimized using geometric construction procedures that yield the solutions to specific motion/path/function generation problems. This process is very cumbersome as it involves repeated geometric constructions in order to decide upon the optimum design. Even after the design has been optimized, it is not possible to animate the mechanism without actually building it.

Hence, there is a need for automating this process using the computer and thereby save the time and effort spent in designing mechanisms. By abstracting the geometrical constructions (like drawing lines, circles, etc or finding intersections between geometrical objects) through a CAD-like interface, CRANKS provides a way of performing the entire synthesis of the mechanism on the computer. Once the mechanism has been synthesized, it can be animated so as to obtain the required trace of the coupler point. The entire synthesis process can also be redone in order to change intermediate parameters, thereby yielding a different mechanism.

CRANKS is a program written entirely in Java and uses the Swing toolkit to provide the GUI. It was written using JDK 1.5.0 and is compatible with all later versions. It is platform independent, requiring only the Java Virtual Machine (compatible with versions 1.5.0 or higher) in order to execute. The binary release contains the compiled code distributed as a single JAR file executable, which may be used to run the application. It may also be used in conjunction with the corresponding HTML file to run the program as an applet, thereby giving additional security by preventing the program from interacting with the client computer's file system. The applet version is also intended as a demonstration tool, especially for use online.

FEATURES - version 1.0
----------------------

* Animation and synthesis of four bar mechanisms
* Provides a CAD interface for performing most simple constructions required for the synthesis of mechanisms
* "Undo/redo construction" capability
* Flexibility of redesigning mechanism by repeating construction steps with changes to input parameters, without having to perform all constructions again
* Enables the extraction of the abstract version of the mechanism from the drawing made
* Animation of mechanism alone by providing link parameters
* Calculating the trace (locus) of coupler point
* Zoom/Pan view
* Open/Save construction procedures for future reference and repetition (not available in applet mode)
* Print capability (not available in applet mode)

LICENSE
-------

CRANKS is free software, and you are welcome to redistribute and modify it under the terms of the GNU General Public License (either version 2 or any later version). For details, please see the file COPYING located in the installation directory.

INSTALLATION
------------

The installation procedure that you need to follow depends on the type of distribution that you have downloaded. You may identify the distribution type from the name of the downloaded compressed file.

Binary Files Package (cranks_XXX_binary.zip or cranks_XXX_binary.tar.gz):

This package unpacks to yield a folder "cranks", which contains the binary files necessary for running the program. The package contents are :
* Folder "bin" containing one file "CRANKS.jar", which is the executable JAR file used to run the application
* Folder "lib" containing one file "jdic.jar", which is a library used by CRANKS. See Libraries below for details
* File "COPYING" containing a copy of the GNU General Public License
* File "CRANKS.html", which can be used to run CRANKS as an applet

You will need:

* Java Virtual Machine (compatible with versions 1.5.0 and higher). You may download this from the [Sun website](http://java.sun.org). In order to run the binaries, you need to install only Sun's Java Runtime Environment (JRE) 1.5.0 or higher. If you are going to compile source files as well, you may prefer to install Sun's Java Development Kit (JDK) 1.5.0 or higher. Installing any version of JDK also installs the corresponding version of JRE.

* Internet Browser: Windows users - Internet Explorer 5 or later, or Mozilla 1.4 or later; Linux/Solaris users - Gnome 2.0, Ximian Evolution 1.4 or later, Mozilla 1.4 or later. This is required by the JDIC library, which allows CRANKS to open your default browser to navigate web links.

Installation is as simple as merely unpacking the compressed package, as it already contains pre-compiled files. Use any common unzip or tar utility to decompress the files and start working right away!

Source Files Package (cranks_XXX_source.zip or cranks_XXX_source.tar.gz):

This package contains the source files which need to be compiled in order to produce the executable binaries. The package contents are :
* Folder "lib" containing one file "jdic.jar", which is a compiled library used by CRANKS. See Libraries below for details
* Folder "nbproject" containing the project build files, necessary for building the project using Apache Ant
* Folder "src containing all the Java source files for building CRANKS
* File "build.xml", which is the Ant build script required for building
* File "COPYING" containing a copy of the GNU General Public License
* File "CRANKS.html", which can be used to run CRANKS as an applet

You will need:

* Java Virtual Machine (compatible with versions 1.5.0 and higher). You may download this from the [Sun website](http://java.sun.org). In order to run the binaries, you need to install only Sun's Java Runtime Environment (JRE) 1.5.0 or higher. If you are going to compile source files as well, you will need to install Sun's Java Development Kit (JDK) 1.5.0 or higher. Installing any version of JDK also installs the corresponding version of JRE.

* Jakarta Ant (versions 1.6.2 and higher - I have not tried using older versions, so please inform me if you find that they work as well). You may install it from [here](http://ant.apache.org). This is used to run the build scripts in order to compile the source files.

* Internet Browser: Windows users - Internet Explorer 5 or later, or Mozilla 1.4 or later; Linux/Solaris users - Gnome 2.0, Ximian Evolution 1.4 or later, Mozilla 1.4 or later. This is required by the JDIC library, which allows CRANKS to open your default browser to navigate web links.

The procedure for compiling the binaries from source files is:
1. Unzip (or untar) the compressed package into a folder of your choice.
2. Ensure that the bin folder in the Ant installation directory is on your PATH variable
3. Ensure that the JAVA_HOME variable points to your JDK installation directory
4. Use "ant <target-name>" to run any of the available targets. Eg: "ant compile" will compile the sources into the "build" folder; "ant jar" will compile the files and create the JAR executable in the "bin" folder; "ant release" will compile the files, build the JAR executable and will create in the "release" folder, the release packages similar to this package that you downloaded.

Note: Do NOT rename or change the location of any of the files/folders in the base directory, without making corresponding changes to the "project.properties" file in the "nbproject" folder. This may lead to the Ant build script not functioning. Similarly, the JAR executable needs to find the library files at the relative path "../lib", in order to function properly.

Note: Since I use Sun's [NetBeans IDE](http://www.netbeans.org) to edit my Java projects, I have created source packages directly from my NetBeans project folder. This means that after unzipping the package, you can directly open it as a project under Netbeans and use all the shortcuts for compiling, building, etc. Again, just as above, please take care when renaming/restructuring files if you want to ensure this compatibility with NetBeans.

LIBRARIES
---------

CRANKS bundles the following libraries:

* jdic.jar : JDesktop Integration Components ([JDIC](http://jdic.dev.java.net/)) is a open-source library that is released by the 'GNU Lesser General Public License. CRANKS bundles the version 0.9 binary file in order to allow CRANKS to open your default browser to navigate web links.

USING CRANKS
------------

This is a brief description of the functions of this program and will be replaced by more detailed documentation at a later stage. You can run CRANKS as an application or as an applet. You can access the program in the application mode by running "CRANKS.jar" in the bin folder. The program starts in applet mode if the "CRANKS.html" file is opened. However, it is recommended that you use the former, since the application mode is more versatile. Although they look similar, you will notice that the File menu is absent in the applet, thereby preventing access to local files on the system. Applets are useful when a user wants to run a program from an online webpage, but wants to maintain security by preventing it from having accesss to local files on his/her computer.

Before you start working with CRANKS, it would be useful to familiarize yourself with names of the menus and their respective menu items. As you may notice, most of the names are self-explanatory and it should not take a long time to get used to working with CRANKS. As mentioned in the introduction, CRANKS provides an interface for animating and designing abstract mechanisms. Hence it operates in two modes - Animation and Drawing. The various functions that can be performed under each of these modes are listed in their respective menus.

You can animate a known mechanism by going to the Animation mode. By default, the system starts off with an existing mechanism. You may change the link lengths and other properties through the Set Link Lengths menu-item. Once a mechanism has been set up, it can be animated using the Start/Pause Animation options. The speed and direction of rotation can also be controlled. If your mechanism has a ternary link (i.e. a coupler point has been defined on the link opposite the fixed link), then a trace of the coupler point may also be obtained. Try experimenting with different link lengths and configurations. You may also want to try changing the fixed link and thus obtain kinematic inversions of the same mechanism. Animating mechanisms was never simpler!

Once you are familiar with animating a mechanism, you can proceed to the Drawing stage, by switching to Drawing mode. Don't worry about losing the mechanism that you have defined while in the Animation mode. You can always go back to it by switching back to Animation mode. As you may have already noticed, all graphical work takes place on an imaginary graph paper. The mouse co-ordinates are shown in the status bar at the bottom. By moving the mouse around a bit, you can see that the program view window starts out by default by showing the first quadrant of the co-ordinate plane. By zooming/panning the view, you may see other portions. However, since your workspace is currently empty, you may not notice the difference. So let's try drawing something.

The drawing mode lets you construct geometrical objects like points, lines, circles and triangles (These shapes are enough to start synthesizing mechanisms). The program can also calculate intersections between objects. Intermediate constructions may be hidden by hiding the corresponding objects. Note that it is NOT possible to remove an object; it can only be hidden from view. However, you may use the undo/redo feature to correct mistakes. Alternately, you can hide the mistake and re-perform the construction (though, you would agree with me that undoing is easier and more elegant!). Every object created has a unique ID number that records the chronological order of creation of objects. By moving the mouse over geometrical objects you can see this number displayed in the popup window. This number is used to identify objects when performing constructions.

If you are familiar with the constructions required for synthesizing mechanisms, you may define a sample design problem and find a solution in the form of a four bar mechanism (a set of four points, cyclically joined by lines to give a quadrilateral). A problem may be defined by drawing a set of points or lines required to specify the given motion/path/function generation problem. Once you have finished drawing this mechanism, you must formally export it to the animation mode for animation. This is done using the Create Mechanism option in the Construction menu. Here you must define the links in the form of 4 properly connected lines and the ternary link (if there is one) as a triangle. Once the mechanism is thus created, it may be animated as explained before. You can make changes to the links in the animation mode to see the effect on the animation. However, this will not affect the drawing. You can always get back the original mechanism that you created, by going to the drawing mode and re-creating the mechanism from the lines drawn.

Finally, if you are not happy with the mechanism, you may re-design it. This means that you re-perform the same sequence of constructions with modifications along the way. Modifications may go so far as changing any intermediate construction (Eg:  relocating a point that was drawn, changing the length of a line, changing the radius of a circle, choosing a different set of two points to construct a line with, etc) but should not change the underlying type of construction (Eg: constructing a circle in place of a line, etc). The idea is that the sequence of constructions being the same, it should be possible to change the assumptions in the design to obtain a better design. If the original problem definition remains the same (i.e. those original points and/or lines are not changed), then the final design obtained is assured to be a solution to that problem, whatever be the changes made along the way. Re-designing is done by proceeding along the sequence of steps and modifying any intermediate ones.

In order to see how design problems are solved using CRANKS, you may have a look at the examples given in the cranks_demos package. In order to see the demos, download and unpack the package into a local folder on your computer. Now start CRANKS as an application (by running the JAR file) and click on the Open Workspace option in the File menu. Navigate to the folder containing the demo files and open any one. The final mechanism stored in that file is now displayed. Now switch to drawing mode to see the drawing. Using the redesign feature in the Construction menu, you can step through each of the construction steps (without performing any modifications) in order to see the process of creating a mechanism. You can repeat this procedure as many times as you want without affecting the stored mechanism/drawing. Once you familiar with the order of the steps, you may click Modify on any of the intermediate steps and try changing it to see the effect on the final mechanism. If you feel you have made a mistake, you can always go back a few steps. If all fails, the Cancel Redesign button is there to bail you out so you can start your experimentation afresh. You can always recover the original co nstructions by re-opening the saved file, as long as you have not saved any of your changes. Happy designing!
