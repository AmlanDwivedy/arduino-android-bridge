char incomingByte;  // incoming data
long  FAN_LED = 12;      // LED pin for fan
long  HEATER_LED = 2;
 
void setup() {
  Serial.begin(9600); // initialization
  pinMode(FAN_LED, OUTPUT);
  pinMode(HEATER_LED, OUTPUT);
  Serial.println("Press 1 to LED ON or 0 to LED OFF...");
}
 
void loop() {
  if (Serial.available() > 0) {  // if the data came
    incomingByte = Serial.read(); // read byte
    Serial.println(incomingByte);
    if(incomingByte == '0') {
       digitalWrite(FAN_LED, LOW);  // if 1, switch LED Off
       digitalWrite(HEATER_LED, HIGH);  // if 1, switch LED Off
       Serial.println("LED OFF. Press 1 to LED ON!");  // print message
    }
    if(incomingByte == '1') {
       digitalWrite(FAN_LED, HIGH); // if 0, switch LED on
       digitalWrite(HEATER_LED, LOW);
       Serial.println("LED ON. Press 0 to LED OFF!");
    }
  }
}
