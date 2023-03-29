#!/bin/bash

java dotCallGraphGenerator instrumentLetters main
cp sootOutput/instrumentLetters.class .
java instrumentLetters

