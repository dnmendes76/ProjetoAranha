int ledPin = 13;
int state = 0;
int flag = 0;

void setup(){
 pinMode(ledPin, OUTPUT);
 digitalWrite(ledPin, LOW);
 Serial.begin(9600);
}

void loop(){
  if(Serial.available() > 0 ){
   state = Serial.read(); 
   flag = 0;
  }  
  if(state == 'B'){
    digitalWrite(ledPin, LOW);
    if(flag == 0 ){
      Serial.println("LED: off");
      flag = 1;
    }
  }
  else if(state == 'A'){
    digitalWrite(ledPin, HIGH);
    if(flag == 0 ){
      Serial.println("LED: on");
      flag = 1;
    }
  }
}
