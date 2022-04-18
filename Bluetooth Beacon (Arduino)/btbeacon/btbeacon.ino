#include <SoftwareSerial.h>

static int TX = 4;
static int RX = 5;

SoftwareSerial BTSerial(TX, RX); // TX/RX
void setup(){
  Serial.begin(9600);
  Serial.println("Hello!");
  BTSerial.begin(9600);
}

void loop(){
  while(BTSerial.available()){
    byte data = BTSerial.read();
    Serial.write(data);
  }
  while(Serial.available()){
    byte data = Serial.read();
    BTSerial.write(data);
  }
}
