#include <ESP8266WiFi.h>

const char* ssid = "Ajay";
const char* password = "Dr.Strange";
const char* host = "10.216.16.251";   // <-- YOUR SERVER IP

#define REED D5
#define DOOR_ID "DOOR1"   // Change for each ESP

void setup() {
  Serial.begin(115200);
  pinMode(REED, INPUT_PULLUP);

  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
  }
}

void loop() {

  WiFiClient client;
  String state;

  if (digitalRead(REED) == LOW) {
    state = "CLOSED";
  } else {
    state = "OPEN";
  }

  if (client.connect(host, 80)) {
    client.println(String(DOOR_ID) + ":" + state);
    client.stop();
  }

  delay(2000);
}