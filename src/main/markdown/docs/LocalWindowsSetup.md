## Windows Installation

- **Windows Installer** : Get the current [`owlcms_setup_${revision}.exe`](https://github.com/${env.REPO_OWNER}/${env.O_REPO_NAME}/releases/download/${revision}/owlcms_setup_${revision}.exe) file (located in the `assets` section at the bottom of each release in the [release repository](https://github.com/${env.REPO_OWNER}/${env.O_REPO_NAME}/releases/latest) ).
- Download and open the installer
  > You may get false alarms about the software being potentially dangerous.  *Rest assured that it is absolutely not*.  Use the `...` and dropdown menu options to get and keep the downloaded files anyway.
  >
  > If you get a blue `Windows protected your PC` warning, use the `More Info` button and `Install Anyway`.  See this page for more information [Make Windows Defender Allow Installation](DefenderOff)
  
-  The installer will prompt you for an installation location.  The default is usually correct.

  ![020_installLocation](img\LocalInstall\020_installLocation.png)

-  Accept all the defaults.  Doing so will create a shortcut on your desktop.

  ![030_desktop](img\LocalInstall\030_desktop.png)

-  Double-clicking on the icon will start the server and a browser. See [Initial Startup](#initial-startup) for how to proceed.

-  If you just want to use dummy data to practice (which will not touch the actual database), right-click on the icon, double-click on "Open File Location" and then double-click on the `demo-owlcms.exe` file.

## Initial Startup

> The following instructions assume that you are connected to a network.  If you are running a competition with several laptops, plug in your router, and *connect your laptop to the router prior to starting OWLCMS*. In that way OWLCMS will be able to tell you what network address it is getting from the router.

When OWLCMS is started on a laptop, two windows are visible:  a command-line window, and an internet browser

![040_starting](img\LocalInstall\040_starting.png)

- The terminal (command-line) window (typically with a black background) is where the OWLCMS primary web server shows its execution log.  Something like this will be visible to show that all is going well.  If you have already started the owlcms program, you may see an error message telling you that you can only have one at a time -- you will need to find the other one and stop it.
  ![log](img/LocalInstall/log.png)
  
- Normally, a browser will be opened automatically.  If the browser does not open automatically, start a browser and navigate to http://localhost:8080 .  The browser will sit there waiting for the program to finish loading the database and become ready.

- After the browser page loads, if you look at the top, you will see what address to use when connecting from other laptops

  ![startup](img/LocalInstall/startup.png)

  In this example the other laptops on the network would use the address `http://192.168.1.174:8080/` to communicate with the primary server.  "(wired)" refers to the fact that the primary laptop is connected via an Ethernet cable to its router -- see [Local Access](EquipmentSetup#local-access-over-a-local-network) for discussion.  

  > When running a competition with a local router, it is recommended to connect the owlcms server to the router with an Ethernet cable.  If a (wired) address is shown, this is the one you should use on the other laptops.

  The addresses shown <u>depend on your own specific networking setup</u> and you normally use one of the addresses displayed on the home page.

  If none of the addresses listed work, something in your networking setup is preventing access.   The most likely cause is a firewall running on the server or on the network, which will need to be disabled. 

- All the other displays and screens connect to the primary server.  <u>You can stop the program by clicking on the x</u> or clicking in the window and typing `Control-C`.  If you stop the program, all the other screens and displays will wait.  If you restart the main program, they will notice and reload.  Normally there is no need to reload them manually, but there is no harm in doing so.

## Accessing the Program Files and Configuration

In order to uninstall owlcms4, to report problems, or to change some program configurations, you may need to access the program directory. In order to do so, right-click on the desktop shortcut and select "Open File Location"

![070_openLocation](img\LocalInstall\070_openLocation.png)

If you do so, you will see the installation directory content:

![080_files](img\LocalInstall\080_files.png)

- `owlcms.exe` starts the owlcms server.  `demo-owlcms.exe` does the same, but using fictitious data that is reset anew on every start; this makes it perfect for practicing.

- `unins000.exe` is the unistaller.  It will cleanly uninstall everything (including the database and logs, so be careful)

- `database` contains a file ending in `.db` which contains competition data and is managed using the [H2 database engine](https://www.h2database.com/html/main.html). 

- `logs` contains the execution journal of the program where the full details of what happened are written. If you report bugs, you will be asked to send a copy of the files found in that directory (and possibly a copy of the files in the database folder as well).

- `local` is a directory that is used for translating the screens and documents to other languages, or to add alternate formats for results documents.

- `jre`  contains the Java Runtime Environment

- the file ending in `.jar` is the OWLCMS application in executable format

- the `owlcms.l4j.ini` file is used to override application settings (for example, to force the display language) or technical settings

## Control Access to the Application

Mischevious users can probably figure out your WiFi network password, and gain access to the application. To prevent this, you will need to start the application with an extra parameter.

- `PIN` is an arbitrary strings of characters that will be requested when starting the first screen whenever you start a new session (typically, once per browser, or when the system is restarted). 

- On Windows, go to the installation directory (see [Accessing the Program Files and Configuration](LocalSetup#control-access-to-the-application) for how) and right-click on the `owlcms.l4j.ini` file; select `Edit` and add a line that reads 

  ```
  -DPIN=5612
  ```

  to define the pin (use your own value instead of 5612, obviously).  You can then use `owlcms.exe` as usual


## Defining the language

You can use the same technique as for the PIN to force a language to be used on all the screens.  By default, OWLCMS will respect the browser settings.  To force a locale (say Canadian French, whose code is `fr_CA`)-- a locale is a language with possible per-country variations --  you can

-  define the Java system property `locale` (small letters) using the syntax 
  `java -Dlocale=fr_CA` (on Windows, add `-Dlocale=fr_CA` to the `owlcms.l4j.ini` file).  
- Alternately, define the environment variable `LOCALE` with the value `fr_CA` 

If neither `-Dlocale` or `LOCALE` are defined, the [language setting](Preparation#display-language) from the competition information page is used.

## Configuration Parameters

See the [Configuration Parameters](Configuration.md  ' :include') page to see additional configuration options in addition to the ones presented on this page.