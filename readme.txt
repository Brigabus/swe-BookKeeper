This is a repository for our CP3490 Software Engineering Term Project: BookKeeper

Java code is written using IntelliJ idea and stored in code/src/main/java/

We're using a Maven Java environment and SQLite for database implementations

INSTALLATION INSTRUCTIONS:

Option A:
> Download the BookKeeper-Standalone.zip file from the releases section.
> Extract the included folder somewhere on your PC
> Run the run.bat file (Or start the jar file using 'java -jar SWE-BookKeeper.jar')
	>The jar file should create any additional files it needs to run upon execution

OPTION B:
> Download the repository as a ZIP file
> Extract the ZIP archive to a location on your computer
> Open the .java files using an IDE and run them from there
	> BookKeeper should automatically create any additional files that it needs to run



USER ACCOUNT TYPES:
	>STUDENTS:
	> Able to view existing bookings in the schedule gui
		>Able to click on existing bookings to bring up more information about the booking
	>Able to manage their account details using the account management tab

	>TEACHERS:
	>Able to view existing bookings in the schedule gui
		>Able to click on existing bookings to bring up more information about the booking
			>If the booking belongs to the signed in teacher, they can modify the booking
		>Able to click on empty time slots in the schedule gui to create new bookings in the timeslot
	>Able to view a list of their created bookings in the booking management tab
		>Able to create new bookings using the appropriate button
		>Able to double click on bookings in order to modify them
 	>Able to manage their account details using the account management tab

 	>ADMINS:
 	>Able to view existing bookings in the schedule gui
 		>Able to click on existing bookings to bring up more information about the booking
 			>Able to modify all bookings
 		>Able to click on empty time slots in the schedule gui to create new bookings in the timeslot
 	>Able to view a list of currently created bookings in the booking management tab
 		>Able to create new bookings using the appropriate button
 		>Able to double click on bookings in order to modify them
 	>Able to manage their account details using the account management tab
