## WIP

```
sudo apt install wiringpi
sudo apt install pigpio
```

``` /dev/input/by-id/usb-USB_Keyboard_USB_Keyboard_SN201706VER1-event-kbd ```

```sudo java -jar servoTester-1.0-SNAPSHOT.jar --Dpi4j.library.path="/opt/pi4j/lib"```

raspi3Bで動かすときはjar内の`natives/linux-arm`に`libconnector.so`を入れる  
gradleで自動化とかしたい気持ち