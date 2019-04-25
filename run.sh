#!/bin/bash
URL="https://docs.google.com/spreadsheets/d/1YsA8jDCvN2fEnT38JC1ca_UeSUtjK0tn5M2PUg-QE7c/edit#gid=1971103039"
DIRPATH="/home/korisnik/Desktop/XML"
PROG=reportsXML-1.0-SNAPSHOT.jar
OPTION="-create"
#options -create -updateI -updateS
java -jar $PROG $OPTION $URL $DIRPATH
