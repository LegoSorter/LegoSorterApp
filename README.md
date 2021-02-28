# Lego Sorter App

**Lego Sorter App** is an android application designed to work with [Lego Sorter Server](https://github.com/LegoSorter/LegoSorterServer). \
The purpose of this application is to send images of lego bricks to the server and handle responses.

## How to start
The easiest way is to download already released apk, which is available [here](https://github.com/LegoSorter/LegoSorterApp/releases/download/1.0.0-alpha/app-debug.apk). \
After the installation, insert a correct ip address of Lego Sorter Server and have fun!

## Available Features
**Lego Sorter App** provides two basic modes for working with lego bricks. 

### Capture mode
This mode is designed for capturing lego bricks and storing them on the server - the main use case of this mode is dataset creation. \
User inputs a name of lego bricks which are going to be captured, and then they are stored on the server with an appropriate label. \
This way with *auto-capture mode* it's possible to create a big dataset in reasonable time. \
Captured images can contain a lot of bricks of the same type - all of them will be cut from the original image and then saved individually.

### Analyze mode
This mode allows analyzing captured images in a context of detected bricks. \
In this mode all detected bricks are marked on the live camera preview, so a user can test a detection model.