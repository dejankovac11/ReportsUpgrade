#!/bin/bash
URL="https://docs.google.com/spreadsheets/d/1YsA8jDCvN2fEnT38JC1ca_UeSUtjK0tn5M2PUg-QE7c/edit#gid=374258422"
DIRPATH="/home/korisnik/Desktop/XML"
PROG=reportsXML-1.0-SNAPSHOT.jar
java -jar $PROG -create $URL $DIRPATH
