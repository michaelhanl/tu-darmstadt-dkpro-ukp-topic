#!/bin/bash

## set working dir to main (pwd)?!

## set runtime parameters 
## in "all" mode the rule file is automatically detected, if there are rule files in the "config"
## directory with the respective language extension ("EN" or "DE").
v3="dkpro.ukp.topic.structure.jar"
input="input"
output="output"
modal="pcfg"
rule="config/Rules_EN.xml"

parameters="-all $1 -modal $modal -inDir $input -outDir $output"

java -jar $v3 $parameters


## java -cp annolab.jar org.annolab.tre.cli.Main Rules_EN.xml file.xml
## java -cp annolab.jar org.annolab.tre.cli.LearnMain Rules_EN.xml file.xml out.xml
