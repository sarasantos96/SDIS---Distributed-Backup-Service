#!/bin/bash
javac *.java
gnome-terminal -e "java Peer 1 224.0.0.1 1024 224.0.0.2 1025 224.0.0.3 1026"
gnome-terminal -e "java Peer 2 224.0.0.1 1024 224.0.0.2 1025 224.0.0.3 1026"
gnome-terminal -e "java Peer 3 224.0.0.1 1024 224.0.0.2 1025 224.0.0.3 1026"
gnome-terminal -e "java Peer 4 224.0.0.1 1024 224.0.0.2 1025 224.0.0.3 1026"

